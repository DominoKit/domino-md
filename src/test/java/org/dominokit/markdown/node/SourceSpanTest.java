/*
 * Copyright © 2019 Dominokit
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
package org.dominokit.markdown.node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class SourceSpanTest {

  @Test
  public void subSpanShouldMirrorSubstringSemantics() {
    SourceSpan span = SourceSpan.of(1, 2, 3, 5);

    assertThat(span.subSpan(0)).isSameAs(span);
    assertThat(span.subSpan(0, 5)).isSameAs(span);
    assertThat(span.subSpan(1)).isEqualTo(SourceSpan.of(1, 3, 4, 4));
    assertThat(span.subSpan(5)).isEqualTo(SourceSpan.of(1, 7, 8, 0));
    assertThat(span.subSpan(1, 4)).isEqualTo(SourceSpan.of(1, 3, 4, 3));
    assertThat(span.subSpan(2, 3)).isEqualTo(SourceSpan.of(1, 4, 5, 1));
  }

  @Test
  public void subSpanShouldRejectOutOfBoundsArguments() {
    SourceSpan span = SourceSpan.of(1, 2, 3, 5);

    assertThatThrownBy(() -> span.subSpan(-1)).isInstanceOf(IndexOutOfBoundsException.class);
    assertThatThrownBy(() -> span.subSpan(6)).isInstanceOf(IndexOutOfBoundsException.class);
    assertThatThrownBy(() -> span.subSpan(0, -1)).isInstanceOf(IndexOutOfBoundsException.class);
    assertThatThrownBy(() -> span.subSpan(0, 6)).isInstanceOf(IndexOutOfBoundsException.class);
    assertThatThrownBy(() -> span.subSpan(2, 1)).isInstanceOf(IndexOutOfBoundsException.class);
  }
}
