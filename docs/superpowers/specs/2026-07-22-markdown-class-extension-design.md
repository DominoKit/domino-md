# Markdown Class Extension Design

## Goal

Add an optional renderer extension that applies CSS classes to generated HTML and Elemental2 DOM
output based on markdown node type and rendered tag name, plus a built-in `dui` preset layered on
top of the generic mechanism.

## Requirements

- The feature must be opt-in.
- The feature must work for both `HtmlRenderer` and `Elemental2Renderer`.
- The feature must merge with existing `class` attributes.
- The generic extension must let callers attach arbitrary classes to all rendered elements, to a
  specific node type, and to a specific tag name.
- The `dui` preset must add a fixed `dui` base class and `dui-md-*` classes for the core rendered
  element set.
- The feature must not change parser behavior or core renderer defaults.
- The feature must not be auto-registered through service discovery.

## Architecture

The generic extension is built as a renderer-side class injector that installs one attribute
provider for HTML output and one attribute provider for Elemental2 output. Both providers share the
same class-rule set so the two renderer families behave identically.

Class resolution uses three inputs:

1. base classes applied to every generated element
2. node-type rules keyed by markdown AST class
3. tag-name rules keyed by rendered HTML tag

When a rendered element already has a `class` attribute, the extension preserves it and appends the
configured classes in deterministic order.

The `DuiClassExtension` is a thin preset that configures the generic extension with the repository's
default class names. It exists as a convenience for users who want a ready-made styling contract
without custom configuration.

## Public API Shape

- `org.dominokit.markdown.ext.classes.MarkdownClassExtension`
- `org.dominokit.markdown.ext.dui.DuiClassExtension`

The generic extension exposes a builder with these methods:

- `builder()`
- `classes(String...)`
- `nodeClasses(Class<? extends Node>, String...)`
- `tagClasses(String, String...)`
- `build()`

## Expected Behavior

- Rendering `# Hello` with the `dui` preset yields an `h1` element with `dui`, `dui-md-heading`,
  and `dui-md-h1`.
- Rendering fenced code blocks preserves the renderer's `language-*` class and appends the new
  class names.
- Rendering through Elemental2 uses the same rule set and class merging behavior as HTML output.

## Non-Goals

- No changes to parsing.
- No change to the core renderer output when the extension is not installed.
- No automatic discovery or default activation of the class-styling extension.
