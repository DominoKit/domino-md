# Current Status Summary

Status date: 2026-06-25

## Purpose

This document is a compact checkpoint for future sessions. It summarizes:

- what has already been implemented in `domino-md`
- what is currently verified and stable
- what is still deferred for later work
- what order the remaining work should follow

For detailed design notes, see:

- `v1-scope-list.md`
- `docs/deferred-engine-features.md`
- `docs/extensions.md`
- `gwt-compatibility-report.md`
- `commonmark-compliance-report.md`

## Current State

The repository now contains a browser-safe Markdown engine port based on the upstream
`commonmark-java` `commonmark` module, adapted to `org.dominokit.markdown`.

Implemented and verified today:

- AST port
- block parser core
- inline parser core
- HTML renderer
- CommonMark conformance harness
- GWT/browser compilation and browser-path tests
- Elemental2 DOM renderer
- built-in extension support
- plain-text renderer
- Markdown-to-Markdown renderer
- bundled extension discovery

## Completed Milestones

### Phase 1: Audit and plan

Completed:

- compatibility audit against upstream `commonmark-java`
- initial scope definition for the browser-safe port
- package and API mapping decisions

### Phase 2: AST foundation

Completed:

- `org.dominokit.markdown.node`
- source-span support
- visitor support
- link, list, code, HTML, and definition node types

### Phase 3: Block parser core

Completed:

- `Parser.parse(String)`
- block parser public APIs
- paragraphs
- ATX headings
- Setext headings
- thematic breaks
- block quotes
- bullet and ordered lists
- fenced and indented code blocks
- HTML blocks

### Phase 4: Inline parser core

Completed:

- inline parser extension APIs
- escapes
- entities
- code spans
- emphasis and strong emphasis
- inline links and images
- reference links
- hard and soft line breaks
- inline HTML
- link reference definition parsing and resolution

### Phase 5: HTML renderer

Completed:

- renderer base contracts
- `HtmlRenderer`
- HTML writer and renderer context
- attribute provider hooks
- node renderer override hooks
- URL sanitization hooks
- escaped raw HTML mode

### Phase 6: Conformance harness

Completed:

- checked-in CommonMark spec test data
- full HTML conformance harness
- strict green snapshot with `652 / 652` examples passing

### Phase 7: Browser compatibility

Completed:

- real GWT module setup for runtime and tests
- browser-path test coverage through `GWTTestCase`
- `gwt:test` wired into Maven `test`
- `gwt:compile` wired into Maven `verify`
- browser-safe replacements for JVM-only or regex-heavy runtime paths

### Elemental2 DOM renderer

Completed:

- `org.dominokit.markdown.renderer.elemental2`
- direct DOM rendering using Elemental2 nodes instead of HTML strings
- raw HTML handler override support
- attribute provider support
- custom node renderer support
- URL sanitization and soft-break configuration

### Engine extensions

Completed:

- built-in extension support for:
  - strikethrough
  - task list items
  - GFM tables
  - autolink
- extension wiring for parser, HTML renderer, Elemental2 renderer, text renderer, and Markdown
  renderer where applicable

### Plain-text renderer

Completed:

- `org.dominokit.markdown.renderer.text`
- upstream-shaped `TextContentRenderer` API
- line-break rendering modes
- extension support for strikethrough, task list items, and tables

### Markdown renderer

Completed:

- `org.dominokit.markdown.renderer.markdown`
- upstream-shaped `MarkdownRenderer` API
- canonical Markdown rendering from the AST
- semantic round-trip coverage against the checked-in CommonMark examples

### Bundled extension discovery

Completed:

- `org.dominokit.markdown.extensions.discovery.ExtensionDiscovery`
- `org.dominokit.markdown.extensions.discovery.StandardExtensionSets`
- bundled extension discovery for the built-in engine extensions
- deterministic extension ordering
- service registration metadata for future higher-level compile-time aggregation work

## What Is Verified

The following validation is currently in place:

- JVM unit and integration tests
- browser-path execution through `MarkdownSuite`
- `mvn test` runs JVM tests and GWT browser tests
- `mvn verify` runs the GWT compile path
- CommonMark HTML conformance currently passes `652 / 652`
- Markdown renderer semantic round-trip coverage runs across the checked-in CommonMark examples

## Important Current Boundaries

These are intentional and should remain true unless a later phase explicitly changes them:

- the parser remains `String`-input based
- `Parser.parseReader(Reader)` and other JVM I/O parser entry points are still absent
- explicit builder-based extension registration remains the core API
- browser-safe runtime code does not depend on `ServiceLoader`, `File`, `Path`, or DOM APIs in
  the parser core
- Elemental2 remains a renderer concern, not a parser concern
- bundled extension discovery exists, but application-wide browser aggregation is not yet solved in
  this artifact

## Remaining Engine Work

These are the remaining deferred engine-level items for future sessions:

### 1. JVM stream or file parsing helpers

Still pending.

Scope:

- add optional JVM-only helpers for parsing from:
  - `Reader`
  - `InputStream`
  - `Path`
  - `File`
- keep this outside the browser-safe runtime core
- avoid weakening the current `String`-based parser baseline

Recommended direction:

- implement this as a separate JVM-oriented helper layer or module
- keep `Parser` itself browser-safe

### 2. JPMS module descriptors

Still pending.

Scope:

- add `module-info.java` only after the artifact structure is considered stable enough
- treat this as a packaging concern, not a parser or renderer feature

Recommended direction:

- do this after the JVM helper work is settled

### 3. Optional future application-level extension aggregation

Still deferred.

This is separate from bundled extension discovery.

Current state:

- `ExtensionDiscovery.load()` returns the built-in engine extension set
- the repository now includes service registration metadata and discoverable-friendly constructors
- this is enough for bundled engine discovery inside `domino-md`

Still not implemented:

- a higher-level browser/application build that uses `domino-auto` to aggregate extension
  registrations contributed outside this artifact

Reason:

- a loader generated inside `domino-md` cannot automatically see extensions introduced later by a
  consuming application build

## Deferred Product Work

These items are not engine work and are still deferred to `domino-ui` or later product layers:

- Elemental2 editor UI
- Domino UI wrapper
- split-view preview shell
- toolbar commands
- browser demo app

## Recommended Next Order

For future sessions, the recommended order is:

1. JVM stream or file parsing helpers
2. JPMS module descriptors
3. optional application-level browser extension aggregation, only if a real consuming build needs
   it

## Useful Starting Points For The Next Session

If the next session continues engine work, start with:

- `docs/deferred-engine-features.md`
- `v1-scope-list.md`
- `docs/current-status-summary.md`

If the next session continues UI work in another repository, start from the deferred product list
instead of modifying the engine core.
