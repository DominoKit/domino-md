# Cookbook

This page shows real-world recipes, ordered from simple to more advanced.

## Recipe 1: Render Markdown Preview In An Editor

Goal: parse user input and update a live HTML preview.

```java
Parser parser = Parser.builder()
    .extensions(ExtensionDiscovery.load())
    .build();

HtmlRenderer renderer = HtmlRenderer.builder()
    .extensions(ExtensionDiscovery.load())
    .softbreak("<br>")
    .build();

String markdown = editor.getValue();
String previewHtml = renderer.render(parser.parse(markdown));
previewPane.setInnerHtml(previewHtml);
```

Why this works:

- the parser and renderer share the same extension set
- the HTML renderer preserves line breaks in a preview-friendly way
- the preview is derived from the AST, not from ad hoc string manipulation

### Variant: Preserve The Raw HTML Nodes

If you want raw HTML to stay as HTML:

```java
HtmlRenderer renderer = HtmlRenderer.builder()
    .escapeHtml(false)
    .build();
```

If you want raw HTML to be shown literally:

```java
HtmlRenderer renderer = HtmlRenderer.builder()
    .escapeHtml(true)
    .build();
```

## Recipe 2: Browser-Side DOM Preview

Goal: render to a detached DOM fragment and mount it in the page.

```java
Parser parser = Parser.builder()
    .extensions(ExtensionDiscovery.load())
    .build();

String markdown = """
Hello **world**
""";

Elemental2Renderer renderer = Elemental2Renderer.builder()
    .extensions(ExtensionDiscovery.load())
    .softBreakRendering(SoftBreakRendering.BR_ELEMENT)
    .build();

DocumentFragment fragment = renderer.render(parser.parse(markdown));
previewContainer.appendChild(fragment);
```

Why this is useful:

- no HTML string parsing step
- easier integration with browser DOM workflows
- raw HTML handling can be customized if needed

### Variant: Custom Raw HTML Handling

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

Return `null` to fall back to safe default behavior, or return a custom DOM node if you want to
materialize the raw HTML yourself.

## Recipe 3: Build A Readable Text Summary

Goal: extract text for previews, feeds, indexing, or notifications.

```java
Parser parser = Parser.builder()
    .extensions(ExtensionDiscovery.load())
    .build();

String markdown = """
First line
Second line
""";

TextContentRenderer renderer = TextContentRenderer.builder()
    .lineBreakRendering(LineBreakRendering.COMPACT)
    .build();

String summary = renderer.render(parser.parse(markdown));
```

### Variant: Flatten Text Completely

```java
TextContentRenderer renderer = TextContentRenderer.builder()
    .stripNewlines(true)
    .build();
```

This is useful when you want a single-line summary.

### Variant: Keep Paragraph Separation

```java
TextContentRenderer renderer = TextContentRenderer.builder()
    .lineBreakRendering(LineBreakRendering.SEPARATE_BLOCKS)
    .build();
```

This keeps block boundaries more readable in exported text.

## Recipe 4: Canonicalize Markdown Before Saving

Goal: normalize user-authored Markdown into a stable format before persisting it.

```java
Parser parser = Parser.builder()
    .extensions(ExtensionDiscovery.load())
    .build();

String userMarkdown = """
# Document

Body text.
""";

MarkdownRenderer renderer = MarkdownRenderer.builder()
    .extensions(ExtensionDiscovery.load())
    .build();

String canonicalMarkdown = renderer.render(parser.parse(userMarkdown));
database.save(canonicalMarkdown);
```

Use this when you want:

- consistent formatting
- easier diffs
- standardized syntax before storage

### Important Note

Canonical Markdown is not source-preserving.

That means the output may:

- change heading style
- normalize whitespace
- emit equivalent syntax in a different form

## Recipe 5: Parse Only A Restricted Markdown Subset

Goal: accept a narrow feature set for a controlled input surface.

