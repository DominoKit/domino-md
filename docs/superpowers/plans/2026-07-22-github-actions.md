# GitHub Actions Workflow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add GitHub Actions workflows that verify pull requests and publish releases from this repository using the existing Maven build and Sonatype Central setup.

**Architecture:** Keep CI intentionally small and explicit. One workflow handles pull request verification with `mvn verify`, and one workflow handles release publishing on pushes to the release branches. Both workflows reuse the repository's Maven build and Java 11 so they stay aligned with the repository's build configuration and test execution.

**Tech Stack:** GitHub Actions, `actions/checkout@v4`, `actions/setup-java@v4`, Maven Wrapper, Java 11, Sonatype Central publishing.

## Global Constraints

- Java compiler and runtime target: `11`
- Maven entry point: `mvn`
- Verification command: `mvn verify -B -e`
- Release command: `mvn --no-transfer-progress clean deploy -B -e -Dci=true -Dgpg.passphrase=${{ secrets.gpg_passphrase }}`
- Release branch targets: `main` and `development`
- Publishing server id: `central`
- GPG private key secret: `secrets.gpg_private_key`
- GPG passphrase secret: `secrets.gpg_passphrase`
- Maven Central credentials: `secrets.CENTRAL_USER` and `secrets.CENTRAL_TOKEN`

---

### Task 1: Add the pull request verification workflow

**Files:**
- Create: `.github/workflows/verify.yaml`

**Interfaces:**
- Consumes: repository checkout, Java 11, Maven Wrapper
- Produces: PR validation job that runs the full Maven verify lifecycle

- [ ] **Step 1: Write the workflow file**

```yaml
#
# Copyright © ${year} Dominokit
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

name: Verify

on:
  pull_request:
    types: [assigned, opened, synchronize, reopened]

jobs:
  verify:
    runs-on: ubuntu-latest

    steps:
      - name: Check out Git repository
        uses: actions/checkout@v4

      - name: Set up Java and Maven
        uses: actions/setup-java@v4
        with:
          java-version: 11
          distribution: temurin

      - name: Run Maven verify
        run: ./mvnw verify -B -e
```

- [ ] **Step 2: Validate the workflow syntax**

Run: `python - <<'PY'
import yaml
from pathlib import Path
yaml.safe_load(Path('.github/workflows/verify.yaml').read_text())
print('verify.yaml ok')
PY`

Expected: `verify.yaml ok`

- [ ] **Step 3: Commit the workflow**

```bash
git add .github/workflows/verify.yaml
git commit -m "ci: add pull request verification workflow"
```

### Task 2: Add the release publishing workflow

**Files:**
- Create: `.github/workflows/deploy.yaml`

**Interfaces:**
- Consumes: repository checkout, Java 11, Maven Wrapper, Maven Central secrets, GPG secrets
- Produces: release job that verifies the build and deploys to Sonatype Central on push to `main` or `development`

- [ ] **Step 1: Write the workflow file**

```yaml
#
# Copyright © 2019 Dominokit
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

name: Deploy

on:
  push:
    branches: [main, development]

jobs:
  verify:
    runs-on: ubuntu-latest
    steps:
      - name: Check out Git repository
        uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          java-version: 11
          distribution: temurin
          cache: maven

      - name: Run Maven verify
        run: ./mvnw verify -B -e

  release:
    needs: verify
    runs-on: ubuntu-latest
    steps:
      - name: Check out Git repository
        uses: actions/checkout@v4

      - name: Set up Java for deployment
        uses: actions/setup-java@v4
        with:
          java-version: 11
          distribution: temurin
          cache: maven
          server-id: central
          server-username: CENTRAL_USER
          server-password: CENTRAL_TOKEN

      - name: Import GPG key
        run: |
          echo "${{ secrets.gpg_private_key }}" | gpg --batch --import
        env:
          GPG_TTY: $(tty)

      - name: Deploy with Maven
        run: ./mvnw --no-transfer-progress clean deploy -B -e -Dci=true -Dgpg.passphrase=${{ secrets.gpg_passphrase }}
        env:
          CENTRAL_USER: ${{ secrets.CENTRAL_USER }}
          CENTRAL_TOKEN: ${{ secrets.CENTRAL_TOKEN }}
```

- [ ] **Step 2: Validate the workflow syntax**

Run: `python - <<'PY'
import yaml
from pathlib import Path
yaml.safe_load(Path('.github/workflows/deploy.yaml').read_text())
print('deploy.yaml ok')
PY`

Expected: `deploy.yaml ok`

- [ ] **Step 3: Commit the workflow**

```bash
git add .github/workflows/deploy.yaml
git commit -m "ci: add deployment workflow"
```
