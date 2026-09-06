import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

const stylesheet = readFileSync(resolve(__dirname, "styles.css"), "utf8");

describe("dashboard layout styles", () => {
  it("allows activity result sections to shrink inside the dashboard panel", () => {
    expect(stylesheet).toContain(".activity-results > *");
    expect(stylesheet).toContain("min-width: 0;");
    expect(stylesheet).toContain("max-width: 100%;");
  });
});
