# Elemental2 DOM Renderer Spec

Status date: 2026-06-25
Implementation status: implemented on 2026-06-25

## Purpose

Add a second renderer beside the existing HTML string renderer that renders the Markdown AST to
actual `elemental2.dom` nodes instead of HTML text.

This renderer belongs in `domino-md` because it is still a rendering concern over the shared
Markdown AST. The editor, toolbar, layout, and higher-level UI concerns remain deferred to the
`domino-ui` project.

## Goals

- render the existing CommonMark-compatible AST to real Elemental2 DOM nodes
- avoid `innerHTML` and avoid building the DOM by parsing generated HTML strings
- preserve the current parser and AST boundary
- keep URL sanitization, raw HTML policy, and extensibility explicit
- support GWT/browser compilation and execution in this repository

## Implementation Result

- implemented in `org.dominokit.markdown.renderer.elemental2`
- uses detached `DocumentFragment` output and direct `DomGlobal.document` node construction
- keeps the parser and AST free of Elemental2 dependencies
- verified through `MarkdownSuite` browser tests and full `mvn -q verify`

## Non-Goals

- editor widgets
- textarea handling
- preview panes or split layouts
- toolbar commands
- DOM diffing or incremental rendering
- Markdown-to-DOM mutation tracking
- custom browser event handling
- Domino UI wrappers

## Package Layout

The renderer should live in a dedicated package:

```text
org.dominokit.markdown.renderer.elemental2
```

Expected public types:

```text
Elemental2Renderer
ElementNodeRenderer
ElementNodeRendererContext
ElementNodeRendererFactory
ElementAttributeProvider
ElementAttributeProviderFactory
ElementAttributeProviderContext
RawHtmlHandler
SoftBreakRendering
```

Expected internal support:

```text
org.dominokit.markdown.internal.renderer
```

Reuse the existing node-dispatch support where practical. Avoid large generic refactors unless they
reduce duplication without destabilizing the existing HTML renderer.

## Public API

The Elemental2 renderer should not implement the current string `Renderer` interface because the
output type is different.

Initial API shape:

```java
public final class Elemental2Renderer {

    public static Builder builder();

    public elemental2.dom.DocumentFragment render(org.dominokit.markdown.node.Node node);

    public static final class Builder {
        public Builder softBreakRendering(SoftBreakRendering rendering);
        public Builder sanitizeUrls(boolean sanitizeUrls);
        public Builder urlSanitizer(org.dominokit.markdown.renderer.html.UrlSanitizer urlSanitizer);
        public Builder rawHtmlHandler(RawHtmlHandler rawHtmlHandler);
        public Builder attributeProviderFactory(ElementAttributeProviderFactory factory);
        public Builder nodeRendererFactory(ElementNodeRendererFactory factory);
        public Elemental2Renderer build();
    }
}
```

Notes:

- `render(...)` should always return a `DocumentFragment`
- callers are responsible for appending the fragment to the live DOM
- the renderer should never clear or mutate caller-owned containers
- reusing `renderer.html.UrlSanitizer` keeps URL policy aligned across both renderers

## Rendering Model

The renderer should construct DOM nodes directly through `DomGlobal.document`.

Required behavior:

- build under a detached `DocumentFragment`
- create `HTMLElement` and `Text` nodes directly
- never use `innerHTML` inside the renderer implementation
- never parse rendered HTML strings back into DOM nodes

## Node Mapping

Core node rendering should mirror the current HTML renderer as closely as possible.

Block nodes:

- `Document` -> append children to the root `DocumentFragment`
- `Paragraph` -> `<p>` unless omitted by tight-list or single-paragraph policy
- `Heading` -> `<h1>` through `<h6>`
- `BlockQuote` -> `<blockquote>`
- `BulletList` -> `<ul>`
- `OrderedList` -> `<ol>` with `start` when needed
- `ListItem` -> `<li>`
- `FencedCodeBlock` -> `<pre><code>`
- `IndentedCodeBlock` -> `<pre><code>`
- `ThematicBreak` -> `<hr>`
- `HtmlBlock` -> handled by raw HTML policy

Inline nodes:

- `Text` -> `Text` node
- `Emphasis` -> `<em>`
- `StrongEmphasis` -> `<strong>`
- `Code` -> `<code>`
- `Link` -> `<a>`
- `Image` -> `<img>`
- `SoftLineBreak` -> rendered according to `SoftBreakRendering`
- `HardLineBreak` -> `<br>`
- `HtmlInline` -> handled by raw HTML policy

Code block details:

- fenced code blocks with info strings should apply `class="language-<lang>"`
- only the first info token should become the language class, matching the HTML renderer

Image details:

- `alt` text extraction should match the current HTML renderer behavior
- entity-decoded inline text should appear in the `alt` attribute value

## Soft Break Policy

The Elemental2 renderer should expose an enum rather than a raw string:

```java
public enum SoftBreakRendering {
    NEWLINE_TEXT,
    SPACE_TEXT,
    BR_ELEMENT
}
```

Default:

- `NEWLINE_TEXT`

Rationale:

- this mirrors the current HTML renderer default most closely
- it preserves source-sensitive rendering behavior without forcing visual `<br>` output

## Raw HTML Policy

Raw HTML needs an explicit DOM policy because the renderer must not rely on `innerHTML`.

Default behavior:

- raw HTML is rendered as literal text content, not parsed into live DOM elements

Required hook:

```java
public interface RawHtmlHandler {
    elemental2.dom.Node renderInline(String literal);
    elemental2.dom.Node renderBlock(String literal);
}
```

Builder behavior:

- if no handler is provided, inline HTML becomes a text node and block HTML becomes a `<p>` with a
  text node, matching the current safe escaped mode
- if a handler is provided, it may return a custom node or `null`
- if the handler returns `null`, fall back to the safe literal-text behavior

Security note:

- any handler that parses raw HTML into DOM is explicitly caller-owned and unsafe by default
- the core renderer should not ship a built-in unsafe HTML parser

## URL Handling

The Elemental2 renderer should mirror the HTML renderer URL behavior:

- optional sanitization through `sanitizeUrls(true)`
- reusable `UrlSanitizer`
- percent-encoding should be preserved through the existing `Escaping.percentEncodeUrl(...)`

Required behavior:

- link `href` and image `src` must be sanitized before assignment when sanitization is enabled
- `rel="nofollow"` should be set on links when sanitization is enabled, matching the HTML renderer
- percent-encoding should be applied before assigning `href` or `src`

## Attribute Providers

The renderer should expose DOM-side attribute providers similar to the HTML renderer:

```java
public interface ElementAttributeProvider {
    void setAttributes(
        org.dominokit.markdown.node.Node node,
        String tagName,
        java.util.Map<String, String> attributes);
}
```

Behavior:

- default attributes are assembled first
- providers can add, replace, or remove attributes
- attributes are applied to DOM elements after provider mutation
- one fresh provider instance should be created per render pass

## Node Renderer Overrides

The renderer should support custom node renderer factories similar to the HTML renderer.

Required behavior:

- first-registered renderer for a node type wins
- core renderer is registered last
- custom renderers must be able to override core-node rendering and support custom nodes

Suggested contract:

```java
public interface ElementNodeRenderer {
    java.util.Set<Class<? extends org.dominokit.markdown.node.Node>> getNodeTypes();
    elemental2.dom.Node render(
        org.dominokit.markdown.node.Node node,
        ElementNodeRendererContext context);
}
```

Context responsibilities:

- render child nodes
- extend attributes
- create elements and text nodes through the shared document
- expose soft-break policy and raw-HTML policy

## Dependency Boundary

This new renderer will introduce an Elemental2 dependency into this repository, but it must remain
isolated to the new renderer package.

Rules:

- parser, AST, and HTML renderer packages must not gain Elemental2 imports
- Elemental2-specific code must stay under `renderer.elemental2` or tightly-scoped support code
- if this repository is later split into multiple artifacts, this package should be movable into an
  optional `domino-md-elemental2` artifact with minimal churn

## Testing Strategy

The Elemental2 renderer should be validated primarily through the browser toolchain already added in
Phase 7.

Required tests:

- GWT/browser-side rendering tests for representative block and inline structures
- URL sanitization tests
- raw HTML default-literal behavior tests
- attribute provider tests
- node renderer override tests
- alt-text extraction tests

Recommended comparison strategy:

- render the same AST with the HTML renderer and the Elemental2 renderer
- serialize the Elemental2 fragment in test code only
- compare the serialized DOM structure against the expected HTML output for supported cases

## Acceptance Criteria

The phase is complete when all of the following are true:

- `Elemental2Renderer` renders the supported core AST nodes to `DocumentFragment`
- the renderer does not use `innerHTML`
- browser-side GWT tests pass for the new renderer
- `mvn verify` remains green
- parser/core conformance stays green
- the existing HTML renderer behavior is unchanged
- the editor phases remain deferred out of this repository

## Implementation Order

Recommended task order:

1. Add `renderer.elemental2` package and builder skeleton
2. Add minimal fragment rendering for `Document`, `Paragraph`, `Text`, `Heading`
3. Add link, image, emphasis, strong emphasis, code, and line-break support
4. Add list, block quote, thematic break, and code-block support
5. Add raw HTML policy and URL sanitizer integration
6. Add attribute providers and node renderer overrides
7. Add browser-side tests and parity checks
8. Update compatibility and scope docs after implementation

## Relationship To Deferred Editor Work

This phase does not replace the deferred editor work. It only supplies a DOM-native rendering
primitive that a future `domino-ui` editor can consume.

Deferred to `domino-ui`:

- editor widgets
- toolbar behavior
- preview layouts
- interaction model
- application styling
- Domino component integration
