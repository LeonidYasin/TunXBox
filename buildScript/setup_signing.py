#!/usr/bin/env python3
"""Run once on the OWNER'S computer with gh authenticated. Keeps an offline key backup.
No private value is printed, committed, or passed on the command line.
"""
import argparse, base64, hashlib, json, os, pathlib, secrets, subprocess

p = argparse.ArgumentParser(description=__doc__)
p.add_argument('--repo', default='LeonidYasin/TunXBox')
p.add_argument('--branch', default='feature/tv-remote-support')
p.add_argument('--backup-dir', type=pathlib.Path, default=pathlib.Path.home() / 'TunXBox-signing-backup')
a = p.parse_args()
subprocess.run(['gh', 'auth', 'status'], check=True)
existing = json.loads(subprocess.check_output(['gh', 'secret', 'list', '--repo', a.repo, '--json', 'name']))
if any(s['name'] == 'TUNXBOX_SIGNING_BUNDLE' for s in existing) and not (a.backup_dir / 'signing-bundle.json').exists():
    raise SystemExit('Signing secret already exists: use its original offline backup; do not rotate the key.')
a.backup_dir.mkdir(mode=0o700, parents=True, exist_ok=True)
a.backup_dir.chmod(0o700)
key = a.backup_dir / 'release.keystore'
bundle_file = a.backup_dir / 'signing-bundle.json'
if bundle_file.exists():
    bundle_file.chmod(0o600)
    bundle = json.loads(bundle_file.read_text())
    if not key.exists(): key.write_bytes(base64.b64decode(bundle['keystore'])); key.chmod(0o600)
else:
    if key.exists(): raise SystemExit('Existing keystore without bundle: stop to avoid overwriting its identity.')
    password = secrets.token_urlsafe(32)
    env = dict(os.environ, TUNXBOX_STORE_PASSWORD=password)
    subprocess.run(['keytool', '-genkeypair', '-storetype', 'PKCS12', '-keystore', str(key), '-alias', 'tunxbox',
                    '-keyalg', 'RSA', '-keysize', '3072', '-validity', '10000',
                    '-dname', 'CN=TunXBox, O=TunXBox', '-storepass:env', 'TUNXBOX_STORE_PASSWORD',
                    '-keypass:env', 'TUNXBOX_STORE_PASSWORD'], env=env, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    key.chmod(0o600)
    bundle = dict(keystore=base64.b64encode(key.read_bytes()).decode(), storePassword=password, alias='tunxbox', keyPassword=password)
    bundle_file.touch(mode=0o600); bundle_file.write_text(json.dumps(bundle))
env = dict(os.environ, TUNXBOX_STORE_PASSWORD=bundle['storePassword'])
cert = subprocess.run(['keytool', '-exportcert', '-keystore', str(key), '-alias', bundle['alias'], '-storepass:env', 'TUNXBOX_STORE_PASSWORD'], env=env, capture_output=True, check=True).stdout
fingerprint = hashlib.sha256(cert).hexdigest()
prior_pin = subprocess.run(['gh', 'api', f'repos/{a.repo}/contents/signing-certificate.sha256?ref={a.branch}'], capture_output=True)
if prior_pin.returncode == 0:
    pinned = base64.b64decode(json.loads(prior_pin.stdout)['content']).decode().strip()
    if pinned != fingerprint: raise SystemExit('Pinned certificate differs: refusing key rotation.')
# Store private material only in Actions Secrets; public fingerprint only in the PR branch.
subprocess.run(['gh', 'secret', 'set', 'TUNXBOX_SIGNING_BUNDLE', '--repo', a.repo], input=json.dumps(bundle).encode(), check=True)
path = f'repos/{a.repo}/contents/signing-certificate.sha256'
old = subprocess.run(['gh', 'api', path + '?ref=' + a.branch], capture_output=True)
payload = dict(message='Pin TunXBox signing certificate', branch=a.branch, content=base64.b64encode((fingerprint+'\n').encode()).decode())
if old.returncode == 0: payload['sha'] = json.loads(old.stdout)['sha']
subprocess.run(['gh', 'api', '--method', 'PUT', path, '--input', '-'], input=json.dumps(payload).encode(), check=True, stdout=subprocess.DEVNULL)
print('Signing secret saved; public certificate pinned in PR branch. Keep the private offline backup. Do not upload it to GitHub files or chat.')
print('Certificate SHA256: ' + fingerprint)
