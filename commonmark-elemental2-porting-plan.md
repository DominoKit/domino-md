# Full Implementation Plan: Porting `commonmark-java` to a GWT/J2CL-Compatible Core and Building an Elemental2 Markdown Editor

**Document purpose:**  
This document provides a deep implementation plan for creating a client-side Markdown solution for Elemental2-based applications by separating the project into two independent concerns:

1. A **pure Java, GWT/J2CL-compatible CommonMark parser and renderer**.
2. An **Elemental2 Markdown editor UI layer** that consumes the parser/renderer through stable interfaces.

The most important architectural rule is:

> The Markdown parser and renderer must not depend on Elemental2, browser DOM APIs, Domino UI, or any frontend component framework.

The parser/renderer should be a reusable Java library. The Elemental2 editor should be only one consumer of that library.

---

## 1. Executive Summary

The proposed project should be treated as two related but separate products.

### Product A — Markdown Engine

A pure Java Markdown engine compatible with GWT/J2CL.

Responsibilities:

- Parse Markdown text.
- Build a Markdown AST.
- Render AST to safe or configurable HTML.
- Optionally render AST back to Markdown.
- Optionally expose extension points for tables, task lists, strikethrough, autolinks, heading anchors, and custom blocks.
- Run against CommonMark conformance examples.
- Avoid JVM-only features that break in browser compilation.

This layer must not contain:

- Elemental2 APIs.
- Browser DOM APIs.
- `innerHTML`.
- UI components.
- Editor selection logic.
- Toolbar logic.
- Clipboard logic.
- File upload logic.
- Domino UI components.

### Product B — Elemental2 Editor

A browser-side Markdown editor component built with Elemental2.

Responsibilities:

- Provide the editing surface.
- Provide toolbar commands.
- Manage textarea selection.
- Provide live preview.
- Debounce rendering.
- Support edit-only, preview-only, and split view modes.
- Optionally provide scroll synchronization.
- Optionally provide Domino UI wrapper components.
- Optionally integrate a sanitizer hook before setting preview HTML.

This layer must not implement Markdown parsing directly.

It should depend on an abstraction like:

```java
public interface MarkdownRenderer {
    String render(String markdown);
}
```

The editor should not care whether the renderer is:

- The ported CommonMark renderer.
- A simplified renderer.
- A server-backed renderer.
- A JavaScript renderer exposed through JsInterop.
- A mocked renderer used by tests.

---

## 2. Source Project and Specification Anchors

The recommended base project is `commonmark-java`.

Relevant facts to use when shaping the port:

- `commonmark-java` provides classes for parsing Markdown input into an AST, visiting/manipulating nodes, and rendering to HTML or Markdown.
- It originally started as a port of `commonmark.js`.
- It exposes a parser/AST/renderer architecture, which is good for separating core parsing from UI concerns.
- CommonMark itself provides a standardized Markdown syntax and a comprehensive test suite that can be used to validate implementations.

Primary references:

- `commonmark-java`: https://github.com/commonmark/commonmark-java
- CommonMark specification: https://spec.commonmark.org/
- CommonMark project site: https://commonmark.org/
- CommonMark spec repository: https://github.com/commonmark/commonmark-spec

---

## 3. Top-Level Goals

### 3.1 Functional Goals

The engine should support, at minimum:

- Paragraphs
- ATX headings
- Setext headings
- Thematic breaks
- Block quotes
- Indented code blocks
- Fenced code blocks
- Ordered lists
- Unordered lists
- Nested lists
- Inline emphasis
- Strong emphasis
- Code spans
- Links
- Images
- Hard line breaks
- Soft line breaks
- HTML escaping
- Entity escaping
- Backslash escaping
- CommonMark-compatible delimiter behavior

The editor should support, at minimum:

- Textarea-based Markdown editing
- Live preview
- Preview refresh on input
- Debounced preview rendering
- Toolbar commands for common Markdown operations
- Selection wrapping
- Selection replacement
- Cursor preservation
- Basic keyboard shortcuts
- Configurable render pipeline
- Configurable sanitizer
- CSS-based layout modes

### 3.2 Non-Goals for the First Version

The first version should not attempt to provide:

- A full WYSIWYG Markdown editor.
- Rich DOM editing using `contenteditable`.
- Collaborative editing.
- Operational transform.
- CRDT support.
- Markdown syntax highlighting inside the textarea.
- Full GitHub Flavored Markdown compatibility.
- File/image upload handling.
- Mermaid rendering.
- Math rendering.
- Server-side storage.
- Real-time synchronization.
- Commenting/annotations.
- A full CMS.

These can be future layers, but including them in the first version will make the port much harder.

---

## 4. Core Architectural Principle

The most important boundary is this:

```text
Markdown source text
      |
      v
Pure Java Markdown parser
      |
      v
Pure Java Markdown AST
      |
      v
Pure Java renderer
      |
      v
HTML string
      |
      v
Optional sanitizer
      |
      v
Elemental2 preview container
```

The parser and renderer stop at producing strings or AST nodes.

The editor is responsible for:

- Calling the renderer.
- Applying sanitizer policy.
- Assigning HTML to preview DOM.
- Handling user interaction.

---

## 5. Proposed Maven/Gradle Module Layout

A clean multi-module project is recommended.

```text
markdown-parent
├── markdown-core
├── markdown-html
├── markdown-text
├── markdown-extension-api
├── markdown-extension-table
├── markdown-extension-task-list
├── markdown-extension-strikethrough
├── markdown-extension-autolink
├── markdown-test-common
├── markdown-gwt-tests
├── markdown-j2cl-tests
├── markdown-elemental2
├── markdown-domino-ui
└── markdown-demo
```

### 5.1 `markdown-core`

Pure Java only.

Contains:

- AST node classes
- Parser interfaces
- Block parser
- Inline parser
- Source scanner
- Delimiter stack
- Reference map
- Visitor API
- Node traversal
- Parser configuration
- Extension registration API

Must not depend on:

- Elemental2
- DOM APIs
- JsInterop
- Domino UI
- Servlet APIs
- JVM reflection
- file APIs

### 5.2 `markdown-html`

Pure Java HTML renderer.

Contains:

- `HtmlRenderer`
- HTML escaping utilities
- HTML node rendering handlers
- Renderer options
- Link/image policy hooks
- Raw HTML policy
- Attribute provider API

This module may depend on `markdown-core`.

It must not depend on Elemental2.

### 5.3 `markdown-text`

Optional pure Java Markdown/text renderer.

Contains:

- Markdown renderer
- Plain text renderer
- Optional AST-to-Markdown support

This is useful for:

- Round-trip tests
- Copy as Markdown
- Extracting text
- Search indexing
- Preview fallback

### 5.4 `markdown-extension-api`

Optional, but recommended if you want a clean extension model.

Contains:

- Extension interfaces
- Block parser extension SPI
- Inline parser extension SPI
- Renderer extension SPI
- Attribute provider extension SPI

### 5.5 `markdown-extension-*`

Each extension should be isolated.

Examples:

