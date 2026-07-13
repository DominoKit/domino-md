# Developer Docs

This folder is the working guide for contributors who needs to modify `domino-md`
directly.

The goal is practical orientation:

- How the engine is structured
- Where the parser, AST, renderers, and extensions live
- How a parse/render request flows through the codebase
- How to build, test, and verify changes
- What conventions the project follows

## Start Here

1. [Architecture](architecture.md)
2. [Package Map](package-map.md)
3. [Development Guide](development-guide.md)

## What This Repository Is

`domino-md` is a browser-safe Markdown engine for Java and GWT-based applications. It provides:

- parsing from Markdown text into an AST
- HTML rendering
- Elemental2 DOM rendering
- plain-text rendering
- Markdown-to-Markdown rendering
- explicit extension hooks for parser and renderer behavior

The engine is intentionally structured around small, composable public APIs and a large internal
implementation surface.

## Key Entry Points

- `org.dominokit.markdown.parser.Parser`
- `org.dominokit.markdown.renderer.html.HtmlRenderer`
- `org.dominokit.markdown.renderer.elemental2.Elemental2Renderer`
- `org.dominokit.markdown.renderer.text.TextContentRenderer`
- `org.dominokit.markdown.renderer.markdown.MarkdownRenderer`
- `org.dominokit.markdown.extensions.discovery.ExtensionDiscovery`

## Related Documents

- [`docs/current-status-summary.md`](../current-status-summary.md)
- [`docs/extensions.md`](../extensions.md)
- [`docs/deferred-engine-features.md`](../deferred-engine-features.md)

## Repository Rules of Thumb

- Treat `parser`, `renderer`, `node`, and `ext` packages as the public surface.
- Treat `internal` packages as implementation details unless a class is clearly designed as a
  reusable helper.
- Prefer explicit builder configuration over hidden global state.
- Keep browser-safe code free of JVM-only dependencies.
- Preserve the Apache 2.0 header on new Java files.

