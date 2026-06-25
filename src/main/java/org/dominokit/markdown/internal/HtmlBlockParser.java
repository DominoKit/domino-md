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
package org.dominokit.markdown.internal;

import java.util.Locale;
import java.util.Set;
import org.dominokit.markdown.node.Block;
import org.dominokit.markdown.node.HtmlBlock;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.parser.SourceLine;
import org.dominokit.markdown.parser.block.AbstractBlockParser;
import org.dominokit.markdown.parser.block.AbstractBlockParserFactory;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.BlockStart;
import org.dominokit.markdown.parser.block.MatchedBlockParser;
import org.dominokit.markdown.parser.block.ParserState;

public class HtmlBlockParser extends AbstractBlockParser {

  private static final Set<String> RAW_HTML_TAGS = Set.of("script", "pre", "style", "textarea");

  private static final Set<String> BLOCK_6_TAGS =
      Set.of(
          "address",
          "article",
          "aside",
          "base",
          "basefont",
          "blockquote",
          "body",
          "caption",
          "center",
          "col",
          "colgroup",
          "dd",
          "details",
          "dialog",
          "dir",
          "div",
          "dl",
          "dt",
          "fieldset",
          "figcaption",
          "figure",
          "footer",
          "form",
          "frame",
          "frameset",
          "h1",
          "h2",
          "h3",
          "h4",
          "h5",
          "h6",
          "head",
          "header",
          "hr",
          "html",
          "iframe",
          "legend",
          "li",
          "link",
          "main",
          "menu",
          "menuitem",
          "nav",
          "noframes",
          "ol",
          "optgroup",
          "option",
          "p",
          "param",
          "search",
          "section",
          "summary",
          "table",
          "tbody",
          "td",
          "tfoot",
          "th",
          "thead",
          "title",
          "tr",
          "track",
          "ul");

  private final HtmlBlock block = new HtmlBlock();
  private final LineCondition closingPattern;
  private boolean finished;
  private BlockContent content = new BlockContent();

  private HtmlBlockParser(LineCondition closingPattern) {
    this.closingPattern = closingPattern;
  }

  @Override
  public Block getBlock() {
    return block;
  }

  @Override
  public BlockContinue tryContinue(ParserState state) {
    if (finished) {
      return BlockContinue.none();
    }
    if (state.isBlank() && closingPattern == null) {
      return BlockContinue.none();
    }
    return BlockContinue.atIndex(state.getIndex());
  }

  @Override
  public void addLine(SourceLine line) {
    content.add(line.getContent());
    if (closingPattern != null && closingPattern.matches(line.getContent())) {
      finished = true;
    }
  }

  @Override
  public void closeBlock() {
    block.setLiteral(content.getString());
    content = null;
  }

  public static class Factory extends AbstractBlockParserFactory {

    @Override
    public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
      int nextNonSpace = state.getNextNonSpaceIndex();
      CharSequence line = state.getLine().getContent();

