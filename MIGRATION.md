# Review and rollout notes

## Scope

This branch consolidates the application around a pure `.cljc` engine, fixes cell boundaries and state retention, upgrades the JVM dependencies, and supplies a self-contained browser playground with a current self-hosted ClojureScript compiler.

The existing `master` already contains the self-hosted branch. No additional merge from `self-host-clojurescript` is needed. The Klipse page under `docs/` and the historical branches are retained during the migration to the new GitHub Pages deployment.

## Intentional changes

- Cells are booleans internally. The old unused chess/framework API (`zone.lambda.game.board`) and utilities are removed from the maintained source tree. The replacement public API is `game-of-life.engine/new-state` and `step`.
- Dead edges are now the default. Previously a horizontal neighbour could spill into the adjacent row. `:wrap` now wraps x and y separately; it is an explicit option, not the previous accidental topology.
- Both adapters retain current state rather than an infinite, globally rooted generation sequence. The desktop simulation updates at its configured frame rate. Browser requests are serialized by the UI, with stale responses discarded after pause/reset/edit.
- Rules and seed data have one maintained source. The old HTML files remain only as the live legacy deployment, not as build inputs.
- The browser adds six classic patterns and an empty board, with labels and descriptions generated from the shared catalog. Both browser and desktop accept cell clicks during playback. Browser edits invalidate in-flight generations so old results cannot overwrite them; changing a pattern or pressing Reset restores its seed. Browser keyboard and touch editing are also supported. Edits are kept in memory only.
- The replacement browser uses `cljs.js` 1.12.145 instead of Klipse's currently declared 1.10.597 compiler. It retains real in-browser ClojureScript evaluation and exposes a small cell-rule function contract. It is not a general-purpose package-loading REPL or a DOM editor.
- User edits are applied explicitly, rather than evaluating partially typed code. Rules are tested on all 18 boolean/neighbour-count combinations before the new worker becomes active. A rule should be pure; validation invokes it before simulation.
- The compiler is bundled with the artifact. The uncompressed worker bundle is approximately 8 MB; configure compression on a host that supports it. No asset is loaded from a CDN or moving Git reference.
- `:simple` optimization is intentional: self-hosted evaluation needs the compiler and runtime namespace names. Do not switch the worker to `:advanced` without a separate compatibility design.
- The rule worker has no DOM and supports `cljs.core` without an external namespace loader. Worker termination provides recovery from accidental infinite loops, not a hardened sandbox for untrusted remote programs. A dedicated site origin is preferable if this playground is later integrated with authenticated applications.
- Processing may print a native-access warning on recent JDKs. No global native-access JVM flag is imposed; select a recent JDK for both Leiningen and its subprocess as described in the README. Unused Quil export dependencies are excluded, including the old PDF and Bouncy Castle stack. This application supports the 2D renderer; reintroducing export/OpenGL features requires a separate dependency review.

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

## GitHub Pages deployment

The repository's Pages publishing source must be **GitHub Actions**. The workflow in `.github/workflows/ci.yml` replaces publication from `master:/docs` with the tested `target/site/` build. Changing the publishing source does not itself deploy this PR.

On a push to `master` (including a merge), or a manual workflow run on `master`:

1. Run JVM tests and desktop smoke tests on Java 21 and 25.
2. Build the browser site and run ClojureScript, worker, and browser tests under the `/game-of-life/` prefix.
3. Package that exact site, preserving `default-rule.cljs`, `js/worker.js`, and `.nojekyll`.
4. Deploy only after **both jobs succeed**. Only the deployment job receives Pages write and OIDC permissions. Pull requests package the artifact for validation but skip deployment. Master runs are serialized without cancelling an active deployment.

After the first deployment, verify <https://matlux.github.io/game-of-life/>: step, apply an edited rule, try invalid code, and recover from a runaway rule. The public deployment cannot be exercised by a pull request; review its deployment result after merging. Only after this check should the legacy `docs/` copies and their dependence on `self-host-clojurescript` be retired.

For rollback after a later release, revert the faulty application change on `master`; the workflow rebuilds, tests, and deploys the reverted version. To roll back this initial migration to the legacy site, disable the new workflow first (so it cannot overwrite the rollback), switch Pages back to **Deploy from a branch**, `master:/docs`, and trigger/verify the legacy Pages build. Keep the historical self-hosted branch available for that fallback.

No server-side Clojure runtime is needed. Infra1 and matlux.net remain alternative static hosts for the same complete artifact; moving there can be reviewed separately. A restrictive Content Security Policy must allow the self-hosted evaluator's JavaScript evaluation within its worker. Accounts, saving/sharing programs, arbitrary library imports, and multi-user execution are outside this change.
