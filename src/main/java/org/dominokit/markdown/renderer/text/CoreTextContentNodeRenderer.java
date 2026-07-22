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

/**
 * Core text-content node renderer.
 *
 * <p>This renderer turns the markdown AST into readable plain text rather than a verbatim source
 * reproduction. It preserves enough structure to keep block quotes, lists, links, and images
 * understandable while still collapsing markdown syntax into a human-friendly summary.
 */
public class CoreTextContentNodeRenderer extends AbstractVisitor implements NodeRenderer {

  protected final TextContentNodeRendererContext context;
  private final TextContentWriter textContent;

  private ListHolder listHolder;

  /**
   * Create a renderer bound to the active text-content rendering context.
   *
   * @param context render-time context used for output and line-break policy
   */
  public CoreTextContentNodeRenderer(TextContentNodeRendererContext context) {
    this.context = context;
    this.textContent = context.getWriter();
  }

  /**
   * Return the node types handled by this renderer.
   *
   * <p>The set mirrors the built-in markdown AST so the renderer can act as the default fallback
   * for plain-text output.
   */
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

  /** Render the supplied node by dispatching to the matching visit method. */
  @Override
  public void render(Node node) {
    node.accept(this);
  }

  /** Render the document by visiting its children. */
  @Override
  public void visit(Document document) {
    visitChildren(document);
  }

  /** Render block quotes using guillemets to keep the nesting visually distinct in plain text. */
  @Override
  public void visit(BlockQuote blockQuote) {
    textContent.write('\u00AB');
    visitChildren(blockQuote);
    textContent.resetBlock();
    textContent.write('\u00BB');
    textContent.block();
  }

  /** Render bullet lists while preserving nesting and optional bullet markers. */
  @Override
  public void visit(BulletList bulletList) {
    textContent.pushTight(bulletList.isTight());
    listHolder = new BulletListHolder(listHolder, bulletList);
    visitChildren(bulletList);
    textContent.popTight();
    textContent.block();
    listHolder = listHolder.getParent();
  }

  /** Render inline code as quoted text. */
  @Override
  public void visit(Code code) {
    textContent.write('"');
    textContent.write(code.getLiteral());
    textContent.write('"');
  }

  /**
   * Render fenced code blocks as plain text content.
   *
   * <p>The trailing newline is stripped before emission so the surrounding block separator can
   * decide how much spacing to add.
   */
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

  /** Render hard line breaks as either whitespace or actual newlines depending on mode. */
  @Override
  public void visit(HardLineBreak hardLineBreak) {
    if (stripNewlines()) {
      textContent.whitespace();
    } else {
      textContent.line();
    }
  }

  /**
   * Render headings as plain text followed by either a separator or a colon, depending on the
   * configured line-break mode.
   */
  @Override
  public void visit(Heading heading) {
    visitChildren(heading);
    if (stripNewlines()) {
      textContent.write(": ");
    } else {
      textContent.block();
    }
  }

  /** Render thematic breaks as separators when line breaks are preserved. */
  @Override
  public void visit(ThematicBreak thematicBreak) {
    if (!stripNewlines()) {
      textContent.write("***");
    }
    textContent.block();
  }

  /** Render inline HTML as plain text. */
  @Override
  public void visit(HtmlInline htmlInline) {
    writeText(htmlInline.getLiteral());
  }

  /** Render HTML blocks as plain text. */
  @Override
  public void visit(HtmlBlock htmlBlock) {
    writeText(htmlBlock.getLiteral());
  }

  /** Render images as descriptive link-like text. */
  @Override
  public void visit(Image image) {
    writeLink(image, image.getTitle(), image.getDestination());
  }

  /** Render indented code blocks as plain text content. */
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

  /** Render links as readable link-like text. */
  @Override
  public void visit(Link link) {
    writeLink(link, link.getTitle(), link.getDestination());
  }

  /** Render list items using the current list marker and indentation context. */
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

  /** Render ordered lists while tracking the numeric marker state. */
  @Override
  public void visit(OrderedList orderedList) {
    textContent.pushTight(orderedList.isTight());
    listHolder = new OrderedListHolder(listHolder, orderedList);
    visitChildren(orderedList);
    textContent.popTight();
    textContent.block();
    listHolder = listHolder.getParent();
  }

