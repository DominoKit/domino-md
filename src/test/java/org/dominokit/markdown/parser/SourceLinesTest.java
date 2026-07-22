/*
 * Copyright © 2026 Dominokit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dominokit.markdown.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.dominokit.markdown.node.SourceSpan;
import org.junit.Test;

public class SourceLinesTest {

  @Test
  public void getContentAndSourceSpansShouldCombineAllLines() {
    SourceLines sourceLines = SourceLines.empty();
    SourceLine first = SourceLine.of("one", SourceSpan.of(0, 0, 0, 3));
    SourceLine second = SourceLine.of("two", SourceSpan.of(1, 1, 5, 3));

    sourceLines.addLine(first);
    sourceLines.addLine(second);

    assertThat(sourceLines.getContent()).isEqualTo("one\ntwo");
    assertThat(sourceLines.getSourceSpans())
        .containsExactly(SourceSpan.of(0, 0, 0, 3), SourceSpan.of(1, 1, 5, 3));
  }
}
