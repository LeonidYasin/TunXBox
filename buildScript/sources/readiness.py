#!/usr/bin/env python3
"""Emit an honest readiness report, not a store approval signal."""
import json,pathlib
ROOT=pathlib.Path(__file__).resolve().parents[2]
def main():
    data=json.loads((ROOT/'buildScript/sources/lock.json').read_text())
    blockers=[
        'libneko: explicit license/redistribution permission missing in pinned snapshot',
        'Yacd: confirm MIT copyright/asset notices for pinned historical source',
        'GeoIP/Geosite: verify all data-input licenses and preservation',
        'F-Droid: approved toolchain provisioning, scanner and clean build not yet verified',
        'Review and commit actual catalog screenshots; generated launcher is not a screenshot',
        'Tagged release SHA and ARM real-device validation required before enabling recipe',
        'Separate GitHub mirror repositories blocked by current integration permissions (403)',
        'All-transitive/off-GitHub source preservation not yet complete',
    ]
    lines=['# Readiness report','','Status: **preparation only — not ready to submit**.','','## Preserved/pinned','','- '+ '\n- '.join(data['components']),'','## Remaining gates','']
    lines+=['- '+x for x in blockers]
    lines+=['','Run-specific CI results must be checked separately. A successful workflow is not an inclusion approval.','']
    p=ROOT/'fdroid/READINESS.md';p.write_text('\n'.join(lines));print(p)
if __name__=='__main__':main()
