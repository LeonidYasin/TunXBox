#!/usr/bin/env python3
"""Fail closed on signer changes and Android downgrades, including workflow reruns."""
import argparse, json, pathlib

def verify(previous, current, allow_legacy=False):
    if current.get('applicationId') != 'com.tunxbox.app' or not current.get('signingCertificateSha256'):
        raise ValueError('Current APK has no verified application/signing identity')
    legacy = not previous.get('signingCertificateSha256')
    if legacy and not allow_legacy:
        raise ValueError('Legacy temporary-key release requires explicit one-time signer migration')
    if not legacy and previous['signingCertificateSha256'] != current['signingCertificateSha256']:
        raise ValueError('Signing identity changed; refusing incompatible update')
    if previous.get('applicationId', 'com.tunxbox.app') != current['applicationId']:
        raise ValueError('Application identity changed')
    if current['versionCode'] <= previous.get('versionCode', 0):
        raise ValueError('versionCode must increase; do not republish an older or identical build')
    return not legacy

if __name__ == '__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('previous',type=pathlib.Path);p.add_argument('current',type=pathlib.Path);p.add_argument('--allow-legacy',action='store_true');a=p.parse_args()
    current=json.loads(a.current.read_text())
    compatible=verify(json.loads(a.previous.read_text()), current, a.allow_legacy)
    current['upgradeCompatibleWithPrevious']=compatible
    a.current.write_text(json.dumps(current, indent=2)+'\n')
    print('Upgrade identity/version verified' if compatible else 'Explicit legacy signer migration: export settings; one-time uninstall is required for old temporary-key APKs.')
