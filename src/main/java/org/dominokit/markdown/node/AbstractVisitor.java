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

/**
 * Abstract visitor that visits all children by default.
 *
 * <p>Can be used to only process certain nodes. If you override a method and want visiting to
 * descend into children, call {@link #visitChildren(Node)}.
 */
public abstract class AbstractVisitor implements Visitor {

  /** Visit the block quote's children by default. */
  @Override
  public void visit(BlockQuote blockQuote) {
    visitChildren(blockQuote);
  }

  /** Visit the bullet list's children by default. */
  @Override
  public void visit(BulletList bulletList) {
    visitChildren(bulletList);
  }

  /** Visit the code node's children by default. */
  @Override
  public void visit(Code code) {
    visitChildren(code);
  }

  /** Visit the custom block's children by default. */
  @Override
  public void visit(CustomBlock customBlock) {
    visitChildren(customBlock);
  }

  /** Visit the custom node's children by default. */
  @Override
  public void visit(CustomNode customNode) {
    visitChildren(customNode);
  }

  /** Visit the document's children by default. */
  @Override
  public void visit(Document document) {
    visitChildren(document);
  }

  /** Visit the emphasis node's children by default. */
  @Override
  public void visit(Emphasis emphasis) {
    visitChildren(emphasis);
  }

  /** Visit the fenced code block's children by default. */
  @Override
  public void visit(FencedCodeBlock fencedCodeBlock) {
    visitChildren(fencedCodeBlock);
  }

  /** Visit the hard line break's children by default. */
  @Override
  public void visit(HardLineBreak hardLineBreak) {
    visitChildren(hardLineBreak);
  }

  /** Visit the heading's children by default. */
  @Override
  public void visit(Heading heading) {
    visitChildren(heading);
  }

  /** Visit the HTML block's children by default. */
  @Override
  public void visit(HtmlBlock htmlBlock) {
    visitChildren(htmlBlock);
  }

  /** Visit the HTML inline's children by default. */
  @Override
  public void visit(HtmlInline htmlInline) {
    visitChildren(htmlInline);
  }

  /** Visit the image's children by default. */
  @Override
  public void visit(Image image) {
    visitChildren(image);
  }

  /** Visit the indented code block's children by default. */
  @Override
  public void visit(IndentedCodeBlock indentedCodeBlock) {
    visitChildren(indentedCodeBlock);
  }

  /** Visit the link's children by default. */
  @Override
  public void visit(Link link) {
    visitChildren(link);
  }

  /** Visit the link reference definition's children by default. */
  @Override
  public void visit(LinkReferenceDefinition linkReferenceDefinition) {
    visitChildren(linkReferenceDefinition);
  }

  /** Visit the list item's children by default. */
  @Override
  public void visit(ListItem listItem) {
    visitChildren(listItem);
  }

  /** Visit the ordered list's children by default. */
  @Override
  public void visit(OrderedList orderedList) {
    visitChildren(orderedList);
  }

  /** Visit the paragraph's children by default. */
  @Override
  public void visit(Paragraph paragraph) {
    visitChildren(paragraph);
  }

  /** Visit the soft line break's children by default. */
  @Override
  public void visit(SoftLineBreak softLineBreak) {
    visitChildren(softLineBreak);
  }

  /** Visit the strong emphasis node's children by default. */
  @Override
  public void visit(StrongEmphasis strongEmphasis) {
    visitChildren(strongEmphasis);
  }

  /** Visit the text node's children by default. */
  @Override
  public void visit(Text text) {
    visitChildren(text);
  }

  /** Visit the thematic break's children by default. */
  @Override
  public void visit(ThematicBreak thematicBreak) {
    visitChildren(thematicBreak);
  }

  /**
   * Visit all children of the supplied parent node in document order.
   *
   * @param parent node whose children should be traversed
   */
  protected void visitChildren(Node parent) {
    Node node = parent.getFirstChild();
    while (node != null) {
      Node next = node.getNext();
      node.accept(this);
      node = next;
    }
  }
}