```java
Parser parser = Parser.builder()
    .enabledBlockTypes(Set.of(Heading.class, Paragraph.class, ListBlock.class))
    .build();
```

This is useful for:

- limited editor widgets
- controlled CMS fields
- preview systems that should reject some Markdown constructs

## Recipe 6: Highlight Source Locations In An Editor

Goal: use source spans to map parsed nodes back to the original text.

```java
Parser parser = Parser.builder()
    .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
    .build();

String markdown = """
Hello **world**
""";

Node document = parser.parse(markdown);
```

You can then inspect `getSourceSpans()` on nodes to associate parsed structure with editor ranges.

### Practical Workflow

1. parse with source spans enabled
2. locate the node you want to highlight
3. read the node's span list
4. map those offsets back into your editor model

## Recipe 7: Enable Common GitHub-Flavored Markdown Features

Goal: turn on the built-in extension set for a typical GitHub-flavored Markdown experience.

```java
List<Extension> extensions = ExtensionDiscovery.load();

Parser parser = Parser.builder().extensions(extensions).build();
HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
```

This gives you:

- autolinks
- strikethrough
- tables
- task list items

## Recipe 8: Configure Autolink For URLs Only

```java
Extension autolink = AutolinkExtension.builder()
    .linkTypes(AutolinkType.URL)
    .build();
```

Use this when you want to recognize bare URLs but not email addresses or `www.` prefixes.

## Recipe 9: Require Double-Tilde Strikethrough

```java
Extension strikethrough = StrikethroughExtension.builder()
    .requireTwoTildes(true)
    .build();
```

This is a good fit when you want stricter Markdown parsing rules.

## Recipe 10: Combine Custom Extensions With Built-Ins

```java
List<Extension> extensions = new ArrayList<>(ExtensionDiscovery.load());
extensions.add(new MyCustomExtension());

Parser parser = Parser.builder().extensions(extensions).build();
HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
```

The important point is that extensions are just values. You can assemble them however you want
before passing them into the builders.

## Recipe 11: Render The Same Content In Multiple Formats

Goal: build HTML, text, and Markdown from one parsed document.

```java
Parser parser = Parser.builder().extensions(ExtensionDiscovery.load()).build();
String markdown = """
# Title

Hello **world**
""";

Node document = parser.parse(markdown);

String html = HtmlRenderer.builder().extensions(ExtensionDiscovery.load()).build().render(document);
String text = TextContentRenderer.builder().extensions(ExtensionDiscovery.load()).build().render(document);
String canonical = MarkdownRenderer.builder().extensions(ExtensionDiscovery.load()).build().render(document);
```

This is useful for:

- preview panes
- notifications
- export pipelines
- regression tests

## Recipe 12: Add A Custom HTML Attribute

```java
HtmlRenderer renderer = HtmlRenderer.builder()
    .attributeProviderFactory(context -> new AttributeProvider() {
      @Override
      public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
        if ("a".equals(tagName)) {
          attributes.put("rel", "noopener");
        }
      }
    })
    .build();
```

Use this for:

- analytics attributes
- accessibility attributes
- link rel policies
- application-specific classes

## Recipe 13: Add A Custom DOM Attribute

```java
Elemental2Renderer renderer = Elemental2Renderer.builder()
    .attributeProviderFactory(context -> new ElementAttributeProvider() {
      @Override
      public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
        if ("img".equals(tagName)) {
          attributes.put("loading", "lazy");
        }
      }
    })
    .build();
```

## Recipe 14: Write A Custom Parser Extension

Typical parser extension workflow:

```java
public final class MyExtension implements Parser.ParserExtension {
  @Override
  public void extend(Parser.Builder parserBuilder) {
    parserBuilder.customDelimiterProcessor(new MyDelimiterProcessor());
    parserBuilder.postProcessor(new MyPostProcessor());
  }
}
```

Then register it:

```java
Parser parser = Parser.builder()
    .extensions(List.of(new MyExtension()))
    .build();
```

