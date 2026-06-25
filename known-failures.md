# Known Failures

Status date: 2026-06-25

## CommonMark Core Spec

There are no known failures in the current V1 core CommonMark conformance snapshot.

The checked-in spec harness in `src/test/java/org/dominokit/markdown/conformance/CommonMarkSpecTest.java`
passes all `652` imported examples from `src/test/resources/spec.txt`.

If future regressions or intentionally deferred examples appear, record them here using:
- spec section
- example number within that section
- current behavior
- expected behavior
- owner or planned phase
