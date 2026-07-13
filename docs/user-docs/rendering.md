# Rendering Output

This page explains how to render the AST into each supported output format.

## HTML Rendering

`HtmlRenderer` is the most common renderer.

### Basic Example

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("Hello **world**");

HtmlRenderer renderer = HtmlRenderer.builder().build();
String html = renderer.render(document);
```

### Writing Into An Appendable

```java
StringBuilder out = new StringBuilder();
HtmlRenderer renderer = HtmlRenderer.builder().build();
renderer.render(document, out);
```

### Configuring Soft Breaks

```java
HtmlRenderer renderer = HtmlRenderer.builder()
    .softbreak("<br>")
    .build();
```

Soft line breaks are emitted as the fragment you choose.

### Escaping Raw HTML

```java
HtmlRenderer renderer = HtmlRenderer.builder()
    .escapeHtml(true)
    .build();
```

When enabled, literal HTML nodes are escaped instead of interpreted as markup.

### Sanitizing URLs

```java
HtmlRenderer renderer = HtmlRenderer.builder()
    .sanitizeUrls(true)
    .build();
```

When enabled, links and images are passed through the configured `UrlSanitizer` before emission.

### Omitting The Outer Paragraph

```java
HtmlRenderer renderer = HtmlRenderer.builder()
    .omitSingleParagraphP(true)
    .build();
```

This is useful when the document contains one top-level paragraph and you want the rendered HTML
without the wrapper `<p>`.

## Elemental2 DOM Rendering

`Elemental2Renderer` renders directly into a detached `DocumentFragment`.

### Basic Example

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("Hello **world**");

Elemental2Renderer renderer = Elemental2Renderer.builder().build();
DocumentFragment fragment = renderer.render(document);
```

The result is safe to insert into the DOM after rendering.

### Configuring Soft Breaks

```java
Elemental2Renderer renderer = Elemental2Renderer.builder()
    .softBreakRendering(SoftBreakRendering.BR_ELEMENT)
    .build();
```

### Configuring Raw HTML Handling

```java
Elemental2Renderer renderer = Elemental2Renderer.builder()
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

Provide a handler when you want custom DOM behavior for raw HTML content.

### Sanitizing URLs

```java
Elemental2Renderer renderer = Elemental2Renderer.builder()
    .sanitizeUrls(true)
    .build();
```

## Plain Text Rendering

`TextContentRenderer` extracts readable text.

### Basic Example

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("Hello **world**");

TextContentRenderer renderer = TextContentRenderer.builder().build();
String text = renderer.render(document);
```

### Line Break Behavior

```java
TextContentRenderer renderer = TextContentRenderer.builder()
    .lineBreakRendering(LineBreakRendering.COMPACT)
    .build();
```

The older `stripNewlines(boolean)` method still exists for compatibility, but `lineBreakRendering`
is the preferred API.

## Markdown Rendering

`MarkdownRenderer` rebuilds canonical Markdown from the AST.

### Basic Example

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("Hello **world**");

MarkdownRenderer renderer = MarkdownRenderer.builder().build();
String markdown = renderer.render(document);
```

### Why The Output May Change

Markdown rendering is semantic, not source-preserving.

That means:

- spacing may change
- heading style may change
- references may be normalized
- the output may choose a canonical form that differs from the input

### Custom Markdown Node Renderers

You can register your own Markdown node renderer factory when you need to alter the emitted syntax.

```java
MarkdownRenderer renderer = MarkdownRenderer.builder()
    .nodeRendererFactory(context -> new MyMarkdownNodeRenderer(context))
    .build();
```

## Common Rendering Patterns

### Parse Once, Render Many

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("""
# Title

Content here.
""");

String html = HtmlRenderer.builder().build().render(document);
String text = TextContentRenderer.builder().build().render(document);
String md = MarkdownRenderer.builder().build().render(document);
```

### Render To Different Targets

The same AST can feed:

- `String` HTML output
- a detached DOM fragment
- plain text
- Markdown text

That is useful when you need previews, exports, and accessibility text from the same source.

## Rendering Behavior Summary

- HTML rendering is the default general-purpose output path.
- Elemental2 rendering is for browser-side DOM materialization.
- Text rendering is for summaries, previews, and search indexing.
- Markdown rendering is for normalization and round-tripping.
