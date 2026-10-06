// Executes the actual shipped inline JS with a minimal DOM. No real subscription request.
const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const html = fs.readFileSync('app/src/main/assets/tv-transfer.html', 'utf8');
const script = html.match(/<script>([\s\S]*?)<\/script>/)[1];
let tests = 0;
function page(result = {ok: true, status: 200, data: {imported: 3}}) {
  const fields = new Map(); const requests = [];
  const element = id => {
    if (!fields.has(id)) fields.set(id, {value: '', textContent: '', disabled: false, files: [], events: {}, addEventListener(type, callback) { this.events[type] = callback; }});
    return fields.get(id);
  };
  const context = {URL, URLSearchParams, TextEncoder, AbortController, setTimeout, clearTimeout,
    location: {hash: '#session=' + '0'.repeat(64), pathname: '/'}, history: {replaceState() {}}, document: {getElementById: element},
    fetch: async (url, options) => { requests.push({url, options, body: JSON.parse(options.body)}); return {ok: result.ok, status: result.status, json: async () => result.data}; }};
  vm.runInNewContext(script, context);
  return {element, requests, submit: () => element('transfer').events.submit({preventDefault() {}})};
}
async function test(name, fn) { await fn(); tests++; console.log('PASS ' + name); }
(async () => {
  await test('opaque HTTPS path pasted in main field becomes subscription', async () => {
    const p = page(); const url = 'https://subscriptions.example.invalid/opaque-TestToken'; p.element('profiles').value = url; await p.submit();
    assert.deepEqual(p.requests[0].body, {subscription_url: url}); assert.equal(p.requests[0].options.headers['X-Session-Token'].length, 64);
  });
  await test('upper-case and percent-encoded URL is preserved', async () => {
    const p = page(); const url = 'HTTPS://subscriptions.example.invalid:8443/api%2Fsub?token=a%2Bb'; p.element('profiles').value = url; await p.submit(); assert.deepEqual(p.requests[0].body, {subscription_url: url});
  });
  await test('explicit root subscription works', async () => {
    const p = page(); p.element('subscription').value = 'https://example.invalid/'; await p.submit(); assert.deepEqual(p.requests[0].body, {subscription_url: 'https://example.invalid/'});
  });
  await test('root HTTP proxy stays a profile', async () => {
    const p = page(); p.element('profiles').value = 'http://proxy.example.invalid:8080'; await p.submit(); assert.deepEqual(p.requests[0].body, {profiles: 'http://proxy.example.invalid:8080'});
  });
  await test('upstream wrapper in main field works', async () => {
    const p = page(); const url = 'sn://subscription?url=https%3A%2F%2Fexample.invalid%2Fsub'; p.element('profiles').value = url; await p.submit(); assert.deepEqual(p.requests[0].body, {subscription_url: url});
  });
  await test('multiline profiles stay profiles', async () => {
    const p = page(); const value = 'vless://test@example.invalid:443\nss://test@example.invalid:8388'; p.element('profiles').value = value; await p.submit(); assert.deepEqual(p.requests[0].body, {profiles: value});
  });
  await test('empty input cannot send', async () => { const p = page(); await p.submit(); assert.equal(p.requests.length, 0); });
  await test('conflicting fields cannot send', async () => { const p = page(); p.element('profiles').value = 'vless://test@example.invalid:443'; p.element('subscription').value = 'https://example.invalid/sub'; await p.submit(); assert.equal(p.requests.length, 0); });
  await test('invalid explicit subscription cannot send', async () => { const p = page(); p.element('subscription').value = 'file:///private'; await p.submit(); assert.equal(p.requests.length, 0); });
  await test('download failure explains TV network and hides raw server error', async () => {
    const p = page({ok: false, status: 400, data: {code: 'subscription_failed', error: 'SECRET token'}}); p.element('profiles').value = 'https://example.invalid/sub'; await p.submit();
    assert.match(p.element('status').textContent, /TV получил ссылку/); assert.match(p.element('status').textContent, /VPN на смартфоне/); assert.doesNotMatch(p.element('status').textContent, /SECRET/); assert.equal(p.element('send').disabled, false);
  });
  await test('unsupported provider response has a distinct explanation', async () => {
    const p = page({ok: false, status: 400, data: {code: 'subscription_format'}}); p.element('subscription').value = 'https://example.invalid/sub'; await p.submit(); assert.match(p.element('status').textContent, /не нашёл поддерживаемых профилей/);
  });
  await test('expired pairing asks for a new QR', async () => { const p = page({ok: false, status: 403, data: {error: 'Invalid or expired'}}); p.element('profiles').value = 'vless://test@example.invalid:443'; await p.submit(); assert.match(p.element('status').textContent, /отсканируйте QR заново/); });
  await test('oversize file rejected before reading', async () => { const p = page(); p.element('file').files = [{size: 3 * 1024 * 1024, text() {throw Error('Should not read');}}]; await p.element('file').events.change(); assert.match(p.element('status').textContent, /больше 2 МиБ/); });
  console.log(`Browser transfer summary: tests=${tests}, failures=0`);
})().catch(error => { console.error(error); process.exit(1); });
