# Configuration

This page collects the most important configuration options in one place.

## Parser Configuration

### Start With The Builder

```java
Parser parser = Parser.builder().build();
```

The default parser is enough for standard Markdown.

### Enable Only Specific Block Types

```java
Parser parser = Parser.builder()
    .enabledBlockTypes(Set.of(Heading.class, ListBlock.class))
    .build();
```

This is useful when you want to restrict what the parser recognizes.

### Include Source Spans

```java
Parser parser = Parser.builder()
    .includeSourceSpans(IncludeSourceSpans.ALL)
    .build();
```

This adds origin information to nodes for debugging or editor features.

### Limit Open Blocks

```java
Parser parser = Parser.builder()
    .maxOpenBlockParsers(8)
    .build();
```

This limits how many non-document block parsers may remain open at once.

### Register Custom Syntax

```java
Parser parser = Parser.builder()
    .customBlockParserFactory(new MyBlockParserFactory())
    .customInlineContentParserFactory(new MyInlineParserFactory())
    .customDelimiterProcessor(new MyDelimiterProcessor())
    .linkProcessor(new MyLinkProcessor())
    .linkMarker('?')
    .postProcessor(new MyPostProcessor())
    .build();
```

This is the advanced hook path when the built-in Markdown rules are not enough.

## HTML Renderer Configuration

### Basic Setup

```java
HtmlRenderer renderer = HtmlRenderer.builder().build();
```

### Common Options

```java
HtmlRenderer renderer = HtmlRenderer.builder()
    .softbreak("<br>")
    .escapeHtml(true)
    .sanitizeUrls(true)
    .urlSanitizer(new DefaultUrlSanitizer())
    .percentEncodeUrls(true)
    .omitSingleParagraphP(true)
    .build();
```

### Attribute Providers

```java
HtmlRenderer renderer = HtmlRenderer.builder()
    .attributeProviderFactory(context -> new MyAttributeProvider())
    .build();
```

Use attribute providers when you need to add or adjust HTML attributes during rendering.

### Custom HTML Node Renderers

```java
HtmlRenderer renderer = HtmlRenderer.builder()
    .nodeRendererFactory(context -> new MyHtmlNodeRenderer(context))
    .build();
```

## Elemental2 Renderer Configuration

### Basic Setup

```java
Elemental2Renderer renderer = Elemental2Renderer.builder().build();
```

### Common Options

```java
Elemental2Renderer renderer = Elemental2Renderer.builder()
    .softBreakRendering(SoftBreakRendering.BR_ELEMENT)
    .sanitizeUrls(true)
    .urlSanitizer(new DefaultUrlSanitizer())
    .percentEncodeUrls(true)
    .omitSingleParagraphP(true)
    .rawHtmlHandler(new RawHtmlHandler() {
        @Override
        public elemental2.dom.Node renderInline(String literal) {
            return null;
        }

        @Override
        public elemental2.dom.Node renderBlock(String literal) {
            return null;
        }
    })
    .build();
```

### Element Attribute Providers

```java
Elemental2Renderer renderer = Elemental2Renderer.builder()
    .attributeProviderFactory(context -> new MyElementAttributeProvider())
    .build();
```

### Custom DOM Node Renderers

```java
Elemental2Renderer renderer = Elemental2Renderer.builder()
    .nodeRendererFactory(context -> new MyElementNodeRenderer(context))
    .build();
```

## Text Renderer Configuration

### Basic Setup

```java
TextContentRenderer renderer = TextContentRenderer.builder().build();
```

### Line Break Rendering

```java
TextContentRenderer renderer = TextContentRenderer.builder()
    .lineBreakRendering(LineBreakRendering.COMPACT)
    .build();
```

The older compatibility method is still available:

```java
TextContentRenderer renderer = TextContentRenderer.builder()
    .stripNewlines(false)
    .build();
```

### Custom Text Node Renderers

```java
TextContentRenderer renderer = TextContentRenderer.builder()
    .nodeRendererFactory(context -> new MyTextNodeRenderer(context))
    .build();
```

## Markdown Renderer Configuration

### Basic Setup

```java
MarkdownRenderer renderer = MarkdownRenderer.builder().build();
```

### Custom Markdown Node Renderers

```java
MarkdownRenderer renderer = MarkdownRenderer.builder()
    .nodeRendererFactory(context -> new MyMarkdownNodeRenderer(context))
    .build();
```

## Choosing The Right Renderer

Use HTML when you need browser or server HTML output.

Use Elemental2 when you need browser-side DOM nodes.

Use plain text when you need readable extraction.

Use Markdown rendering when you need canonical Markdown output or round-trip tests.

## Configuration Strategy

### Start Simple

Use the default builders first.

### Add Extensions Second

Register only the extensions you actually need.

### Tune Behavior Last

Only after the default setup is working should you customize:

- source spans
- soft breaks
- sanitization
- custom renderers
- custom syntax hooks
