#!/usr/bin/env python3
"""Run unchanged executable smoke harness with its obsolete fixed-count gate replaced."""
import pathlib,subprocess,sys,tempfile
ROOT=pathlib.Path(__file__).resolve().parents[1]
def patched_harness(text):
    if text.count("python3 buildScript/verify_emulator_results.py\n") == 1 and "PYTEST" not in text:
        return text
    start='python3 - "$out" <<\'PYTEST\'\n';end='\nPYTEST'
    if text.count(start)!=1:raise ValueError('Unexpected smoke harness validation block')
    before,body=text.split(start,1)
    if end not in body:raise ValueError('Missing smoke harness validation terminator')
    old,after=body.split(end,1)
    if "assert totals['tests']==10" not in old or "('failures','errors','skipped')" not in old:raise ValueError('Unknown legacy smoke gate; inspect before changing')
    return before+'python3 buildScript/verify_emulator_results.py\n'+after
if __name__=='__main__':
    if len(sys.argv) not in (2,3) or sys.argv[1] not in ('4096','16384'):raise SystemExit('Expected page size and optional Preview/Oss flavor')
    if len(sys.argv)==3 and sys.argv[2] not in ('Preview','Oss','Fdroid'):raise SystemExit('Unsupported test flavor')
    text=patched_harness((ROOT/'buildScript/run_emulator_smoke.sh').read_text())
    with tempfile.TemporaryDirectory(prefix='tunxbox-smoke-') as directory:
        script=pathlib.Path(directory)/'run.sh';script.write_text(text)
        subprocess.run(['bash',str(script),*sys.argv[1:]],cwd=ROOT,check=True)
