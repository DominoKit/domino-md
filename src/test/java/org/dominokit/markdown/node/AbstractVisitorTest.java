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

public class AbstractVisitorTest {

  @Test
  public void replacingNodeInVisitorShouldNotDestroyVisitOrder() {
    Visitor visitor =
        new AbstractVisitor() {
          @Override
          public void visit(Text text) {
            text.insertAfter(new Code(text.getLiteral()));
            text.unlink();
          }
        };

    Paragraph paragraph = new Paragraph();
    paragraph.appendChild(new Text("foo"));
    paragraph.appendChild(new Text("bar"));

    paragraph.accept(visitor);

    assertCode("foo", paragraph.getFirstChild());
    assertCode("bar", paragraph.getFirstChild().getNext());
    assertThat(paragraph.getFirstChild().getNext().getNext()).isNull();
    assertCode("bar", paragraph.getLastChild());
  }

  private static void assertCode(String expectedLiteral, Node node) {
    assertThat(node).isInstanceOf(Code.class);
    assertThat(((Code) node).getLiteral()).isEqualTo(expectedLiteral);
  }
}
