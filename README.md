# domino-md

`domino-md` is a browser-safe Markdown engine for Java and GWT-based applications.

It provides:

- Markdown parsing into an AST
- HTML rendering
- Elemental2 DOM rendering
- plain-text rendering
- Markdown-to-Markdown rendering
- built-in extensions for autolink, strikethrough, tables, and task list items

## Quick Build

The project uses Maven and targets Java 11.

```bash
mvn test -q
```

Use this for the normal JVM + browser-path test flow.

```bash
mvn verify -q
```

Use this when you also want the GWT compile step.

## Documentation

### Start Here

- [User Docs](docs/user-docs/README.md)
- [Advanced User Docs](docs/advanced-user-docs/README.md)
- [Developer Docs](docs/developer-docs/README.md)

### User Docs

- [Getting Started](docs/user-docs/getting-started.md)
- [Parsing and ASTs](docs/user-docs/parsing-and-asts.md)
- [Rendering Output](docs/user-docs/rendering.md)
- [Extensions](docs/user-docs/extensions.md)
- [Configuration](docs/user-docs/configuration.md)
- [Examples](docs/user-docs/examples.md)

### Advanced User Docs

- [API Reference](docs/advanced-user-docs/api-reference.md)
- [Cookbook](docs/advanced-user-docs/cookbook.md)

### Developer Docs

- [Architecture](docs/developer-docs/architecture.md)
- [Package Map](docs/developer-docs/package-map.md)
- [Development Guide](docs/developer-docs/development-guide.md)

### Existing Notes

- [Current Status Summary](docs/current-status-summary.md)
- [Extensions Overview](docs/extensions.md)
- [Deferred Engine Features](docs/deferred-engine-features.md)

## Entry Points

- `org.dominokit.markdown.parser.Parser`
- `org.dominokit.markdown.renderer.html.HtmlRenderer`
- `org.dominokit.markdown.renderer.elemental2.Elemental2Renderer`
- `org.dominokit.markdown.renderer.text.TextContentRenderer`
- `org.dominokit.markdown.renderer.markdown.MarkdownRenderer`
- `org.dominokit.markdown.extensions.discovery.ExtensionDiscovery`

## Example

```java
Parser parser = Parser.builder().build();
HtmlRenderer renderer = HtmlRenderer.builder().build();

String html = renderer.render(parser.parse("Hello **world**"));
```

## License

Apache License 2.0

