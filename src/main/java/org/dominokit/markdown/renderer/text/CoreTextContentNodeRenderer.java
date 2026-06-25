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
package org.dominokit.markdown.renderer.text;

import java.util.Set;
import org.dominokit.markdown.node.AbstractVisitor;
import org.dominokit.markdown.node.BlockQuote;
import org.dominokit.markdown.node.BulletList;
import org.dominokit.markdown.node.Code;
import org.dominokit.markdown.node.Document;
import org.dominokit.markdown.node.Emphasis;
import org.dominokit.markdown.node.FencedCodeBlock;
import org.dominokit.markdown.node.HardLineBreak;
import org.dominokit.markdown.node.Heading;
import org.dominokit.markdown.node.HtmlBlock;
import org.dominokit.markdown.node.HtmlInline;
import org.dominokit.markdown.node.Image;
import org.dominokit.markdown.node.IndentedCodeBlock;
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.ListItem;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.OrderedList;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.node.SoftLineBreak;
import org.dominokit.markdown.node.StrongEmphasis;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.node.ThematicBreak;
import org.dominokit.markdown.renderer.NodeRenderer;

/** The node renderer that renders all the core nodes. */
public class CoreTextContentNodeRenderer extends AbstractVisitor implements NodeRenderer {

  protected final TextContentNodeRendererContext context;
  private final TextContentWriter textContent;

  private ListHolder listHolder;

  public CoreTextContentNodeRenderer(TextContentNodeRendererContext context) {
    this.context = context;
    this.textContent = context.getWriter();
  }

  @Override
  public Set<Class<? extends Node>> getNodeTypes() {
    return Set.of(
        Document.class,
        Heading.class,
        Paragraph.class,
        BlockQuote.class,
        BulletList.class,
        FencedCodeBlock.class,
        HtmlBlock.class,
        ThematicBreak.class,
        IndentedCodeBlock.class,
        Link.class,
        ListItem.class,
        OrderedList.class,
        Image.class,
        Emphasis.class,
        StrongEmphasis.class,
        Text.class,
        Code.class,
        HtmlInline.class,
        SoftLineBreak.class,
        HardLineBreak.class);
  }

  @Override
  public void render(Node node) {
    node.accept(this);
  }

  @Override
  public void visit(Document document) {
    visitChildren(document);
  }

  @Override
  public void visit(BlockQuote blockQuote) {
    textContent.write('\u00AB');
    visitChildren(blockQuote);
    textContent.resetBlock();
    textContent.write('\u00BB');
    textContent.block();
  }

  @Override
  public void visit(BulletList bulletList) {
    textContent.pushTight(bulletList.isTight());
    listHolder = new BulletListHolder(listHolder, bulletList);
    visitChildren(bulletList);
    textContent.popTight();
    textContent.block();
    listHolder = listHolder.getParent();
  }

  @Override
  public void visit(Code code) {
    textContent.write('"');
    textContent.write(code.getLiteral());
    textContent.write('"');
  }

  @Override
  public void visit(FencedCodeBlock fencedCodeBlock) {
    String literal = stripTrailingNewline(fencedCodeBlock.getLiteral());
    if (stripNewlines()) {
      textContent.writeStripped(literal);
    } else {
      textContent.write(literal);
    }
    textContent.block();
  }

  @Override
  public void visit(HardLineBreak hardLineBreak) {
    if (stripNewlines()) {
      textContent.whitespace();
    } else {
      textContent.line();
    }
  }

  @Override
  public void visit(Heading heading) {
    visitChildren(heading);
    if (stripNewlines()) {
      textContent.write(": ");
    } else {
      textContent.block();
    }
  }

  @Override
  public void visit(ThematicBreak thematicBreak) {
    if (!stripNewlines()) {
      textContent.write("***");
    }
    textContent.block();
  }

  @Override
  public void visit(HtmlInline htmlInline) {
    writeText(htmlInline.getLiteral());
  }

  @Override
  public void visit(HtmlBlock htmlBlock) {
    writeText(htmlBlock.getLiteral());
  }

  @Override
  public void visit(Image image) {
    writeLink(image, image.getTitle(), image.getDestination());
  }

  @Override
  public void visit(IndentedCodeBlock indentedCodeBlock) {
    String literal = stripTrailingNewline(indentedCodeBlock.getLiteral());
    if (stripNewlines()) {
      textContent.writeStripped(literal);
    } else {
      textContent.write(literal);
    }
    textContent.block();
  }

  @Override
  public void visit(Link link) {
    writeLink(link, link.getTitle(), link.getDestination());
  }

