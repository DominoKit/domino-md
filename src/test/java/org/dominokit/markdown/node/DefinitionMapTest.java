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

import org.junit.Test;

public class DefinitionMapTest {

  @Test
  public void putIfAbsentShouldNormalizeLabelsAndKeepFirstDefinition() {
    DefinitionMap<String> definitions = new DefinitionMap<>(String.class);

    assertThat(definitions.putIfAbsent("  Fo o  ", "first")).isNull();
    assertThat(definitions.putIfAbsent("fo O", "second")).isEqualTo("first");
    assertThat(definitions.get("fO o")).isEqualTo("first");
    assertThat(definitions.keySet()).containsExactly("FO O");
    assertThat(definitions.values()).containsExactly("first");
  }
}
