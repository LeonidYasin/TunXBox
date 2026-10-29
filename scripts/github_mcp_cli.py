#!/usr/bin/env python3
"""
GitHub & MCP CLI Helper Script.
Provides tools to manage Actions, Build Logs, Pull Requests, Issues, and Branches
on GitHub without hardcoded credentials.
"""

import argparse
import io
import json
import os
import re
import subprocess
import sys
import urllib.error
import urllib.request
import zipfile


def detect_repo() -> str:
    """Attempt to detect repo in 'owner/repo' format from env or git remote."""
    env_repo = os.getenv("GITHUB_REPOSITORY")
    if env_repo:
        return env_repo.strip()

    try:
        url = (
            subprocess.check_output(
                ["git", "config", "--get", "remote.origin.url"],
                stderr=subprocess.DEVNULL,
            )
            .decode("utf-8")
            .strip()
        )
        # Parse SSH or HTTPS remote URL
        # e.g., git@github.com:owner/repo.git or https://github.com/owner/repo.git
        match = re.search(r"github\.com[:/]([^/]+)/([^/]+?)(?:\.git)?$", url)
        if match:
            return f"{match.group(1)}/{match.group(2)}"
    except Exception:
        pass

    return ""


def github_request(
    endpoint: str,
    method: str = "GET",
    data: dict = None,
    token: str = None,
    raw: bool = False,
    text_response: bool = False,
):
    """Perform an HTTP request to the GitHub REST API."""
    if not token:
        print(
            "Error: GitHub Token is required. Set GITHUB_TOKEN environment variable or pass --token.",
            file=sys.stderr,
        )
        sys.exit(1)

    url = (
        f"https://api.github.com{endpoint}"
        if endpoint.startswith("/")
        else endpoint
    )
    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github.v3+json",
        "User-Agent": "GitHub-MCP-CLI/1.0",
    }

    body = None
    if data is not None:
        headers["Content-Type"] = "application/json"
        body = json.dumps(data).encode("utf-8")

    req = urllib.request.Request(url, data=body, headers=headers, method=method)

    try:
        with urllib.request.urlopen(req) as response:
            res_data = response.read()
            if raw:
                return res_data
            if text_response:
                return res_data.decode("utf-8", errors="replace")
            if response.status in (200, 201, 204):
                res_body = res_data.decode("utf-8", errors="replace")
                return json.loads(res_body) if res_body else {}
    except urllib.error.HTTPError as e:
        if e.code == 302 and raw:  # GitHub API log redirects
            redirect_url = e.headers.get("Location")
            if redirect_url:
                req_red = urllib.request.Request(redirect_url)
                with urllib.request.urlopen(req_red) as red_res:
                    return red_res.read()
        print(f"HTTP Error {e.code}: {e.reason}", file=sys.stderr)
        try:
            error_details = json.loads(e.read().decode("utf-8"))
            print(
                f"Message: {error_details.get('message', '')}", file=sys.stderr
            )
        except Exception:
            pass
        sys.exit(1)
    except Exception as e:
        print(f"Request failed: {e}", file=sys.stderr)
        sys.exit(1)


# --- Actions & Workflows Handlers ---


