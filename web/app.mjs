import { LifeWorker } from "./worker-client.mjs";

const el = Object.fromEntries(["board", "generation", "population", "toggle", "step", "reset", "speed", "speed-value", "pattern", "pattern-description", "boundary", "colour", "rule", "apply", "restore", "status", "rule-name"].map(id => [id, document.getElementById(id)]));
const workerURL = new URL("./js/worker.js", import.meta.url);
const context = el.board.getContext("2d");
let worker = new LifeWorker(workerURL);
let candidate = null;
let state = null;
let defaultSource = "";
let running = false;
let busy = true;
let stepping = false;
let epoch = 0;
let timer;
let catalog = [];
let selectedCell = [30, 30];

function status(message, error = false) {
  el.status.textContent = message;
  el.status.classList.toggle("error", error);
}

function controls() {
  el.toggle.textContent = running ? "Pause" : "Play";
  el.toggle.disabled = busy || !state || worker.closed || (stepping && !running);
  el.step.disabled = busy || !state || worker.closed || stepping || running;
  el.reset.disabled = busy || !state || worker.closed;
  el.apply.disabled = busy || !state || !defaultSource;
  el.rule.disabled = !defaultSource;
  el.restore.disabled = !defaultSource;
  el.pattern.disabled = busy || !state || worker.closed;
  el.boundary.disabled = busy || !state || worker.closed;
  el.board.setAttribute("aria-disabled", String(busy || !state));
}

function draw() {
  if (!state) return;
  context.fillStyle = "#0c100e";
  context.fillRect(0, 0, el.board.width, el.board.height);
  context.fillStyle = el.colour.value;
  const cellWidth = el.board.width / state.width;
  const cellHeight = el.board.height / state.height;
  state.board.forEach((alive, i) => {
    if (alive) context.fillRect((i % state.width) * cellWidth, Math.floor(i / state.width) * cellHeight, cellWidth, cellHeight);
  });
  context.strokeStyle = "#243027";
  context.lineWidth = 0.5;
  context.beginPath();
  for (let x = 1; x < state.width; x++) {
    context.moveTo(x * cellWidth, 0); context.lineTo(x * cellWidth, el.board.height);
  }
  for (let y = 1; y < state.height; y++) {
    context.moveTo(0, y * cellHeight); context.lineTo(el.board.width, y * cellHeight);
  }
  context.stroke();
  const [x, y] = selectedCell;
  if (document.activeElement === el.board) {
    context.strokeStyle = "#ffffff";
    context.lineWidth = 2;
    context.strokeRect(x * cellWidth + 1, y * cellHeight + 1, cellWidth - 2, cellHeight - 2);
  }
  el.generation.textContent = state.generation;
  el.population.textContent = state.board.filter(Boolean).length;
  el.board.setAttribute("aria-label", `Game of Life board, generation ${state.generation}, ${el.population.textContent} live cells. Selected cell ${x + 1}, ${y + 1}: ${state.board[y * state.width + x] ? "alive" : "dead"}`);
}

function toggleCell(x, y) {
  if (busy || !state) return;
  // A generation computed from the pre-edit board must not overwrite this edit.
  // Keep playing; advance() schedules a fresh generation once that request ends.
  epoch += 1;
  const board = state.board.slice();
  const index = y * state.width + x;
  board[index] = !board[index];
  state = { ...state, board };
  draw();
}

el.board.addEventListener("click", event => {
  if (busy || !state) return;
  const rect = el.board.getBoundingClientRect();
  const x = Math.floor((event.clientX - rect.left - el.board.clientLeft) / el.board.clientWidth * state.width);
  const y = Math.floor((event.clientY - rect.top - el.board.clientTop) / el.board.clientHeight * state.height);
  if (x < 0 || y < 0 || x >= state.width || y >= state.height) return;
  selectedCell = [x, y];
  el.board.focus({ preventScroll: true });
  toggleCell(x, y);
});
el.board.addEventListener("keydown", event => {
  if (busy || !state) return;
  const directions = { ArrowLeft: [-1, 0], ArrowRight: [1, 0], ArrowUp: [0, -1], ArrowDown: [0, 1] };
  if (directions[event.key]) {
    event.preventDefault();
    const [dx, dy] = directions[event.key];
    selectedCell = [Math.max(0, Math.min(state.width - 1, selectedCell[0] + dx)),
      Math.max(0, Math.min(state.height - 1, selectedCell[1] + dy))];
    draw();
  } else if (event.key === "Enter" || event.key === " ") {
    event.preventDefault();
    if (!event.repeat) toggleCell(...selectedCell);
  }
});
el.board.addEventListener("focus", draw);
el.board.addEventListener("blur", draw);

