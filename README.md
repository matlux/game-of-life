# Game of Life

One Clojure/ClojureScript engine, two ways to play:

- **JVM desktop:** a Quil window with the original 60 × 60 seed.
- **Browser playground:** edit and compile real ClojureScript rules, then run them on a canvas. All execution happens in your browser; no application server is required.

The browser build includes its own pinned ClojureScript compiler. It does not download code from a Git branch or a third-party CDN.

## Requirements

- Java **21 or 25** (LTS targets in CI). Local tests and desktop smoke tests were also run on Java 23 and 24.
- Leiningen **2.11+** (CI uses 2.12.0).
- Node.js **22+** and npm for browser tests and the preview server. Node is not needed by the deployed site.

Pinned runtime versions: Clojure **1.12.6**, Quil **4.3.1563**, ClojureScript **1.12.145**. Quil is used only by the desktop renderer. Unused monads, numeric-tower, browser rendering, and PDF/SVG/DXF export dependencies have been removed or excluded.

## Run the desktop application

Make sure Leiningen uses a recent JDK. Java 8 cannot run the upgraded dependencies.
On macOS, list installed JDKs with `/usr/libexec/java_home -V`, then select one
for the current terminal (replace `21` with your installed version):

```sh
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
export JAVA_CMD="$JAVA_HOME/bin/java"
export LEIN_JAVA_CMD="$JAVA_CMD"
lein version
```

Set all three variables because Leiningen itself and its application subprocess
can otherwise select different Java installations. `lein version` should report
the selected Java version. These exports affect only the current terminal.

```sh
lein run
```

Space pauses/resumes, **N** advances one generation and pauses, and **R** restores the original seed. Closing the window exits the application. The simulation advances at 10 generations per second while playing and retains only the current board.

Left-click a cell to turn it on or off while playing or paused. Editing preserves the generation counter and playback state.

Recent JDKs may print a native-access warning from Processing when opening the
desktop window. The project does not add a global native-access flag; it is not
needed to run on the tested JDKs. If startup reports
`Unrecognized option: --enable-native-access=ALL-UNNAMED`, update this branch and
select a recent JDK as above.

## Run the browser playground locally

```sh
lein web-build
node test/web/serve.mjs
```

Open <http://127.0.0.1:8766/game-of-life/>. The first build downloads the compiler and may take a minute. Subsequent builds reuse the compiler cache.

The board starts paused. Use **Play**, **Pause**, **Step**, or **Reset board**. Choose the original seed, Glider, Blinker, **Pulsar**, **Pentadecathlon**, **Lightweight spaceship**, **Gosper glider gun**, **Acorn**, **Diehard**, or an **Empty board**. Each preset has an explanation of what to watch for. Select dead edges or wraparound; change speed and colour independently of the rules. The descriptions assume Conway’s rules; this finite 60 × 60 board and its edges can alter travelling or long-lived patterns.

**Click or tap any cell to toggle it, even while playing.** For keyboard editing, focus the board, choose a cell with the arrow keys, and press Enter or Space. An outline marks the selected cell. Edits do not advance time or pause playback. A generation calculated from the board before an edit is discarded, so it cannot overwrite the edit; the next calculation uses the edited board. Under Conway’s rules, a newly added isolated cell can naturally die on the next generation. Editing is temporarily disabled while loading, resetting, or applying a rule. **Reset board** restores the selected seed.

