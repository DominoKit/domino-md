# API Reference

This page is a practical reference for the public classes, builders, enums, and extension points
that most application code uses.

It is organized by area:

- parser and AST
- renderers
- built-in extensions
- configuration enums and helper strategies

## Parser API

### `Parser`

`Parser` is the main entry point for turning Markdown text into an AST.

Core usage:

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("Hello **world**");
```

Key ideas:

- the parser consumes a `String`
- the parser returns a `Node` tree
- each `parse` call uses fresh internal parser state
- parser configuration is captured by the builder

Use `Parser` when you need:

- HTML rendering
- text extraction
- AST inspection
- custom post-processing
- round-tripping into Markdown

### `Parser.Builder`

The builder is where you configure parsing behavior.

Important methods:

- `extensions(Iterable<? extends Extension>)`
- `enabledBlockTypes(Set<Class<? extends Block>>)`
- `includeSourceSpans(IncludeSourceSpans)`
- `maxOpenBlockParsers(int)`
- `customBlockParserFactory(BlockParserFactory)`
- `customInlineContentParserFactory(InlineContentParserFactory)`
- `customDelimiterProcessor(DelimiterProcessor)`
- `linkProcessor(LinkProcessor)`
- `linkMarker(Character)`
- `postProcessor(PostProcessor)`
- `inlineParserFactory(InlineParserFactory)`

### Parser configuration examples

Parse only selected core block types:

```java
Parser parser = Parser.builder()
    .enabledBlockTypes(Set.of(Heading.class, ListBlock.class))
    .build();
```

Collect source spans:

```java
Parser parser = Parser.builder()
    .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
    .build();
```

Register custom syntax:

```java
Parser parser = Parser.builder()
    .customBlockParserFactory(new MyBlockParserFactory())
    .customInlineContentParserFactory(new MyInlineParserFactory())
    .customDelimiterProcessor(new MyDelimiterProcessor())
    .linkProcessor(new MyLinkProcessor())
    .postProcessor(new MyPostProcessor())
    .build();
```

### `Parser.ParserExtension`

This is the parser-side extension contract.

An implementation can register:

- block parser factories
- inline content parser factories
- delimiter processors
- link processors
- link markers
- post-processors

This is the hook used by bundled extensions such as autolink, strikethrough, tables, and task
lists.

## AST API

### `Node`

`Node` is the base type for all AST nodes.

Common responsibilities:

- tree links
- child traversal
- source span storage
- visitor dispatch

### `Block` and inline nodes

Block nodes include:

- `Document`
- `Paragraph`
- `Heading`
- `ListBlock`
- `ListItem`
- `BlockQuote`
- `FencedCodeBlock`
- `IndentedCodeBlock`
- `HtmlBlock`
- `ThematicBreak`

Inline nodes include:

- `Text`
- `Code`
- `Link`
- `Image`
- `Emphasis`
- `StrongEmphasis`
- `HtmlInline`
- `SoftLineBreak`
- `HardLineBreak`

### `SourceSpan` and `SourceSpans`

These types track origin information from the source text.

Use them when you need:

- editor diagnostics
- source mapping
- debugging
- highlighting exact Markdown ranges

### `Visitor` and `AbstractVisitor`

The visitor API is the standard way to walk a tree without writing long `instanceof` chains.

Typical pattern:

```java
class TitlesOnlyVisitor extends AbstractVisitor {
    @Override
    public void visit(Heading heading) {
        System.out.println(heading.getLevel());
    }
}
```

### `Nodes`

`Nodes` provides utility iteration helpers for working with sibling ranges.

### `DefinitionMap`

`DefinitionMap` stores reference definitions keyed by normalized labels.

It is used internally by the parser, but it is still useful to understand because it explains how
reference definitions are collected and reused.

## Common Hook Contracts

These are the public interfaces and support types that extensions and advanced integrations use
when they need to plug into the engine more deeply.

### Base contracts

- `Extension`
- `Renderer`
- `NodeRenderer`

Use these as the root types for extension and rendering participation.

### Parser-side contracts

- `InlineParserFactory`
- `InlineParser`
- `InlineParserContext`
- `PostProcessor`
- `BlockParserFactory`
- `BlockParser`
- `ParserState`
- `InlineContentParserFactory`
- `InlineContentParser`
- `InlineParserState`
- `DelimiterProcessor`
- `DelimiterRun`
- `LinkProcessor`
- `LinkInfo`
- `LinkResult`
- `ParsedInline`
- `Position`
- `Scanner`

These types matter when you are building custom syntax support.

### Renderer-side contracts

- `HtmlNodeRendererContext`
- `HtmlNodeRendererFactory`
- `AttributeProvider`
- `AttributeProviderFactory`
- `AttributeProviderContext`
- `ElementNodeRendererContext`
- `ElementNodeRendererFactory`
- `ElementAttributeProvider`
- `ElementAttributeProviderFactory`
- `ElementAttributeProviderContext`
- `TextContentNodeRendererContext`
- `TextContentNodeRendererFactory`
- `MarkdownNodeRendererContext`
- `MarkdownNodeRendererFactory`
- `RawHtmlHandler`
- `UrlSanitizer`

These types matter when you are customizing output behavior.

### Utility and helper types

- `DefaultUrlSanitizer`
- `IncludeSourceSpans`
- `LineBreakRendering`
- `SoftBreakRendering`
- `AutolinkType`
- `TextContentWriter`
- `HtmlWriter`
- `MarkdownWriter`

These classes are part of the public surface and are useful when wiring advanced application
behavior.

## HTML Renderer API

### `HtmlRenderer`

`HtmlRenderer` renders AST nodes to HTML strings or appendables.

Main methods:

- `builder()`
- `render(Node, Appendable)`
- `render(Node)`

Builder options:

- `softbreak(String)`
- `escapeHtml(boolean)`
- `sanitizeUrls(boolean)`
- `urlSanitizer(UrlSanitizer)`
- `percentEncodeUrls(boolean)`
- `omitSingleParagraphP(boolean)`
- `attributeProviderFactory(AttributeProviderFactory)`
- `nodeRendererFactory(HtmlNodeRendererFactory)`
- `extensions(Iterable<? extends Extension>)`

Example:

```java
HtmlRenderer renderer = HtmlRenderer.builder()
    .softbreak("<br>")
    .sanitizeUrls(true)
    .percentEncodeUrls(true)
    .build();
