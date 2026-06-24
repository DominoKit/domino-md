# Risk List

Status date: 2026-06-24

## High

### Runtime entity resource loading

Source:
- `org.commonmark.internal.util.Html5Entities`

Risk:
- upstream loads `entities.txt` from the classpath at runtime
- browser compilation should not depend on classpath resource loading

Mitigation:
- replace resource loading with generated or checked-in Java constants before porting the parser utilities

Decision:
- must be resolved in the first implementation slice

### Regex-heavy HTML parsing

Source:
- `org.commonmark.internal.HtmlBlockParser`

Risk:
- large regex tables can cause J2CL compilation pain, runtime differences, or generated-size inflation

Mitigation:
- port as-is first for correctness
- if browser compile or tests fail, replace with manual scanners before broadening scope

Decision:
- keep in v1, but treat as the first fallback target

## Medium

### `Reader`-based API surface

Source:
- `org.commonmark.parser.Parser`
- `org.commonmark.internal.DocumentParser`
- `org.commonmark.internal.util.LineReader`

Risk:
- increases JVM-only surface without helping the browser use case

Mitigation:
- keep the browser core string-only
- move any stream adapters to a later JVM helper layer if needed

Decision:
- exclude from first port

### Regex and Unicode normalization behavior

Source:
- `org.commonmark.internal.util.Escaping`
- `org.commonmark.internal.inline.AutolinkInlineParser`

Risk:
- browser-compiled regex behavior and case folding need verification against tests

Mitigation:
- retain code initially
- add focused JVM and GWT/J2CL tests for autolinks, label normalization, and percent-encoding

Decision:
- keep in first port with test coverage

### Exported `parser.beta` package

Source:
- `org.commonmark.parser.beta.*`

Risk:
- the current parser architecture depends on types under an upstream package explicitly labeled `beta`
- hiding or redesigning it too early increases port risk

Mitigation:
- preserve the package for the first port
- revisit API exposure after the core parser is stable

Decision:
- keep as-is under the new package root for now

### Local build configuration drift

Source:
- `pom.xml`

Risk:
- the configured GWT module name is `org.dominokit.domino.logger.Logging`, which does not match this repository

Mitigation:
- correct the GWT/J2CL build configuration before Phase 7 compilation work

Decision:
- do not block the audit on this, but track it as an early project risk

## Low

### JPMS module descriptor

Source:
- upstream `module-info.java`

Risk:
- unnecessary complexity for the browser-safe port

Mitigation:
- omit from the first port

Decision:
- exclude

### Appendable streaming writer

Source:
- `org.commonmark.renderer.html.HtmlWriter`

Risk:
- low portability concern because primary browser usage will render to `StringBuilder`

Mitigation:
- keep API and validate through renderer tests

Decision:
- keep

## Confirmed Non-Risks In Audited Main Sources

Not found:
- reflection
- `ServiceLoader`
- concurrency primitives
- threads and thread locals
- environment variables
- system property lookups
- weak references or finalization hooks