```text
markdown-extension-table
markdown-extension-task-list
markdown-extension-strikethrough
markdown-extension-autolink
markdown-extension-heading-anchor
```

Do not put all extensions in the core module.

Reason:

- Keeps core smaller.
- Makes GWT/J2CL output smaller.
- Lets users include only what they need.
- Makes compatibility easier to test.

### 5.6 `markdown-elemental2`

Elemental2 browser UI layer.

Contains:

- `MarkdownEditor`
- `MarkdownPreview`
- `MarkdownToolbar`
- `MarkdownTextArea`
- `MarkdownEditorConfig`
- `MarkdownEditorMode`
- `MarkdownCommand`
- `MarkdownCommandRegistry`
- `MarkdownRenderer` abstraction
- `MarkdownSanitizer` abstraction
- Event/listener APIs

Depends on:

- `markdown-core` only if it needs AST-related types
- preferably only `markdown-html` through a renderer implementation
- Elemental2

Should not contain parser internals.

### 5.7 `markdown-domino-ui`

Optional wrapper for Domino UI users.

Contains:

- Domino component wrapper
- Domino styling integration
- Domino events
- Form integration
- Validation integration
- Theme integration

Depends on:

- `markdown-elemental2`
- Domino UI

Should not depend directly on parser internals.

### 5.8 `markdown-demo`

Demo browser application.

Contains:

- Sample editor page
- Split preview mode
- Toolbar demo
- Raw HTML policy demo
- Extension demo
- Large-document demo
- RTL/LTR demo
- Theme demo

---

## 6. Compatibility Audit Plan

Before porting, audit the source library in detail.

### 6.1 Search for JVM-Only APIs

Inspect the source code for:

```text
java.io
java.nio
java.lang.reflect
java.util.ServiceLoader
java.util.Properties
java.util.ResourceBundle
java.util.Locale
java.util.regex
java.util.concurrent
Thread
ThreadLocal
ClassLoader
System.getProperty
System.getenv
Objects.requireNonNullElse
String.formatted
```

Not all of these are automatically forbidden, but each should be reviewed.

### 6.2 GWT/J2CL Risk Levels

Classify every finding.

#### Low Risk

Usually acceptable:

```text
java.util.List
java.util.Map
java.util.Set
StringBuilder
String
Character
Collections
Arrays
Iterator
```

#### Medium Risk

Needs verification:

```text
java.util.regex.Pattern
Locale
Comparator behavior
LinkedHashMap ordering
Unicode handling
String.format
Throwable stack traces
```

#### High Risk

Avoid or replace:

```text
reflection
ServiceLoader
ClassLoader resources
java.io.File
java.nio.file
ThreadLocal
synchronized concurrency design
WeakHashMap if used in tricky ways
finalization
native image-specific behavior
```

### 6.3 Compatibility Report Output

Create a document like:

```text
compatibility-audit.md
```

For each issue:

```text
File:
Line:
API:
Risk:
Used for:
GWT/J2CL status:
Replacement strategy:
Decision:
```

Example:

```text
File: Parser.java
API: java.util.ServiceLoader
Risk: High
Used for: auto-loading extensions
Replacement: explicit builder registration
Decision: remove ServiceLoader path from browser build
```

---

## 7. Porting Strategy

There are three possible strategies.

### 7.1 Strategy A — Fork and Modify

Fork `commonmark-java` and modify it directly.

Pros:

- Fastest start.
- Keeps original structure.
- Easy to compare with upstream.

Cons:

- Upstream merges may become difficult.
- Browser-specific changes may pollute the original architecture.

Use this if speed matters.

### 7.2 Strategy B — Extract and Repackage

Copy only required modules/classes into a new project.

Pros:

- Clean browser-first codebase.
- Remove unused JVM code early.
- Easier to simplify APIs.

Cons:

- More work.
- Harder to track upstream changes.

Use this if you want a long-lived GWT/J2CL-friendly library.

### 7.3 Strategy C — Compatibility Layer

Keep most source code unchanged and provide compatibility replacements.

Pros:

- Easier upstream tracking.
- Less code modification.

Cons:

- May become awkward.
- Compatibility shims can hide problems.

Use this only if the original source is already very close to compatible.

### 7.4 Recommended Approach

Start with Strategy A for discovery, then move toward Strategy B.

Suggested flow:

```text
1. Fork commonmark-java.
2. Build only core + HTML renderer.
3. Run JVM tests.
4. Try J2CL/GWT compilation.
5. Patch compatibility issues.
6. Extract browser-safe modules into a clean project.
7. Keep a mapping document from original classes to ported classes.
```

---

## 8. Core Parser/Renderer Porting Plan

This section covers only the parser/renderer. No Elemental2 code belongs here.

### 8.1 Identify Minimal Core Surface

Initial required packages likely include:

```text
org.commonmark.node
org.commonmark.parser
org.commonmark.parser.block
org.commonmark.parser.inline
org.commonmark.renderer
org.commonmark.renderer.html
org.commonmark.internal
```

Initial classes to preserve conceptually:

```text
Parser
Parser.Builder
Node
Document
Block
Paragraph
Heading
Text
Emphasis
StrongEmphasis
Code
FencedCodeBlock
IndentedCodeBlock
Link
Image
ListBlock
BulletList
OrderedList
ListItem
BlockQuote
ThematicBreak
HtmlRenderer
HtmlRenderer.Builder
NodeRenderer
NodeRendererContext
AttributeProvider
```

Exact class names can remain the same if licensing and package strategy allow it.

### 8.2 Decide Package Names

Options:

#### Keep original package

```java
org.commonmark
```

Pros:

- Easier migration.
- Existing examples work.

Cons:

- Can conflict with normal JVM dependency.
- Users may accidentally mix dependencies.

#### Use new package

```java
org.dominokit.markdown
```

or:

```java
dev.dominokit.markdown
```

Pros:

- No conflict.
- Clearly browser-compatible fork.
- Better branding if published separately.

Cons:

- More changes.
- Existing docs/examples need adaptation.

#### Recommendation

Use a new package for the browser-compatible port if this becomes a published library.

Example:

```java
org.dominokit.markdown
```

or:

```java
org.dominokit.commonmark
```

### 8.3 Parser API Design

Keep a builder-based API.

```java
Parser parser = Parser.builder()
        .extensions(List.of())
        .build();

Node document = parser.parse(markdown);
```

For browser compatibility, avoid APIs that require varargs of complex extension types if they increase generated code size unnecessarily. Varargs are fine technically, but builder methods with lists may be clearer.

Suggested API:

```java
public final class Parser {

    public static Builder builder() {
        return new Builder();
    }

    public Node parse(String input) {
        // parse input
    }

    public static final class Builder {
        public Builder extension(Extension extension) {}
        public Builder extensions(List<? extends Extension> extensions) {}
        public Parser build() {}
    }
}
```

### 8.4 AST Design

AST nodes should remain plain Java objects.

Base node:

```java
public abstract class Node {

    private Node firstChild;
    private Node lastChild;
    private Node next;
    private Node previous;
    private Node parent;

    public Node getFirstChild() {}
    public Node getLastChild() {}
    public Node getNext() {}
    public Node getPrevious() {}
    public Node getParent() {}

    public void appendChild(Node child) {}
    public void prependChild(Node child) {}
    public void insertAfter(Node sibling) {}
    public void insertBefore(Node sibling) {}
    public void unlink() {}

    public abstract void accept(Visitor visitor);
}
```

This model is GWT/J2CL friendly.

Avoid:

- Reflection-based visitor dispatch.
- Dynamic class lookup.
- Annotation processing requirements at runtime.

### 8.5 Visitor API

Use normal static dispatch through `accept`.

```java
public interface Visitor {
    void visit(Document document);
    void visit(Paragraph paragraph);
    void visit(Heading heading);
    void visit(Text text);
    void visit(Emphasis emphasis);
    void visit(StrongEmphasis strongEmphasis);
    void visit(Link link);
    void visit(Image image);
}
```

For extensibility, provide a fallback:

```java
void visit(CustomNode customNode);
```

or:

```java
void visit(Node node);
```

But be careful: too much generic dispatch may make rendering harder.

### 8.6 Source Scanner

The parser should use a source scanner abstraction rather than raw repeated substring operations.

Goals:

- Avoid too much allocation.
- Make line/column tracking possible later.
- Keep Unicode behavior predictable.
- Make tests easier.

Possible interface:

```java
final class SourceScanner {
    private final String input;
    private int index;

    boolean hasNext();
    char peek();
    char next();
    boolean match(char c);
    boolean match(String s);
    String substring(int start, int end);
    int position();
    void position(int position);
}
```

For line-based block parsing, use a line abstraction:

```java
final class SourceLine {
    private final String content;
    private final int lineNumber;
}
```

### 8.7 Block Parsing

The block parser should recognize:

- Document
- Paragraph
- Heading
- Thematic break
- Block quote
- List item
- Ordered list
- Bullet list
- Indented code block
- Fenced code block
- HTML block if supported
- Link reference definitions

Suggested block parsing flow:

```text
1. Normalize line endings.
2. Split input into lines.
3. Iterate lines.
4. Maintain stack of open block parsers.
5. Try continuing existing blocks.
6. Try starting new blocks.
7. Add paragraph text when no block matches.
8. Finalize open blocks.
9. Run inline parser over inline-containing blocks.
```

### 8.8 Inline Parsing

Inline parsing should handle:

- Text
- Escapes
- Entities
- Code spans
- Emphasis delimiters
- Strong delimiters
- Links
- Images
- Autolinks if enabled
- Raw HTML inline if enabled
- Hard line breaks
- Soft line breaks

Use a delimiter processor model.

```java
public interface DelimiterProcessor {
    char getOpeningCharacter();
    char getClosingCharacter();
    int getMinLength();
    int process(DelimiterRun openingRun, DelimiterRun closingRun);
}
```

This helps extensions add strikethrough or custom delimiters.

### 8.9 Reference Link Handling

Markdown references require a document-level reference map.

Example:

```markdown
[hello][id]

[id]: https://example.com "Title"
```

Parser should collect:

```java
public final class LinkReferenceDefinition {
    private final String label;
    private final String destination;
    private final String title;
}
```

Normalize labels according to CommonMark rules.

Be careful with:

- Case-insensitive matching
- Whitespace normalization
- Unicode case folding differences between JVM and JavaScript

For browser compatibility, implement normalization explicitly.

### 8.10 HTML Rendering

HTML rendering should be separate from parsing.

Suggested API:

```java
HtmlRenderer renderer = HtmlRenderer.builder()
        .escapeHtml(true)
        .softbreak("\n")
        .sanitizeUrls(true)
        .build();

String html = renderer.render(document);
```

Options:

```java
public final class HtmlRendererOptions {
    private boolean escapeHtml;
    private boolean allowRawHtml;
    private boolean sanitizeUrls;
    private String softbreak;
    private UrlSanitizer urlSanitizer;
}
```

Default policy should be safe.

Recommended defaults for browser preview:

```text
escapeHtml: true
allowRawHtml: false
sanitizeUrls: true
softbreak: "\n"
```

### 8.11 URL Sanitization

Do not allow dangerous schemes by default.

Allowed:

```text
http
https
mailto
tel
relative URLs
fragment URLs
```

Blocked by default:

```text
javascript:
data:
vbscript:
file:
```

Suggested interface:

```java
public interface UrlSanitizer {
    String sanitizeLinkUrl(String url);
    String sanitizeImageUrl(String url);
}
```

Default behavior:

```text
If URL is allowed: return escaped URL.
If URL is blocked: return empty string or "#".
```

For images, you may choose a stricter default:

```text
allow http
allow https
allow relative
block data by default
```

### 8.12 Raw HTML Policy

CommonMark allows raw HTML, but a browser-side editor should be safe by default.

Define:

```java
public enum RawHtmlPolicy {
    ESCAPE,
    STRIP,
    ALLOW
}
```

Recommended default:

```java
RawHtmlPolicy.ESCAPE
```

If the user wants raw HTML preview:

```text
Markdown renderer may allow it,
but Elemental2 layer should still pass output through sanitizer.
```

### 8.13 Entity Handling

The parser/renderer should handle HTML entities carefully.

For rendering:

- Escape `&`
- Escape `<`
- Escape `>`
- Escape quotes in attributes
- Avoid double-escaping already-handled nodes

Utility:

```java
public final class HtmlEscaper {
    public static void escapeHtml(String input, Appendable out) {}
    public static void escapeHtmlAttribute(String input, Appendable out) {}
}
```

Avoid `Appendable` if it causes J2CL issues; use `StringBuilder` if simpler.

### 8.14 Renderer Node Handlers

Renderer should use static node renderer handlers.

```java
public interface NodeRenderer {
    Set<Class<? extends Node>> getNodeTypes();
    void render(Node node);
}
```

But `Class<? extends Node>` may be problematic if used heavily in J2CL.

Alternative browser-friendly API:

```java
public interface NodeRenderer {
    boolean supports(Node node);
    void render(Node node, HtmlWriter html);
}
```

This avoids map lookups by class.

However, it is slower.

A compromise:

```java
public interface NodeType {
    String name();
}
```

Each node returns a stable type:

```java
public String getNodeType() {
    return "paragraph";
}
```

Then renderers use string keys.

Recommendation:

- For simplicity, keep class-based approach first if J2CL accepts it.
- If generated output or compatibility becomes bad, switch to explicit node type IDs.

---

## 9. Extension Architecture

Do not port all extensions at once.

### 9.1 Core First

Core CommonMark must compile and pass tests before extensions.

### 9.2 Extension Types

Support three extension kinds:

```java
public interface ParserExtension extends Extension {
    void extend(Parser.Builder builder);
}

public interface HtmlRendererExtension extends Extension {
    void extend(HtmlRenderer.Builder builder);
}

public interface MarkdownExtension extends ParserExtension, HtmlRendererExtension {
}
```

But multiple inheritance may be awkward. Simpler:

```java
public interface Extension {
    void extend(MarkdownExtensionContext context);
}
```

Where context exposes:

