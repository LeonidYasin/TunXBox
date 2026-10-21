#!/usr/bin/env python3
"""Fail early on missing/mismatched stable publication inputs; never publish."""
import json
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parents[1]


def validate(root=ROOT):
    root = pathlib.Path(root).resolve()
    required = ('PACKAGE_NAME', 'VERSION_NAME', 'VERSION_CODE')
    properties = {}
    for line in (root / 'nb4a.properties').read_text().splitlines():
        if not line.strip() or line.lstrip().startswith('#') or '=' not in line:
            continue
        key, value = (part.strip() for part in line.split('=', 1))
        if key in required and key in properties:
            raise ValueError('Duplicate release property: ' + key)
        properties[key] = value
    if any(key not in properties for key in required):
        raise ValueError('Missing required release properties')
    if properties['PACKAGE_NAME'] != 'com.tunxbox.app':
        raise ValueError('Stable package must retain com.tunxbox.app')
    version = properties['VERSION_NAME']
    component = r'(?:0|[1-9][0-9]*)'
    if not re.fullmatch(component + r'\.' + component + r'\.' + component, version):
        raise ValueError('Stable version must be canonical MAJOR.MINOR.PATCH, not RC/path')
    code = properties['VERSION_CODE']
    if not re.fullmatch(r'[1-9][0-9]*', code):
        raise ValueError('VERSION_CODE must be a positive canonical integer')
    code = int(code) * 1_000_000 + 999_999
    if code > 2_100_000_000:
        raise ValueError('Stable Android versionCode exceeds supported range')
    notes = (root / 'docs' / 'releases' / (version + '.md')).resolve()
    if not notes.is_relative_to(root) or not notes.is_file():
        raise ValueError('Missing in-repository stable release notes for ' + version)
    text = notes.read_text().strip()
    lines = text.splitlines()
    if not lines or lines[0] != '# TunXBox ' + version or not any(line.strip() for line in lines[1:]):
        raise ValueError('Release notes need matching title and nonempty body')
    pin = (root / 'signing-certificate.sha256').read_text().strip()
    if not re.fullmatch(r'[0-9a-fA-F]{64}', pin):
        raise ValueError('Missing or malformed public signing certificate SHA256')
    return {'version': version, 'package': properties['PACKAGE_NAME'],
            'versionCode': code, 'notes': notes.relative_to(root).as_posix(),
            'signingCertificateSha256': pin.lower(), 'publishes': False}


if __name__ == '__main__':
    print(json.dumps(validate(), sort_keys=True))
