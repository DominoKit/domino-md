# Compatibility Audit

Audit date: 2026-06-24

Upstream baseline:
- Repository: `https://github.com/commonmark/commonmark-java`
- Branch: `main`
- Commit: `9477a93b6b0965efc54c55bd40ad88fbbe25bc6f`
- Core module audited: `commonmark`

Audit scope:
- `commonmark/src/main/java`
- Focused on the parser, AST, HTML renderer, and immediate support code needed for the first browser-safe port

Excluded from this audit:
- extension modules under `commonmark-ext-*`
- Android and integration-test modules
- benchmark-only code

## Summary

The upstream `commonmark` core is close to GWT/J2CL-compatible, but not directly portable unchanged.

The main blockers are:
- `Reader`-based parsing entry points
- classpath resource loading for HTML entities
- regex-heavy HTML and autolink parsing paths that need browser compilation verification

The main positive findings are:
- no reflection usage found in the audited main sources
- no `ServiceLoader` usage found in the audited main sources
- no thread or concurrency primitives found in the audited main sources
- no environment or system-property dependent behavior found in the audited main sources

Recommended implementation decision:
- use the upstream code as the audit reference
- port into this repository under a new package root
- keep class and subpackage structure close to upstream for easier comparison
- remove or replace JVM-specific entry points instead of adding compatibility shims for them

## Findings

### 1. Reader-based public parse API

File: `commonmark/src/main/java/org/commonmark/parser/Parser.java`  
Line: `16-17`, `84-107`  
API: `java.io.Reader`, `java.io.IOException`  
Risk: Medium  
Used for: JVM stream-based parsing entry point  
GWT/J2CL status: Technically possible only with emulated types or adapters, but not useful for browser-first API design  
Replacement strategy: Keep `parse(String)` as the main API. Drop `parseReader(Reader)` from the first browser-safe port. If needed later, add a separate JVM-only adapter outside the browser core.  
Decision: Remove from initial port.

### 2. Reader-based internal document parser

File: `commonmark/src/main/java/org/commonmark/internal/DocumentParser.java`  
Line: `16-18`, `147-160`  
API: `java.io.Reader`, `java.io.IOException`  
Risk: Medium  
Used for: internal line-by-line parsing from a character stream  
GWT/J2CL status: Not needed for browser-first usage where Markdown input is already a `String`  
Replacement strategy: Keep only the `parse(String)` path in the initial port.  
Decision: Remove the `Reader` overload from the initial port.

### 3. Line reader helper coupled to `Reader`

File: `commonmark/src/main/java/org/commonmark/internal/util/LineReader.java`  
Line: `3-5`, `15-149`  
API: `java.io.Closeable`, `java.io.Reader`, `java.io.IOException`  
Risk: Medium  
Used for: supporting `parseReader` and preserving original line terminators  
GWT/J2CL status: Avoidable if the browser-safe core only accepts `String` input  
Replacement strategy: Do not port this class in the first implementation slice. Reintroduce only if a non-browser adapter needs it later.  
Decision: Exclude from initial port.

### 4. Classpath resource loading for HTML entities

File: `commonmark/src/main/java/org/commonmark/internal/util/Html5Entities.java`  
Line: `3-8`, `14-16`, `50-69`  
API: `InputStream`, `InputStreamReader`, `BufferedReader`, `Charset`, `StandardCharsets`, `Class.getResourceAsStream`  
Risk: High  
Used for: loading named HTML entities from `entities.txt` at runtime  
GWT/J2CL status: Classpath resource loading is not a safe assumption in browser compilation  
Replacement strategy: Generate a Java source constant at build time or check in a static Java map/array containing the entity table. Remove runtime resource loading entirely.  
Decision: Replace with generated or checked-in Java data.

### 5. Regex and locale-heavy escaping utilities

