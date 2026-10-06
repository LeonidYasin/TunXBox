#!/usr/bin/env python3
"""Read a private signing bundle only from env; never log its contents."""
import base64, hashlib, json, os, pathlib, re, subprocess


def prepare(bundle, root, pinned):
    data = json.loads(bundle)
    required = ['keystore', 'storePassword', 'alias', 'keyPassword']
    if any(not isinstance(data.get(k), str) or not data[k] for k in required):
        raise ValueError('Incomplete signing bundle')
    if any('\n' in data[k] or '\r' in data[k] for k in required):
        raise ValueError('Multiline signing metadata is not supported')
    expected = pinned.read_text().strip().lower()
    if not re.fullmatch('[0-9a-f]{64}', expected):
        raise ValueError('Missing or invalid pinned signing certificate')
    private = root / '.signing'
    private.mkdir(mode=0o700, exist_ok=True)
    key = private / 'release.keystore'
    key.write_bytes(base64.b64decode(data['keystore'], validate=True)); key.chmod(0o600)
    env = dict(os.environ, TUNXBOX_STORE_PASSWORD=data['storePassword'])
    cert = subprocess.run(['keytool', '-exportcert', '-keystore', str(key), '-alias', data['alias'],
                           '-storepass:env', 'TUNXBOX_STORE_PASSWORD'], env=env, capture_output=True)
    if cert.returncode != 0 or hashlib.sha256(cert.stdout).hexdigest() != expected:
        raise ValueError('Signing certificate does not match pinned identity')
    return dict(TUNXBOX_SIGNING_KEYSTORE=str(key.resolve()), KEYSTORE_PASS=data['storePassword'],
                ALIAS_NAME=data['alias'], ALIAS_PASS=data['keyPassword'], TUNXBOX_REQUIRE_SIGNING='1')

if __name__ == '__main__':
    try:
        values = prepare(os.environ.get('TUNXBOX_SIGNING_BUNDLE', ''), pathlib.Path.cwd(), pathlib.Path('signing-certificate.sha256'))
        for name in ['KEYSTORE_PASS', 'ALIAS_PASS']:
            print('::add-mask::' + values[name])
        with open(os.environ['GITHUB_ENV'], 'a') as output:
            for key, value in values.items():
                output.write(key + '=' + value + '\n')
        print('Pinned signing identity verified')
    except Exception:
        raise SystemExit('Signing is not configured or identity verification failed; refusing to publish. Run buildScript/setup_signing.py with repository-admin GitHub access.')