```

### `HtmlRenderer.HtmlRendererExtension`

Use this when an extension needs to register HTML renderer behavior.

### Attribute providers

- `AttributeProvider`
- `AttributeProviderFactory`
- `AttributeProviderContext`

Use these when you want to add or override HTML attributes.

### URL handling

- `UrlSanitizer`
- `DefaultUrlSanitizer`

Use these when you want to control what destination URLs are allowed in HTML output.

### Node renderer hooks

- `HtmlNodeRendererContext`
- `HtmlNodeRendererFactory`
- `NodeRenderer`
- `CoreHtmlNodeRenderer`

These are the extension points for changing how specific node types render.

## Elemental2 Renderer API

### `Elemental2Renderer`

`Elemental2Renderer` renders to a detached `DocumentFragment`.

Main methods:

- `builder()`
- `render(Node)`

Builder options:

- `softBreakRendering(SoftBreakRendering)`
- `sanitizeUrls(boolean)`
- `urlSanitizer(UrlSanitizer)`
- `percentEncodeUrls(boolean)`
- `omitSingleParagraphP(boolean)`
- `rawHtmlHandler(RawHtmlHandler)`
- `attributeProviderFactory(ElementAttributeProviderFactory)`
- `nodeRendererFactory(ElementNodeRendererFactory)`
- `extensions(Iterable<? extends Extension>)`

Example:

```java
Elemental2Renderer renderer = Elemental2Renderer.builder()
    .softBreakRendering(SoftBreakRendering.BR_ELEMENT)
    .sanitizeUrls(true)
    .build();
