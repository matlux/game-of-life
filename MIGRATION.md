# Review and rollout notes

## Scope

This branch consolidates the application around a pure `.cljc` engine, fixes cell boundaries and state retention, upgrades the JVM dependencies, and supplies a self-contained browser playground with a current self-hosted ClojureScript compiler.

The existing `master` already contains the self-hosted branch. No additional merge from `self-host-clojurescript` is needed. The public Klipse page under `docs/` and the historical branches are retained until deployment migration is explicitly chosen.

## Intentional changes

- Cells are booleans internally. The old unused chess/framework API (`zone.lambda.game.board`) and utilities are removed from the maintained source tree. The replacement public API is `game-of-life.engine/new-state` and `step`.
- Dead edges are now the default. Previously a horizontal neighbour could spill into the adjacent row. `:wrap` now wraps x and y separately; it is an explicit option, not the previous accidental topology.
- Both adapters retain current state rather than an infinite, globally rooted generation sequence. The desktop simulation updates at its configured frame rate. Browser requests are serialized by the UI, with stale responses discarded after pause/reset/edit.
- Rules and seed data have one maintained source. The old HTML files remain only as the live legacy deployment, not as build inputs.
- The replacement browser uses `cljs.js` 1.12.145 instead of Klipse's currently declared 1.10.597 compiler. It retains real in-browser ClojureScript evaluation and exposes a small cell-rule function contract. It is not a general-purpose package-loading REPL or a DOM editor.
- User edits are applied explicitly, rather than evaluating partially typed code. Rules are tested on all 18 boolean/neighbour-count combinations before the new worker becomes active. A rule should be pure; validation invokes it before simulation.
- The compiler is bundled with the artifact. The uncompressed worker bundle is approximately 8 MB; configure compression on a host that supports it. No asset is loaded from a CDN or moving Git reference.
- `:simple` optimization is intentional: self-hosted evaluation needs the compiler and runtime namespace names. Do not switch the worker to `:advanced` without a separate compatibility design.
- The rule worker has no DOM and supports `cljs.core` without an external namespace loader. Worker termination provides recovery from accidental infinite loops, not a hardened sandbox for untrusted remote programs. A dedicated site origin is preferable if this playground is later integrated with authenticated applications.
- Native Java access is enabled for Processing. Unused Quil export dependencies are excluded, including the old PDF and Bouncy Castle stack. This application supports the 2D renderer; reintroducing export/OpenGL features requires a separate dependency review.

## Review locally

1. Run `lein test`, then `lein desktop-smoke` and `lein run` on a machine with a display.
2. Run `lein web-build` and `node test/web/serve.mjs`.
3. Open `http://127.0.0.1:8766/game-of-life/`.
4. Step, play, pause, reset, and try both boundary settings. Select the blinker or glider for recognizable behaviour.
5. Apply `(fn [alive? _] alive?)`; step and verify the cells remain unchanged.
6. Apply `(fn [_ _] false)`; step and verify all cells die.
7. Enter invalid code and verify the previous board and rule survive.
8. Apply `(loop [] (recur))`; after three seconds the page should report termination and remain usable. Restore Conway and continue.
9. Run the documented ClojureScript, worker and browser suites; inspect CI results and the generated site artifact.

## Deployment is deferred

Current GitHub Pages configuration serves `master:/docs`. This branch does not change that setting or add an automatic deployment job.

When the replacement is approved:

1. Choose GitHub Pages, infra1, or a matlux.net location. No server-side Clojure runtime is needed for the browser version.
2. Deploy the already-tested `game-of-life-site` CI artifact to a preview location. Preserve the full artifact, including `default-rule.cljs` and `js/worker.js`.
3. Verify HTTPS, relative paths, worker loading, code evaluation, errors, and recovery at the actual public URL. A restrictive Content Security Policy must allow the self-hosted evaluator's JavaScript evaluation within its worker.
4. For GitHub Pages, explicitly switch the publishing source to GitHub Actions and add a reviewed deployment workflow. For infra1, serve the artifact as static files and enable compression. Avoid caching HTML indefinitely; use cache revalidation or versioned releases for the worker and default source.
5. Keep a previous artifact for rollback; publish each complete release together so HTML, compiler, and rule source remain consistent.
6. After the replacement is live, retire the legacy `docs/index.html`/`test.html` copies and remove their dependence on `self-host-clojurescript`. Only then consider deleting that branch.

Accounts, saving/sharing programs, arbitrary library imports, and multi-user execution are outside this change. Hosting and those product choices can be reviewed separately.
