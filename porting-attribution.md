# Porting Attribution

Status date: 2026-06-24

## Scope

The initial AST port in this repository was adapted from:
- upstream project: `commonmark-java`
- upstream repository: `https://github.com/commonmark/commonmark-java`
- upstream commit: `9477a93b6b0965efc54c55bd40ad88fbbe25bc6f`

Ported source scope in this task:
- `commonmark/src/main/java/org/commonmark/node/*`

Initial local targets:
- `src/main/java/org/dominokit/markdown/node/*`
- `src/main/java/org/dominokit/markdown/internal/util/Escaping.java` as a temporary AST-only compatibility helper

## Adaptations Made

The ported AST code was intentionally kept close to upstream, with these changes:
- package root changed from `org.commonmark` to `org.dominokit.markdown`
- Apache project headers were added to local source files
- a minimal `Escaping.normalizeLabelContent` helper was introduced so the AST package can compile before the full parser/renderer utility port
- JVM tests were rewritten locally in JUnit 4 style to match this repository build

## Licensing Note

The upstream `commonmark-java` repository declares a BSD 2-Clause license in its root `LICENSE.txt`.

This repository is Apache 2.0 licensed. The AST port keeps explicit documentation of the upstream source and commit so
the provenance of adapted code remains clear while the browser-safe fork evolves.
