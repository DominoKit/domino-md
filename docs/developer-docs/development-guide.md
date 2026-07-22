We woi# Development Guide

This page is the practical guide for changing the codebase.

## Tooling

The project is built with Maven and targets Java 11.

Important build inputs from `pom.xml`:

- `maven.compiler.source` and `maven.compiler.target` are both `11`
- GWT support is wired into `test` and `verify`
- formatting is enforced with `spotless-maven-plugin`
- license headers are checked with `license-maven-plugin`

## Common Commands

```bash
mvn test -q
mvn verify -q
mvn spotless:check -q
```

Use `test` for the normal JVM + browser-path test flow. Use `verify` when you want to include the
GWT compile step as well.
Use `spotless:check` when you want to validate Java formatting without running the full test suite.

## Test Layout

Tests are split across standard JUnit tests and GWT browser-path coverage.

Notable test areas:

- AST and node behavior under `src/test/java/org/dominokit/markdown/node`
- parser behavior under `src/test/java/org/dominokit/markdown/parser`
- renderer behavior under `src/test/java/org/dominokit/markdown/renderer`
- extension behavior under `src/test/java/org/dominokit/markdown/ext`
- browser-path coverage under `src/test/java/org/dominokit/markdown/gwt`
- CommonMark conformance under `src/test/java/org/dominokit/markdown/conformance`

The checked-in CommonMark spec fixture is stored in:

- `src/test/resources/spec.txt`

The browser suite is driven through:

- `src/test/java/org/dominokit/markdown/gwt/MarkdownSuite.java`

## Change Flow

When adding or changing behavior, follow this order:

1. Update the parser or renderer contract if the change affects public behavior.
2. Update the relevant internal implementation.
3. Add or adjust tests.
4. Run `mvn test -q`.
5. Run `mvn verify -q` when the change affects browser behavior or renderer compatibility.

## Entry Points

### Parsing

Primary entry point:

- `Parser.builder()`
- `Parser.parse(String)`

Use parser builders to register:

- extensions
- enabled block types
- custom block parser factories
- custom inline content parser factories
- custom delimiter processors
- custom link processors
- custom link markers
- source-span mode
- max open block parsers

### HTML rendering

Primary entry point:

- `HtmlRenderer.builder()`
- `HtmlRenderer.render(Node, Appendable)`
- `HtmlRenderer.render(Node)`

### Elemental2 rendering

Primary entry point:

- `Elemental2Renderer.builder()`
- `Elemental2Renderer.render(Node)`

### Plain-text rendering

Primary entry point:

- `TextContentRenderer.builder()`
- `TextContentRenderer.render(Node, Appendable)`
- `TextContentRenderer.render(Node)`

### Markdown rendering

Primary entry point:

- `MarkdownRenderer.builder()`
- `MarkdownRenderer.render(Node, Appendable)`
- `MarkdownRenderer.render(Node)`

### Bundled extensions

Use:

- `ExtensionDiscovery.load()`

when you want the built-in extension set in deterministic order.

## Coding Rules

### Keep the parser browser-safe

Do not add JVM-only APIs to the core parser path unless they are isolated behind a separate helper
layer that does not affect browser builds.

### Preserve the public surface

This repository intentionally mirrors the upstream `commonmark-java` shape in many places. Prefer
changes that keep that mental model intact unless there is a strong reason to diverge.

### Separate API from implementation

Public classes live in the outer packages. Implementation details belong in `internal` packages.

### Prefer explicit configuration

Builders are the main configuration mechanism. Avoid hidden global configuration and mutable static
state.

### Keep renderers modular

Each renderer family has its own context, factory, and core node renderer. Put format-specific
behavior in the matching package instead of leaking it into the parser.

### Use scanners for syntax-sensitive code

The engine uses character scanners and small matchers instead of regex-heavy logic in the hot parse
paths. Follow that pattern when adding syntax-sensitive code.

### Keep helper classes small

If a method needs a lot of state, prefer a dedicated helper, visitor, or nested class instead of a
large ad hoc block.

## Implementation Highlights

- The parser is a two-stage system: block parsing first, then inline resolution.
- Renderers work from the AST, not from the original source text.
- Source spans are optional and should not distort the core parsing model.
- Extensions may participate in parsing, post-processing, and rendering.
- The project already includes strikethrough, task lists, tables, and autolink support.
- The Markdown renderer is meant for canonical round-tripping, not byte-for-byte source
  preservation.

## What To Check Before Submitting Changes

- Are new public APIs documented?
- Are tests added or updated?
- Do the changes preserve browser compatibility?
- Do the changes respect the package boundaries?
- Do the renderers still produce stable output for the existing spec coverage?
