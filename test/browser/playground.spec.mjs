import { test, expect } from "@playwright/test";

test.beforeEach(async ({ page }) => {
  await page.goto("./");
  await expect(page.getByRole("button", { name: "Play", exact: true })).toBeEnabled();
});

async function apply(page, source) {
  await page.getByLabel("ClojureScript rule", { exact: true }).fill(source);
  await page.getByRole("button", { name: "Apply rule", exact: true }).click();
}

async function clickCell(page, x, y) {
  const board = page.locator("#board");
  const bounds = await board.boundingBox();
  await board.click({ position: { x: 1 + (x + 0.5) * (bounds.width - 2) / 60,
    y: 1 + (y + 0.5) * (bounds.height - 2) / 60 } });
}

test("famous seeds load with explanations and reset restores a clicked cell", async ({ page }) => {
  for (const [pattern, population] of [["pulsar", 48], ["pentadecathlon", 12],
    ["lightweight-spaceship", 9], ["gosper-glider-gun", 36], ["acorn", 7], ["diehard", 7], ["blank", 0]]) {
    await page.getByRole("combobox", { name: "Pattern", exact: true }).selectOption(pattern);
    await expect(page.locator("#population")).toHaveText(String(population));
    await expect(page.locator("#pattern-description")).not.toBeEmpty();
    await expect(page.getByRole("button", { name: "Step", exact: true })).toBeEnabled();
  }
  await clickCell(page, 0, 0);
  await expect(page.locator("#population")).toHaveText("1");
  await expect(page.locator("#board")).toHaveAttribute("aria-label", /Selected cell 1, 1: alive/);
  await clickCell(page, 0, 0);
  await expect(page.locator("#population")).toHaveText("0");
  await clickCell(page, 59, 59);
  await expect(page.locator("#board")).toHaveAttribute("aria-label", /Selected cell 60, 60: alive/);
  await page.getByRole("button", { name: "Reset board", exact: true }).click();
  await expect(page.locator("#population")).toHaveText("0");
});

test("clicking during an in-flight generation preserves the edit and playback continues", async ({ page }) => {
  await page.getByRole("combobox", { name: "Pattern", exact: true }).selectOption("blank");
  // This is a real compiled rule: delay its first simulation call after the 18 validation calls.
  await apply(page, `(let [calls (atom 0)] (fn [alive? _]
    (when (= 19 (swap! calls inc))
      (let [until (+ (.now js/Date) 1000)]
        (loop [] (when (< (.now js/Date) until) (recur))))) alive?))`);
  await expect(page.locator("#status")).toContainText("Rule applied");
  await page.getByRole("button", { name: "Step", exact: true }).click();
  await expect(page.getByRole("button", { name: "Step", exact: true })).toBeDisabled();
  await clickCell(page, 20, 25);
  await expect(page.locator("#population")).toHaveText("1");
  await expect(page.getByRole("button", { name: "Step", exact: true })).toBeEnabled();
  await expect(page.locator("#generation")).toHaveText("0");
  await expect(page.locator("#population")).toHaveText("1");
  await page.getByRole("button", { name: "Play", exact: true }).click();
  await expect.poll(async () => Number(await page.locator("#generation").textContent())).toBeGreaterThan(1);
  await clickCell(page, 21, 25);
  await expect(page.getByRole("button", { name: "Pause", exact: true })).toBeVisible();
  await page.waitForTimeout(500);
  await expect(page.locator("#population")).toHaveText("2");
  await page.getByRole("button", { name: "Pause", exact: true }).click();
});

test("keyboard editing toggles cells without advancing time", async ({ page }) => {
  await page.getByRole("combobox", { name: "Pattern", exact: true }).selectOption("blank");
  await expect(page.getByRole("button", { name: "Step", exact: true })).toBeEnabled();
  const board = page.locator("#board");
  await board.focus();
  await board.press("Enter");
  await board.press("ArrowRight");
  await board.press("Space");
  await expect(page.locator("#population")).toHaveText("2");
  await board.press("ArrowLeft");
  await board.press("Space");
  await expect(page.locator("#population")).toHaveText("1");
  await expect(page.locator("#generation")).toHaveText("0");
});

test.describe("touch editing", () => {
  test.use({ hasTouch: true, viewport: { width: 390, height: 844 } });
  test("maps taps to cells on the scaled mobile board", async ({ page }) => {
    await page.getByRole("combobox", { name: "Pattern", exact: true }).selectOption("blank");
    await expect(page.getByRole("button", { name: "Step", exact: true })).toBeEnabled();
    const bounds = await page.locator("#board").boundingBox();
    await page.touchscreen.tap(bounds.x + 1 + 59.5 * (bounds.width - 2) / 60,
      bounds.y + 1 + 59.5 * (bounds.height - 2) / 60);
    await expect(page.locator("#population")).toHaveText("1");
    await expect(page.locator("#board")).toHaveAttribute("aria-label", /Selected cell 60, 60: alive/);
  });
});

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
  await page.getByRole("combobox", { name: "Pattern", exact: true }).selectOption("blinker");
  await expect(page.locator("#population")).toHaveText("3");
  await page.getByRole("combobox", { name: "Edges", exact: true }).selectOption("wrap");
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
