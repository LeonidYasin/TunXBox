# TunXBox continuation

Read [AGENTS.md](AGENTS.md) and [docs/agent-handoff.md](https://github.com/LeonidYasin/TunXBox/blob/fix/tv-pointer-scroll-1.6/docs/agent-handoff.md) before work. They contain the current branch, release evidence, unresolved issues and safety constraints. Do not infer current publication from an old chat or branch name.

Before any build, RC, device-test or release action, follow **Build delivery and publication — mandatory decision procedure** in `AGENTS.md`. Normal owner device testing and “auto-update/autopublication” mean a verified published rolling prerelease visible in TunXBox channel `test`; `artifact_only` is only for an explicitly requested manual/private candidate and does not satisfy in-app delivery. Stable requires separate explicit approval.
