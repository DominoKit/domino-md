# Extensions

Status date: 2026-06-25

## Available extensions

The engine now ships these optional extensions:

- `org.dominokit.markdown.ext.gfm.strikethrough.StrikethroughExtension`
- `org.dominokit.markdown.ext.task.list.items.TaskListItemsExtension`
- `org.dominokit.markdown.ext.gfm.tables.TablesExtension`
- `org.dominokit.markdown.ext.autolink.AutolinkExtension`

## Registration

Extensions are registered explicitly on each builder. The same extension set can be reused across
the parser, HTML renderer, and Elemental2 DOM renderer:

```java
Set<Extension> extensions =
    Set.of(
        StrikethroughExtension.create(),
        TaskListItemsExtension.create(),
        TablesExtension.create(),
        AutolinkExtension.create());

Parser parser = Parser.builder().extensions(extensions).build();

HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();

Elemental2Renderer domRenderer =
    Elemental2Renderer.builder().extensions(extensions).build();
```

## Bundled discovery

The engine also exposes a browser-safe helper for the bundled extension set:

```java
List<Extension> extensions = ExtensionDiscovery.load();

Parser parser = Parser.builder().extensions(extensions).build();
HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();
Elemental2Renderer domRenderer = Elemental2Renderer.builder().extensions(extensions).build();
```

## Notes

- Strikethrough supports `~text~` and `~~text~~` by default.
- Task list items render disabled checkbox inputs in both HTML and Elemental2 DOM output.
- Tables use the GitHub-style pipe table syntax with alignment markers.
- Autolink turns plain URLs, email addresses, and `www.` links into normal `Link` nodes during a
  parser post-processing pass.
- `ExtensionDiscovery.load()` returns the bundled engine extensions in deterministic order.
- Multi-artifact browser aggregation is still a higher-level build concern because a discovery
  loader generated inside this artifact cannot see extensions introduced later by a consuming
  application build.
