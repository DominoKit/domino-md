# V1 Scope List

Status date: 2026-06-24

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
