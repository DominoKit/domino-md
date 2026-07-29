# Third-Party Notices

This repository is Apache 2.0 licensed. Some code and test fixtures are adapted from upstream
projects with their own attribution requirements.

## CommonMark-derived source code

A substantial portion of the parser, AST, renderer, and support code in this repository was adapted
from the upstream `commonmark-java` project:

- upstream repository: `https://github.com/commonmark/commonmark-java`
- upstream commit: `9477a93b6b0965efc54c55bd40ad88fbbe25bc6f`
- upstream license: BSD 2-Clause

The full upstream BSD 2-Clause license text is included in:

- [`THIRD_PARTY_LICENSES/commonmark-java-BSD-2-Clause.txt`](THIRD_PARTY_LICENSES/commonmark-java-BSD-2-Clause.txt)

## CommonMark specification fixture

The conformance fixture at [`src/test/resources/spec.txt`](src/test/resources/spec.txt) is derived
from the CommonMark specification snapshot and is licensed under CC-BY-SA 4.0.

- title: CommonMark Spec
- author: John MacFarlane
- license: [CC-BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)

## Repository policy

Dominokit source changes remain under Apache 2.0. Where upstream-derived code remains, the package
documentation and this notice record the provenance so downstream consumers can identify the copied
or adapted portions clearly.
