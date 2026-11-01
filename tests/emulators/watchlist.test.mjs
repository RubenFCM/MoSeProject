import { test } from 'node:test';
import assert from 'node:assert/strict';
import { randomUUID } from 'node:crypto';

// No npm packages, real users, credentials, or production endpoints are needed.
const project = process.env.GCLOUD_PROJECT;
assert.ok(project?.startsWith('demo-'), 'Tests require a Firebase demo project');
function localHost(value) {
  assert.ok(value, 'Both Firebase emulators must be started with emulators:exec');
  const url = new URL(`http://${value}`);
  assert.ok(['127.0.0.1', 'localhost', '[::1]'].includes(url.hostname), 'Emulator must be local');
  return url.origin;
}
const auth = localHost(process.env.FIREBASE_AUTH_EMULATOR_HOST);
const firestore = localHost(process.env.FIRESTORE_EMULATOR_HOST);
const database = `projects/${project}/databases/(default)`;
const authApi = `${auth}/identitytoolkit.googleapis.com/v1`;
async function request(url, body, token, method = 'POST') {
  const response = await fetch(url, { method, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) }, ...(body ? { body: JSON.stringify(body) } : {}) });
  let result; try { result = await response.json(); } catch { result = null; }
  return { status: response.status, result };
}
async function account(verified = true) {
  const email = `watchlist-${randomUUID()}@example.test`;
  const password = 'LocalTestOnly!123';
  const signup = await request(`${authApi}/accounts:signUp?key=emulator-only`, { email, password, returnSecureToken: true });
  assert.equal(signup.status, 200, 'Auth emulator signup must succeed');
  if (verified) {
    const sent = await request(`${authApi}/accounts:sendOobCode?key=emulator-only`, { requestType: 'VERIFY_EMAIL', idToken: signup.result.idToken });
    assert.equal(sent.status, 200);
    const codes = await request(`${auth}/emulator/v1/projects/${project}/oobCodes`, null, null, 'GET');
    const code = codes.result.oobCodes.find(c => c.email === email && c.requestType === 'VERIFY_EMAIL');
    assert.ok(code);
    const applied = await request(`${authApi}/accounts:update?key=emulator-only`, { oobCode: code.oobCode });
    assert.equal(applied.status, 200);
  }
  const signedIn = await request(`${authApi}/accounts:signInWithPassword?key=emulator-only`, { email, password, returnSecureToken: true });
  assert.equal(signedIn.status, 200);
  const token = signedIn.result.idToken;
  assert.equal(JSON.parse(Buffer.from(token.split('.')[1], 'base64url')).email_verified, verified);
  return { uid: signup.result.localId, token };
}
function fields(data) {
  return Object.fromEntries(Object.entries(data).map(([key, value]) => [key, encode(value)]));
}
function encode(value) {
  if (value === null) return { nullValue: null };
  if (Array.isArray(value)) return { arrayValue: { values: value.map(encode) } };
  if (typeof value === 'object') return { mapValue: { fields: fields(value) } };
  if (typeof value === 'number') return { integerValue: String(value) };
  if (typeof value === 'boolean') return { booleanValue: value };
  return { stringValue: value };
}
const movie = (id = 123) => ({ tmdbId: id, mediaType: 'movie', catalog: { title: 'Test movie', posterPath: null, releaseDate: null, runtimeMinutes: 120, providers: null }, status: 'pending', progress: { kind: 'movie', unit: 'minutes', positionMinutes: null }, progressVersion: 0 });
const series = () => ({ ...movie(), mediaType: 'tv', catalog: { ...movie().catalog, runtimeMinutes: null }, progress: { kind: 'tv', unit: 'minutes', current: null, lastCompleted: null } });
const path = (uid, id = 'movie_123') => `${database}/documents/users/${uid}/watchlist/${id}`;
async function write(user, data, { id = 'movie_123', create = true, mask = null, providersTimestamp = false, uid = user.uid } = {}) {
  const write = { update: { name: path(uid, id), fields: fields(data) }, updateTransforms: [{ fieldPath: 'updatedAt', setToServerValue: 'REQUEST_TIME' }] };
  if (create) { write.updateTransforms.push({ fieldPath: 'createdAt', setToServerValue: 'REQUEST_TIME' }); write.currentDocument = { exists: false }; }
  if (mask) write.updateMask = { fieldPaths: mask };
  if (providersTimestamp) write.updateTransforms.push({ fieldPath: 'catalog.providers.checkedAt', setToServerValue: 'REQUEST_TIME' });
  return request(`${firestore}/v1/${database}/documents:commit`, { writes: [write] }, user.token);
}
async function read(user, uid = user.uid, id = 'movie_123') {
  return request(`${firestore}/v1/${path(uid, id)}`, null, user.token, 'GET');
}

