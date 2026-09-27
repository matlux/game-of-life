// Serve the built artifact under a project-site prefix, just like GitHub Pages.
import { createServer } from "node:http";
import { readFile } from "node:fs/promises";
import { resolve, sep, extname } from "node:path";

const root = resolve("target/site");
const types = { ".html": "text/html", ".css": "text/css", ".mjs": "text/javascript", ".js": "text/javascript", ".cljs": "text/plain" };
createServer(async (request, response) => {
  try {
    const url = new URL(request.url, "http://127.0.0.1:8766");
    if (!url.pathname.startsWith("/game-of-life/")) throw new Error("Not found");
    const path = resolve(root, decodeURIComponent(url.pathname.slice("/game-of-life/".length)) || "index.html");
    if (!path.startsWith(root + sep)) throw new Error("Not found");
    const body = await readFile(path);
    response.writeHead(200, { "Content-Type": types[extname(path)] || "application/octet-stream", "Cache-Control": "no-store" });
    response.end(body);
  } catch {
    response.writeHead(404);
    response.end("Not found");
  }
}).listen(8766, "127.0.0.1", () => console.log("Preview: http://127.0.0.1:8766/game-of-life/"));
