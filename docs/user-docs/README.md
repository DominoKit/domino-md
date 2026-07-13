# User Documentation

This folder is the usage guide for people who want to consume `domino-md` from application code.
It is written for the typical path a new user will take:

1. parse Markdown
2. inspect the AST
3. render to HTML, DOM, text, or Markdown
4. enable extensions
5. tune configuration

## Recommended Reading Order

1. [Getting Started](getting-started.md)
2. [Parsing and ASTs](parsing-and-asts.md)
3. [Rendering Output](rendering.md)
4. [Extensions](extensions.md)
5. [Configuration](configuration.md)
6. [Examples](examples.md)

## What You Can Do With `domino-md`

- turn Markdown strings into an AST
- render the AST into HTML
- render the AST into Elemental2 DOM nodes
- render the AST into plain text
- render the AST back to canonical Markdown
- enable GitHub-flavored extensions such as tables, task lists, strikethrough, and autolinks
- build custom parsing or rendering behavior when the built-in behavior is not enough

## Entry Points

- `org.dominokit.markdown.parser.Parser`
- `org.dominokit.markdown.renderer.html.HtmlRenderer`
- `org.dominokit.markdown.renderer.elemental2.Elemental2Renderer`
- `org.dominokit.markdown.renderer.text.TextContentRenderer`
- `org.dominokit.markdown.renderer.markdown.MarkdownRenderer`
- `org.dominokit.markdown.extensions.discovery.ExtensionDiscovery`

## Example Progression

The docs intentionally move from simple to advanced:

- first parse and render a paragraph
- then inspect headings, links, lists, and children
- then enable built-in extensions
- then customize rendering behavior
- then add your own parser or renderer hooks

That progression matters because the API is designed to stay simple for basic use while still
allowing advanced customization when you need it.