## Recipe 15: Write A Custom Renderer Extension

If your syntax needs custom rendering, register a renderer hook too:

```java
public final class MyHtmlExtension implements HtmlRenderer.HtmlRendererExtension {
  @Override
  public void extend(HtmlRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(context -> new MyHtmlNodeRenderer(context));
  }
}
```

## Recipe 16: Static-Site Markdown Pipeline

Goal: render Markdown files into static HTML.

```java
Parser parser = Parser.builder().extensions(ExtensionDiscovery.load()).build();
HtmlRenderer renderer = HtmlRenderer.builder().extensions(ExtensionDiscovery.load()).build();

for (Path path : markdownFiles) {
  String markdown = Files.readString(path);
  String html = renderer.render(parser.parse(markdown));
  Path output = outputDir.resolve(replaceExtension(path.getFileName().toString(), ".html"));
  Files.writeString(output, html);
}
```

### Why This Pattern Works

- parsing is done once per file
- renderers are reusable
- extensions stay consistent across files
- the code stays easy to test

## Recipe 17: Compare HTML And Canonical Markdown In Tests

Goal: verify that a Markdown fragment round-trips predictably.

```java
Parser parser = Parser.builder().extensions(ExtensionDiscovery.load()).build();
MarkdownRenderer markdownRenderer = MarkdownRenderer.builder().extensions(ExtensionDiscovery.load()).build();
HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(ExtensionDiscovery.load()).build();

Node document = parser.parse(input);
String normalized = markdownRenderer.render(document);
String html = htmlRenderer.render(parser.parse(normalized));
```

This pattern is useful when you want to test semantic stability rather than exact source output.

## Recipe 18: Choose The Right Tool

Use these quick rules:

- `Parser` when you need an AST
- `HtmlRenderer` when you need HTML
- `Elemental2Renderer` when you need browser DOM
- `TextContentRenderer` when you need readable text
- `MarkdownRenderer` when you need canonical Markdown

## Recipe 19: Style Rendered Markdown With CSS Classes

Goal: attach predictable CSS hooks to the HTML or DOM that comes out of markdown rendering.

Start with the built-in preset if you want the repository's default class naming scheme:

```java
List<Extension> extensions = List.of(new DuiClassExtension());

Parser parser = Parser.builder()
    .extensions(extensions)
    .build();

HtmlRenderer htmlRenderer = HtmlRenderer.builder()
    .extensions(extensions)
    .build();

String html = htmlRenderer.render(parser.parse("# Title"));
```

The rendered heading will carry the generic `dui` class plus `dui-md-heading` and `dui-md-h1`.
That makes it easy to theme all markdown output consistently without replacing the renderer.

### Variant: Add Your Own Classes

If you want your own naming scheme, use the generic extension directly:

```java
MarkdownClassExtension markdownClasses =
    MarkdownClassExtension.builder()
        .classes("article-content", "prose")
        .nodeClasses(Paragraph.class, "article-paragraph")
        .nodeClasses(Heading.class, "article-heading")
        .tagClasses("h1", "heading-xl")
        .tagClasses("p", "paragraph-body")
        .build();

List<Extension> extensions = List.of(markdownClasses);

Parser parser = Parser.builder()
    .extensions(extensions)
    .build();

Elemental2Renderer renderer = Elemental2Renderer.builder()
    .extensions(extensions)
    .build();
```

This is useful when you want:

- one shared base class for all rendered content
- node-specific selectors for layout or spacing
- tag-specific selectors for headings, paragraphs, links, or code blocks
- a single extension that works for both HTML strings and detached DOM fragments

### Important Note

The class extension merges with any classes already present on the element.

That means built-in renderer classes such as `language-java` on fenced code blocks stay intact
while your custom class names are appended.

## Final Advice

Start with the default builders and the built-in extension set.

Only customize when you can clearly explain the user-visible change you need.