def handle_workflows(args, token, repo):
    owner, repo_name = repo.split("/", 1)
    if args.action == "list":
        limit = args.limit or 20
        endpoint = f"/repos/{owner}/{repo_name}/actions/runs?per_page={limit}"
        if args.workflow_id:
            endpoint = f"/repos/{owner}/{repo_name}/actions/workflows/{args.workflow_id}/runs?per_page={limit}"
        runs = github_request(endpoint, token=token)
        print(f"=== WORKFLOW RUNS ({owner}/{repo_name}) ===")
        for run in runs.get("workflow_runs", []):
            print(
                f"Run #{run['id']} [{run['status']}/{run['conclusion'] or 'in_progress'}] - "
                f"{run['name']} ({run['head_branch'] or run['head_sha'][:7]})"
            )
            print(f"  Event: {run['event']} | Commit: {run['head_sha'][:7]}")
            print(f"  URL: {run['html_url']}\n")

    elif args.action == "get":
        if not args.run_id:
            print("Error: --run-id is required for 'get' action", file=sys.stderr)
            sys.exit(1)
        run = github_request(
            f"/repos/{owner}/{repo_name}/actions/runs/{args.run_id}",
            token=token,
        )
        print(json.dumps(run, indent=2))

    elif args.action == "jobs":
        if not args.run_id:
            print(
                "Error: --run-id is required for 'jobs' action",
                file=sys.stderr,
            )
            sys.exit(1)
        jobs_res = github_request(
            f"/repos/{owner}/{repo_name}/actions/runs/{args.run_id}/jobs",
            token=token,
        )
        print(f"=== JOBS FOR RUN #{args.run_id} ({owner}/{repo_name}) ===")
        for job in jobs_res.get("jobs", []):
            print(
                f"Job #{job['id']} [{job['status']}/{job['conclusion'] or 'in_progress'}] - {job['name']}"
            )
            print(f"  Runner: {job.get('runner_name', 'N/A')}")
            print(f"  URL: {job.get('html_url', '')}")
            if job.get("steps"):
                print("  Steps:")
                for step in job["steps"]:
                    print(
                        f"    - {step['name']} [{step['status']}/{step.get('conclusion', 'N/A')}]"
                    )
            print()

    elif args.action == "dispatch":
        if not args.workflow_id or not args.ref:
            print(
                "Error: --workflow-id and --ref are required for workflow dispatch",
                file=sys.stderr,
            )
            sys.exit(1)
        payload = {"ref": args.ref}
        if args.inputs:
            try:
                payload["inputs"] = json.loads(args.inputs)
            except Exception:
                # Key-value pairs like key1=val1,key2=val2
                inputs = {}
                for item in args.inputs.split(","):
                    if "=" in item:
                        k, v = item.split("=", 1)
                        inputs[k.strip()] = v.strip()
                payload["inputs"] = inputs

        github_request(
            f"/repos/{owner}/{repo_name}/actions/workflows/{args.workflow_id}/dispatches",
            method="POST",
            data=payload,
            token=token,
        )
        print(
            f"Successfully dispatched workflow '{args.workflow_id}' on ref '{args.ref}'"
        )

    elif args.action == "cancel":
        if not args.run_id:
            print(
                "Error: --run-id is required for 'cancel' action",
                file=sys.stderr,
            )
            sys.exit(1)
        github_request(
            f"/repos/{owner}/{repo_name}/actions/runs/{args.run_id}/cancel",
            method="POST",
            token=token,
        )
        print(f"Cancelled workflow run #{args.run_id}")

    elif args.action == "rerun":
        if not args.run_id:
            print(
                "Error: --run-id is required for 'rerun' action",
                file=sys.stderr,
            )
            sys.exit(1)
        github_request(
            f"/repos/{owner}/{repo_name}/actions/runs/{args.run_id}/rerun",
            method="POST",
            token=token,
        )
        print(f"Triggered rerun for workflow run #{args.run_id}")


# --- Build Logs Handlers ---


