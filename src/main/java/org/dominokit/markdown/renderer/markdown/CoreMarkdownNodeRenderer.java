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
package org.dominokit.markdown.renderer.markdown;

import java.util.ArrayList;
import java.util.List;
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
import org.dominokit.markdown.text.AsciiMatcher;
import org.dominokit.markdown.text.CharMatcher;
import org.dominokit.markdown.text.Characters;

/**
 * The core node renderer that renders the built-in node types to canonical Markdown text.
 *
 * <p>This renderer intentionally favors semantically equivalent Markdown over exact source
 * preservation.
 */
public class CoreMarkdownNodeRenderer extends AbstractVisitor implements NodeRenderer {

  private final AsciiMatcher textEscape;
  private final CharMatcher textEscapeInHeading;
  private final CharMatcher linkDestinationNeedsAngleBrackets =
      AsciiMatcher.builder().c(' ').c('(').c(')').c('<').c('>').c('\n').c('\\').build();
  private final CharMatcher linkDestinationEscapeInAngleBrackets =
      AsciiMatcher.builder().c('<').c('>').c('\n').c('\\').build();
  private final CharMatcher linkTitleEscapeInQuotes =
      AsciiMatcher.builder().c('"').c('\n').c('\\').build();

  protected final MarkdownNodeRendererContext context;
  private final MarkdownWriter writer;
  private ListHolder listHolder;

