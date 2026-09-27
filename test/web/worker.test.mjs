import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { Worker } from "node:worker_threads";
import { resolve } from "node:path";
import { LifeWorker } from "../../web/worker-client.mjs";

const fixtures = JSON.parse(readFileSync("target/test-fixtures.json", "utf8"));

// Run the exact production web-worker bundle, with only the messaging API
// adapted to Node. The ClojureScript compiler and engine are not mocked.
function nodeWorkerFactory(path) {
  const thread = new Worker(`
    const { parentPort, workerData } = require('node:worker_threads');
    global.self = global;
    global.postMessage = data => parentPort.postMessage(data);
    parentPort.on('message', data => global.onmessage({ data }));
    require('node:vm').runInThisContext(require('node:fs').readFileSync(workerData, 'utf8'));
  `, { eval: true, workerData: path });
  const adapter = { terminate: () => thread.terminate(), postMessage: data => thread.postMessage(data) };
  thread.on("message", data => adapter.onmessage?.({ data }));
  thread.on("error", error => adapter.onerror?.({ message: error.message }));
  return adapter;
}

function runtime(t, timeout = 3000) {
  const client = new LifeWorker(resolve("target/site/js/worker.js"), { factory: nodeWorkerFactory, timeout });
  t.after(() => client.close());
  return client;
}

test("production ClojureScript matches the JVM for 20 generations with both boundary modes", async t => {
  const client = runtime(t);
  for (const boundary of ["dead", "wrap"]) {
    let { state } = await client.request("seed", { pattern: "original", boundary });
    assert.deepEqual(state, fixtures.trajectories[boundary][0]);
    for (const expected of fixtures.trajectories[boundary].slice(1)) {
      ({ state } = await client.request("step", { state }));
      assert.deepEqual(state, expected);
    }
  }
});

test("canonical source compiles at runtime and matches JVM Conway behaviour", async t => {
  const client = runtime(t);
  assert.equal((await client.request("evaluate", { source: fixtures["rule-source"] })).applied, true);
  const { state } = await client.request("step", { state: fixtures.trajectories.dead[0] });
  assert.deepEqual(state, fixtures.trajectories.dead[1]);
});

test("edited ClojureScript functions and local helper definitions change the running rule", async t => {
  const client = runtime(t);
  const original = fixtures.trajectories.dead[0];
  await client.request("evaluate", { source: "(let [keep-cell (fn [cell] cell)] (fn [alive? neighbours] (keep-cell alive?)))" });
  const frozen = await client.request("step", { state: original });
  assert.deepEqual(frozen.state.board, original.board);
  assert.equal(frozen.state.generation, 1);
  await client.request("evaluate", { source: "(fn [_ _] false)" });
  assert.ok((await client.request("step", { state: original })).state.board.every(x => x === false));
});

test("syntax errors, non-functions, non-boolean results, and missing libraries are rejected", async t => {
  const client = runtime(t);
  for (const source of ["(fn [", "42", "(fn [_ _] nil)", "(fn [_ _] (throw (js/Error. \"bad rule\")))", "(require '[missing.library :as missing])"]) {
    await assert.rejects(client.request("evaluate", { source }));
  }
  const { state } = await client.request("step", { state: fixtures.trajectories.dead[0] });
  assert.deepEqual(state, fixtures.trajectories.dead[1]);
});

test("an infinite evaluation is terminated and a fresh runtime remains usable", async t => {
  const client = runtime(t, 500);
  await assert.rejects(client.request("evaluate", { source: "(loop [] (recur))" }), /took too long/);
  assert.equal(client.closed, true);
  const replacement = runtime(t);
  const { state } = await replacement.request("step", { state: fixtures.trajectories.dead[0] });
  assert.deepEqual(state, fixtures.trajectories.dead[1]);
});

test("a rule that hangs only during stepping is also terminated", async t => {
  const client = runtime(t, 800);
  await client.request("evaluate", { source: "(let [calls (atom 0)] (fn [_ _] (if (> (swap! calls inc) 18) (loop [] (recur)) false)))" });
  await assert.rejects(client.request("step", { state: fixtures.trajectories.dead[0] }), /took too long/);
});

test("closing a worker cancels requests instead of leaving callers waiting", async t => {
  const client = runtime(t);
  await client.ready;
  const pending = client.request("evaluate", { source: "(loop [] (recur))" });
  const rejected = assert.rejects(pending, /Runtime replaced|Runtime stopped/);
  client.close();
  await rejected;
});
