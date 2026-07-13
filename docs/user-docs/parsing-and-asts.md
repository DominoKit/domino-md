# Parsing and ASTs

This page explains how parsing works and how to work with the AST that comes back from the parser.

## What The Parser Produces

`Parser.parse(String)` returns an AST rooted at `org.dominokit.markdown.node.Node`.

The root is usually a `Document`, and its children represent the block structure of the input.
Those block nodes may themselves contain inline children.

## Simple Parse Example

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("Hello **world**");
```

If you are only rendering, you may not need to inspect the tree yourself. If you want to
understand or transform the content, the AST is the right place to work.

## Inspecting The Tree

The node tree behaves like a linked structure:

- `getFirstChild()`
- `getNext()`
- `getParent()`
- `getLastChild()`

Example:

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("""
# Title

First paragraph.
""");

Node firstChild = document.getFirstChild();
```

In this example, the first child is a heading node.

## Visiting Nodes

The public node model supports the visitor pattern.

```java
import org.dominokit.markdown.node.AbstractVisitor;
import org.dominokit.markdown.node.Heading;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.node.Text;

class MyVisitor extends AbstractVisitor {
    @Override
    public void visit(Heading heading) {
        System.out.println("heading level: " + heading.getLevel());
    }

    @Override
    public void visit(Paragraph paragraph) {
        System.out.println("paragraph");
    }

    @Override
    public void visit(Text text) {
        System.out.println(text.getLiteral());
    }
}

Node document = Parser.builder().build().parse("Hello");
document.accept(new MyVisitor());
```

## Accessing Inline Content

Block nodes such as paragraphs and headings can contain inline nodes.

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("This is **bold** text.");

Node paragraph = document.getFirstChild();
Node inline = paragraph.getFirstChild();
```

Inline children may be:

- `Text`
- `Code`
- `Emphasis`
- `StrongEmphasis`
- `Link`
- `Image`
- `HtmlInline`
- line break nodes

## Source Spans

Source spans are optional. Enable them when you need line and column information for diagnostics,
editor integrations, or source mapping.

```java
import org.dominokit.markdown.parser.IncludeSourceSpans;

Parser parser = Parser.builder()
    .includeSourceSpans(IncludeSourceSpans.ALL)
    .build();

Node document = parser.parse("Hello");
```

When source spans are enabled, nodes can expose origin information through `getSourceSpans()`.

## Working With Specific Node Types

### Headings

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("# Title");
Heading heading = (Heading) document.getFirstChild();
int level = heading.getLevel();
```

### Links

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("[OpenAI](https://openai.com)");
Link link = (Link) document.getFirstChild().getFirstChild();

String destination = link.getDestination();
String title = link.getTitle();
```

### Images

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("![alt text](image.png)");
Image image = (Image) document.getFirstChild().getFirstChild();
```

### Lists

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("""
- One
- Two
""");

ListBlock list = (ListBlock) document.getFirstChild();
```

## Tree Mutations

The AST is mutable. That means you can:

- append children
- insert siblings
- remove nodes
- replace nodes

This is useful for post-processing, custom transforms, or application-specific rewriting before
rendering.

Example:

```java
Parser parser = Parser.builder().build();
Node document = parser.parse("Hello");
Paragraph paragraph = (Paragraph) document.getFirstChild();
paragraph.appendChild(new Text(" world"));
```

## Common Parse-Time Concepts

### Block structure first

Block parsing decides where paragraphs, headings, lists, quotes, and code blocks begin and end.

### Inline structure second

Inline parsing then resolves emphasis, links, code spans, and related syntax inside block
containers.

### Post-processing last

Some extensions, such as autolink and task lists, modify the tree after the inline parser has
finished.

## Practical Advice

- Use the AST when you need transformation or inspection.
- Render directly when you only need output.
- Enable source spans only when you need them, because they add extra bookkeeping.
- Prefer visitors over ad hoc `instanceof` chains when walking larger trees.