def handle_logs(args, token, repo):
    owner, repo_name = repo.split("/", 1)

    if args.action == "get-job":
        if not args.job_id:
            print(
                "Error: --job-id is required for 'get-job' action",
                file=sys.stderr,
            )
            sys.exit(1)
        log_text = github_request(
            f"/repos/{owner}/{repo_name}/actions/jobs/{args.job_id}/logs",
            token=token,
            text_response=True,
        )
        if args.output:
            with open(args.output, "w", encoding="utf-8") as f:
                f.write(log_text)
            print(f"Job #{args.job_id} log written to {args.output}")
        else:
            print(f"=== LOG FOR JOB #{args.job_id} ===")
            print(log_text)

    elif args.action == "failed":
        if not args.run_id:
            print(
                "Error: --run-id is required for 'failed' action",
                file=sys.stderr,
            )
            sys.exit(1)

        jobs_res = github_request(
            f"/repos/{owner}/{repo_name}/actions/runs/{args.run_id}/jobs",
            token=token,
        )
        jobs = jobs_res.get("jobs", [])
        failed_jobs = [j for j in jobs if j.get("conclusion") == "failure"]

        print(f"=== FAILED LOG SUMMARY FOR RUN #{args.run_id} ({owner}/{repo_name}) ===")
        if not failed_jobs:
            print("No failed jobs found in this workflow run.")
            return

        for job in failed_jobs:
            print(f"\n--- Failed Job: {job['name']} (ID: {job['id']}) ---")
            failed_steps = [
                s
                for s in job.get("steps", [])
                if s.get("conclusion") == "failure"
            ]
            for step in failed_steps:
                print(f"  Failed Step: {step['name']} (Number: {step['number']})")

            # Try downloading raw log for job to extract error snippet
            try:
                raw_log = github_request(
                    f"/repos/{owner}/{repo_name}/actions/jobs/{job['id']}/logs",
                    token=token,
                    text_response=True,
                )
                lines = raw_log.splitlines()
                error_lines = []
                for line in lines:
                    if any(
                        keyword in line.lower()
                        for keyword in [
                            "error",
                            "fail",
                            "fatal",
                            "exception",
                            "##[error]",
                        ]
                    ):
                        error_lines.append(line)

                print("  Key log errors / failure lines:")
                snippet = error_lines[-30:] if error_lines else lines[-30:]
                for err in snippet:
                    print(f"    {err}")
            except Exception as e:
                print(f"  Could not retrieve log snippet for job {job['id']}: {e}")

    elif args.action == "download":
        if not args.run_id:
            print(
                "Error: --run-id is required for 'download' action",
                file=sys.stderr,
            )
            sys.exit(1)
        raw_zip = github_request(
            f"/repos/{owner}/{repo_name}/actions/runs/{args.run_id}/logs",
            token=token,
            raw=True,
        )
        out_filename = args.output or f"workflow_logs_run_{args.run_id}.zip"
        with open(out_filename, "wb") as f:
            f.write(raw_zip)
        print(f"Downloaded workflow logs for run #{args.run_id} to {out_filename}")


# --- PR Handlers ---


def handle_prs(args, token, repo):
    owner, repo_name = repo.split("/", 1)
    if args.action == "list":
        state = args.state or "all"
        prs = github_request(
            f"/repos/{owner}/{repo_name}/pulls?state={state}&per_page=100",
            token=token,
        )
        print(f"=== PULL REQUESTS ({owner}/{repo_name} - state: {state}) ===")
        for pr in prs:
            status = (
                "OPEN"
                if pr["state"] == "open"
                else ("MERGED" if pr.get("merged_at") else "CLOSED")
            )
            print(
                f"#{pr['number']} [{status}] {pr['title']} (by @{pr['user']['login']})"
            )
            print(f"  URL: {pr['html_url']}")
            print(f"  Branch: {pr['head']['ref']} -> {pr['base']['ref']}\n")

    elif args.action == "get":
        if not args.number:
            print("Error: --number is required for 'get' action", file=sys.stderr)
            sys.exit(1)
        pr = github_request(
            f"/repos/{owner}/{repo_name}/pulls/{args.number}", token=token
        )
        print(json.dumps(pr, indent=2))

    elif args.action == "create":
        if not args.title or not args.head or not args.base:
            print(
                "Error: --title, --head, and --base are required for 'create' action",
                file=sys.stderr,
            )
            sys.exit(1)
        payload = {
            "title": args.title,
            "head": args.head,
            "base": args.base,
            "body": args.body or "",
        }
        pr = github_request(
            f"/repos/{owner}/{repo_name}/pulls",
            method="POST",
            data=payload,
            token=token,
        )
        print(f"Successfully created PR #{pr['number']}: {pr['html_url']}")

    elif args.action == "merge":
        if not args.number:
            print(
                "Error: --number is required for 'merge' action",
                file=sys.stderr,
            )
            sys.exit(1)
        res = github_request(
            f"/repos/{owner}/{repo_name}/pulls/{args.number}/merge",
            method="PUT",
            token=token,
        )
        print(f"PR #{args.number} merged: {res.get('message', 'Success')}")