  public CoreMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
    this.context = context;
    this.writer = context.getWriter();
    this.textEscape =
        AsciiMatcher.builder().anyOf("[]<>`*_&\n\\").anyOf(context.getSpecialCharacters()).build();
    this.textEscapeInHeading = AsciiMatcher.builder(textEscape).anyOf("#").build();
  }

  @Override
  public Set<Class<? extends Node>> getNodeTypes() {
    return Set.of(
        BlockQuote.class,
        BulletList.class,
        Code.class,
        Document.class,
        Emphasis.class,
        FencedCodeBlock.class,
        HardLineBreak.class,
        Heading.class,
        HtmlBlock.class,
        HtmlInline.class,
        Image.class,
        IndentedCodeBlock.class,
        Link.class,
        ListItem.class,
        OrderedList.class,
        Paragraph.class,
        SoftLineBreak.class,
        StrongEmphasis.class,
        Text.class,
        ThematicBreak.class);
  }

  @Override
  public void render(Node node) {
    node.accept(this);
  }

  @Override
  public void visit(Document document) {
    visitChildren(document);
    writer.line();
  }

  @Override
  public void visit(ThematicBreak thematicBreak) {
    String literal = thematicBreak.getLiteral();
    if (literal == null) {
      literal = "___";
    }
    writer.raw(literal);
    writer.block();
  }

  @Override
  public void visit(Heading heading) {
    if (heading.getLevel() <= 2) {
      LineBreakVisitor lineBreakVisitor = new LineBreakVisitor();
      heading.accept(lineBreakVisitor);
      if (lineBreakVisitor.hasLineBreak()) {
        visitChildren(heading);
        writer.line();
        writer.raw(heading.getLevel() == 1 ? "===" : "---");
        writer.block();
        return;
      }
    }

    for (int i = 0; i < heading.getLevel(); i++) {
      writer.raw('#');
    }
    writer.raw(' ');
    visitChildren(heading);
    writer.block();
  }

  @Override
  public void visit(IndentedCodeBlock indentedCodeBlock) {
    writer.writePrefix("    ");
    writer.pushPrefix("    ");
    List<String> lines = getLines(indentedCodeBlock.getLiteral());
    for (int i = 0; i < lines.size(); i++) {
      writer.raw(lines.get(i));
      if (i != lines.size() - 1) {
        writer.line();
      }
    }
    writer.popPrefix();
    writer.block();
  }

  @Override
  public void visit(FencedCodeBlock codeBlock) {
    String literal = codeBlock.getLiteral();
    String fenceChar = codeBlock.getFenceCharacter() != null ? codeBlock.getFenceCharacter() : "`";
    int openingFenceLength;
    if (codeBlock.getOpeningFenceLength() != null) {
      openingFenceLength = codeBlock.getOpeningFenceLength();
    } else {
      int fenceCharsInLiteral = findMaxRunLength(fenceChar, literal);
      openingFenceLength = Math.max(fenceCharsInLiteral + 1, 3);
    }
    int closingFenceLength =
        codeBlock.getClosingFenceLength() != null
            ? codeBlock.getClosingFenceLength()
            : openingFenceLength;

    String openingFence = repeat(fenceChar, openingFenceLength);
    String closingFence = repeat(fenceChar, closingFenceLength);
    int indent = codeBlock.getFenceIndent();

    if (indent > 0) {
      String indentPrefix = repeat(" ", indent);
      writer.writePrefix(indentPrefix);
      writer.pushPrefix(indentPrefix);
    }

    writer.raw(openingFence);
    if (codeBlock.getInfo() != null) {
      writer.raw(codeBlock.getInfo());
    }
    writer.line();
    if (!literal.isEmpty()) {
      List<String> lines = getLines(literal);
      for (String line : lines) {
        writer.raw(line);
        writer.line();
      }
    }
    writer.raw(closingFence);
    if (indent > 0) {
      writer.popPrefix();
    }
    writer.block();
  }

  @Override
  public void visit(HtmlBlock htmlBlock) {
    List<String> lines = getLines(htmlBlock.getLiteral());
    for (int i = 0; i < lines.size(); i++) {
      writer.raw(lines.get(i));
      if (i != lines.size() - 1) {
        writer.line();
      }
    }
    writer.block();
  }

  @Override
  public void visit(Paragraph paragraph) {
    visitChildren(paragraph);
    writer.block();
  }

  @Override
  public void visit(BlockQuote blockQuote) {
    writer.writePrefix("> ");
    writer.pushPrefix("> ");
    visitChildren(blockQuote);
    writer.popPrefix();
    writer.block();
  }

  @Override
  public void visit(BulletList bulletList) {
    writer.pushTight(bulletList.isTight());
    listHolder = new BulletListHolder(listHolder, bulletList);
    visitChildren(bulletList);
    listHolder = listHolder.parent;
    writer.popTight();
    writer.block();
  }

  @Override
  public void visit(OrderedList orderedList) {
    writer.pushTight(orderedList.isTight());
    listHolder = new OrderedListHolder(listHolder, orderedList);
    visitChildren(orderedList);
    listHolder = listHolder.parent;
    writer.popTight();
    writer.block();
  }

  @Override
  public void visit(ListItem listItem) {
    int markerIndent = listItem.getMarkerIndent() != null ? listItem.getMarkerIndent() : 0;
    String marker;
    if (listHolder instanceof BulletListHolder) {
      marker = repeat(" ", markerIndent) + ((BulletListHolder) listHolder).marker;
    } else if (listHolder instanceof OrderedListHolder) {
      OrderedListHolder orderedListHolder = (OrderedListHolder) listHolder;
      marker = repeat(" ", markerIndent) + orderedListHolder.number + orderedListHolder.delimiter;
      orderedListHolder.number++;
    } else {
      throw new IllegalStateException("Unknown list holder type: " + listHolder);
    }

    Integer contentIndent = listItem.getContentIndent();
    String spaces =
        contentIndent != null ? repeat(" ", Math.max(contentIndent - marker.length(), 1)) : " ";
    writer.writePrefix(marker);
    writer.writePrefix(spaces);
    writer.pushPrefix(repeat(" ", marker.length() + spaces.length()));

    if (listItem.getFirstChild() == null) {
      writer.block();
    } else {
      visitChildren(listItem);
    }

    writer.popPrefix();
  }

  @Override
  public void visit(Code code) {
    String literal = code.getLiteral();
    int backticks = findMaxRunLength("`", literal);
    for (int i = 0; i < backticks + 1; i++) {
      writer.raw('`');
    }

    boolean addSpace =
        literal.startsWith("`")
            || literal.endsWith("`")
            || (literal.startsWith(" ")
                && literal.endsWith(" ")
                && Characters.hasNonSpace(literal));
    if (addSpace) {
      writer.raw(' ');
    }
    writer.raw(literal);
    if (addSpace) {
      writer.raw(' ');
    }

    for (int i = 0; i < backticks + 1; i++) {
      writer.raw('`');
    }
  }

  @Override
  public void visit(Emphasis emphasis) {
    String delimiter = emphasis.getOpeningDelimiter();
    if (delimiter == null) {
      delimiter = writer.getLastChar() == '*' ? "_" : "*";
    }
    writer.raw(delimiter);
    super.visit(emphasis);
    writer.raw(emphasis.getClosingDelimiter() != null ? emphasis.getClosingDelimiter() : delimiter);
  }

  @Override
  public void visit(StrongEmphasis strongEmphasis) {
    String delimiter = strongEmphasis.getOpeningDelimiter();
    if (delimiter == null) {
      delimiter = "**";
    }
    writer.raw(delimiter);
    super.visit(strongEmphasis);
    writer.raw(
        strongEmphasis.getClosingDelimiter() != null
            ? strongEmphasis.getClosingDelimiter()
            : delimiter);
  }

  @Override
  public void visit(Link link) {
    writeLinkLike(link.getTitle(), link.getDestination(), link, "[");
  }

  @Override
  public void visit(Image image) {
    writeLinkLike(image.getTitle(), image.getDestination(), image, "![");
  }

  @Override
  public void visit(HtmlInline htmlInline) {
    writer.raw(htmlInline.getLiteral());
  }

  @Override
  public void visit(HardLineBreak hardLineBreak) {
    writer.raw("  ");
    writer.line();
  }

  @Override
  public void visit(SoftLineBreak softLineBreak) {
    writer.line();
  }

  @Override
  public void visit(Text text) {
    String literal = text.getLiteral();
    if (writer.isAtLineStart() && !literal.isEmpty()) {
      char c = literal.charAt(0);
      switch (c) {
        case '-':
          writer.raw("\\-");
          literal = literal.substring(1);
          break;
        case '#':
          writer.raw("\\#");
          literal = literal.substring(1);
          break;
        case '=':
          if (text.getPrevious() != null) {
            writer.raw("\\=");
            literal = literal.substring(1);
          }
          break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          OrderedListMarker marker = parseOrderedListMarker(literal);
          if (marker != null) {
            writer.raw(marker.number);
            writer.raw("\\" + marker.delimiter);
            literal = literal.substring(marker.endIndex);
          }
          break;
        case '\t':
          writer.raw("&#9;");
          literal = literal.substring(1);
          break;
        case ' ':
          writer.raw("&#32;");
          literal = literal.substring(1);
          break;
        default:
          break;
      }
    }

    CharMatcher escape = text.getParent() instanceof Heading ? textEscapeInHeading : textEscape;
    if (literal.endsWith("!") && text.getNext() instanceof Link) {
      writer.text(literal.substring(0, literal.length() - 1), escape);
      writer.raw("\\!");
    } else {
      writer.text(literal, escape);
    }
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

  private static int findMaxRunLength(String needle, String s) {
    int maxRunLength = 0;
    int pos = 0;
    while (pos < s.length()) {
      pos = s.indexOf(needle, pos);
      if (pos == -1) {
        return maxRunLength;
      }
      int runLength = 0;
      do {
        pos += needle.length();
        runLength++;
      } while (s.startsWith(needle, pos));
      maxRunLength = Math.max(runLength, maxRunLength);
    }
    return maxRunLength;
  }

  private static boolean contains(String s, CharMatcher charMatcher) {
    for (int i = 0; i < s.length(); i++) {
      if (charMatcher.matches(s.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  private static String repeat(String s, int count) {
    StringBuilder sb = new StringBuilder(s.length() * count);
    for (int i = 0; i < count; i++) {
      sb.append(s);
    }
    return sb.toString();
  }

  private static List<String> getLines(String literal) {
    List<String> lines = new ArrayList<>();
    if (literal.isEmpty()) {
      return lines;
    }

    int start = 0;
    while (start < literal.length()) {
      int lineBreak = literal.indexOf('\n', start);
      if (lineBreak == -1) {
        lines.add(literal.substring(start));
        break;
      }
      lines.add(literal.substring(start, lineBreak));
      start = lineBreak + 1;
      if (start == literal.length()) {
        break;
      }
    }
    return lines;
  }

  private void writeLinkLike(String title, String destination, Node node, String opener) {
    String safeDestination = destination != null ? destination : "";
    writer.raw(opener);
    visitChildren(node);
    writer.raw(']');
    writer.raw('(');
    if (contains(safeDestination, linkDestinationNeedsAngleBrackets)) {
      writer.raw('<');
      writer.text(safeDestination, linkDestinationEscapeInAngleBrackets);
      writer.raw('>');
    } else {
      writer.raw(safeDestination);
    }
    if (title != null) {
      writer.raw(' ');
      writer.raw('"');
      writer.text(title, linkTitleEscapeInQuotes);
      writer.raw('"');
    }
    writer.raw(')');
  }

  private static OrderedListMarker parseOrderedListMarker(String literal) {
    int length = literal.length();
    int index = 0;
    while (index < length && index < 9 && isAsciiDigit(literal.charAt(index))) {
      index++;
    }
    if (index == 0 || index >= length) {
      return null;
    }
    if (isAsciiDigit(literal.charAt(index))) {
      return null;
    }
    char delimiter = literal.charAt(index);
    if (delimiter != '.' && delimiter != ')') {
      return null;
    }
    return new OrderedListMarker(literal.substring(0, index), delimiter, index + 1);
  }

  private static boolean isAsciiDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static class ListHolder {
    final ListHolder parent;

    private ListHolder(ListHolder parent) {
      this.parent = parent;
    }
  }

  private static class BulletListHolder extends ListHolder {
    final String marker;

    private BulletListHolder(ListHolder parent, BulletList bulletList) {
      super(parent);
      this.marker = bulletList.getMarker() != null ? bulletList.getMarker() : "-";
    }
  }

  private static class OrderedListHolder extends ListHolder {
    final String delimiter;
    private int number;

    private OrderedListHolder(ListHolder parent, OrderedList orderedList) {
      super(parent);
      this.delimiter =
          orderedList.getMarkerDelimiter() != null ? orderedList.getMarkerDelimiter() : ".";
      this.number =
          orderedList.getMarkerStartNumber() != null ? orderedList.getMarkerStartNumber() : 1;
    }
  }

  private static final class OrderedListMarker {
    private final String number;
    private final char delimiter;
    private final int endIndex;

    private OrderedListMarker(String number, char delimiter, int endIndex) {
      this.number = number;
      this.delimiter = delimiter;
      this.endIndex = endIndex;
    }
  }

  /** Visits nodes to check whether a heading contains soft or hard line breaks. */
  private static class LineBreakVisitor extends AbstractVisitor {
    private boolean lineBreak;

    private boolean hasLineBreak() {
      return lineBreak;
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
      super.visit(softLineBreak);
      lineBreak = true;
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
      super.visit(hardLineBreak);
      lineBreak = true;
    }
  }
}
