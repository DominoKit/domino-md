# Porting Attribution

Status date: 2026-06-25

## Scope

The initial AST and parser port in this repository was adapted from:
- upstream project: `commonmark-java`
- upstream repository: `https://github.com/commonmark/commonmark-java`
- upstream commit: `9477a93b6b0965efc54c55bd40ad88fbbe25bc6f`

Ported source scope so far:
- `commonmark/src/main/java/org/commonmark/node/*`
- `commonmark/src/main/java/org/commonmark/parser/*`
- `commonmark/src/main/java/org/commonmark/parser/block/*`
- `commonmark/src/main/java/org/commonmark/parser/delimiter/*`
- `commonmark/src/main/java/org/commonmark/parser/beta/*`
- `commonmark/src/main/java/org/commonmark/renderer/*`
- `commonmark/src/main/java/org/commonmark/renderer/html/*`
- `commonmark/src/main/java/org/commonmark/text/AsciiMatcher.java`
- `commonmark/src/main/java/org/commonmark/text/CharMatcher.java`
- `commonmark/src/main/java/org/commonmark/text/Characters.java`
- `commonmark/src/main/java/org/commonmark/internal/Definitions.java`
- `commonmark/src/main/java/org/commonmark/internal/Bracket.java`
- `commonmark/src/main/java/org/commonmark/internal/Delimiter.java`
- `commonmark/src/main/java/org/commonmark/internal/DocumentParser.java`
- `commonmark/src/main/java/org/commonmark/internal/DocumentBlockParser.java`
- `commonmark/src/main/java/org/commonmark/internal/InlineParserContextImpl.java`
- `commonmark/src/main/java/org/commonmark/internal/InlineParserImpl.java`
- `commonmark/src/main/java/org/commonmark/internal/renderer/NodeRendererMap.java`
- `commonmark/src/main/java/org/commonmark/internal/LinkReferenceDefinitionParser.java`
- `commonmark/src/main/java/org/commonmark/internal/ParagraphParser.java`
- `commonmark/src/main/java/org/commonmark/internal/StaggeredDelimiterProcessor.java`
- `commonmark/src/main/java/org/commonmark/internal/HeadingParser.java`
- `commonmark/src/main/java/org/commonmark/internal/ThematicBreakParser.java`
- `commonmark/src/main/java/org/commonmark/internal/BlockQuoteParser.java`
- `commonmark/src/main/java/org/commonmark/internal/IndentedCodeBlockParser.java`
- `commonmark/src/main/java/org/commonmark/internal/FencedCodeBlockParser.java`
- `commonmark/src/main/java/org/commonmark/internal/ListBlockParser.java`
- `commonmark/src/main/java/org/commonmark/internal/ListItemParser.java`
- `commonmark/src/main/java/org/commonmark/internal/HtmlBlockParser.java`
- `commonmark/src/main/java/org/commonmark/internal/inline/*`
- `commonmark/src/main/java/org/commonmark/internal/util/LinkScanner.java`
- `commonmark/src/main/java/org/commonmark/internal/util/Escaping.java`
- `commonmark/src/main/resources/org/commonmark/internal/util/entities.txt` data adapted into generated Java source

Current local targets:
- `src/main/java/org/dominokit/markdown/node/*`
- `src/main/java/org/dominokit/markdown/parser/*`
- `src/main/java/org/dominokit/markdown/parser/block/*`
- `src/main/java/org/dominokit/markdown/parser/delimiter/*`
- `src/main/java/org/dominokit/markdown/parser/beta/*`
- `src/main/java/org/dominokit/markdown/renderer/*`
- `src/main/java/org/dominokit/markdown/renderer/html/*`
- `src/main/java/org/dominokit/markdown/text/AsciiMatcher.java`
- `src/main/java/org/dominokit/markdown/text/CharMatcher.java`
- `src/main/java/org/dominokit/markdown/text/Characters.java`
- `src/main/java/org/dominokit/markdown/internal/Definitions.java`
- `src/main/java/org/dominokit/markdown/internal/Bracket.java`
- `src/main/java/org/dominokit/markdown/internal/Delimiter.java`
- `src/main/java/org/dominokit/markdown/internal/DocumentParser.java`
- `src/main/java/org/dominokit/markdown/internal/DocumentBlockParser.java`
- `src/main/java/org/dominokit/markdown/internal/InlineParserContextImpl.java`
- `src/main/java/org/dominokit/markdown/internal/InlineParserImpl.java`
- `src/main/java/org/dominokit/markdown/internal/renderer/NodeRendererMap.java`
- `src/main/java/org/dominokit/markdown/internal/LinkReferenceDefinitionParser.java`
- `src/main/java/org/dominokit/markdown/internal/ParagraphParser.java`
- `src/main/java/org/dominokit/markdown/internal/StaggeredDelimiterProcessor.java`
- `src/main/java/org/dominokit/markdown/internal/HeadingParser.java`
- `src/main/java/org/dominokit/markdown/internal/ThematicBreakParser.java`
- `src/main/java/org/dominokit/markdown/internal/BlockQuoteParser.java`
- `src/main/java/org/dominokit/markdown/internal/IndentedCodeBlockParser.java`
- `src/main/java/org/dominokit/markdown/internal/FencedCodeBlockParser.java`
- `src/main/java/org/dominokit/markdown/internal/ListBlockParser.java`
- `src/main/java/org/dominokit/markdown/internal/ListItemParser.java`
- `src/main/java/org/dominokit/markdown/internal/HtmlBlockParser.java`
- `src/main/java/org/dominokit/markdown/internal/inline/*`
- `src/main/java/org/dominokit/markdown/internal/util/Escaping.java`
- `src/main/java/org/dominokit/markdown/internal/util/LinkScanner.java`
- `src/main/java/org/dominokit/markdown/internal/util/Html5Entities.java` generated from upstream entity data to avoid runtime resource loading
- `src/main/java/org/dominokit/markdown/internal/util/Parsing.java`

## Adaptations Made

The ported code was intentionally kept close to upstream, with these changes:
- package root changed from `org.commonmark` to `org.dominokit.markdown`
- Apache project headers were added to local source files
- `Parser` currently exposes a string-only entry point and does not include upstream `Reader`-based parsing helpers
- the HTML renderer port currently includes only the upstream core renderer and HTML renderer packages, while the upstream Markdown and plain-text renderers remain deferred
- `HeadingParser` still uses local/manual ATX heading scanning instead of porting the upstream heading scanner stack
- the inline parser, delimiter stack, and reference-definition parser were ported in a later phase after the block parser foundation was already integrated
- `Html5Entities` embeds the upstream entity table as generated Java source so the port keeps the no-classpath-resource-loading constraint
- local JVM tests cover the same parser and HTML renderer slices as upstream but were rewritten in JUnit 4 style to match this repository build

## Licensing Note

The upstream `commonmark-java` repository declares a BSD 2-Clause license in its root `LICENSE.txt`.

This repository is Apache 2.0 licensed. The current AST and parser port keeps explicit documentation of the upstream
source and commit so the provenance of adapted code remains clear while the browser-safe fork evolves.