# --- Issues Handlers ---


def handle_issues(args, token, repo):
    owner, repo_name = repo.split("/", 1)
    if args.action == "list":
        state = args.state or "all"
        issues = github_request(
            f"/repos/{owner}/{repo_name}/issues?state={state}&per_page=100",
            token=token,
        )
        print(f"=== ISSUES ({owner}/{repo_name} - state: {state}) ===")
        for issue in issues:
            if "pull_request" in issue:
                continue  # Skip PRs listed under issues API
            print(
                f"#{issue['number']} [{issue['state'].upper()}] {issue['title']} (by @{issue['user']['login']})"
            )
            print(f"  URL: {issue['html_url']}\n")

    elif args.action == "create":
        if not args.title:
            print(
                "Error: --title is required for 'create' action",
                file=sys.stderr,
            )
            sys.exit(1)
        payload = {"title": args.title, "body": args.body or ""}
        issue = github_request(
            f"/repos/{owner}/{repo_name}/issues",
            method="POST",
            data=payload,
            token=token,
        )
        print(
            f"Successfully created Issue #{issue['number']}: {issue['html_url']}"
        )

    elif args.action == "close":
        if not args.number:
            print(
                "Error: --number is required for 'close' action",
                file=sys.stderr,
            )
            sys.exit(1)
        payload = {"state": "closed"}
        issue = github_request(
            f"/repos/{owner}/{repo_name}/issues/{args.number}",
            method="PATCH",
            data=payload,
            token=token,
        )
        print(f"Closed Issue #{issue['number']}")


# --- Branches Handlers ---


def handle_branches(args, token, repo):
    owner, repo_name = repo.split("/", 1)
    if args.action == "list":
        branches = github_request(
            f"/repos/{owner}/{repo_name}/branches?per_page=100", token=token
        )
        print(f"=== BRANCHES ({owner}/{repo_name}) ===")
        for branch in branches:
            print(
                f"- {branch['name']} (commit: {branch['commit']['sha'][:7]})"
            )

    elif args.action == "create":
        if not args.name or not args.from_sha:
            print(
                "Error: --name and --from-sha are required for branch creation",
                file=sys.stderr,
            )
            sys.exit(1)
        payload = {"ref": f"refs/heads/{args.name}", "sha": args.from_sha}
        github_request(
            f"/repos/{owner}/{repo_name}/git/refs",
            method="POST",
            data=payload,
            token=token,
        )
        print(f"Successfully created branch '{args.name}'")

    elif args.action == "delete":
        if not args.name:
            print(
                "Error: --name is required for branch deletion", file=sys.stderr
            )
            sys.exit(1)
        github_request(
            f"/repos/{owner}/{repo_name}/git/refs/heads/{args.name}",
            method="DELETE",
            token=token,
        )
        print(f"Deleted branch '{args.name}'")