      if (state.getIndent() < 4 && line.charAt(nextNonSpace) == '<') {
        for (int blockType = 1; blockType <= 7; blockType++) {
          if (blockType == 7
              && (matchedBlockParser.getMatchedBlockParser().getBlock() instanceof Paragraph
                  || state.getActiveBlockParser().canHaveLazyContinuationLines())) {
            continue;
          }
          String content = line.subSequence(nextNonSpace, line.length()).toString();
          if (matchesBlockType(blockType, content)) {
            return BlockStart.of(new HtmlBlockParser(getClosingCondition(blockType)))
                .atIndex(state.getIndex());
          }
        }
      }
      return BlockStart.none();
    }
  }

  private static boolean matchesBlockType(int blockType, String content) {
    switch (blockType) {
      case 1:
        return startsNamedTag(content, RAW_HTML_TAGS, false);
      case 2:
        return content.startsWith("<!--");
      case 3:
        return content.startsWith("<?");
      case 4:
        return content.length() >= 3
            && content.startsWith("<!")
            && isAsciiUpperCase(content.charAt(2));
      case 5:
        return content.startsWith("<![CDATA[");
      case 6:
        return startsNamedTag(content, BLOCK_6_TAGS, true);
      case 7:
        return isTagLine(content);
      default:
        return false;
    }
  }

  private static LineCondition getClosingCondition(int blockType) {
    switch (blockType) {
      case 1:
        return line -> containsNamedClosingTag(line.toString(), RAW_HTML_TAGS);
      case 2:
        return line -> line.toString().contains("-->");
      case 3:
        return line -> line.toString().contains("?>");
      case 4:
        return line -> line.toString().indexOf('>') >= 0;
      case 5:
        return line -> line.toString().contains("]]>");
      default:
        return null;
    }
  }

  private static boolean startsNamedTag(
      String content, Set<String> names, boolean allowClosingTag) {
    if (!content.startsWith("<")) {
      return false;
    }

    int index = 1;
    if (allowClosingTag && index < content.length() && content.charAt(index) == '/') {
      index++;
    } else if (!allowClosingTag && index < content.length() && content.charAt(index) == '/') {
      return false;
    }

    int nameStart = index;
    if (nameStart >= content.length() || !isTagNameStart(content.charAt(nameStart))) {
      return false;
    }
    index++;
    while (index < content.length() && isTagNameContinue(content.charAt(index))) {
      index++;
    }

    String tagName = content.substring(nameStart, index).toLowerCase(Locale.ROOT);
    if (!names.contains(tagName)) {
      return false;
    }

    if (index >= content.length()) {
      return true;
    }

    char next = content.charAt(index);
    return isHtmlWhitespace(next)
        || next == '>'
        || (next == '/' && index + 1 < content.length() && content.charAt(index + 1) == '>');
  }

  private static boolean containsNamedClosingTag(String content, Set<String> names) {
    String lower = content.toLowerCase(Locale.ROOT);
    for (String tagName : names) {
      if (lower.contains("</" + tagName + ">")) {
        return true;
      }
    }
    return false;
  }

  private static boolean isTagLine(String content) {
    int end = parseHtmlTag(content, 0);
    if (end == -1) {
      return false;
    }

    for (int i = end; i < content.length(); i++) {
      if (!isHtmlWhitespace(content.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  private static int parseHtmlTag(String content, int index) {
    if (index >= content.length() || content.charAt(index) != '<') {
      return -1;
    }

    if (index + 1 < content.length() && content.charAt(index + 1) == '/') {
      return parseClosingTag(content, index + 2);
    }
    return parseOpenTag(content, index + 1);
  }

  private static int parseClosingTag(String content, int index) {
    if (index >= content.length() || !isTagNameStart(content.charAt(index))) {
      return -1;
    }

    index++;
    while (index < content.length() && isTagNameContinue(content.charAt(index))) {
      index++;
    }
    index = skipHtmlWhitespace(content, index);
    return index < content.length() && content.charAt(index) == '>' ? index + 1 : -1;
  }

  private static int parseOpenTag(String content, int index) {
    if (index >= content.length() || !isTagNameStart(content.charAt(index))) {
      return -1;
    }

    index++;
    while (index < content.length() && isTagNameContinue(content.charAt(index))) {
      index++;
    }

    int afterWhitespace = skipHtmlWhitespace(content, index);
    boolean whitespace = afterWhitespace > index;
    index = afterWhitespace;
    while (whitespace && index < content.length() && isAttributeStart(content.charAt(index))) {
      index++;
      while (index < content.length() && isAttributeContinue(content.charAt(index))) {
        index++;
      }

      afterWhitespace = skipHtmlWhitespace(content, index);
      if (afterWhitespace < content.length() && content.charAt(afterWhitespace) == '=') {
        index = skipHtmlWhitespace(content, afterWhitespace + 1);
        index = parseAttributeValue(content, index);
        if (index == -1) {
          return -1;
        }
        afterWhitespace = skipHtmlWhitespace(content, index);
      }

      whitespace = afterWhitespace > index;
      index = afterWhitespace;
    }

    if (index < content.length() && content.charAt(index) == '/') {
      index++;
    }
    return index < content.length() && content.charAt(index) == '>' ? index + 1 : -1;
  }

  private static int parseAttributeValue(String content, int index) {
    if (index >= content.length()) {
      return -1;
    }

    char valueStart = content.charAt(index);
    if (valueStart == '\'') {
      int end = content.indexOf('\'', index + 1);
      return end == -1 ? -1 : end + 1;
    } else if (valueStart == '"') {
      int end = content.indexOf('"', index + 1);
      return end == -1 ? -1 : end + 1;
    }

    int end = index;
    while (end < content.length() && !isAttributeValueEnd(content.charAt(end))) {
      end++;
    }
    return end > index ? end : -1;
  }

  private static int skipHtmlWhitespace(String content, int index) {
    while (index < content.length() && isHtmlWhitespace(content.charAt(index))) {
      index++;
    }
    return index;
  }

  private static boolean isAsciiUpperCase(char c) {
    return c >= 'A' && c <= 'Z';
  }

  private static boolean isTagNameStart(char c) {
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
  }

  private static boolean isTagNameContinue(char c) {
    return isTagNameStart(c) || (c >= '0' && c <= '9') || c == '-';
  }

  private static boolean isAttributeStart(char c) {
    return isTagNameStart(c) || c == '_' || c == ':';
  }

  private static boolean isAttributeContinue(char c) {
    return isAttributeStart(c) || (c >= '0' && c <= '9') || c == '.' || c == '-';
  }

  private static boolean isAttributeValueEnd(char c) {
    return isHtmlWhitespace(c)
        || c == '"'
        || c == '\''
        || c == '='
        || c == '<'
        || c == '>'
        || c == '`';
  }

  private static boolean isHtmlWhitespace(char c) {
    switch (c) {
      case ' ':
      case '\t':
      case '\n':
      case '\u000B':
      case '\f':
      case '\r':
        return true;
      default:
        return false;
    }
  }

  @FunctionalInterface
  private interface LineCondition {
    boolean matches(CharSequence line);
  }
}