  /** Render paragraphs as flowed text with a block separator afterward. */
  @Override
  public void visit(Paragraph paragraph) {
    visitChildren(paragraph);
    textContent.block();
  }

  /** Render soft line breaks as either whitespace or actual newlines depending on mode. */
  @Override
  public void visit(SoftLineBreak softLineBreak) {
    if (stripNewlines()) {
      textContent.whitespace();
    } else {
      textContent.line();
    }
  }

  /** Render plain text using the configured whitespace-collapsing rules. */
  @Override
  public void visit(Text text) {
    writeText(text.getLiteral());
  }

  /** Delegate child traversal to the active rendering context. */
  @Override
  protected void visitChildren(Node parent) {
    Node node = parent.getFirstChild();
    while (node != null) {
      Node next = node.getNext();
      context.render(node);
      node = next;
    }
  }

  /**
   * Write text using the configured newline handling.
   *
   * @param text text to render
   */
  private void writeText(String text) {
    if (stripNewlines()) {
      textContent.writeStripped(text);
    } else {
      textContent.write(text);
    }
  }

  /**
   * Render a link or image into a readable text representation.
   *
   * <p>The output keeps titles and destinations understandable without reproducing full markdown
   * syntax.
   */
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

  /**
   * @return whether line breaks should be flattened
   */
  private boolean stripNewlines() {
    return context.lineBreakRendering() == LineBreakRendering.STRIP;
  }

  /**
   * Remove a single trailing newline, if present.
   *
   * @param s input text
   * @return text without one trailing newline
   */
  private static String stripTrailingNewline(String s) {
    if (s.endsWith("\n")) {
      return s.substring(0, s.length() - 1);
    }
    return s;
  }

  // Keep for browser compatibility where String.repeat is not universally available.
  /**
   * Repeat the given string a fixed number of times.
   *
   * @param s string to repeat
   * @param count repetition count
   * @return repeated string
   */
  private static String repeat(String s, int count) {
    StringBuilder sb = new StringBuilder(s.length() * count);
    for (int i = 0; i < count; i++) {
      sb.append(s);
    }
    return sb.toString();
  }

  /** Stack frame for bullet lists. */
  private static final class BulletListHolder extends ListHolder {
    private final String marker;

    /**
     * Create a bullet-list stack frame.
     *
     * @param parent enclosing list holder
     * @param list current bullet list
     */
    private BulletListHolder(ListHolder parent, BulletList list) {
      super(parent);
      this.marker = list.getMarker();
    }

    /**
     * @return the bullet marker used for this list
     */
    private String getMarker() {
      return marker;
    }
  }

  /** Base stack frame for list rendering. */
  private abstract static class ListHolder {
    private final ListHolder parent;

    /**
     * Create a list stack frame.
     *
     * @param parent enclosing list holder
     */
    private ListHolder(ListHolder parent) {
      this.parent = parent;
    }

    /**
     * @return the enclosing list holder, or {@code null} at the root
     */
    private ListHolder getParent() {
      return parent;
    }
  }

  /** Stack frame for ordered lists. */
  private static final class OrderedListHolder extends ListHolder {
    private final String delimiter;
    private int counter;

    /**
     * Create an ordered-list stack frame.
     *
     * @param parent enclosing list holder
     * @param list current ordered list
     */
    private OrderedListHolder(ListHolder parent, OrderedList list) {
      super(parent);
      this.delimiter = list.getMarkerDelimiter() != null ? list.getMarkerDelimiter() : ".";
      this.counter = list.getMarkerStartNumber() != null ? list.getMarkerStartNumber() : 1;
    }

    /**
     * @return the ordered-list delimiter, such as {@code .} or {@code )}
     */
    private String getDelimiter() {
      return delimiter;
    }

    /**
     * @return the current list item number
     */
    private int getCounter() {
      return counter;
    }

    /** Advance the ordered-list marker to the next number. */
    private void increaseCounter() {
      counter++;
    }
  }
}