```java
public interface MarkdownExtensionContext {
    Parser.Builder parserBuilder();
    HtmlRenderer.Builder htmlRendererBuilder();
}
```

This can cause unwanted coupling between parser and renderer.

Better separation:

```java
public interface ParserExtension {
    void extend(Parser.Builder parserBuilder);
}

public interface HtmlRendererExtension {
    void extend(HtmlRenderer.Builder rendererBuilder);
}
```

### 9.3 No ServiceLoader

Do not auto-discover extensions with `ServiceLoader`.

Browser builds should use explicit registration:

```java
Parser parser = Parser.builder()
        .extension(TablesExtension.create())
        .extension(TaskListExtension.create())
        .build();
```

### 9.4 Extension Priority

Some extensions need ordering.

Provide optional priority:

```java
public interface Prioritized {
    int priority();
}
```

Default:

```java
int DEFAULT_PRIORITY = 100;
```

But avoid overengineering in the first version.

### 9.5 First Extensions to Add

Recommended order:

1. Strikethrough
2. Task lists
3. Tables
4. Autolinks
5. Heading anchors

Reason:

- Strikethrough is mostly inline.
- Task lists build on lists.
- Tables are more complex.
- Autolinks require URL/email detection.
- Heading anchors involve rendering options and slug generation.

---

## 10. Elemental2 Editor Architecture

This section covers only the editor/UI layer.

### 10.1 Editor Layer Goals

The editor should:

- Be lightweight.
- Be framework-friendly.
- Avoid parser internals.
- Work with any renderer implementation.
- Be easy to wrap in Domino UI.
- Be easy to style.
- Be safe by default.

### 10.2 Core Editor Classes

```text
MarkdownEditor
MarkdownEditorConfig
MarkdownEditorMode
MarkdownTextArea
MarkdownToolbar
MarkdownPreview
MarkdownCommand
MarkdownCommandRegistry
MarkdownSelection
MarkdownRenderer
MarkdownSanitizer
MarkdownRenderScheduler
MarkdownEditorEvents
```

### 10.3 `MarkdownRenderer` Interface

This interface belongs in the editor/integration layer, not necessarily in the parser core.

```java
public interface MarkdownRenderer {
    String render(String markdown);
}
```

A renderer adapter can wrap the pure Java parser:

```java
public final class CommonMarkMarkdownRenderer implements MarkdownRenderer {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public CommonMarkMarkdownRenderer(Parser parser, HtmlRenderer renderer) {
        this.parser = parser;
        this.renderer = renderer;
    }

    @Override
    public String render(String markdown) {
        Node document = parser.parse(markdown == null ? "" : markdown);
        return renderer.render(document);
    }
}
```

### 10.4 `MarkdownSanitizer` Interface

The editor should not hardcode sanitizer implementation.

```java
public interface MarkdownSanitizer {
    String sanitize(String html);
}
```

Default:

```java
public final class NoOpMarkdownSanitizer implements MarkdownSanitizer {
    @Override
    public String sanitize(String html) {
        return html == null ? "" : html;
    }
}
```

Safer default:

```java
public final class EscapeOnlyMarkdownSanitizer implements MarkdownSanitizer {
    @Override
    public String sanitize(String html) {
        return html == null ? "" : html;
    }
}
```

If the renderer already escapes raw HTML and sanitizes URLs, a no-op sanitizer may be acceptable for controlled usage, but the API should still allow a stronger sanitizer.

Optional JsInterop DOMPurify adapter:

```java
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "DOMPurify")
final class DOMPurify {
    static native String sanitize(String html);
}

public final class DomPurifyMarkdownSanitizer implements MarkdownSanitizer {
    @Override
    public String sanitize(String html) {
        return DOMPurify.sanitize(html == null ? "" : html);
    }
}
```

Keep this in a separate optional module if you want the core Elemental2 editor to avoid global JS dependencies.

### 10.5 Editor Config

```java
public final class MarkdownEditorConfig {

    private MarkdownRenderer renderer;
    private MarkdownSanitizer sanitizer;
    private MarkdownEditorMode mode;
    private boolean livePreview;
    private int renderDebounceMillis;
    private boolean toolbarEnabled;
    private boolean shortcutsEnabled;
    private boolean scrollSyncEnabled;
    private String initialValue;
    private String placeholder;
}
```

Defaults:

```text
mode: SPLIT
livePreview: true
renderDebounceMillis: 150
toolbarEnabled: true
shortcutsEnabled: true
scrollSyncEnabled: false
initialValue: ""
placeholder: "Write Markdown..."
```

### 10.6 Editor Modes

```java
public enum MarkdownEditorMode {
    EDIT_ONLY,
    PREVIEW_ONLY,
    SPLIT,
    TABBED
}
```

Start with:

```text
EDIT_ONLY
PREVIEW_ONLY
SPLIT
```

Add `TABBED` later.

### 10.7 DOM Structure

Suggested DOM structure:

```html
<div class="dm-markdown-editor dm-markdown-editor-split">
  <div class="dm-markdown-toolbar"></div>
  <div class="dm-markdown-body">
    <textarea class="dm-markdown-input"></textarea>
    <div class="dm-markdown-preview"></div>
  </div>
</div>
```

For preview-only:

```html
<div class="dm-markdown-editor dm-markdown-editor-preview-only">
  <div class="dm-markdown-preview"></div>
</div>
```

### 10.8 CSS Class Naming

Use stable class names:

```text
dm-markdown-editor
dm-markdown-toolbar
dm-markdown-body
dm-markdown-input
dm-markdown-preview
dm-markdown-button
dm-markdown-button-active
dm-markdown-hidden
dm-markdown-split
dm-markdown-edit-only
dm-markdown-preview-only
```

Do not hardcode colors in Java.

Expose CSS variables:

```css
.dm-markdown-editor {
  --dm-md-border-color: #ddd;
  --dm-md-background: #fff;
  --dm-md-preview-background: #fff;
  --dm-md-input-background: #fff;
  --dm-md-toolbar-background: #f7f7f7;
  --dm-md-text-color: #222;
  --dm-md-muted-text-color: #666;
  --dm-md-focus-color: #4f46e5;
}
```

### 10.9 Elemental2 Construction

```java
HTMLDivElement root = (HTMLDivElement) DomGlobal.document.createElement("div");
root.className = "dm-markdown-editor dm-markdown-editor-split";

HTMLDivElement toolbar = (HTMLDivElement) DomGlobal.document.createElement("div");
toolbar.className = "dm-markdown-toolbar";

HTMLDivElement body = (HTMLDivElement) DomGlobal.document.createElement("div");
body.className = "dm-markdown-body";

HTMLTextAreaElement input = (HTMLTextAreaElement) DomGlobal.document.createElement("textarea");
input.className = "dm-markdown-input";

HTMLDivElement preview = (HTMLDivElement) DomGlobal.document.createElement("div");
preview.className = "dm-markdown-preview";

body.appendChild(input);
body.appendChild(preview);
root.appendChild(toolbar);
root.appendChild(body);
```

### 10.10 Render Pipeline

