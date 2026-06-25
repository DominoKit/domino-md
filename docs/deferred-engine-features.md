# Deferred Engine Features

Status date: 2026-06-25

## Purpose

This document explains the remaining non-UI engine features that are still deferred after the core
parser, HTML renderer, Elemental2 DOM renderer, GWT compatibility work, and extension phase.

These features are:

- Markdown-to-Markdown rendering
- plain-text rendering
- automatic extension discovery
- JVM stream or file parsing helpers
- JPMS module descriptors

The goal here is not only to list them, but to describe:

- what each feature does
- why it is useful
- where it should live in the codebase
- how it should work at runtime
- how it should be implemented without weakening the browser-safe core

## Current Constraints

These deferred features must respect the current architecture:

- the parser core remains the single source of truth for Markdown parsing
- `Parser.parse(String)` remains the baseline parse path
- explicit builder registration remains the default extension model
- browser-safe runtime code must not depend on `java.io.File`, `java.nio.file`,
  `java.util.ServiceLoader`, or other JVM-only APIs
- compile-time code generation is acceptable when the generated runtime output stays plain Java and
  browser-safe
- Elemental2 remains a renderer concern, not a parser concern

That means not every deferred feature should be added directly into the existing core package
surface. Some are better implemented as optional JVM-only layers.

## Recommended Order

Recommended implementation order:

1. plain-text rendering
2. Markdown-to-Markdown rendering
3. automatic extension discovery
4. JVM stream or file parsing helpers
5. JPMS module descriptors

Rationale:

- the text renderer is small, self-contained, and browser-safe
- the Markdown renderer is larger but still pure renderer work over the existing AST
- automatic discovery can be added with compile-time generated loaders for browser builds while
  keeping the explicit extension model stable
- JVM parsing helpers are useful, but they should stay outside the browser-safe core and can wait
  until after the renderer and discovery work
- JPMS should be last because it is sensitive to the final artifact and package layout

## Feature Summary

| Feature | Recommended package or module | Browser-safe | Main output |
| --- | --- | --- | --- |
| Markdown-to-Markdown rendering | `org.dominokit.markdown.renderer.markdown` | yes, after small compatibility fixes | canonical Markdown text |
| plain-text rendering | `org.dominokit.markdown.renderer.text` | yes | readable plain text |
| automatic extension discovery | optional discovery support layer with generated browser loader and optional JVM bridge later | yes, for the generated-loader path | discovered `Extension` instances |
| JVM stream or file parsing helpers | separate JVM-only parser helper module | no | `Node` from `Reader`, `InputStream`, `Path`, or `File` |
| JPMS module descriptors | `module-info.java` per final artifact | JVM-only concern | explicit Java modules |

## Markdown-to-Markdown Rendering

### What it is

A renderer that takes an AST and emits Markdown text instead of HTML.

It is not expected to preserve the original source byte-for-byte. It should produce Markdown that
is semantically equivalent when parsed again.

### Why it is useful

- round-trip parser tests
- canonicalization or normalization of Markdown
- copy-as-Markdown features in higher-level tools
- exporting edited AST content back to Markdown
- extension interoperability tests

### Recommended package

```text
org.dominokit.markdown.renderer.markdown
```

### Recommended public API

Stay close to upstream `commonmark-java`:

```java
public final class MarkdownRenderer implements Renderer {
    public static Builder builder();

    public static final class Builder {
        public Builder nodeRendererFactory(MarkdownNodeRendererFactory factory);
        public Builder extensions(Iterable<? extends Extension> extensions);
        public MarkdownRenderer build();
    }

    public interface MarkdownRendererExtension extends Extension {
        void extend(Builder rendererBuilder);
    }
}
```

Supporting public types:

```java
MarkdownWriter
MarkdownNodeRendererContext
MarkdownNodeRendererFactory
```

### How it works

The renderer walks the AST and emits a canonical Markdown form.

Key behaviors:

- headings default to ATX when possible
- multi-line level 1 and 2 headings may use Setext output
- links are rendered as inline links
- fenced code blocks choose a fence length that is safe for the literal content
- list items use stored marker metadata where available
- text is escaped conservatively so reparsing yields the same AST

The renderer needs context stacks for:

- nested list prefixes
- block boundaries
- current writer prefix and indentation
- extra escape characters requested by extension renderers

