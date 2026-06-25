# Port Class Map

Audit baseline:
- Upstream repository: `commonmark-java`
- Upstream commit: `9477a93b6b0965efc54c55bd40ad88fbbe25bc6f`

## Mapping Rule

Default mapping rule:

```text
org.commonmark... -> org.dominokit.markdown...
```

The first port should preserve:
- class names
- subpackage names
- public API shape where browser-safe

The goal is to minimize semantic drift and make upstream comparison straightforward during the initial port.

## Module Scope

Port now:
- `commonmark`

Do not port yet:
- `commonmark-ext-*`
- `commonmark-android-test`
- `commonmark-integration-test`
- `commonmark-test-util`

## Package Map

| Upstream package | Target package | Status | Notes |
| --- | --- | --- | --- |
| `org.commonmark` | `org.dominokit.markdown` | Port | Keep `Extension` and root package docs. |
| `org.commonmark.node` | `org.dominokit.markdown.node` | Port | Core AST stays close to upstream. |
| `org.commonmark.parser` | `org.dominokit.markdown.parser` | Port with edits | Drop `parseReader(Reader)` from `Parser`. |
| `org.commonmark.parser.block` | `org.dominokit.markdown.parser.block` | Port | Needed for block parsing and extension hooks. |
| `org.commonmark.parser.delimiter` | `org.dominokit.markdown.parser.delimiter` | Port | Needed for emphasis processing and extension hooks. |
| `org.commonmark.parser.beta` | `org.dominokit.markdown.parser.beta` | Port | Keep package for initial compatibility even though it is experimental upstream. |
| `org.commonmark.renderer` | `org.dominokit.markdown.renderer` | Port | Keep base renderer contracts. |
| `org.commonmark.renderer.html` | `org.dominokit.markdown.renderer.html` | Port | Main rendering target for MVP. |
| `org.commonmark.renderer.markdown` | `org.dominokit.markdown.renderer.markdown` | Port | Browser-safe Markdown renderer now implemented. |
| `org.commonmark.renderer.text` | `org.dominokit.markdown.renderer.text` | Port | Browser-safe plain-text renderer now implemented. |
| n/a | `org.dominokit.markdown.extensions.discovery` | New | Browser-safe bundled extension discovery helpers. |
| `org.commonmark.text` | `org.dominokit.markdown.text` | Port | Used by parser internals. |
| `org.commonmark.internal` | `org.dominokit.markdown.internal` | Port selectively | Keep only parser and HTML support internals needed by MVP. |
| `org.commonmark.internal.inline` | `org.dominokit.markdown.internal.inline` | Port | Required by inline parser. |
| `org.commonmark.internal.renderer` | `org.dominokit.markdown.internal.renderer` | Port | Required by HTML renderer. |
| `org.commonmark.internal.util` | `org.dominokit.markdown.internal.util` | Port selectively | Replace resource loading and omit `LineReader`. |

## Public Type Surface To Port First

### Root package

- `Extension`

### AST

Port all core node types in `org.commonmark.node`, including:
- `Node`
- `Block`
- `Document`
- `Paragraph`
- `Text`
- `Heading`
- `Emphasis`
- `StrongEmphasis`
- `Code`
- `Link`
- `Image`
- `SoftLineBreak`
- `HardLineBreak`
- `BlockQuote`
- `BulletList`
- `OrderedList`
- `ListBlock`
- `ListItem`
- `FencedCodeBlock`
- `IndentedCodeBlock`
- `HtmlBlock`
- `HtmlInline`
- `ThematicBreak`
- `LinkReferenceDefinition`
- `SourceSpan`
- `SourceSpans`
- visitor classes and helper abstractions

### Parser API

Port first:
- `Parser`
- `IncludeSourceSpans`
- `SourceLine`
- `SourceLines`
- `PostProcessor`
- `InlineParser`
- `InlineParserContext`
- `InlineParserFactory`
- block parser extension interfaces
- delimiter extension interfaces
- `parser.beta.*` support interfaces used by the current upstream parser design

Explicit first-port edit:
- remove `Parser.parseReader(Reader)`

### HTML Renderer API

Port first:
- `Renderer`
- `NodeRenderer`
- `HtmlRenderer`
- `HtmlNodeRendererContext`
- `HtmlNodeRendererFactory`
- `AttributeProvider`
- `AttributeProviderFactory`
- `AttributeProviderContext`
- `UrlSanitizer`
- `DefaultUrlSanitizer`
- `CoreHtmlNodeRenderer`
- `HtmlWriter`

### Markdown Renderer API

Ported:
- `MarkdownRenderer`
- `MarkdownNodeRendererContext`
- `MarkdownNodeRendererFactory`
- `MarkdownWriter`

### Text Renderer API

Ported:
- `TextContentRenderer`
- `TextContentNodeRendererContext`
- `TextContentNodeRendererFactory`
- `TextContentWriter`
- `LineBreakRendering`

## Internal Types To Exclude In First Port

- `module-info.java`
- `org.commonmark.internal.util.LineReader`
- any code reachable only from `parseReader(Reader)`

## Special Handling

### Resource-backed entities

Upstream:
- `org.commonmark.internal.util.Html5Entities`

Port decision:
- keep class name and package shape
- replace runtime file loading with generated or checked-in Java constants

### Experimental parser package

Upstream exports `org.commonmark.parser.beta` and uses it internally for the current inline parser architecture.

Port decision:
- keep the package in the first port
- revisit public exposure only after the parser is stable and compiling under GWT/J2CL

### Package naming decision

Chosen root package:

```text
org.dominokit.markdown
```

Reason:
- avoids collisions with upstream JVM `org.commonmark` artifacts
- makes browser-safe fork identity explicit
- still preserves an obvious migration path from upstream type names
