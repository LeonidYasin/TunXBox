# Third-party provenance and outstanding reviews

TunXBox retains the original NekoBox/SagerNet authorship and GPL-3.0-or-later declaration in LICENSE and AUTHORS. COPYING contains the full GPL version 3; no upstream copyright is reassigned.

| Component | Pinned source | License status |
|---|---|---|
| sing-box fork | buildScript/sources/lock.json | GPL-3.0-or-later declaration in source LICENSE; review dependencies too |
| libneko | buildScript/sources/lock.json | **Unresolved:** selected snapshot contains no explicit LICENSE; ask upstream |
| gomobile fork | buildScript/sources/lock.json | Preserve included LICENSE/AUTHORS/PATENTS and source notices |
| Yacd-meta dashboard | buildScript/sources/lock.json | package.json declares MIT; standalone license/copyright notices need confirmation |
| sing-geoip / sing-geosite generators | buildScript/sources/lock.json | GPL declaration applies to generator code, not automatically to every data input |
| JetBrains Mono Regular 1.0.3 | buildScript/sources/lock.json; exact official TTF SHA-256 match | Historical **Apache-2.0**, not later OFL. Original license and attribution bundled in app/src/main/assets/third-party/. FontLab outline sources preserved; font rebuild not verified |
| Android libraries | app/build.gradle.kts and resolved Gradle graph | Transitive source/license audit pending |
| Go libraries | libcore/go.mod/go.sum and pinned tool go.mod files | Module caches may preserve sources; license audit remains required |
| JS libraries/fonts | pinned Yacd pnpm-lock.yaml | Keep package licenses/notices; source availability of build-tool binaries must be reviewed |

A source backup is not a legal clearance or proof that every transitive dependency is FOSS. Do not use the app's GPL notice to relabel separately licensed or unlicensed components.