That last point is important. The Markdown renderer is different from the HTML and Elemental2
renderers because extension renderers may need extra characters escaped in plain text. The upstream
API already models this:

```java
public interface MarkdownNodeRendererFactory {
    NodeRenderer create(MarkdownNodeRendererContext context);
    Set<Character> getSpecialCharacters();
}
```

That should be kept in this fork because it is the cleanest way to let extension renderers declare
their syntax needs without hard-coding them into the core renderer.

### Implementation approach

Port the upstream package shape closely:

- `MarkdownRenderer`
- `MarkdownWriter`
- `MarkdownNodeRendererContext`
- `MarkdownNodeRendererFactory`
- `CoreMarkdownNodeRenderer`

Expected compatibility edits:

- upstream `CoreMarkdownNodeRenderer` uses regex in a few places
- this fork should replace those runtime regex paths with manual scanners where doing so avoids
  GWT regressions, following the same approach already used in the parser compatibility fixes

### Important limitation

This renderer should not promise source preservation.

Examples of expected normalization:

- a parsed reference link may render back as an inline link
- original spacing and escaping style may change
- ATX and Setext heading style may change

The contract is AST equivalence after reparse, not source equivalence.

### Tests

Add:

- direct renderer fixture tests for all core node types
- AST round-trip tests: `parse -> render markdown -> parse again -> compare structure`
- extension round-trip tests for strikethrough, tables, task list items, and autolinks
- GWT/browser rendering tests if the package is kept browser-safe

## Plain-Text Rendering

### What it is

A renderer that extracts readable text content from the AST without Markdown or HTML markup.

This feature maps closely to upstream `TextContentRenderer`.

### Why it is useful

- search indexing
- text previews
- accessibility-oriented fallbacks
- copy-as-text features
- debugging and snapshot testing

### Recommended package

```text
org.dominokit.markdown.renderer.text
```

### Recommended public API

Stay close to upstream:

```java
public final class TextContentRenderer implements Renderer {
    public static Builder builder();

    public static final class Builder {
        public Builder lineBreakRendering(LineBreakRendering mode);
        public Builder nodeRendererFactory(TextContentNodeRendererFactory factory);
        public Builder extensions(Iterable<? extends Extension> extensions);
        public TextContentRenderer build();
    }

    public interface TextContentRendererExtension extends Extension {
        void extend(Builder rendererBuilder);
    }
}
```

Supporting public types:

```java
TextContentWriter
TextContentNodeRendererContext
TextContentNodeRendererFactory
LineBreakRendering
```

### How it works

The renderer walks the AST and writes readable text with minimal formatting hints.

Examples:

- emphasis and strong emphasis usually contribute just their child text
- code spans may be wrapped in quotes to preserve readability
- links and images may include visible child text and optionally title or destination context
- lists preserve item separation
- block boundaries become spaces, line breaks, or blank lines depending on configuration

The line break policy should stay compatible with upstream:

- `STRIP`: flatten everything into a single line
- `COMPACT`: use single line breaks between blocks
- `SEPARATE_BLOCKS`: preserve clearer block separation

### Implementation approach

This is the easiest remaining renderer to port because:

- it is pure Java
- it only depends on the AST
- it already fits the existing `NodeRendererMap` pattern

Implementation should closely follow upstream:

- `TextContentRenderer`
- `TextContentWriter`
- `CoreTextContentNodeRenderer`

### Tests

Add:

- fixture tests for all line break modes
- list formatting tests
- block quote and code block tests
- extension tests for tables and task list items
- browser tests if the renderer is kept in the transpiled surface

## Automatic Extension Discovery

### What it is

An optional convenience feature that finds extension implementations automatically instead of
requiring every consumer to register them manually in code.

### Why it is useful

- server-side applications that want convention-based setup
- browser applications that want a prebuilt standard extension bundle
- CLI tools that load feature packs from the classpath
- test harnesses that want a default extension bundle

### Why it is still deferred

The project intentionally removed runtime `ServiceLoader` behavior from the browser-safe path.

That decision should stay intact. What changed is that browser-safe discovery is still feasible if
it is done at compile time and the generated runtime code is plain Java.

### Recommended design