The shared pattern catalog in `patterns.cljc` supplies both the worker’s seeds and the generated `patterns.json` dropdown labels and descriptions. These are standard [Conway patterns](https://conwaylife.com/wiki/Category:Patterns); no pattern files are fetched at runtime.

The editor evaluates a ClojureScript expression whose result is a function:

```clojure
(fn [alive? neighbours]
  (or (= neighbours 3)
      (and alive? (= neighbours 2))))
```

`alive?` is a boolean and `neighbours` is an integer from 0 to 8. The function must return `true` or `false`. For example, `(fn [alive? _] alive?)` freezes the cells; `(fn [_ _] false)` kills every cell.

Click **Apply rule** or press Ctrl/⌘ + Enter. Applying pauses playback and preserves the current board. Syntax errors and invalid return values preserve the previous rule. Each edit compiles in a fresh worker, so a failed edit cannot redefine helpers used by the previous rule. **Restore Conway** cancels an in-progress edit or replaces a stopped runtime while preserving the last board. **Reset board** retains the active rule and loads the selected pattern.

The evaluator includes `cljs.core`; helper definitions inside `let` or `do` are supported. Additional library imports and DOM access are not provided. Evaluation and stepping run in a disposable Web Worker with a three-second deadline. Workers protect page responsiveness; they are not a security sandbox for arbitrary third-party code. There is no server-side code execution or code sharing/persistence in this version.

## Test

```sh
lein test
lein desktop-smoke
lein web-build
lein cljs-test-build
node target/cljs-tests.js
npm ci
npm run test:workers
npx playwright install chromium
npm run test:browser
```

`desktop-smoke` needs a display. It opens a window, checks that at least five generations render, and closes it. Linux CI uses `xvfb-run`.

The shared `.cljc` tests exercise Conway's truth table, oscillator periods, spaceship movement, glider-gun emissions, Diehard’s 130-generation lifetime, rectangular boards, both boundary modes, state isolation and invalid inputs. Production-worker tests compare complete boards against JVM-generated fixtures for 20 generations in both edge modes. They also compile the canonical rule and edited functions and terminate runaway evaluation/stepping. Browser tests exercise the actual page, preset selection, mouse/keyboard/touch editing, edits during an in-flight generation, recovery controls, mobile layout, and operation under a `/game-of-life/` URL prefix.

GitHub Actions runs JVM tests and desktop smoke tests on Java 21 and 25, plus ClojureScript, worker, and browser tests. A successful browser job uploads a `game-of-life-site` artifact and packages that same build for GitHub Pages. After all checks pass on `master`, the deployment job publishes the new playground. Pull requests never deploy. You can also manually run the workflow on `master` to rebuild, test, and publish it.

## Source layout

| Path | Responsibility |
| --- | --- |
| `src/game_of_life/engine.cljc` | Pure board construction, boundaries, neighbour counting and stepping |
| `src/game_of_life/rules.cljc` | Canonical Conway rule |
| `src/game_of_life/patterns.cljc` | Shared patterns, including the original seed |
| `src/game_of_life/desktop.clj` | Quil rendering and desktop controls |
| `src/game_of_life/evaluator.cljs` | Self-hosted compilation and rule validation |
| `src/game_of_life/worker.cljs` | Browser worker protocol and simulation execution |
| `web/` | Browser UI and disposable worker client |
| `dev/game_of_life/build.clj` | Static build, editable rule extraction and JVM test fixtures |
| `target/site/` | Generated, portable static site; ignored by Git |
| `docs/` | Frozen legacy Klipse site, retained during the Pages migration |

The editable default expression is **generated from** `rules.cljc` during the build. It is not a second handwritten rule implementation. Only the engine and patterns are shared; neither DOM APIs nor Quil dependencies are needed to use the pure engine.

```clojure
(require '[game-of-life.engine :as life]
         '[game-of-life.patterns :as patterns])

(def state (life/new-state patterns/glider :wrap))
(life/step state)
(life/step state (fn [alive? _] alive?))
```

## Deployment and branch history

`self-host-clojurescript` was merged into `master` in December 2016. The modernization starts from that existing merge. The old `experiment` and `parallel-experimental` branches remain historical experiments; their old source trees are not merged into the new engine.

**Do not delete `self-host-clojurescript` yet:** the legacy `docs/index.html` still loads its framework from that branch. Retain it until the first successful deployment of the replacement has been verified.

GitHub Pages publishes the **contents** of `target/site/` (including `.nojekyll`) at <https://matlux.github.io/game-of-life/> after a successful `master` workflow run. The repository's Pages source must be **GitHub Actions**, rather than `master:/docs`. Merging this PR will publish the replacement once CI succeeds. All runtime URLs are relative; a static directory behind infra1's reverse proxy or a suitable matlux.net path can also serve the same artifact. See [MIGRATION.md](MIGRATION.md) for rollout and rollback instructions.

## License

Copyright © 2016 Mathieu Gauthron.

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version. Third-party dependencies retain their own licenses.