```text
input event
  |
  v
schedule render
  |
  v
read textarea value
  |
  v
MarkdownRenderer.render(markdown)
  |
  v
MarkdownSanitizer.sanitize(html)
  |
  v
preview.innerHTML = sanitizedHtml
  |
  v
fire preview updated event
```

### 10.11 Render Scheduler

Use debouncing.

```java
public final class MarkdownRenderScheduler {

    private double timeoutId = -1;
    private final int delayMillis;
    private final Runnable task;

    public void schedule() {
        if (timeoutId != -1) {
            DomGlobal.clearTimeout(timeoutId);
        }

        timeoutId = DomGlobal.setTimeout(ignore -> {
            timeoutId = -1;
            task.run();
        }, delayMillis);
    }
}
```

For immediate render:

```java
public void renderNow() {
    if (timeoutId != -1) {
        DomGlobal.clearTimeout(timeoutId);
        timeoutId = -1;
    }
    task.run();
}
```

### 10.12 Editor Events

Provide events/listeners:

```java
public interface MarkdownEditorListener {
    void onValueChanged(String value);
    void onPreviewUpdated(String html);
    void onModeChanged(MarkdownEditorMode mode);
    void onCommandExecuted(MarkdownCommand command);
}
```

Or more granular:

```java
public interface ValueChangeListener {
    void onValueChanged(String oldValue, String newValue);
}
```

Avoid depending on Domino events in the base Elemental2 module.

---

## 11. Editor Commands

Editor commands should operate on textarea text and selection.

### 11.1 Command Interface

```java
public interface MarkdownCommand {
    String name();
    String label();
    String shortcut();
    void execute(MarkdownEditorContext context);
}
```

### 11.2 Editor Context

```java
public interface MarkdownEditorContext {
    String getValue();
    void setValue(String value);
    MarkdownSelection getSelection();
    void setSelection(int start, int end);
    void focus();
    void renderNow();
}
```

### 11.3 Selection Model

```java
public final class MarkdownSelection {
    private final int start;
    private final int end;
    private final String selectedText;

    public boolean isCollapsed() {
        return start == end;
    }
}
```

### 11.4 Basic Selection Utility

```java
public final class MarkdownTextOperations {

    public static TextEdit wrap(String value, int start, int end, String before, String after) {
        String selected = value.substring(start, end);
        String replacement = before + selected + after;
        String next = value.substring(0, start) + replacement + value.substring(end);

        return new TextEdit(
                next,
                start + before.length(),
                start + before.length() + selected.length()
        );
    }
}
```

### 11.5 TextEdit Result

```java
public final class TextEdit {
    private final String value;
    private final int selectionStart;
    private final int selectionEnd;
}
```

### 11.6 Commands to Implement First

```text
Bold
Italic
Inline code
Code block
Heading 1
Heading 2
Heading 3
Bullet list
Ordered list
Block quote
Insert link
Insert image
Horizontal rule
Undo
Redo
Preview toggle
Split toggle
```

### 11.7 Bold Command

Input:

```text
hello world
```

Selection:

```text
world
```

Output:

```text
hello **world**
```

Implementation:

```java
public final class BoldCommand implements MarkdownCommand {
    @Override
    public void execute(MarkdownEditorContext context) {
        MarkdownSelection selection = context.getSelection();
        TextEdit edit = MarkdownTextOperations.wrap(
                context.getValue(),
                selection.getStart(),
                selection.getEnd(),
                "**",
                "**"
        );
        context.setValue(edit.getValue());
        context.setSelection(edit.getSelectionStart(), edit.getSelectionEnd());
        context.focus();
        context.renderNow();
    }
}
```

### 11.8 Toggle Line Prefix Commands

For headings, lists, and quotes, line-based operations are needed.

Example bullet toggle:

```text
one
two
three
```

becomes:

```text
- one
- two
- three
```

If all selected lines already have `- `, remove it.

Utility:

```java
public static TextEdit toggleLinePrefix(
        String value,
        int selectionStart,
        int selectionEnd,
        String prefix
) {
    // 1. Expand selection to full lines.
    // 2. Split into lines.
    // 3. Detect whether all non-empty lines already have prefix.
    // 4. Add or remove prefix.
    // 5. Rebuild value.
    // 6. Return adjusted selection.
}
```

### 11.9 Link Command

For selected text:

```text
Dominokit
```

Output:

```text
[Dominokit](https://example.com)
```

If selection is empty:

```text
[link text](https://example.com)
```

Do not open dialogs in the base command if you want framework independence.

Better:

```java
public interface LinkInputProvider {
    LinkInput requestLink(String selectedText);
}
```

For the MVP, command can insert placeholder:

```text
[selected text](url)
```

### 11.10 Keyboard Shortcuts

Start with:

```text
Ctrl+B / Cmd+B -> bold
Ctrl+I / Cmd+I -> italic
Ctrl+K / Cmd+K -> link
Ctrl+Shift+7 -> ordered list
Ctrl+Shift+8 -> unordered list
Ctrl+Alt+1 -> heading 1
Ctrl+Alt+2 -> heading 2
Ctrl+Alt+3 -> heading 3
```

Elemental2 keydown handler:

```java
input.addEventListener("keydown", evt -> {
    KeyboardEvent keyEvent = (KeyboardEvent) evt;

    boolean mod = keyEvent.ctrlKey || keyEvent.metaKey;

    if (mod && "b".equalsIgnoreCase(keyEvent.key)) {
        keyEvent.preventDefault();
        commandRegistry.execute("bold", context);
    }
});
```

---

## 12. Preview Rendering Safety

### 12.1 Safe-by-Default Pipeline

Recommended default:

```text
Markdown
  -> parser
  -> AST
  -> HTML renderer with raw HTML escaped
  -> URL sanitizer
  -> optional DOM sanitizer
  -> preview.innerHTML
```

### 12.2 Why Sanitizer Still Exists

Even if your renderer is safe by default, exposing a sanitizer hook is useful because users may:

- Enable raw HTML.
- Add custom renderers.
- Add custom extensions.
- Render user-generated content.
- Support embedded media.
- Support custom HTML attributes.

### 12.3 Preview API

```java
public final class MarkdownPreview {

    private final HTMLDivElement element;
    private MarkdownSanitizer sanitizer;

    public void setHtml(String html) {
        element.innerHTML = sanitizer.sanitize(html == null ? "" : html);
    }

    public HTMLElement element() {
        return element;
    }
}
```

### 12.4 Strict Preview Mode

Offer a strict mode:

```java
MarkdownEditorConfig config = MarkdownEditorConfig.builder()
        .strictPreview(true)
        .build();
```

Strict mode means:

```text
raw HTML disabled
unknown URLs blocked
image data URLs blocked
target blank gets rel noopener noreferrer
```

---

## 13. GWT/J2CL Testing Strategy

Testing must happen in layers.

### 13.1 JVM Unit Tests

These are fastest.

Test:

- AST operations
- Block parsing
- Inline parsing
- HTML rendering
- Escaping
- URL sanitizer
- Extension behavior
- Editor text operations that are pure Java

