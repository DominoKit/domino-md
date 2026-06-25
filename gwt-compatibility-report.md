# GWT Compatibility Report

Status date: 2026-06-25

## Scope

Phase 7 validates that the pure Java Markdown core and HTML renderer can be transpiled and executed
 through the repository's existing browser toolchain:

- GWT version: `2.13.1`
- Maven plugin: `net.ltgt.gwt.maven:gwt-maven-plugin:1.1.0`
- main module: `org.dominokit.Markdown`
- browser test module: `org.dominokit.MarkdownTest`

This repository does not currently carry a separate J2CL-specific Maven module. Browser
compatibility is therefore confirmed through the checked-in GWT compile and `GWTTestCase` path that
now runs in `mvn verify`.

## Checked-In Proof

Main module and browser test descriptors:
- `src/main/resources/org/dominokit/Markdown.gwt.xml`
- `src/test/resources/org/dominokit/MarkdownTest.gwt.xml`

Browser-side shared rendering assertions:
- `src/test/java/org/dominokit/markdown/gwt/GwtRenderingCases.java`
- `src/test/java/org/dominokit/markdown/gwt/Elemental2RenderingCases.java`
- `src/test/java/org/dominokit/markdown/gwt/GwtRenderingCasesJvmTest.java`
- `src/test/java/org/dominokit/markdown/gwt/MarkdownSuite.java`

Build wiring:
- `pom.xml` now runs `gwt:test` during the Maven `test` phase for `MarkdownSuite`
- `pom.xml` now runs `gwt:compile` during the Maven `verify` phase for `org.dominokit.Markdown`

## Current Result

Confirmed on 2026-06-25 with:
- `mvn -q gwt:test -Dtest=MarkdownSuite`
- `mvn -q gwt:compile -Dgwt.forceCompilation=true`
- `mvn -q verify`

Outcome:
- browser-side `GWTTestCase` rendering suite passes
- main markdown GWT module compiles successfully
- full repository `verify` passes with the browser compile/test path enabled

Phase 7A follow-up:
- the Elemental2 DOM renderer also passes browser-side rendering assertions inside
  `MarkdownSuite`
- `pom.xml` now carries `elemental2-dom`, and `Markdown.gwt.xml` inherits `elemental2.dom.Dom`

Phase 11 follow-up:
- browser-side tests now also cover the shipped extension set for strikethrough, task list items,
  tables, and autolinks on both HTML and Elemental2 DOM renderers
- browser-side tests now also execute the plain-text `TextContentRenderer` path, including
  extension-backed output for strikethrough, task list items, tables, and autolinks
- browser-side tests now also execute the Markdown renderer path, including canonical output for
  headings, escaping, links, strikethrough, task list items, tables, and autolinks

## Compatibility Adaptations

The browser toolchain exposed a small set of JVM-only assumptions which were removed in this phase:

- `Characters` now uses generated Unicode range tables in `UnicodeCharacterData` instead of relying
  on `Character.getType(int)` and related category constants not emulated by GWT
- `Escaping`, `HtmlBlockParser`, `AutolinkInlineParser`, and `BackslashInlineParser` no longer rely
  on `java.util.regex.Pattern` in the runtime core
- `LinkScanner` no longer relies on `Character.isISOControl`

These changes keep the parser/renderer free of Elemental2, DOM, and GWT-specific runtime APIs while
remaining compatible with the browser compiler.