test('verified owner creates once, concurrent duplicate creates cannot reset progress', async () => {
  const user = await account();
  const attempts = await Promise.all(Array.from({ length: 4 }, () => write(user, movie())));
  assert.equal(attempts.filter(r => r.status === 200).length, 1);
  const updated = await write(user, { status: 'in_progress', progress: { kind: 'movie', unit: 'minutes', positionMinutes: 45 }, progressVersion: 1 }, { create: false, mask: ['status', 'progress', 'progressVersion'] });
  assert.equal(updated.status, 200);
  assert.notEqual((await write(user, movie())).status, 200);
  const saved = (await read(user)).result.fields;
  assert.equal(saved.progress.mapValue.fields.positionMinutes.integerValue, '45');
  assert.equal(saved.catalog.mapValue.fields.title.stringValue, 'Test movie');
  assert.ok(saved.createdAt.timestampValue && saved.updatedAt.timestampValue);
});

test('unauthenticated, unverified, and other users cannot read or write a private entry', async () => {
  const owner = await account(); const other = await account(); const unverified = await account(false);
  assert.equal((await write(owner, movie())).status, 200);
  assert.equal((await request(`${firestore}/v1/${path(owner.uid)}`, null, null, 'GET')).status, 403);
  assert.equal((await read(other, owner.uid)).status, 403);
  assert.equal((await read(unverified, owner.uid)).status, 403);
  assert.equal((await write(unverified, movie())).status, 403);
  assert.equal((await write(other, movie(999), { uid: owner.uid, id: 'movie_999' })).status, 403);
});

test('rules reject extra fields, wrong IDs, negative and excessive minutes, and inconsistent states', async () => {
  const user = await account();
  assert.equal((await write(user, { ...movie(), token: 'not-allowed' })).status, 403);
  assert.equal((await write(user, movie(), { id: 'movie_999' })).status, 403);
  assert.equal((await write(user, movie())).status, 200);
  for (const [status, minute] of [['in_progress', -1], ['in_progress', 121], ['pending', 45], ['watched', 45], ['in_progress', 120]]) {
    assert.equal((await write(user, { status, progress: { kind: 'movie', unit: 'minutes', positionMinutes: minute }, progressVersion: 1 }, { create: false, mask: ['status', 'progress', 'progressVersion'] })).status, 403);
  }
});

test('provider refresh preserves progress, unknown catalog values are accepted, and only owner can delete', async () => {
  const user = await account(); const other = await account();
  const data = movie(); data.catalog.runtimeMinutes = null;
  assert.equal((await write(user, data)).status, 200);
  assert.equal((await write(user, { status: 'in_progress', progress: { kind: 'movie', unit: 'minutes', positionMinutes: 10 }, progressVersion: 1 }, { create: false, mask: ['status','progress','progressVersion'] })).status, 200);
  const providers = { region: 'ES', link: null, subscription: [], free: [], ads: [], rent: [], buy: [] };
  assert.equal((await write(user, { catalog: { ...data.catalog, providers } }, { create: false, mask: ['catalog'], providersTimestamp: true })).status, 200);
  const stored = (await read(user)).result.fields;
  assert.equal(stored.progress.mapValue.fields.positionMinutes.integerValue, '10');
  assert.equal(stored.progressVersion.integerValue, '1');
  assert.ok(stored.catalog.mapValue.fields.providers.mapValue.fields.checkedAt.timestampValue);
  assert.equal((await request(`${firestore}/v1/${path(user.uid)}`, null, other.token, 'DELETE')).status, 403);
  assert.equal((await request(`${firestore}/v1/${path(user.uid)}`, null, user.token, 'DELETE')).status, 200);
  assert.equal((await read(user)).status, 404);
});

test('series rules accept season zero and completion without completing the entire show', async () => {
  const user = await account();
  assert.equal((await write(user, series(), { id: 'tv_123' })).status, 200);
  const episode = { id: 33, season: 0, number: 1, title: null, airDate: null, runtimeMinutes: 42 };
  const progress = { kind: 'tv', unit: 'minutes', current: { episode, positionMinutes: 18, mode: 'watching' }, lastCompleted: null };
  assert.equal((await write(user, { status: 'in_progress', progress, progressVersion: 1 }, { id: 'tv_123', create: false, mask: ['status','progress','progressVersion'] })).status, 200);
  const invalid = { ...progress, current: { ...progress.current, episode: { ...episode, season: -1 } } };
  assert.equal((await write(user, { status: 'in_progress', progress: invalid, progressVersion: 2 }, { id: 'tv_123', create: false, mask: ['status','progress','progressVersion'] })).status, 403);
  const completed = { kind: 'tv', unit: 'minutes', current: null, lastCompleted: episode };
  assert.equal((await write(user, { status: 'in_progress', progress: completed, progressVersion: 2 }, { id: 'tv_123', create: false, mask: ['status','progress','progressVersion'] })).status, 200);
  assert.equal((await read(user, user.uid, 'tv_123')).result.fields.status.stringValue, 'in_progress');
});

test('collection reads must be scoped to the current UID and explicitly limited', async () => {
  const user = await account(); const other = await account();
  assert.equal((await write(user, movie())).status, 200);
  const query = async (uid, limit) => request(`${firestore}/v1/${database}/documents/users/${uid}:runQuery`, {
    structuredQuery: { from: [{ collectionId: 'watchlist' }], ...(limit ? { limit } : {}) }
  }, user.token);
  assert.equal((await query(user.uid, 30)).status, 200);
  assert.equal((await query(user.uid, null)).status, 403);
  assert.equal((await query(user.uid, 31)).status, 403);
  assert.equal((await query(other.uid, 30)).status, 403);
});