### 13.2 CommonMark Spec Tests

Use CommonMark spec examples as conformance tests.

Test format:

```text
markdown input
expected HTML
section name
example number
```

Test runner:

```java
@Test
public void commonMarkExample123() {
    assertRendering(input, expectedHtml);
}
```

Generate tests if possible.

### 13.3 Browser/J2CL Tests

Use browser-based tests for:

- Parser compiles under J2CL.
- Parser output matches expected HTML in browser runtime.
- Unicode behavior matches JVM tests.
- Regex behavior, if used, matches JVM tests.
- Editor input events work.
- Preview updates work.
- Selection operations work in real browser.
- Keyboard shortcuts work.

### 13.4 GWT Compatibility Tests

If supporting classic GWT too:

- Compile a minimal GWT app using the engine.
- Run parser tests in GWTTestCase or equivalent setup.
- Verify generated JS does not fail at runtime.

### 13.5 Golden Tests

Keep golden Markdown files:

```text
src/test/resources/golden/basic.md
src/test/resources/golden/basic.html
src/test/resources/golden/lists.md
src/test/resources/golden/lists.html
src/test/resources/golden/links.md
src/test/resources/golden/links.html
```

### 13.6 Fuzz Tests

Optional but useful.

Generate random Markdown-ish input and ensure:

- Parser does not crash.
- Renderer does not crash.
- Output is valid string.
- Runtime stays reasonable.

---

## 14. Performance Plan

### 14.1 Performance Targets

Initial targets:

```text
Small document, under 10 KB: render under 20 ms
Medium document, around 100 KB: render under 150 ms
Large document, around 500 KB: render under 800 ms
Typing debounce: 100-250 ms
No render on every key if previous render is pending
```

Exact numbers depend on browser/device, but set targets early.

### 14.2 Avoid Excessive Allocation

Review:

- Repeated substring creation
- Repeated regex matching
- Temporary lists
- Per-character object creation
- Recursive traversal depth for large documents

### 14.3 StringBuilder Usage

Rendering should use `StringBuilder`.

```java
StringBuilder out = new StringBuilder();
```

Avoid:

```java
html += ...
```

inside loops.

### 14.4 Incremental Parsing

Do not implement incremental parsing in v1.

It is complex and not required for a useful editor.

Use debounced full-document rendering first.

### 14.5 Optional Worker Rendering

Future enhancement:

- Compile parser into browser JS.
- Run rendering inside a Web Worker.
- Pass Markdown in.
- Return HTML string.
- Update preview on main thread.

This is not needed for v1.

---

## 15. API Design: Public Surface

### 15.1 Core API

```java
Parser parser = Parser.builder().build();
Node document = parser.parse(markdown);
```

### 15.2 HTML Rendering API

```java
HtmlRenderer renderer = HtmlRenderer.builder()
        .rawHtmlPolicy(RawHtmlPolicy.ESCAPE)
        .urlSanitizer(DefaultUrlSanitizer.create())
        .build();

String html = renderer.render(document);
```

### 15.3 One-Step API

Convenience:

```java
Markdown markdown = Markdown.builder().build();
String html = markdown.renderToHtml(input);
```

But keep this convenience class thin.

```java
public final class Markdown {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public String renderToHtml(String markdown) {
        return renderer.render(parser.parse(markdown));
    }
}
```

### 15.4 Editor API

```java
MarkdownEditor editor = MarkdownEditor.create(
        MarkdownEditorConfig.builder()
                .renderer(commonMarkRenderer)
                .sanitizer(sanitizer)
                .mode(MarkdownEditorMode.SPLIT)
                .build()
);

document.body.appendChild(editor.element());
```

### 15.5 Value API

```java
editor.setValue("# Hello");
String markdown = editor.getValue();
String html = editor.getRenderedHtml();
editor.renderNow();
```

### 15.6 Listener API

```java
editor.addValueChangeListener((oldValue, newValue) -> {
    // handle change
});

editor.addPreviewUpdateListener(html -> {
    // handle preview update
});
```

---

## 16. Implementation Phases

### Phase 0 — Repository Setup

Tasks:

- Create repository.
- Add root build.
- Add license files.
- Add module skeletons.
- Add formatting rules.
- Add CI build.
- Add basic README.
- Add package naming decision.
- Add source attribution notes if porting code.

Deliverables:

```text
repository builds
empty modules compile
README explains separation of concerns
```

### Phase 1 — Compatibility Audit

Tasks:

- Clone/fork commonmark-java.
- Identify minimal core and HTML renderer classes.
- Search for unsupported APIs.
- Produce compatibility audit.
- Decide replacement strategy for every issue.
- Decide whether to preserve package names.

Deliverables:

```text
compatibility-audit.md
port-class-map.md
risk list
v1 scope list
```

### Phase 2 — Core AST Port

Tasks:

- Port node hierarchy.
- Port visitor API.
- Port child/sibling manipulation.
- Port basic AST tests.
- Verify JVM tests.

Deliverables:

```text
markdown-core compiles
AST tests pass
no Elemental2 dependency
```

### Phase 3 — Block Parser Port

Tasks:

- Port document parser.
- Port paragraph parser.
- Port heading parser.
- Port thematic break parser.
- Port block quote parser.
- Port list parser.
- Port code block parser.
- Port fenced code block parser.
- Add block-level tests.

Deliverables:

```text
basic block Markdown parses to AST
block tests pass on JVM
```

### Phase 4 — Inline Parser Port

Tasks:

- Port text parsing.
- Port escapes.
- Port entities.
- Port code spans.
- Port emphasis.
- Port strong emphasis.
- Port links.
- Port images.
- Port line breaks.
- Add inline tests.

Deliverables:

```text
core inline syntax parses
inline tests pass on JVM
```

### Phase 5 — HTML Renderer Port

Tasks:

- Port HTML renderer.
- Port HTML writer.
- Add escaping.
- Add raw HTML policy.
- Add URL sanitizer.
- Add attribute provider support.
- Add renderer tests.

Deliverables:

```text
Markdown renders to HTML
safe defaults implemented
renderer tests pass
```

### Phase 6 — CommonMark Conformance

Tasks:

- Import CommonMark examples.
- Build test runner.
- Mark known failures.
- Fix failures by section.
- Track pass percentage.

Deliverables:

```text
commonmark compliance report
known-failures.md
core pass rate target reached
```

Suggested first target:

```text
80%+ examples passing
```

Then:

```text
95%+ examples passing
```

Final:

```text
as close to full conformance as practical
```

### Phase 7 — J2CL/GWT Compilation

Tasks:

- Add J2CL test module.
- Compile core + HTML renderer.
- Fix incompatible APIs.
- Run browser tests.
- Compare JVM/browser outputs.

Deliverables:

```text
core compiles to JS
browser tests pass
compatibility gaps documented
```

### Phase 8 — Elemental2 Editor MVP

Tasks:

- Create editor root component.
- Create textarea component.
- Create preview component.
- Wire renderer interface.
- Wire sanitizer interface.
- Implement debounced render.
- Add split layout.
- Add CSS.
- Add simple demo.