def main():
    parser = argparse.ArgumentParser(
        description="GitHub & MCP CLI Helper Script for Workflows, Build Logs, PRs, Issues, and Branches"
    )
    parser.add_argument(
        "--token",
        default=os.getenv("GITHUB_TOKEN"),
        help="GitHub Access Token (or set GITHUB_TOKEN env var)",
    )
    parser.add_argument(
        "--repo",
        default=detect_repo(),
        help="Repository in 'owner/repo' format (defaults to auto-detected git repo or GITHUB_REPOSITORY env var)",
    )

    subparsers = parser.add_subparsers(
        dest="resource", required=True, help="GitHub resource to interact with"
    )

    # Workflows Subparser
    wf_parser = subparsers.add_parser(
        "workflows", help="Manage GitHub Workflows & Actions"
    )
    wf_parser.add_argument(
        "action", choices=["list", "get", "jobs", "dispatch", "cancel", "rerun"]
    )
    wf_parser.add_argument("--run-id", type=int, help="Workflow run ID")
    wf_parser.add_argument(
        "--workflow-id", help="Workflow ID or filename (e.g. ci.yml)"
    )
    wf_parser.add_argument(
        "--ref", help="Git reference (branch or tag name) for dispatch"
    )
    wf_parser.add_argument(
        "--inputs",
        help="Workflow inputs (JSON string or key1=val1,key2=val2)",
    )
    wf_parser.add_argument(
        "--limit", type=int, default=20, help="Max number of runs to list"
    )

    # Logs Subparser
    logs_parser = subparsers.add_parser(
        "logs", help="Fetch and analyze GitHub Actions build logs"
    )
    logs_parser.add_argument(
        "action", choices=["get-job", "failed", "download"]
    )
    logs_parser.add_argument("--run-id", type=int, help="Workflow run ID")
    logs_parser.add_argument("--job-id", type=int, help="Job ID")
    logs_parser.add_argument(
        "--output", help="Output file path for log or zip download"
    )

    # PR Subparser
    pr_parser = subparsers.add_parser("prs", help="Manage Pull Requests")
    pr_parser.add_argument(
        "action", choices=["list", "get", "create", "merge"]
    )
    pr_parser.add_argument(
        "--state", choices=["open", "closed", "all"], default="all"
    )
    pr_parser.add_argument("--number", type=int, help="PR number")
    pr_parser.add_argument("--title", help="PR title")
    pr_parser.add_argument("--head", help="Head branch")
    pr_parser.add_argument("--base", help="Base branch")
    pr_parser.add_argument("--body", help="PR body/description")

    # Issues Subparser
    issue_parser = subparsers.add_parser("issues", help="Manage Issues")
    issue_parser.add_argument("action", choices=["list", "create", "close"])
    issue_parser.add_argument(
        "--state", choices=["open", "closed", "all"], default="all"
    )
    issue_parser.add_argument("--number", type=int, help="Issue number")
    issue_parser.add_argument("--title", help="Issue title")
    issue_parser.add_argument("--body", help="Issue body/description")

    # Branches Subparser
    branch_parser = subparsers.add_parser("branches", help="Manage Branches")
    branch_parser.add_argument(
        "action", choices=["list", "create", "delete"]
    )
    branch_parser.add_argument("--name", help="Branch name")
    branch_parser.add_argument(
        "--from-sha", help="Source commit SHA for creation"
    )

    args = parser.parse_args()

    token = args.token
    if not token:
        print(
            "Error: GitHub Access Token missing! Pass --token or set GITHUB_TOKEN environment variable.",
            file=sys.stderr,
        )
        sys.exit(1)

    repo = args.repo
    if not repo or "/" not in repo:
        print(
            "Error: Repository in 'owner/repo' format is required! Pass --repo or set GITHUB_REPOSITORY env var.",
            file=sys.stderr,
        )
        sys.exit(1)

    if args.resource == "workflows":
        handle_workflows(args, token, repo)
    elif args.resource == "logs":
        handle_logs(args, token, repo)
    elif args.resource == "prs":
        handle_prs(args, token, repo)
    elif args.resource == "issues":
        handle_issues(args, token, repo)
    elif args.resource == "branches":
        handle_branches(args, token, repo)


if __name__ == "__main__":
    main()
