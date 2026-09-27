import { test, expect } from "@playwright/test";

test.beforeEach(async ({ page }) => {
  await page.goto("./");
  await expect(page.getByRole("button", { name: "Play", exact: true })).toBeEnabled();
});

async function apply(page, source) {
  await page.getByLabel("ClojureScript rule", { exact: true }).fill(source);
  await page.getByRole("button", { name: "Apply rule", exact: true }).click();
}

test("loads without external runtime requests and supports play, pause, step, reset and edges", async ({ page }) => {
  const external = [];
  page.on("request", request => {
    if (new URL(request.url()).origin !== "http://127.0.0.1:8766") external.push(request.url());
  });
  await page.reload();
  await expect(page.locator("#population")).toHaveText("97");
  await page.screenshot({ path: "test-results/desktop-preview.png", fullPage: true });
  await page.getByRole("button", { name: "Step", exact: true }).click();
  await expect(page.locator("#generation")).toHaveText("1");
  await expect(page.locator("#population")).toHaveText("105");
  await page.getByRole("button", { name: "Play", exact: true }).click();
  await expect.poll(async () => Number(await page.locator("#generation").textContent())).toBeGreaterThan(2);
  await page.getByRole("button", { name: "Pause", exact: true }).click();
  const generation = await page.locator("#generation").textContent();
  await page.waitForTimeout(700);
  await expect(page.locator("#generation")).toHaveText(generation);
  await page.getByRole("button", { name: "Reset board", exact: true }).click();
  await expect(page.locator("#generation")).toHaveText("0");
  await page.getByLabel("Pattern", { exact: true }).selectOption("blinker");
  await expect(page.locator("#population")).toHaveText("3");
  await page.getByLabel("Edges", { exact: true }).selectOption("wrap");
  await expect(page.getByRole("button", { name: "Step", exact: true })).toBeEnabled();
  await page.getByRole("button", { name: "Step", exact: true }).click();
  await expect(page.locator("#generation")).toHaveText("1");
  await expect(page.locator("#population")).toHaveText("3");
  expect(external).toEqual([]);
});

test("edits real ClojureScript; failed edits preserve the last rule; Conway can be restored", async ({ page }) => {
  await apply(page, "(fn [_ _] false)");
  await expect(page.locator("#status")).toContainText("Rule applied");
  await page.getByRole("button", { name: "Step", exact: true }).click();
  await expect(page.locator("#population")).toHaveText("0");
  await page.getByRole("button", { name: "Reset board", exact: true }).click();
  await expect(page.locator("#population")).toHaveText("97");
  await apply(page, "(fn [");
  await expect(page.locator("#status")).toContainText("Previous rule and board preserved");
  await expect(page.locator("#generation")).toHaveText("0");
  await expect(page.locator("#population")).toHaveText("97");
  await page.getByRole("button", { name: "Step", exact: true }).click();
  await expect(page.locator("#population")).toHaveText("0");
  await page.getByRole("button", { name: "Restore Conway", exact: true }).click();
  await expect(page.locator("#status")).toContainText("Conway’s rules restored");
  await page.getByRole("button", { name: "Reset board", exact: true }).click();
  await expect(page.locator("#generation")).toHaveText("0");
  await page.getByRole("button", { name: "Step", exact: true }).click();
  await expect(page.locator("#population")).toHaveText("105");
});

test("runaway evaluation times out without blocking the page", async ({ page }) => {
  await apply(page, "(loop [] (recur))");
  await expect(page.locator("#status")).toContainText("took too long");
  await expect(page.locator("#generation")).toHaveText("0");
  await page.getByRole("button", { name: "Step", exact: true }).click();
  await expect(page.locator("#generation")).toHaveText("1");
});

test("Restore Conway can cancel an in-flight evaluation", async ({ page }) => {
  await apply(page, "(loop [] (recur))");
  await expect(page.locator("#status")).toContainText("Compiling");
  await page.getByRole("button", { name: "Restore Conway", exact: true }).click();
  await expect(page.locator("#status")).toContainText("Conway’s rules restored");
  await page.getByRole("button", { name: "Step", exact: true }).click();
  await expect(page.locator("#population")).toHaveText("105");
});

test("a runaway generation stops and can recover on the same board", async ({ page }) => {
  await apply(page, "(let [calls (atom 0)] (fn [_ _] (if (> (swap! calls inc) 18) (loop [] (recur)) false)))");
  await expect(page.locator("#status")).toContainText("Rule applied");
  await page.getByRole("button", { name: "Step", exact: true }).click();
  await expect(page.locator("#status")).toContainText("took too long");
  await expect(page.locator("#generation")).toHaveText("0");
  await page.getByRole("button", { name: "Restore Conway", exact: true }).click();
  await expect(page.locator("#status")).toContainText("Conway’s rules restored");
  await page.getByRole("button", { name: "Step", exact: true }).click();
  await expect(page.locator("#population")).toHaveText("105");
});

test("the page fits a mobile viewport", async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  const fits = await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth);
  expect(fits).toBe(true);
  await expect(page.getByLabel("ClojureScript rule", { exact: true })).toBeVisible();
  await page.screenshot({ path: "test-results/mobile-preview.png", fullPage: true });
});