Do not add hidden auto-discovery into `Parser`, `HtmlRenderer`, or `Elemental2Renderer` directly.

Recommended approach:

- keep explicit `extensions(...)` registration as the core API
- add an optional discovery support layer, for example:

```text
org.dominokit.markdown.extensions.discovery
```

- keep browser and JVM discovery concerns behind that layer instead of in the core builders
- use `domino-auto` for the browser-compatible implementation
- add a JVM `ServiceLoader` bridge only later if it is still needed

### Recommended public API

Prefer a helper utility instead of hidden builder magic:

```java
public final class ExtensionDiscovery {
    public static List<Extension> load();
}
```

Optional later JVM-only overload:

```java
public final class ExtensionDiscovery {
    public static List<Extension> load(ClassLoader classLoader);
}
```

Optional convenience layer:

```java
public final class StandardExtensionSets {
    public static List<Extension> coreGfm();
}
```

### How it works

Browser-compatible path:

1. Keep `org.dominokit.markdown.Extension` as the service interface.
2. Register implementations in `META-INF/services/org.dominokit.markdown.Extension`.
3. Add `domino-auto-api` and the `domino-auto-processor` annotation processor to the build that
   owns discovery.
4. Configure `domino-auto` to include `org.dominokit.markdown`, either with processor arguments or
   with `@DominoAuto(include = "org.dominokit.markdown")`.
5. Let the processor generate `org.dominokit.markdown.Extension_ServiceLoader`.
6. Have `ExtensionDiscovery.load()` delegate to `Extension_ServiceLoader.load()`.
7. Pass the resulting collection into the existing builder `extensions(...)` methods for
   `Parser`, `HtmlRenderer`, and `Elemental2Renderer`.

Example shape:

```java
List<Extension> extensions = ExtensionDiscovery.load();

Parser parser = Parser.builder().extensions(extensions).build();
HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();
Elemental2Renderer domRenderer = Elemental2Renderer.builder().extensions(extensions).build();
```

Optional later JVM bridge:

- isolate it from browser builds
- use `ServiceLoader.load(Extension.class, classLoader)`
- normalize the discovered list through the same ordering policy used by the generated-loader path

### Ordering and determinism

Discovery order should not be left implicit.

`domino-auto` currently builds the generated loader from discovered service entries, and that path
should be treated as unordered unless the discovery layer normalizes it.

Recommended policy:

- introduce an optional ordering contract such as `OrderedExtension`
- sort discovered extensions before handing them to builders
- if no explicit order contract exists, fall back to a stable secondary key such as class name

### Important boundary

This feature should not reintroduce runtime classpath scanning or reflection into browser builds.

That means:

- the core builders stay explicit and side-effect free
- browser discovery uses generated code, not runtime scanning
- any later JVM `ServiceLoader` bridge stays isolated from the browser-safe runtime surface

### Tests

Add:

- a compile-time smoke test that proves `Extension_ServiceLoader` is generated
- JVM and browser tests that call `ExtensionDiscovery.load()` and wire the result through
  `Parser`, `HtmlRenderer`, and `Elemental2Renderer`
- ordering tests that prove the discovery layer returns a deterministic extension order
- integration tests for missing or duplicate registrations if the helper layer validates them

## JVM Stream Or File Parsing Helpers

### What it is

Convenience APIs for parsing Markdown from `Reader`, `InputStream`, `Path`, or `File`.

### Why it is useful

- server-side applications
- command-line tools
- tests that read fixture files directly
- migration from upstream `Parser.parseReader(Reader)`

### Why it should not go back into the core parser

This fork intentionally made `Parser` string-only to keep the browser-safe core free of `java.io`
and `java.nio`.

Reintroducing those APIs directly onto `Parser` would weaken that boundary again.

### Recommended design

Add a separate JVM-only helper package or artifact, for example:

```text
org.dominokit.markdown.parser.io
```

Recommended utility:

```java
public final class ParserIo {
    public static Node parse(Parser parser, Reader reader) throws IOException;
    public static Node parse(Parser parser, InputStream input, Charset charset) throws IOException;
    public static Node parse(Parser parser, Path path, Charset charset) throws IOException;
    public static Node parse(Parser parser, File file, Charset charset) throws IOException;
}
```

### How it works

The first implementation should be simple:

- read the entire input into a `String`
- delegate to `Parser.parse(String)`

This is the recommended design for this fork because:

- it preserves a single parser code path
- it avoids reintroducing a second parsing entry point with separate behavior
- it keeps browser-safe parser internals untouched

If later needed, an internal `LineReader` helper can be restored in the JVM-only package, but that
should be an implementation detail, not a reason to widen the core parser API.

### Compatibility note

This fork should prefer helper utilities over restoring upstream `Parser.parseReader(Reader)`.

If upstream API compatibility becomes more important than keeping `Parser` clean, that can be
revisited later, but it is not the recommended default.

### Tests

Add JVM-only tests for:

- UTF-8 and custom charset inputs
- `\n`, `\r`, and `\r\n` line endings
- empty files and trailing newlines
- behavior equivalence between `ParserIo.parse(...)` and `Parser.parse(String)`

## JPMS Module Descriptors

### What it is

Explicit `module-info.java` descriptors for Java Platform Module System users.

### Why it is useful

- strong encapsulation for JVM consumers
- better module-path support
- explicit exported package surface
- better IDE and build-tool metadata for modular applications

### Why it is deferred

JPMS is sensitive to the final artifact layout.

This project currently ships a single artifact that includes:

- parser core
- HTML renderer
- Elemental2 DOM renderer
- optional extension packages

Adding a descriptor now is possible, but it would lock in module boundaries that may not be ideal.

### Recommended design

There are two viable options.

#### Option A: single module descriptor for the current artifact

Add one `module-info.java` that exports the public packages in the current jar.

Pros:

- fastest path
- minimal structural change

Cons:

- the module would likely need to depend on Elemental2 because `renderer.elemental2` is in the same
  artifact
- optional features and browser-oriented packages would be part of one wide module surface

#### Option B: split artifacts first, then add descriptors

This is the recommended long-term approach.

Example split:

```text
domino-md-core
domino-md-html
domino-md-text
domino-md-markdown
domino-md-elemental2
domino-md-ext-*
domino-md-jvm-support
```

Then each artifact gets its own `module-info.java`.

Pros:

- cleaner module graph
- Elemental2 dependency stays out of pure JVM core modules
- optional renderers and extensions stay optional at module resolution time

Cons:

- requires artifact restructuring first

### How it works

Once the artifact boundaries are final:

- add `module-info.java`
- export only public API packages
- keep `internal` packages unexported
- if automatic extension discovery exists in a separate module, use `uses` and `provides`
  declarations there instead of in the core parser module

### Tests

Add:

- a modular compile smoke test
- a sample module-path consumer build
- checks that public packages are exported and `internal` packages stay hidden

## Cross-Cutting Decisions

### Keep explicit registration as the default

Even if automatic discovery is added later, explicit builder registration should remain the primary
and documented path.

### Keep browser-safe and JVM-only features separated

The deferred backlog naturally splits into:

- browser-safe renderers: Markdown renderer and text renderer
- browser-compatible generated discovery: optional extension loading via `domino-auto`
- JVM-only helpers: parser I/O helpers and JPMS support

That split should be preserved in package layout and build wiring.

### Prefer upstream-compatible public names

Where upstream already has a clean and proven API, keep the same shape:

- `MarkdownRenderer`
- `TextContentRenderer`
- `LineBreakRendering`
- `MarkdownNodeRendererFactory`

This reduces semantic drift and makes future upstream comparison easier.

## Suggested Milestones

### Milestone A

- port `renderer.text`
- add tests
- keep browser compatibility green

### Milestone B

- port `renderer.markdown`
- add AST round-trip tests
- add extension-specific markdown rendering tests

### Milestone C

- add optional generated extension discovery using `domino-auto`
- keep explicit registration as the documented primary path

### Milestone D

- add JVM-only `ParserIo`
- document it as the migration path for `Reader` or file-based parsing

### Milestone E

- decide whether artifact split is needed before JPMS
- add `module-info.java` only after that decision is stable

## Final Recommendation

The next deferred engine implementation should start with renderers, not JVM helpers.

Specifically:

1. implement `renderer.text`
2. implement `renderer.markdown`
3. add the JVM-only helper layer

That sequence keeps the engine moving forward without reopening the parser architecture or
introducing JVM-only dependencies into the core too early.