```

### `Elemental2Renderer.Elemental2RendererExtension`

This is the extension contract for DOM rendering.

### DOM attribute hooks

- `ElementAttributeProvider`
- `ElementAttributeProviderFactory`
- `ElementAttributeProviderContext`

### Raw HTML hooks

- `RawHtmlHandler`

Use this when you want to turn raw HTML literals into custom DOM nodes.

### DOM node renderer hooks

- `ElementNodeRendererContext`
- `ElementNodeRendererFactory`
- `ElementNodeRenderer`
- `CoreElementNodeRenderer`

## Plain Text Renderer API

### `TextContentRenderer`

`TextContentRenderer` renders readable plain text.

Main methods:

- `builder()`
- `render(Node, Appendable)`
- `render(Node)`

Builder options:

- `lineBreakRendering(LineBreakRendering)`
- `stripNewlines(boolean)` for compatibility
- `nodeRendererFactory(TextContentNodeRendererFactory)`
- `extensions(Iterable<? extends Extension>)`

Example:

```java
TextContentRenderer renderer = TextContentRenderer.builder()
    .lineBreakRendering(LineBreakRendering.SEPARATE_BLOCKS)
    .build();
```

### `TextContentRenderer.TextContentRendererExtension`

This is the extension contract for plain-text rendering.

### Text renderer hooks

- `TextContentWriter`
- `TextContentNodeRendererContext`
- `TextContentNodeRendererFactory`
- `CoreTextContentNodeRenderer`

## Markdown Renderer API

### `MarkdownRenderer`

`MarkdownRenderer` turns an AST back into canonical Markdown.

Main methods:

- `builder()`
- `render(Node, Appendable)`
- `render(Node)`

Builder options:

- `nodeRendererFactory(MarkdownNodeRendererFactory)`
- `extensions(Iterable<? extends Extension>)`

Example:

```java
MarkdownRenderer renderer = MarkdownRenderer.builder().build();
```

### `MarkdownRenderer.MarkdownRendererExtension`

This is the extension contract for Markdown output.

### Markdown renderer hooks

- `MarkdownWriter`
- `MarkdownNodeRendererContext`
- `MarkdownNodeRendererFactory`
- `CoreMarkdownNodeRenderer`

## Built-In Extensions

### `AutolinkExtension`

Turns bare URLs, emails, and `www.` links into link nodes.

Builder methods:

- `builder()`
- `linkTypes(AutolinkType...)`
- `linkTypes(Set<AutolinkType>)`
- `build()`

Supported link kinds:

- `AutolinkType.URL`
- `AutolinkType.EMAIL`
- `AutolinkType.WWW`

### `StrikethroughExtension`

Adds `~` and `~~` strikethrough support.

Builder methods:

- `builder()`
- `requireTwoTildes(boolean)`
- `build()`

### `TablesExtension`

Adds GitHub-style pipe table support.

This extension has no tunable builder options.

### `TaskListItemsExtension`

Adds checkbox-style task list item support.

This extension has no tunable builder options.

### Extension discovery

- `ExtensionDiscovery`
- `StandardExtensionSets`

Use `ExtensionDiscovery.load()` to get the bundled extension set in deterministic order.

## Configuration Enums

### `IncludeSourceSpans`

Controls how much source-position information the parser collects.

- `NONE`
- `BLOCKS`
- `BLOCKS_AND_INLINES`

### `LineBreakRendering`

Controls plain-text line-break behavior.

- `STRIP`
- `COMPACT`
- `SEPARATE_BLOCKS`

### `SoftBreakRendering`

Controls Elemental2 soft-break behavior.

- `NEWLINE_TEXT`
- `SPACE_TEXT`
- `BR_ELEMENT`

### `AutolinkType`

Controls which kinds of autolinks the autolink extension recognizes.

- `URL`
- `EMAIL`
- `WWW`

## Supporting Utilities

These are public and often useful when you are customizing behavior:

- `CharMatcher`
- `AsciiMatcher`
- `Characters`
- `UrlSanitizer`
- `DefaultUrlSanitizer`

## Extension and Renderer Flow

The same extension set can often be registered on multiple builders:

```java
Set<Extension> extensions = Set.of(
    AutolinkExtension.create(),
    StrikethroughExtension.create(),
    TablesExtension.create(),
    TaskListItemsExtension.create());

Parser parser = Parser.builder().extensions(extensions).build();
HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();
Elemental2Renderer domRenderer = Elemental2Renderer.builder().extensions(extensions).build();
TextContentRenderer textRenderer = TextContentRenderer.builder().extensions(extensions).build();
MarkdownRenderer markdownRenderer = MarkdownRenderer.builder().extensions(extensions).build();
```

That is the cleanest way to keep parsing and rendering behavior aligned.
