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

  @Override
  public void visit(BlockQuote blockQuote) {
    visitChildren(blockQuote);
  }

  @Override
  public void visit(BulletList bulletList) {
    visitChildren(bulletList);
  }

  @Override
  public void visit(Code code) {
    visitChildren(code);
  }

  @Override
  public void visit(CustomBlock customBlock) {
    visitChildren(customBlock);
  }

  @Override
  public void visit(CustomNode customNode) {
    visitChildren(customNode);
  }

  @Override
  public void visit(Document document) {
    visitChildren(document);
  }

  @Override
  public void visit(Emphasis emphasis) {
    visitChildren(emphasis);
  }

  @Override
  public void visit(FencedCodeBlock fencedCodeBlock) {
    visitChildren(fencedCodeBlock);
  }

  @Override
  public void visit(HardLineBreak hardLineBreak) {
    visitChildren(hardLineBreak);
  }

  @Override
  public void visit(Heading heading) {
    visitChildren(heading);
  }

  @Override
  public void visit(HtmlBlock htmlBlock) {
    visitChildren(htmlBlock);
  }

  @Override
  public void visit(HtmlInline htmlInline) {
    visitChildren(htmlInline);
  }

  @Override
  public void visit(Image image) {
    visitChildren(image);
  }

  @Override
  public void visit(IndentedCodeBlock indentedCodeBlock) {
    visitChildren(indentedCodeBlock);
  }

  @Override
  public void visit(Link link) {
    visitChildren(link);
  }

  @Override
  public void visit(LinkReferenceDefinition linkReferenceDefinition) {
    visitChildren(linkReferenceDefinition);
  }

  @Override
  public void visit(ListItem listItem) {
    visitChildren(listItem);
  }

  @Override
  public void visit(OrderedList orderedList) {
    visitChildren(orderedList);
  }

  @Override
  public void visit(Paragraph paragraph) {
    visitChildren(paragraph);
  }

  @Override
  public void visit(SoftLineBreak softLineBreak) {
    visitChildren(softLineBreak);
  }

  @Override
  public void visit(StrongEmphasis strongEmphasis) {
    visitChildren(strongEmphasis);
  }

  @Override
  public void visit(Text text) {
    visitChildren(text);
  }

  @Override
  public void visit(ThematicBreak thematicBreak) {
    visitChildren(thematicBreak);
  }

  protected void visitChildren(Node parent) {
    Node node = parent.getFirstChild();
    while (node != null) {
      Node next = node.getNext();
      node.accept(this);
      node = next;
    }
  }
}