Deliverables:

```text
Markdown editor works in browser
live preview works
no parser internals leaked into editor
```

### Phase 9 — Toolbar Commands

Tasks:

- Create command API.
- Implement bold.
- Implement italic.
- Implement inline code.
- Implement code block.
- Implement headings.
- Implement bullet list.
- Implement ordered list.
- Implement quote.
- Implement link.
- Implement image.
- Implement horizontal rule.
- Add shortcut support.

Deliverables:

```text
toolbar functional
selection preservation works
command tests pass
```

### Phase 10 — Editor Enhancements

Tasks:

- Add mode switching.
- Add preview-only.
- Add edit-only.
- Add tabbed mode if needed.
- Add scroll sync.
- Add placeholder support.
- Add disabled/read-only support.
- Add resize behavior.
- Add RTL support.
- Add accessibility attributes.

Deliverables:

```text
usable editor component
configurable modes
basic accessibility support
```

### Phase 11 — Extensions

Tasks:

- Add extension API.
- Port/add strikethrough.
- Port/add task list.
- Port/add tables.
- Port/add autolinks.
- Add extension tests.
- Add extension demo.

Deliverables:

```text
extension modules compile separately
extensions work in parser + renderer
browser tests pass
```

### Phase 12 — Domino UI Wrapper

Tasks:

- Create Domino component wrapper.
- Add fluent API.
- Add event integration.
- Add form field integration if needed.
- Add validation integration if needed.
- Add theme classes.

Deliverables:

```text
Domino UI component wrapper
demo in Domino app
```

### Phase 13 — Documentation and Release

Tasks:

- Write README.
- Write getting started guide.
- Write architecture guide.
- Write parser guide.
- Write editor guide.
- Write security guide.
- Write migration guide.
- Publish snapshot.
- Publish release.

Deliverables:

```text
documented library
release artifacts
demo page
```

---

## 17. Detailed Work Breakdown

### 17.1 Parser/Renderer Tasks

```text
P001 Create markdown-core module
P002 Create markdown-html module
P003 Port Node base class
P004 Port Document node
P005 Port Block node classes
P006 Port Inline node classes
P007 Port Visitor interface
P008 Add AST manipulation tests
P009 Port parser builder
P010 Port block parser interfaces
P011 Port paragraph parsing
P012 Port heading parsing
P013 Port thematic break parsing
P014 Port block quote parsing
P015 Port list parsing
P016 Port code block parsing
P017 Port fenced code block parsing
P018 Port link reference parsing
P019 Port inline parser
P020 Port delimiter stack
P021 Port emphasis processing
P022 Port code span parsing
P023 Port link parsing
P024 Port image parsing
P025 Port entity handling
P026 Port HTML renderer
P027 Add raw HTML policy
P028 Add URL sanitizer
P029 Add attribute provider API
P030 Add CommonMark test runner
P031 Fix conformance failures
P032 Add J2CL compile test
P033 Add browser output comparison tests
```

### 17.2 Editor Tasks

```text
E001 Create markdown-elemental2 module
E002 Add MarkdownRenderer interface
E003 Add MarkdownSanitizer interface
E004 Add MarkdownEditorConfig
E005 Add MarkdownEditorMode
E006 Build root DOM structure
E007 Build textarea component
E008 Build preview component
E009 Implement debounced rendering
E010 Implement setValue/getValue
E011 Implement renderNow
E012 Implement editor events
E013 Add split mode CSS
E014 Add edit-only mode
E015 Add preview-only mode
E016 Add toolbar container
E017 Add command registry
E018 Implement bold command
E019 Implement italic command
E020 Implement inline code command
E021 Implement heading commands
E022 Implement list commands
E023 Implement quote command
E024 Implement link command
E025 Implement image command
E026 Implement keyboard shortcuts
E027 Add selection operation tests
E028 Add browser editor tests
E029 Add demo app
E030 Add accessibility pass
```

---

## 18. Compatibility Replacement Examples

### 18.1 Replacing `ServiceLoader`

Do not use:

```java
ServiceLoader.load(Extension.class)
```

Use:

```java
Parser.builder()
        .extension(MyExtension.create())
        .build();
```

### 18.2 Replacing Classpath Resource Loading

Do not load tests/spec from classpath in browser runtime.

For tests:

- JVM test runner can load resources.
- Browser tests should embed generated test data into Java source.
- Or use test framework resource support if available.

### 18.3 Replacing `String.format`

Avoid:

```java
String.format("Expected %s but got %s", expected, actual)
```

Use:

```java
"Expected " + expected + " but got " + actual
```

This avoids locale and formatting compatibility issues.

### 18.4 Regex Minimization

If regex behavior differs or output size becomes too large, replace regex-heavy parsing with scanner logic.

Example:

Instead of:

```java
Pattern.compile("^#{1,6}\\s+")
```

Use:

```java
int count = 0;
while (count < line.length() && count < 6 && line.charAt(count) == '#') {
    count++;
}
```

Markdown parsing often benefits from manual scanning anyway.

---

## 19. Security Plan

### 19.1 Threat Model

Markdown input may be:

- Trusted application text.
- Semi-trusted admin text.
- Fully user-generated content.
- Pasted from unknown sources.

Risks:

- Script injection through raw HTML.
- Dangerous links.
- Dangerous image URLs.
- Attribute injection.
- Extension-generated unsafe HTML.
- Preview using `innerHTML`.

### 19.2 Default Security Mode

Default:

```text
Raw HTML: escaped
Dangerous URLs: blocked
HTML attributes: escaped
Preview sanitizer hook: enabled/configurable
```

### 19.3 Link Rendering

For external links:

```html
<a href="..." rel="noopener noreferrer">...</a>
```

If `target="_blank"` is enabled, always add:

```text
rel="noopener noreferrer"
```

### 19.4 Image Rendering

Default image renderer:

```html
<img src="..." alt="...">
```

Escape:

- `src`
- `alt`
- `title`

Do not allow event handler attributes.

### 19.5 Extension Security

Every extension must define:

```text
Does it generate HTML?
Does it accept URLs?
Does it add attributes?
Does it allow raw user text?
Does it require extra sanitization?
```

---

## 20. Accessibility Plan

The editor should support:

- `aria-label` on textarea.
- Toolbar buttons with labels.
- Keyboard shortcuts.
- Focus management.
- Visible focus styles via CSS.
- Screen-reader-friendly buttons.
- Preview region label.

Example:

```java
input.setAttribute("aria-label", "Markdown editor");
preview.setAttribute("aria-label", "Markdown preview");
preview.setAttribute("role", "region");
```

Toolbar button:

```java
button.setAttribute("type", "button");
button.setAttribute("aria-label", "Bold");
button.title = "Bold (Ctrl+B)";
```

---

## 21. RTL and Internationalization

The parser itself should be direction-neutral.

The editor should support:

```java
editor.setDirection("rtl");
```

or CSS:

```css
.dm-markdown-editor[dir="rtl"] {
  direction: rtl;
}
```

Markdown syntax itself remains LTR in many cases, but content may be Arabic or mixed Arabic/English.

