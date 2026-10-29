import importlib.util
import json
import os
import sys
import unittest
from unittest.mock import MagicMock, patch

# Dynamically import scripts/github_mcp_cli.py
cli_path = os.path.join(
    os.path.dirname(__file__), "..", "scripts", "github_mcp_cli.py"
)
spec = importlib.util.spec_from_file_location("github_mcp_cli", cli_path)
cli = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cli)


class TestGitHubMCPCLI(unittest.TestCase):

    def test_detect_repo_from_env(self):
        with patch.dict(os.environ, {"GITHUB_REPOSITORY": "testowner/testrepo"}):
            self.assertEqual(cli.detect_repo(), "testowner/testrepo")

    @patch("subprocess.check_output")
    def test_detect_repo_from_git_remote(self, mock_sub):
        with patch.dict(os.environ, {}, clear=True):
            mock_sub.return_value = b"https://github.com/myowner/myrepo.git\n"
            self.assertEqual(cli.detect_repo(), "myowner/myrepo")

    @patch("urllib.request.urlopen")
    def test_github_request_success(self, mock_urlopen):
        mock_response = MagicMock()
        mock_response.status = 200
        mock_response.read.return_value = b'{"key": "value"}'
        mock_urlopen.return_value.__enter__.return_value = mock_response

        res = cli.github_request("/test", token="fake_token")
        self.assertEqual(res, {"key": "value"})

    @patch("urllib.request.urlopen")
    def test_handle_workflows_list(self, mock_urlopen):
        mock_response = MagicMock()
        mock_response.status = 200
        mock_response.read.return_value = json.dumps(
            {
                "workflow_runs": [
                    {
                        "id": 123,
                        "status": "completed",
                        "conclusion": "success",
                        "name": "CI",
                        "head_branch": "master",
                        "head_sha": "1234567890",
                        "event": "push",
                        "html_url": "https://github.com/owner/repo/actions/runs/123",
                    }
                ]
            }
        ).encode("utf-8")
        mock_urlopen.return_value.__enter__.return_value = mock_response

        args = MagicMock()
        args.action = "list"
        args.limit = 10
        args.workflow_id = None

        with patch("sys.stdout"):
            cli.handle_workflows(args, "token", "owner/repo")

    @patch("urllib.request.urlopen")
    def test_handle_logs_failed(self, mock_urlopen):
        # 1. First call for jobs list
        mock_jobs_res = MagicMock()
        mock_jobs_res.status = 200
        mock_jobs_res.read.return_value = json.dumps(
            {
                "jobs": [
                    {
                        "id": 999,
                        "name": "Build Job",
                        "conclusion": "failure",
                        "steps": [
                            {
                                "name": "Compile",
                                "number": 1,
                                "conclusion": "failure",
                            }
                        ],
                    }
                ]
            }
        ).encode("utf-8")

        # 2. Second call for raw logs
        mock_log_res = MagicMock()
        mock_log_res.status = 200
        mock_log_res.read.return_value = (
            b"Step 1\nERROR: Failed to compile module\nStep failed\n"
        )

        mock_urlopen.return_value.__enter__.side_effect = [
            mock_jobs_res,
            mock_log_res,
        ]

        args = MagicMock()
        args.action = "failed"
        args.run_id = 123

        with patch("sys.stdout"):
            cli.handle_logs(args, "token", "owner/repo")

    @patch("urllib.request.urlopen")
    def test_handle_prs_list(self, mock_urlopen):
        mock_response = MagicMock()
        mock_response.status = 200
        mock_response.read.return_value = json.dumps(
            [
                {
                    "number": 1,
                    "state": "open",
                    "title": "Test PR",
                    "user": {"login": "dev"},
                    "html_url": "https://github.com/owner/repo/pull/1",
                    "head": {"ref": "feature"},
                    "base": {"ref": "master"},
                }
            ]
        ).encode("utf-8")
        mock_urlopen.return_value.__enter__.return_value = mock_response

        args = MagicMock()
        args.action = "list"
        args.state = "open"

        with patch("sys.stdout"):
            cli.handle_prs(args, "token", "owner/repo")


if __name__ == "__main__":
    unittest.main()
