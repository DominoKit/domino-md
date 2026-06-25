# V1 Scope List

Status date: 2026-06-25

## Upstream Baseline

V1 is based on:
- upstream repository `commonmark-java`
- upstream module `commonmark`
- upstream commit `9477a93b6b0965efc54c55bd40ad88fbbe25bc6f`

## V1 Port Goal

Deliver a browser-safe Markdown engine foundation that:
- parses Markdown from `String`
- produces a reusable AST
- renders AST to HTML
- keeps the codebase independent from Elemental2 and browser DOM APIs

## In Scope

### Packages

- `org.commonmark` core package mapped to `org.dominokit.markdown`
- `node`
- `parser`
- `parser.block`
- `parser.delimiter`
- `parser.beta`
- `renderer`
- `renderer.html`
- `text`
- required `internal*` packages that support the parser and HTML renderer

### Features

- paragraphs
- ATX headings
- Setext headings
- emphasis
- strong emphasis
- code spans
- links
- images
- block quotes
- ordered and unordered lists
- fenced code blocks
- indented code blocks
- thematic breaks
- link reference definitions
- raw HTML nodes in the AST
- HTML rendering
- escaped raw HTML rendering mode
- URL sanitization hook
- explicit builder-based extension registration

### First-port implementation edits

- move package root to `org.dominokit.markdown`
- remove `Reader` parsing entry points
- remove runtime classpath resource loading
- replace the backslash escape regex with a manual character predicate

## Out Of Scope

### Upstream modules

- all `commonmark-ext-*` modules
- `commonmark-android-test`
- `commonmark-integration-test`
- `commonmark-test-util`

### Core features deferred

- Markdown-to-Markdown rendering
- plain-text rendering
- automatic extension discovery
- JVM stream or file parsing helpers
- JPMS module descriptors

### Product features deferred

- Elemental2 editor UI
- Domino UI wrapper
- split-view preview shell
- toolbar commands
- browser demo app

## Acceptance Criteria For Completing V1 Core Port Start

Phase 2 and onward should treat this scope as the definition of the first code slice:
- AST types compile under the new package root
- parser accepts `String` input only
- HTML renderer compiles with explicit URL sanitization configuration
- no Elemental2 dependency exists in the parser/renderer code
- no classpath resource loading remains in the ported core

## Phase 2 Status

Completed on 2026-06-24:
- `org.dominokit.markdown.node` now exists with the base tree model, source-span types, visitor support, list/link/code/html node types, and reference-definition support
- focused JVM tests now cover node relinking, visitor mutation safety, source-span slicing and merging, fence-length validation, and definition-label normalization
- `org.dominokit.markdown.internal.util.Escaping` currently contains only the minimal label-normalization helper required by `DefinitionMap`

Decision:
- keep the AST layer buildable and testable before porting the parser internals
- expand `internal.util.Escaping` to the full upstream-compatible utility only when the parser and renderer layers are ported

## Phase 3 Status

Completed on 2026-06-24:
- `org.dominokit.markdown.parser` and `org.dominokit.markdown.parser.block` now exist with the public string-based parser API, block parser contracts, source-line abstractions, builder hooks, and post-processing hooks
- `org.dominokit.markdown.internal.DocumentParser` and the core block parsers now support paragraphs, ATX headings, Setext headings, thematic breaks, block quotes, bullet and ordered lists, fenced code blocks, indented code blocks, and HTML blocks
- `org.dominokit.markdown.text.Characters` and the parser-side `internal.util.Parsing` helper are now ported to support line walking, indentation, and block-start detection
- focused JVM tests now cover source-line slicing, source-line aggregation, block parsing outcomes, source-span propagation, builder block-type filtering, and custom inline parser factory wiring

Decisions:
- keep `Parser` string-only for V1 and continue to omit upstream `Reader` parsing entry points
- use a temporary plain-text inline parser that emits `Text` and `SoftLineBreak` nodes so block parsing is usable before full inline syntax support lands
- defer delimiter processing, `parser.beta` scanner utilities, and link reference definition parsing to the next parser phase instead of widening this task
- keep `internal.util.Escaping.unescapeString` minimal for now, only covering the fenced-code info-string behavior required by the current block parser slice

## Phase 4 Status

Completed on 2026-06-24:
- `org.dominokit.markdown.parser.beta` and `org.dominokit.markdown.parser.delimiter` now exist with the scanner, custom inline parser, link processor, and delimiter extension APIs needed by the inline parser
- `org.dominokit.markdown.internal.InlineParserImpl` now ports the real inline parsing pipeline for escapes, entities, code spans, emphasis, strong emphasis, inline links, reference links, images, hard and soft line breaks, and inline HTML
- `org.dominokit.markdown.internal.LinkReferenceDefinitionParser` and `Definitions` are now wired back through `ParagraphParser`, `DocumentParser`, and `InlineParserContext` so reference definitions are collected during block parsing and resolved during inline parsing
- parser-side utility support now includes `LinkScanner`, `AsciiMatcher`, `CharMatcher`, and a generated `Html5Entities` table that avoids runtime classpath resource loading while preserving named-entity decoding
- focused JVM tests now cover link reference definition parsing, scanner traversal and source extraction, inline syntax AST output, custom inline content parsers, custom delimiter processors, link markers, and inline context definition lookups

Decisions:
- keep `Parser` string-only for V1 and continue to omit upstream `Reader` parsing entry points even though the upstream parser supports them
- replace the temporary plain-text inline parser entirely in this phase instead of layering additional behavior onto it
- preserve the no-classpath-loading rule by embedding the upstream HTML5 entity data in generated Java source rather than loading `entities.txt` at runtime
- leave HTML rendering, raw HTML output policy, and URL sanitization for the next phase now that the parser-side AST is feature-complete for the V1 inline syntax set

## Phase 5 Status

Completed on 2026-06-25:
- `org.dominokit.markdown.renderer` and `org.dominokit.markdown.renderer.html` now exist with the public renderer contracts, HTML renderer builder, HTML writer, attribute-provider hooks, node-renderer override hooks, and URL sanitization interfaces
- `org.dominokit.markdown.renderer.html.CoreHtmlNodeRenderer` now renders the V1 AST surface for documents, headings, paragraphs, block quotes, ordered and unordered lists, code blocks, thematic breaks, links, images, emphasis, strong emphasis, text, code spans, inline HTML, block HTML, and hard and soft line breaks
- `org.dominokit.markdown.internal.renderer.NodeRendererMap` now ports the upstream first-registered-wins node-dispatch behavior and renderer lifecycle callbacks used by the HTML renderer context
- focused JVM tests now cover raw-versus-escaped HTML rendering, attribute escaping, default and custom URL sanitization, percent-encoded URL output, attribute provider mutation, node renderer overrides, image alt-text extraction, single-paragraph omission, softbreak customization, and builder-based extension wiring

Decisions:
- keep the renderer slice HTML-only for V1 and continue to defer upstream Markdown and plain-text renderers
- preserve the parser and renderer boundary by keeping the renderer string-based and free of Elemental2, DOM, and sanitizer-library dependencies
- reuse the upstream renderer extension points closely so later custom nodes and extension modules can hook into rendering without reworking the public API
