# Porting Attribution

Status date: 2026-06-24

## Scope

The initial AST and parser port in this repository was adapted from:
- upstream project: `commonmark-java`
- upstream repository: `https://github.com/commonmark/commonmark-java`
- upstream commit: `9477a93b6b0965efc54c55bd40ad88fbbe25bc6f`

Ported source scope so far:
- `commonmark/src/main/java/org/commonmark/node/*`
- `commonmark/src/main/java/org/commonmark/parser/*`
- `commonmark/src/main/java/org/commonmark/parser/block/*`
- `commonmark/src/main/java/org/commonmark/text/Characters.java`
- `commonmark/src/main/java/org/commonmark/internal/DocumentParser.java`
- `commonmark/src/main/java/org/commonmark/internal/DocumentBlockParser.java`
- `commonmark/src/main/java/org/commonmark/internal/ParagraphParser.java`
- `commonmark/src/main/java/org/commonmark/internal/HeadingParser.java`
- `commonmark/src/main/java/org/commonmark/internal/ThematicBreakParser.java`
- `commonmark/src/main/java/org/commonmark/internal/BlockQuoteParser.java`
- `commonmark/src/main/java/org/commonmark/internal/IndentedCodeBlockParser.java`
- `commonmark/src/main/java/org/commonmark/internal/FencedCodeBlockParser.java`
- `commonmark/src/main/java/org/commonmark/internal/ListBlockParser.java`
- `commonmark/src/main/java/org/commonmark/internal/ListItemParser.java`
- `commonmark/src/main/java/org/commonmark/internal/HtmlBlockParser.java`

Current local targets:
- `src/main/java/org/dominokit/markdown/node/*`
- `src/main/java/org/dominokit/markdown/parser/*`
- `src/main/java/org/dominokit/markdown/parser/block/*`
- `src/main/java/org/dominokit/markdown/text/Characters.java`
- `src/main/java/org/dominokit/markdown/internal/DocumentParser.java`
- `src/main/java/org/dominokit/markdown/internal/DocumentBlockParser.java`
- `src/main/java/org/dominokit/markdown/internal/ParagraphParser.java`
- `src/main/java/org/dominokit/markdown/internal/HeadingParser.java`
- `src/main/java/org/dominokit/markdown/internal/ThematicBreakParser.java`
- `src/main/java/org/dominokit/markdown/internal/BlockQuoteParser.java`
- `src/main/java/org/dominokit/markdown/internal/IndentedCodeBlockParser.java`
- `src/main/java/org/dominokit/markdown/internal/FencedCodeBlockParser.java`
- `src/main/java/org/dominokit/markdown/internal/ListBlockParser.java`
- `src/main/java/org/dominokit/markdown/internal/ListItemParser.java`
- `src/main/java/org/dominokit/markdown/internal/HtmlBlockParser.java`
- `src/main/java/org/dominokit/markdown/internal/util/Escaping.java` as a temporary compatibility helper while parser and renderer support is still incomplete
- `src/main/java/org/dominokit/markdown/internal/util/Parsing.java`

## Adaptations Made

The ported code was intentionally kept close to upstream, with these changes:
- package root changed from `org.commonmark` to `org.dominokit.markdown`
- Apache project headers were added to local source files
- `Parser` currently exposes a string-only entry point and does not include upstream `Reader`-based parsing helpers
- a temporary inline parser currently emits plain `Text` and `SoftLineBreak` nodes so the block parser can be integrated before the inline syntax port
- `HeadingParser` uses local/manual ATX heading scanning instead of porting the full upstream `parser.beta` scanner package in this phase
- link reference definition parsing and delimiter processing are intentionally deferred to the next parser phase
- a minimal `Escaping` helper was introduced first for label normalization and then expanded only as far as the current parser slice requires
- JVM tests were rewritten locally in JUnit 4 style to match this repository build

## Licensing Note

The upstream `commonmark-java` repository declares a BSD 2-Clause license in its root `LICENSE.txt`.

This repository is Apache 2.0 licensed. The current AST and parser port keeps explicit documentation of the upstream
source and commit so the provenance of adapted code remains clear while the browser-safe fork evolves.
