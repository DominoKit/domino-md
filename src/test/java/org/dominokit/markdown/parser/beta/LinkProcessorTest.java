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
package org.dominokit.markdown.parser.beta;

import static org.assertj.core.api.Assertions.assertThat;

import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.Parser;
import org.junit.Test;

public class LinkProcessorTest {

  @Test
  public void linkMarkerShouldRemainLiteralUnlessProcessorIncludesIt() {
    Parser parser = Parser.builder().linkMarker('^').build();
    Node document = parser.parse("^[test](url)");
    Link link = findFirst(document, Link.class);

    assertThat(link.getDestination()).isEqualTo("url");
    assertThat(((Text) link.getPrevious()).getLiteral()).isEqualTo("^");
  }

  private static <T extends Node> T findFirst(Node node, Class<T> type) {
    if (type.isInstance(node)) {
      return type.cast(node);
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      T found = findFirst(child, type);
      if (found != null) {
        return found;
      }
    }
    return null;
  }
}