File: `commonmark/src/main/java/org/commonmark/internal/util/Escaping.java`  
Line: `3-6`, `14-26`, `47-52`, `107-117`, `120-139`  
API: `StandardCharsets.UTF_8`, `Locale.ROOT`, `Pattern`, `Matcher`  
Risk: Medium  
Used for: entity decoding, percent-encoding URLs, label normalization, whitespace collapsing  
GWT/J2CL status: Likely portable, but requires verification for generated size, regex behavior, and exact Unicode folding behavior  
Replacement strategy: Keep initially, then replace the regex-based helpers with manual scanners only if compilation, correctness, or output-size issues appear.  
Decision: Keep for first port, but add focused tests around URL encoding and label normalization.

### 6. Regex-heavy HTML block parser

File: `commonmark/src/main/java/org/commonmark/internal/HtmlBlockParser.java`  
Line: `9`, `13-74`, `109`, `135-137`  
API: `java.util.regex.Pattern`  
Risk: Medium  
Used for: CommonMark HTML block detection and termination  
GWT/J2CL status: Often portable, but this is one of the highest-risk regex areas because of pattern complexity and browser-compiled output size  
Replacement strategy: Port as-is first because it is behaviorally central. If J2CL compilation or tests expose problems, replace with manual scanners for block starts and terminators.  
Decision: Keep in initial port, mark as first regex fallback candidate.

### 7. Regex-based autolink parsing

File: `commonmark/src/main/java/org/commonmark/internal/inline/AutolinkInlineParser.java`  
Line: `8-20`, `33-35`  
API: `java.util.regex.Pattern`  
Risk: Medium  
Used for: `<scheme:...>` and email autolink recognition  
GWT/J2CL status: Likely portable, but still needs test coverage in browser compilation  
Replacement strategy: Keep initially. If needed, replace with manual ASCII scanners because the accepted syntax is constrained.  
Decision: Keep in initial port.

### 8. Regex for single-character backslash escaping

File: `commonmark/src/main/java/org/commonmark/internal/inline/BackslashInlineParser.java`  
Line: `8-9`, `17`, `29`  
API: `java.util.regex.Pattern`  
Risk: Low  
Used for: checking whether the next character is escapable  
GWT/J2CL status: Portable, but unnecessary complexity for a single-character test  
Replacement strategy: Replace with a manual character predicate during the port.  
Decision: Simplify in ported code.

### 9. Java module descriptor

File: `commonmark/src/main/java/module-info.java`  
Line: `1-11`  
API: JPMS module descriptor  
Risk: Medium  
Used for: Java module exports  
GWT/J2CL status: Not relevant to the browser-safe core and can complicate source reuse  
Replacement strategy: Omit from the first port. Revisit only if a separate JVM-focused packaging step is added later.  
Decision: Exclude from initial port.

### 10. Appendable-based HTML writer

File: `commonmark/src/main/java/org/commonmark/renderer/html/HtmlWriter.java`  
Line: `5`, `13-19`, `64-69`  
API: `Appendable`, `IOException`  
Risk: Low  
Used for: streaming rendered HTML to a target buffer  
GWT/J2CL status: Safe enough for the first port when primarily used with `StringBuilder`  
Replacement strategy: Keep API shape. Prefer `render(Node)` and `StringBuilder` usage in browser code paths.  
Decision: Keep in initial port.

## Negative Findings

The following categories were explicitly searched and not found in the audited main source set:
- `java.lang.reflect`
- `java.util.ServiceLoader`
- `java.util.Properties`
- `java.util.ResourceBundle`
- `java.util.concurrent`
- `Thread`
- `ThreadLocal`
- `System.getProperty`
- `System.getenv`
- `WeakHashMap`
- finalization hooks

## Portability Decision

Phase 1 decision:
- Implementation package root will change from `org.commonmark` to `org.dominokit.markdown`.
- Internal package and class structure should stay close to upstream for the first port.
- The first browser-safe port should target the upstream `commonmark` module only.
- `renderer.markdown`, `renderer.text`, runtime resource loading, and `Reader` parsing APIs are out of the first implementation slice.
