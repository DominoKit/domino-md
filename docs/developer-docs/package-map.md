# Package Map

This page is a working map of the repository's package layout and the main classes in each area.

## Top-Level Packages

### `org.dominokit.markdown`

Small shared API surface:

- `Extension`

### `org.dominokit.markdown.node`

AST types and shared tree helpers.

Key classes:

- `Node`
- `Block`
- `CustomNode`
- `CustomBlock`
- `Document`
- `Paragraph`
- `Heading`
- `BlockQuote`
- `BulletList`
- `OrderedList`
- `ListBlock`
- `ListItem`
- `Text`
- `Code`
- `Link`
- `Image`
- `Emphasis`
- `StrongEmphasis`
- `HtmlBlock`
- `HtmlInline`
- `SoftLineBreak`
- `HardLineBreak`
- `FencedCodeBlock`
- `IndentedCodeBlock`
- `ThematicBreak`
- `LinkReferenceDefinition`
- `DefinitionMap`
- `SourceSpan`
- `SourceSpans`
- `Nodes`
- `Visitor`
- `AbstractVisitor`

### `org.dominokit.markdown.parser`

Public parser API.

Key classes:

- `Parser`
- `InlineParser`
- `InlineParserContext`
- `InlineParserFactory`
- `PostProcessor`
- `SourceLine`
- `SourceLines`
- `IncludeSourceSpans`

### `org.dominokit.markdown.parser.block`

Block parser contracts and parser factory APIs.

Key classes:

- `AbstractBlockParser`
- `AbstractBlockParserFactory`
- `BlockParser`
- `BlockParserFactory`
- `BlockContinue`
- `BlockStart`
- `MatchedBlockParser`
- `ParserState`

### `org.dominokit.markdown.parser.beta`

Inline parsing support contracts.

Key classes:

- `InlineContentParser`
- `InlineContentParserFactory`
- `InlineParserState`
- `LinkInfo`
- `LinkProcessor`
- `LinkResult`
- `ParsedInline`
- `Position`
- `Scanner`

### `org.dominokit.markdown.parser.delimiter`

Delimiter processing contracts used by emphasis and extension syntax.

Key classes:

- `DelimiterProcessor`
- `DelimiterRun`

### `org.dominokit.markdown.renderer`

Renderer abstraction layer.

Key classes:

- `Renderer`
- `NodeRenderer`

### `org.dominokit.markdown.renderer.html`

HTML string rendering.

Key classes:

- `HtmlRenderer`
- `HtmlWriter`
- `HtmlNodeRendererContext`
- `HtmlNodeRendererFactory`
- `AttributeProvider`
- `AttributeProviderFactory`
- `AttributeProviderContext`
- `UrlSanitizer`
- `DefaultUrlSanitizer`
- `CoreHtmlNodeRenderer`

### `org.dominokit.markdown.renderer.elemental2`

Elemental2 DOM rendering.

Key classes:

- `Elemental2Renderer`
- `ElementNodeRendererContext`
- `ElementNodeRendererFactory`
- `ElementAttributeProvider`
- `ElementAttributeProviderFactory`
- `ElementAttributeProviderContext`
- `RawHtmlHandler`
- `SoftBreakRendering`
- `CoreElementNodeRenderer`

### `org.dominokit.markdown.renderer.text`

Plain-text rendering.

Key classes:

- `TextContentRenderer`
- `TextContentWriter`
- `TextContentNodeRendererContext`
- `TextContentNodeRendererFactory`
- `LineBreakRendering`
- `CoreTextContentNodeRenderer`

### `org.dominokit.markdown.renderer.markdown`

Markdown-to-Markdown rendering.

Key classes:

- `MarkdownRenderer`
- `MarkdownWriter`
- `MarkdownNodeRendererContext`
- `MarkdownNodeRendererFactory`
- `CoreMarkdownNodeRenderer`

### `org.dominokit.markdown.ext.autolink`

Autolink extension.

Key classes:

- `AutolinkExtension`
- `AutolinkType`
- `internal.AutolinkPostProcessor`

### `org.dominokit.markdown.ext.gfm.strikethrough`

Strikethrough extension.

Key classes:

- `Strikethrough`
- `StrikethroughExtension`
- `internal.StrikethroughDelimiterProcessor`
- renderer implementations in `internal`

### `org.dominokit.markdown.ext.gfm.tables`

GFM pipe tables.

Key classes:

- `TableBlock`
- `TableHead`
- `TableBody`
- `TableRow`
- `TableCell`
- `TablesExtension`
- renderer and parser helpers in `internal`

### `org.dominokit.markdown.ext.task.list.items`

Task list items.

Key classes:

- `TaskListItemMarker`
- `TaskListItemsExtension`
- post-processing and renderer helpers in `internal`

### `org.dominokit.markdown.extensions.discovery`

Bundled extension discovery.

Key classes:

- `ExtensionDiscovery`
- `StandardExtensionSets`

### `org.dominokit.markdown.internal`

Parser implementation details and shared internal helpers.

Representative classes:

- `DocumentParser`
- `InlineParserImpl`
- `InlineParserContextImpl`
- `BlockQuoteParser`
- `HeadingParser`
- `HtmlBlockParser`
- `IndentedCodeBlockParser`
- `FencedCodeBlockParser`
- `ListBlockParser`
- `ListItemParser`
- `ParagraphParser`
- `ThematicBreakParser`
- `LinkReferenceDefinitionParser`
- `Delimiter`
- `Bracket`
- `Definitions`
- `BlockContent`
- `BlockContinueImpl`
- `BlockStartImpl`

### `org.dominokit.markdown.internal.inline`

Inline parser helpers and built-in inline parsers.

Representative classes:

- `BackslashInlineParser`
- `BackticksInlineParser`
- `EntityInlineParser`
- `AutolinkInlineParser`
- `HtmlInlineParser`
- `CoreLinkProcessor`
- `AsteriskDelimiterProcessor`
- `UnderscoreDelimiterProcessor`
- `EmphasisDelimiterProcessor`

### `org.dominokit.markdown.internal.renderer`

Renderer internals.

Representative class:

- `NodeRendererMap`

### `org.dominokit.markdown.internal.util`

General-purpose internal utilities.

Representative classes:

- `Escaping`
- `Parsing`
- `LinkScanner`
- `Html5Entities`

### `org.dominokit.markdown.text`

Character and Unicode helpers used by parsing and rendering.

Key classes:

- `AsciiMatcher`
- `CharMatcher`
- `Characters`
- `UnicodeCharacterData`

## What Lives Where

Use this rule when adding new code:

- API surface goes in the relevant public package.
- parser or renderer implementation details go in `internal`.
- renderer-specific helpers stay with their renderer package.
- AST node types belong in `node`.
- utilities that are reused across the engine belong in `text` or `internal.util` depending on
  scope.

## Class Naming Conventions

- Public builders are named `Builder`.
- Renderer contexts are named `*Context`.
- Factories are named `*Factory`.
- Visitor implementations are usually package-private or private nested classes when they are not
  part of the public API.
- Extension implementations are named after the feature they add.

