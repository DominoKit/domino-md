# Extensions

Extensions are how you turn on optional syntax and renderer behavior.

This repository ships these built-in extensions:

- autolink
- strikethrough
- GFM tables
- task list items

## Extension Basics

An extension can contribute to one or more of these areas:

- parser behavior
- AST post-processing
- HTML rendering
- Elemental2 DOM rendering
- plain-text rendering
- Markdown rendering

Most applications just reuse the built-in extension factories and register them on the parser and
renderers they use.

## Simple Registration

```java
Set<Extension> extensions =
    Set.of(
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

## Bundled Discovery

If you want the built-in extension set in one call, use:

```java
List<Extension> extensions = ExtensionDiscovery.load();
```

That returns the bundled engine extensions in deterministic order.

## Autolink

Autolink turns bare URLs and email-like text into normal link nodes.

### Basic Example

```java
Extension extension = AutolinkExtension.create();

Parser parser = Parser.builder()
    .extensions(List.of(extension))
    .build();
```

### Limiting The Link Types

```java
Extension extension = AutolinkExtension.builder()
    .linkTypes(AutolinkType.URL)
    .build();
```

You can also pass a set:

```java
Extension extension = AutolinkExtension.builder()
    .linkTypes(EnumSet.of(AutolinkType.URL, AutolinkType.EMAIL))
    .build();
```

## Strikethrough

Strikethrough supports `~text~` or `~~text~~` depending on configuration.

### Default Behavior

```java
Extension extension = StrikethroughExtension.create();
```

### Requiring Two Tildes

```java
Extension extension = StrikethroughExtension.builder()
    .requireTwoTildes(true)
    .build();
```

That mode rejects single-tilde strikethrough and only accepts double-tilde markers.

## Tables

Tables use GitHub-style pipe table syntax.

### Basic Example

```java
Extension extension = TablesExtension.create();
```

Once enabled, pipe tables can be parsed and rendered through the supported output formats.

Example input:

```markdown
| Name | Role |
| --- | --- |
| Ada | Engineer |
| Lin | Writer |
```

## Task List Items

Task list items recognize checklist-style list syntax.

Example input:

```markdown
- [ ] Open issue
- [x] Ship fix
```

Enabling the extension makes the parser rewrite those list items into task-aware AST nodes so the
renderers can emit the correct checkbox output.

## Combining Extensions

Extensions are additive.

```java
Set<Extension> extensions =
    Set.of(
        AutolinkExtension.create(),
        StrikethroughExtension.create(),
        TablesExtension.create(),
        TaskListItemsExtension.create());
```

You can register the same set on:

- the parser
- the HTML renderer
- the Elemental2 renderer
- the plain-text renderer
- the Markdown renderer

## Extension Behavior By Renderer

Not every extension affects every renderer in the same way.

Examples:

- autolink changes parsing and therefore affects all renderers through the AST
- strikethrough has parser and renderer hooks
- tables have parser and renderer hooks
- task lists have post-processing plus renderer hooks

## Practical Advice

- Register extensions on the parser first, then on the renderers that need them.
- Use `ExtensionDiscovery.load()` when you want the built-in set without manually listing each
  extension.
- Use builder APIs when you need to tune extension behavior.

