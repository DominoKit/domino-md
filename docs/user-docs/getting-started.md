# Getting Started

This page shows the smallest useful examples first, then adds more structure step by step.

## 1. Parse Markdown Into an AST

The simplest way to use the library is to parse a Markdown string.

```java
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.parser.Parser;

Parser parser = Parser.builder().build();
Node document = parser.parse("Hello **world**");
```

At this point you have an AST, not rendered output. That AST can be rendered in multiple ways.

## 2. Render To HTML

```java
import org.dominokit.markdown.renderer.html.HtmlRenderer;

Parser parser = Parser.builder().build();
Node document = parser.parse("Hello **world**");

HtmlRenderer renderer = HtmlRenderer.builder().build();
String html = renderer.render(document);
```

Expected output:

```html
<p>Hello <strong>world</strong></p>
```

## 3. Render To Plain Text

```java
import org.dominokit.markdown.renderer.text.TextContentRenderer;

Parser parser = Parser.builder().build();
Node document = parser.parse("Hello **world**");

TextContentRenderer renderer = TextContentRenderer.builder().build();
String text = renderer.render(document);
```

Expected output:

```text
Hello world
```

## 4. Render Back To Markdown

```java
import org.dominokit.markdown.renderer.markdown.MarkdownRenderer;

Parser parser = Parser.builder().build();
Node document = parser.parse("Hello **world**");

MarkdownRenderer renderer = MarkdownRenderer.builder().build();
String markdown = renderer.render(document);
```

The renderer returns canonical Markdown, which means it may not preserve the exact source
formatting, but it should preserve the meaning of the AST.

## 5. Parse And Render In One Place

This is the common pattern in an application:

```java
Parser parser = Parser.builder().build();
HtmlRenderer renderer = HtmlRenderer.builder().build();

String markdown = """
# Welcome

This is **Markdown**.
""";

Node document = parser.parse(markdown);
String html = renderer.render(document);
```

## 6. A Slightly More Realistic Example

```java
Parser parser = Parser.builder().build();
HtmlRenderer renderer = HtmlRenderer.builder()
    .softbreak("<br>")
    .build();

String markdown = """
Hello
world
""";

String html = renderer.render(parser.parse(markdown));
```

This version shows a configuration change. Soft line breaks are rendered as `<br>` instead of the
default newline behavior.

## 7. Working With Output Targets

All renderers follow the same basic idea:

- create a parser
- parse the Markdown string
- pass the AST to a renderer

The output target depends on the renderer:

- `HtmlRenderer` returns a `String` or writes into an `Appendable`
- `Elemental2Renderer` returns a detached `DocumentFragment`
- `TextContentRenderer` returns a `String` or writes into an `Appendable`
- `MarkdownRenderer` returns a `String` or writes into an `Appendable`

## 8. What To Read Next

- [Parsing and ASTs](parsing-and-asts.md)
- [Rendering Output](rendering.md)
- [Extensions](extensions.md)

