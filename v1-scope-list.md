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