Important editor concerns:

- Cursor behavior is browser-managed in textarea.
- Preview should inherit direction.
- Toolbar layout may need mirroring.
- Code blocks should usually remain LTR.

Suggested CSS:

```css
.dm-markdown-preview pre,
.dm-markdown-preview code {
  direction: ltr;
  text-align: left;
}
```

---

## 22. Documentation Plan

Write these docs:

```text
README.md
docs/architecture.md
docs/core-parser.md
docs/html-renderer.md
docs/elemental2-editor.md
docs/security.md
docs/extensions.md
docs/testing.md
docs/gwt-j2cl-compatibility.md
docs/domino-ui-wrapper.md
docs/migration-from-commonmark-java.md
```

### 22.1 README Sections

```text
What is this?
Why separate parser from editor?
Installation
Quick parser usage
Quick editor usage
Security defaults
Supported Markdown features
Browser compatibility
GWT/J2CL compatibility
Extension list
License
```

### 22.2 Architecture Doc

Must emphasize:

```text
Parser/renderer is pure Java.
Elemental2 editor consumes renderer interfaces.
No DOM in parser.
No parser internals in editor.
```

---

## 23. Release Strategy

### 23.1 Versioning

Use semantic versioning:

```text
0.1.0 — first parser MVP
0.2.0 — parser + HTML renderer
0.3.0 — CommonMark tests integrated
0.4.0 — J2CL compatibility
0.5.0 — Elemental2 editor MVP
0.6.0 — toolbar commands
0.7.0 — extensions
1.0.0 — stable parser/editor API
```

### 23.2 Artifact Names

Example:

```xml
<groupId>org.dominokit</groupId>
<artifactId>markdown-core</artifactId>
<version>0.1.0</version>
```

```xml
<artifactId>markdown-html</artifactId>
<artifactId>markdown-elemental2</artifactId>
<artifactId>markdown-domino-ui</artifactId>
```

### 23.3 Snapshot Releases

Publish snapshots early for testing in real Domino/Elemental2 applications.

### 23.4 CI Matrix

Run:

```text
JVM tests
J2CL compile
GWT compile if supported
Browser tests
Demo build
Source/javadoc generation
License check
```

---

## 24. Risk Register

### Risk 1 — Parser has hidden JVM dependencies

Mitigation:

- Audit early.
- Compile with J2CL early.
- Avoid waiting until full port is complete.

### Risk 2 — Regex incompatibility or large JS output

Mitigation:

- Replace complex regex with scanner logic.
- Benchmark output size.
- Keep parser modular.

### Risk 3 — CommonMark tests reveal many failures

Mitigation:

- Track failures by section.
- Prioritize common syntax first.
- Do not block editor MVP on 100% conformance.

### Risk 4 — Editor becomes coupled to parser internals

Mitigation:

- Enforce `MarkdownRenderer` boundary.
- Do not expose AST in editor API by default.
- Keep parser and editor in separate modules.

### Risk 5 — XSS through preview

Mitigation:

- Escape raw HTML by default.
- Sanitize URLs.
- Provide sanitizer hook.
- Document security modes clearly.

### Risk 6 — Too much scope from extensions

Mitigation:

- Core first.
- Extensions later.
- Separate modules.

### Risk 7 — Textarea editor feels too basic

Mitigation:

- Start textarea-first.
- Add commands and shortcuts.
- Later add optional advanced editor adapter.

---

## 25. Definition of Done

### Parser/Renderer Done

```text
Core Markdown syntax supported
HTML renderer works
Safe defaults exist
JVM tests pass
CommonMark conformance tracked
J2CL/browser tests pass
No Elemental2 dependency
Public API documented
```

### Elemental2 Editor Done

```text
Editor renders textarea
Live preview works
Renderer is injectable
Sanitizer is injectable
Toolbar commands work
Keyboard shortcuts work
Modes work
CSS is customizable
Browser tests pass
No parser internals required
```

### Project Done

```text
Artifacts published
Demo available
Docs written
Security behavior documented
Migration path documented
Examples included
```

---

## 26. Recommended First MVP

The smallest useful MVP should be:

```text
markdown-core
markdown-html
markdown-elemental2
markdown-demo
```

Feature set:

```text
Parser:
- paragraphs
- headings
- emphasis
- strong
- links
- images
- code spans
- fenced code blocks
- lists
- block quotes

Renderer:
- HTML output
- escaped raw HTML
- URL sanitizer

Editor:
- textarea
- live preview
- split mode
- bold command
- italic command
- heading command
- link command
- list command
```

This MVP is already valuable and demonstrates the architecture.

---

## 27. Suggested Development Order

Do not build editor first.

Recommended order:

```text
1. Core AST
2. Parser
3. HTML renderer
4. JVM tests
5. CommonMark subset tests
6. J2CL compile
7. Browser rendering tests
8. Elemental2 editor shell
9. Live preview
10. Toolbar commands
11. Extensions
12. Domino wrapper
```

Reason:

- The editor depends on renderer stability.
- The renderer depends on parser stability.
- The parser depends on AST stability.

---

## 28. Final Architecture Diagram

```text
+-------------------------------------------------------------+
|                     Application Layer                       |
|                                                             |
|  Domino App / Elemental2 App / GWT App / J2CL App           |
+-----------------------------+-------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                    markdown-elemental2                      |
|                                                             |
|  MarkdownEditor                                             |
|  MarkdownToolbar                                            |
|  MarkdownPreview                                            |
|  MarkdownTextArea                                           |
|  MarkdownCommandRegistry                                    |
|                                                             |
|  Depends on: MarkdownRenderer + MarkdownSanitizer           |
+-----------------------------+-------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                    Integration Boundary                     |
|                                                             |
|  interface MarkdownRenderer {                               |
|      String render(String markdown);                        |
|  }                                                          |
|                                                             |
|  interface MarkdownSanitizer {                              |
|      String sanitize(String html);                          |
|  }                                                          |
+-----------------------------+-------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                      markdown-html                          |
|                                                             |
|  HtmlRenderer                                               |
|  HtmlWriter                                                 |
|  AttributeProvider                                          |
|  UrlSanitizer                                               |
|  RawHtmlPolicy                                              |
+-----------------------------+-------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                      markdown-core                          |
|                                                             |
|  Parser                                                     |
|  AST Nodes                                                  |
|  Block Parser                                               |
|  Inline Parser                                              |
|  Delimiter Processor                                        |
|  Visitor API                                                |
+-------------------------------------------------------------+
```

---

## 29. Final Recommendation

The correct approach is not to create an “Elemental2 Markdown parser.”

The correct approach is to create:

```text
A GWT/J2CL-compatible CommonMark parser/renderer
+
An Elemental2 Markdown editor component
```

joined by:

```java
public interface MarkdownRenderer {
    String render(String markdown);
}
```

This keeps the library clean, reusable, testable, and easier to maintain.

The parser can evolve independently toward full CommonMark compatibility.

The Elemental2 editor can evolve independently toward better UX, toolbar features, preview modes, and Domino UI integration.

This separation is the key design decision that should guide the entire implementation.
