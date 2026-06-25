# CommonMark Compliance Report

Status date: 2026-06-25

## Scope

This snapshot measures the V1 core port against the upstream CommonMark core specification examples
imported from `commonmark-test-util/src/main/resources/spec.txt` at upstream commit
`9477a93b6b0965efc54c55bd40ad88fbbe25bc6f`.

The checked-in conformance harness lives in:
- `src/test/java/org/dominokit/markdown/conformance/CommonMarkSpecTest.java`
- `src/test/resources/spec.txt`

## Renderer Configuration

The conformance test uses:
- `Parser.builder().build()`
- `HtmlRenderer.builder().percentEncodeUrls(true).build()`

`percentEncodeUrls(true)` matches the upstream CommonMark HTML spec-test expectation for URL output.
Tab comparisons use the same visible-tab normalization as upstream so literal tab characters compare
correctly against the spec text representation.

## Current Result

- imported examples: `652`
- passing examples: `652`
- failing examples: `0`
- pass rate: `100.00%`

## Notes

- The final spec mismatch fixed in this phase was an ATX-heading closing-hash edge case now handled
  by the upstream-style scanner-based heading parser.
- The current snapshot has no known core spec failures. See `known-failures.md`.
