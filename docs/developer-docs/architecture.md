# Architecture

This repository is organized around a simple pipeline:

1. Parse Markdown text into an AST.
2. Apply parser post-processing and extension-specific AST transforms.
3. Render the AST to a target representation.

That target can be:

- HTML text
- Elemental2 DOM nodes
- plain text
- canonical Markdown

## Core Model

The AST lives in `org.dominokit.markdown.node`.

Important node families:

- structural blocks such as `Document`, `Paragraph`, `Heading`, `ListBlock`, and `BlockQuote`
- inline nodes such as `Text`, `Code`, `Emphasis`, `StrongEmphasis`, `Link`, and `Image`
- special nodes such as `HtmlBlock`, `HtmlInline`, `SoftLineBreak`, and `HardLineBreak`
- extension nodes such as table nodes, task-list markers, and strikethrough nodes

The node hierarchy is mutable and linked as a tree. Most renderers walk children by following the
node links instead of copying the tree.

## Parsing Flow

The main parse path is:

`Parser.parse(String)` -> `DocumentParser` -> block parsing -> inline parsing -> post-processing

### Block parsing

`org.dominokit.markdown.internal.DocumentParser` coordinates the block parser stack.

It is responsible for:

- scanning input line by line
- opening and closing block parsers
- handling indentation, blank lines, and container blocks
- collecting source spans when requested
- delegating to block parser implementations for paragraphs, headings, lists, code blocks, and
  HTML blocks

Block parser implementations live under `org.dominokit.markdown.internal` and
`org.dominokit.markdown.parser.block`.

### Inline parsing

`org.dominokit.markdown.internal.InlineParserImpl` handles inline syntax after block structure has
been established.

It resolves:

- emphasis and strong emphasis
- inline links and images
- reference links
- code spans
- entities
- escapes
- inline HTML
- custom inline content parsers
- custom delimiter processors
- custom link processors

This is the most stateful part of the engine. It uses delimiter and bracket stacks to resolve
markup after enough context is available.

### Post-processing

The parser can run `PostProcessor` implementations after AST construction.

Current uses include extension-specific rewrites such as autolink conversion and task-list item
normalization.

## Rendering Architecture

Rendering is AST-driven. The public renderer entry points build a renderer-specific context and a
`NodeRendererMap`, then dispatch to node renderers based on node type.

The main rendering targets are:

- `renderer.html`
- `renderer.elemental2`
- `renderer.text`
- `renderer.markdown`

Each target follows the same basic pattern:

- a builder captures configuration
- the renderer creates a per-render-pass context
- built-in node renderers are registered
- extension renderers are added before the fallback core renderer
- traversal walks the AST and renders each node in document order

## Extension Model

Extensions are explicit.

An extension can contribute one or more of the following:

- parser block factories
- inline content parsers
- delimiter processors
- link processors
- post-processors
- renderer node factories

This is why the project keeps separate extension hooks for parsing and for each renderer family.

## Browser-Safe Design

The engine is intentionally browser-safe in its core:

- parser code avoids JVM-only I/O classes
- runtime helpers avoid `ServiceLoader` in the parser core
- browser execution is validated through GWT tests
- Elemental2 support is isolated to the DOM renderer package

When a feature only makes sense on the JVM, it should remain outside the core parser and renderer
surface unless the browser-safe path is still preserved.

## Conceptual Flow Diagram

```text
Markdown string
  -> Parser
  -> block tree
  -> inline resolution
  -> post-processors
  -> AST
  -> renderer
  -> HTML / DOM / text / Markdown
```

## Important Design Concepts

- AST first: the parser should build a useful tree before any rendering decisions are made.
- Explicit extension registration: no hidden global plugins in the runtime core.
- Renderer specialization: each output format has its own renderer package and context types.
- Separation of concerns: parser logic and renderer logic should not bleed into one another.
- Source spans: origin tracking is optional, but when enabled it should preserve line and column
  information through parsing and slicing.