function pause() {
  running = false;
  clearTimeout(timer);
  epoch += 1; // Discard results from a generation requested before pause/reset/edit.
  controls();
}

function schedule() {
  clearTimeout(timer);
  if (running) timer = setTimeout(advance, 1000 / Number(el.speed.value));
}

async function advance() {
  if (stepping || busy || !state) return;
  const token = epoch;
  const runtime = worker;
  stepping = true;
  controls();
  try {
    const result = await runtime.request("step", { state });
    if (token === epoch) { state = result.state; draw(); }
  } catch (error) {
    if (token === epoch || (runtime === worker && runtime.closed)) {
      pause();
      status(`${error.message} Choose Restore Conway to recover.`, true);
    }
  } finally {
    stepping = false;
    controls();
    schedule();
  }
}

async function resetBoard() {
  pause();
  const token = epoch;
  busy = true;
  controls();
  try {
    const result = await worker.request("seed", { pattern: el.pattern.value, boundary: el.boundary.value });
    if (token === epoch) {
      state = result.state;
      el["pattern-description"].textContent = catalog.find(pattern => pattern.id === el.pattern.value).description;
      draw();
      status("Board reset. Your active rule is unchanged. Press Play to start.");
    }
  } catch (error) {
    if (token === epoch) status(error.message, true);
  } finally {
    if (token === epoch) { busy = false; controls(); }
  }
}

async function applyRule() {
  if (busy || !state) return;
  pause();
  const token = epoch;
  const source = el.rule.value;
  busy = true;
  controls();
  status("Compiling your ClojureScript…");
  // Compile in a fresh worker: failed code cannot mutate helpers used by the
  // previous function. Swap workers only after all 18 rule inputs validate.
  const nextWorker = new LifeWorker(workerURL);
  candidate = nextWorker;
  try {
    await nextWorker.request("evaluate", { source });
    if (token !== epoch) { nextWorker.close(); return; }
    worker.close();
    worker = nextWorker;
    candidate = null;
    el["rule-name"].textContent = source.trim() === defaultSource.trim() ? "Conway’s rules" : "Your rules";
    status("Rule applied. Board preserved. Press Play or Step to see what happens.");
  } catch (error) {
    nextWorker.close();
    if (token === epoch) {
      candidate = null;
      status(`${error.message}\nPrevious rule and board preserved.`, true);
    }
  } finally {
    if (token === epoch) { busy = false; controls(); }
  }
}

async function restoreConway() {
  pause();
  const token = epoch;
  busy = true;
  candidate?.close();
  candidate = null;
  worker.close();
  worker = new LifeWorker(workerURL);
  el.rule.value = defaultSource;
  el["rule-name"].textContent = "Conway’s rules";
  controls();
  try {
    await worker.ready;
    if (token !== epoch) return;
    if (!state) {
      const result = await worker.request("seed", { pattern: el.pattern.value, boundary: el.boundary.value });
      if (token !== epoch) return;
      state = result.state;
    }
    draw();
    status("Conway’s rules restored. Press Play or Step to continue.");
  } catch (error) {
    if (token === epoch) status(error.message, true);
  } finally {
    if (token === epoch) { busy = false; controls(); }
  }
}

el.toggle.addEventListener("click", () => {
  if (running) pause();
  else { running = true; controls(); schedule(); }
});
el.step.addEventListener("click", advance);
el.reset.addEventListener("click", resetBoard);
el.pattern.addEventListener("change", resetBoard);
el.boundary.addEventListener("change", resetBoard);
el.colour.addEventListener("input", draw);
el.speed.addEventListener("input", () => {
  el["speed-value"].textContent = `${el.speed.value} / sec`;
  schedule();
});
el.apply.addEventListener("click", applyRule);
el.restore.addEventListener("click", restoreConway);
el.rule.addEventListener("keydown", event => {
  if (event.key === "Enter" && (event.ctrlKey || event.metaKey)) {
    event.preventDefault();
    applyRule();
  }
});
window.addEventListener("pagehide", () => { pause(); worker.close(); candidate?.close(); });

async function boot() {
  controls();
  try {
    const [response, patternsResponse] = await Promise.all([
      fetch(new URL("./default-rule.cljs", import.meta.url)),
      fetch(new URL("./patterns.json", import.meta.url)),
    ]);
    if (!response.ok || !patternsResponse.ok) throw new Error("Could not load the rule or patterns. Reload the page to try again.");
    catalog = await patternsResponse.json();
    for (const pattern of catalog) el.pattern.add(new Option(pattern.name, pattern.id));
    defaultSource = await response.text();
    el.rule.value = defaultSource;
    await resetBoard();
  } catch (error) {
    busy = false;
    status(error.message, true);
    controls();
  }
}

boot();
