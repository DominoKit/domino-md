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

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class NodeTreeTest {

  @Test
  public void appendPrependInsertAndUnlinkShouldMaintainSiblingPointers() {
    Paragraph paragraph = new Paragraph();
    Text one = new Text("one");
    Text two = new Text("two");
    Text zero = new Text("zero");
    Text onePointFive = new Text("one-point-five");

    paragraph.appendChild(one);
    paragraph.appendChild(two);
    paragraph.prependChild(zero);
    two.insertBefore(onePointFive);
    one.unlink();

    assertThat(paragraph.getFirstChild()).isSameAs(zero);
    assertThat(paragraph.getLastChild()).isSameAs(two);
    assertThat(childrenLiterals(paragraph)).containsExactly("zero", "one-point-five", "two");
    assertThat(zero.getPrevious()).isNull();
    assertThat(two.getNext()).isNull();
    assertThat(one.getParent()).isNull();
    assertThat(one.getNext()).isNull();
    assertThat(one.getPrevious()).isNull();
  }

  @Test
  public void nodesBetweenShouldExcludeBounds() {
    Paragraph paragraph = new Paragraph();
    Text first = new Text("first");
    Text middle = new Text("middle");
    Text last = new Text("last");

    paragraph.appendChild(first);
    paragraph.appendChild(middle);
    paragraph.appendChild(last);

    List<Node> between = new ArrayList<>();
    for (Node node : Nodes.between(first, last)) {
      between.add(node);
    }

    assertThat(between).containsExactly(middle);
  }

  @Test
  public void blockParentMustAlsoBeBlock() {
    Paragraph paragraph = new Paragraph();
    Emphasis emphasis = new Emphasis("*");

    assertThatThrownBy(() -> emphasis.appendChild(paragraph))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Parent of block must also be block");
  }

  @Test
  public void listItemTypedAppendChildShouldAcceptBlocks() {
    ListItem listItem = new ListItem();
    Paragraph paragraph = new Paragraph();

    listItem.appendChild(paragraph);

    assertThat(listItem.getFirstChild()).isSameAs(paragraph);
    assertThat(paragraph.getParent()).isSameAs(listItem);
  }

  @Test
  public void fencedCodeBlockShouldValidateFenceLengths() {
    FencedCodeBlock block = new FencedCodeBlock();

    assertThatThrownBy(() -> block.setOpeningFenceLength(2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(">= 3");
    assertThatThrownBy(() -> block.setClosingFenceLength(2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(">= 3");

    block.setOpeningFenceLength(3);
    assertThatThrownBy(() -> block.setClosingFenceLength(2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(">= 3");

    block.setClosingFenceLength(3);
    assertThat(block.getOpeningFenceLength()).isEqualTo(3);
    assertThat(block.getClosingFenceLength()).isEqualTo(3);
  }

  private static List<String> childrenLiterals(Paragraph paragraph) {
    List<String> values = new ArrayList<>();
    for (Node node = paragraph.getFirstChild(); node != null; node = node.getNext()) {
      values.add(((Text) node).getLiteral());
    }
    return values;
  }
}