  @Override
  public void visit(ListItem listItem) {
    if (listHolder instanceof OrderedListHolder) {
      OrderedListHolder orderedListHolder = (OrderedListHolder) listHolder;
      String marker = orderedListHolder.getCounter() + orderedListHolder.getDelimiter();
      String spaces = " ";
      textContent.write(marker);
      textContent.write(spaces);
      textContent.pushPrefix(repeat(" ", marker.length() + spaces.length()));
      visitChildren(listItem);
      textContent.block();
      textContent.popPrefix();
      orderedListHolder.increaseCounter();
    } else if (listHolder instanceof BulletListHolder) {
      BulletListHolder bulletListHolder = (BulletListHolder) listHolder;
      if (!stripNewlines()) {
        String marker = bulletListHolder.getMarker();
        String spaces = " ";
        textContent.write(marker);
        textContent.write(spaces);
        textContent.pushPrefix(repeat(" ", marker.length() + spaces.length()));
      }
      visitChildren(listItem);
      textContent.block();
      if (!stripNewlines()) {
        textContent.popPrefix();
      }
    }
  }

  @Override
  public void visit(OrderedList orderedList) {
    textContent.pushTight(orderedList.isTight());
    listHolder = new OrderedListHolder(listHolder, orderedList);
    visitChildren(orderedList);
    textContent.popTight();
    textContent.block();
    listHolder = listHolder.getParent();
  }

  @Override
  public void visit(Paragraph paragraph) {
    visitChildren(paragraph);
    textContent.block();
  }

  @Override
  public void visit(SoftLineBreak softLineBreak) {
    if (stripNewlines()) {
      textContent.whitespace();
    } else {
      textContent.line();
    }
  }

  @Override
  public void visit(Text text) {
    writeText(text.getLiteral());
  }

  @Override
  protected void visitChildren(Node parent) {
    Node node = parent.getFirstChild();
    while (node != null) {
      Node next = node.getNext();
      context.render(node);
      node = next;
    }
  }

  private void writeText(String text) {
    if (stripNewlines()) {
      textContent.writeStripped(text);
    } else {
      textContent.write(text);
    }
  }

  private void writeLink(Node node, String title, String destination) {
    boolean hasChild = node.getFirstChild() != null;
    boolean hasTitle = title != null && !title.equals(destination);
    boolean hasDestination = destination != null && !destination.isEmpty();

    if (hasChild) {
      textContent.write('"');
      visitChildren(node);
      textContent.write('"');
      if (hasTitle || hasDestination) {
        textContent.whitespace();
        textContent.write('(');
      }
    }

    if (hasTitle) {
      textContent.write(title);
      if (hasDestination) {
        textContent.colon();
        textContent.whitespace();
      }
    }

    if (hasDestination) {
      textContent.write(destination);
    }

    if (hasChild && (hasTitle || hasDestination)) {
      textContent.write(')');
    }
  }

  private boolean stripNewlines() {
    return context.lineBreakRendering() == LineBreakRendering.STRIP;
  }

  private static String stripTrailingNewline(String s) {
    if (s.endsWith("\n")) {
      return s.substring(0, s.length() - 1);
    }
    return s;
  }

  // Keep for browser compatibility where String.repeat is not universally available.
  private static String repeat(String s, int count) {
    StringBuilder sb = new StringBuilder(s.length() * count);
    for (int i = 0; i < count; i++) {
      sb.append(s);
    }
    return sb.toString();
  }

  private static final class BulletListHolder extends ListHolder {
    private final String marker;

    private BulletListHolder(ListHolder parent, BulletList list) {
      super(parent);
      this.marker = list.getMarker();
    }

    private String getMarker() {
      return marker;
    }
  }

  private abstract static class ListHolder {
    private final ListHolder parent;

    private ListHolder(ListHolder parent) {
      this.parent = parent;
    }

    private ListHolder getParent() {
      return parent;
    }
  }

  private static final class OrderedListHolder extends ListHolder {
    private final String delimiter;
    private int counter;

    private OrderedListHolder(ListHolder parent, OrderedList list) {
      super(parent);
      this.delimiter = list.getMarkerDelimiter() != null ? list.getMarkerDelimiter() : ".";
      this.counter = list.getMarkerStartNumber() != null ? list.getMarkerStartNumber() : 1;
    }

    private String getDelimiter() {
      return delimiter;
    }

    private int getCounter() {
      return counter;
    }

    private void increaseCounter() {
      counter++;
    }
  }
}
