import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./test/browser",
  timeout: 45000,
  expect: { timeout: 15000 },
  workers: 1,
  use: { baseURL: "http://127.0.0.1:8766/game-of-life/", screenshot: "only-on-failure", trace: "retain-on-failure" },
  webServer: { command: "node test/web/serve.mjs", url: "http://127.0.0.1:8766/game-of-life/", reuseExistingServer: !process.env.CI },
});
