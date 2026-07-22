# Markdown Class Extension Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an optional class-injection extension for HTML and Elemental2 rendering, plus a built-in `dui` preset layered on top of the generic extension.

**Architecture:** Introduce one reusable extension that applies classes from node type and rendered tag name while merging with existing renderer classes. Build a thin `DuiClassExtension` preset on top of it with a fixed `dui` base class and `dui-md-*` node/tag classes. Keep the feature opt-in so core rendering behavior stays unchanged unless a caller installs the extension.

**Tech Stack:** Java 11, Maven, JUnit 4, AssertJ, Elemental2 DOM renderer, existing renderer extension hooks.

## Global Constraints

- Preserve existing HTML and Elemental2 rendering output unless the new extension is explicitly installed.
- Merge classes with existing `class` attributes instead of replacing them.
- Keep the new feature optional; do not register it in service discovery.
- Add detailed Javadocs for all new public classes and methods.

---

### Task 1: Add the generic class extension

**Files:**
- Create: `src/main/java/org/dominokit/markdown/ext/classes/MarkdownClassExtension.java`

**Interfaces:**
- Consumes: `HtmlRenderer.Builder`, `Elemental2Renderer.Builder`, `org.dominokit.markdown.node.Node`
- Produces: an `Extension` that can add classes based on node type and tag name

- [ ] **Step 1: Write the failing test**

Create a new test that builds the generic extension with a base class, a node-specific class, and a tag-specific class, then asserts that HTML output contains all of them and preserves existing renderer-added classes such as `language-java`.

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn -q -Dtest=MarkdownClassExtensionTest test`
Expected: compilation or assertion failure because the extension does not exist yet.

- [ ] **Step 3: Write the minimal implementation**

Implement the builder, renderer extension hooks, and class-merging helper.

- [ ] **Step 4: Run the test to verify it passes**

Run: `mvn -q -Dtest=MarkdownClassExtensionTest test`
Expected: PASS.

### Task 2: Add the built-in `dui` preset

**Files:**
- Create: `src/main/java/org/dominokit/markdown/ext/dui/DuiClassExtension.java`
- Modify: `src/test/java/org/dominokit/markdown/gwt/MarkdownSuite.java`

**Interfaces:**
- Consumes: `MarkdownClassExtension`
- Produces: a preset extension that adds `dui` and `dui-md-*` classes for the core rendered element set

- [ ] **Step 1: Write the failing browser-path test**

Add a GWT test that renders a heading, paragraph, fenced code block, and link with `DuiClassExtension` installed and asserts the resulting DOM serialization includes `dui` plus `dui-md-*` classes.

- [ ] **Step 2: Run the browser/unit coverage to verify failure**

Run: `mvn -q test`
Expected: the new browser test fails because the preset does not exist yet.

- [ ] **Step 3: Write the minimal implementation**

Implement `DuiClassExtension` as a thin wrapper over `MarkdownClassExtension` with built-in rules for the standard rendered tags and node types.

- [ ] **Step 4: Run the full test suite**

Run: `mvn -q test`
Expected: PASS.

### Task 3: Document and verify usage

**Files:**
- Modify: `docs/advanced-user-docs/api-reference.md`
- Modify: `docs/advanced-user-docs/cookbook.md`
- Modify: `README.md`

**Interfaces:**
- Consumes: the new extension classes and their builder methods
- Produces: user-facing examples for opting into `dui` and custom class mappings

- [ ] **Step 1: Add usage examples**

Document how to install the preset extension and how to configure custom node/tag classes with the generic builder.

- [ ] **Step 2: Verify the docs stay consistent**

Check that the new examples match the public API signatures exactly.

- [ ] **Step 3: Final verification**

Run: `mvn -q test`
Expected: PASS.
