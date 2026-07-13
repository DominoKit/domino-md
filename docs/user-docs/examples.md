# Examples

This page shows a gradual progression from tiny examples to more realistic usage.

## Example 1: Paragraph

```java
Parser parser = Parser.builder().build();
HtmlRenderer renderer = HtmlRenderer.builder().build();

String html = renderer.render(parser.parse("Hello world"));
```

Output:

```html
<p>Hello world</p>
```

## Example 2: Heading And Paragraph

```java
String markdown = """
# Welcome

This is a paragraph.
""";

String html = HtmlRenderer.builder().build().render(Parser.builder().build().parse(markdown));
```

## Example 3: Link And Emphasis

```java
String markdown = "Visit [OpenAI](https://openai.com) for **models**.";
String html = HtmlRenderer.builder().build().render(Parser.builder().build().parse(markdown));
```

## Example 4: A List

```java
String markdown = """
- First
- Second
""";

String html = HtmlRenderer.builder().build().render(Parser.builder().build().parse(markdown));
```

## Example 5: Tables

```java
Set<Extension> extensions = Set.of(TablesExtension.create());

Parser parser = Parser.builder().extensions(extensions).build();
HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();

String markdown = """
| Name | Value |
| --- | --- |
| One | 1 |
| Two | 2 |
""";

String html = renderer.render(parser.parse(markdown));
```

## Example 6: Task List Items

```java
Set<Extension> extensions = Set.of(TaskListItemsExtension.create());

Parser parser = Parser.builder().extensions(extensions).build();
HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();

String markdown = """
- [ ] Draft docs
- [x] Review docs
""";

String html = renderer.render(parser.parse(markdown));
```

## Example 7: Strikethrough And Autolink Together

```java
Set<Extension> extensions =
    Set.of(
        StrikethroughExtension.create(),
        AutolinkExtension.create());

Parser parser = Parser.builder().extensions(extensions).build();
MarkdownRenderer markdownRenderer = MarkdownRenderer.builder().extensions(extensions).build();

String markdown = "This is ~~old~~ and https://openai.com";
String canonical = markdownRenderer.render(parser.parse(markdown));
```

## Example 8: HTML, Text, And Markdown From One Parse

```java
Parser parser = Parser.builder().extensions(ExtensionDiscovery.load()).build();
Node document = parser.parse("""
# Title

Visit https://openai.com
""");

String html = HtmlRenderer.builder().extensions(ExtensionDiscovery.load()).build().render(document);
String text = TextContentRenderer.builder().extensions(ExtensionDiscovery.load()).build().render(document);
String canonical = MarkdownRenderer.builder().extensions(ExtensionDiscovery.load()).build().render(document);
```

## Example 9: Source Spans For Diagnostics

```java
Parser parser = Parser.builder()
    .includeSourceSpans(IncludeSourceSpans.ALL)
    .build();

Node document = parser.parse("Hello");
```

When you want to highlight the original source in an editor, source spans make that possible.

## Example 10: Custom Extension Configuration

```java
Extension strikethrough = StrikethroughExtension.builder()
    .requireTwoTildes(true)
    .build();

Extension autolink = AutolinkExtension.builder()
    .linkTypes(AutolinkType.URL)
    .build();

Set<Extension> extensions = Set.of(strikethrough, autolink);

Parser parser = Parser.builder().extensions(extensions).build();
HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
```

## Example 11: Render To A StringBuilder

```java
StringBuilder out = new StringBuilder();
HtmlRenderer renderer = HtmlRenderer.builder().build();
renderer.render(Parser.builder().build().parse("Hello"), out);
```

This pattern is useful when you want to manage the output buffer yourself.

## Example 12: Custom Rendering

```java
MarkdownRenderer renderer = MarkdownRenderer.builder()
    .nodeRendererFactory(context -> new MyMarkdownNodeRenderer(context))
    .build();
```

This is the starting point for cases where the default output is not the exact format you want.

