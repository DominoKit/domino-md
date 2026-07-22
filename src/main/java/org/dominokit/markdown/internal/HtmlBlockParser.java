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

/**
 * Parses CommonMark HTML blocks.
 *
 * <p>The parser recognizes the seven HTML block forms defined by the specification, including raw
 * HTML tags, comments, processing instructions, declarations, CDATA sections, block-level tags, and
 * tag lines. Some block types end when a matching closing line is seen; others remain open until a
 * blank line or end of input.
 */
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

  /**
   * Create an HTML block parser with the closing rule appropriate for the matched block type.
   *
   * @param closingPattern line predicate that marks the block as finished, or {@code null} for
   *     open-ended block types
   */
  private HtmlBlockParser(LineCondition closingPattern) {
    this.closingPattern = closingPattern;
  }

  /**
   * @return the HTML block node being accumulated
   */
  @Override
  public Block getBlock() {
    return block;
  }

  /**
   * Continue the block until a closing pattern is seen or, for open-ended HTML blocks, a blank line
   * ends the block.
   *
   * @param state current parser state
   * @return the index at which the current line should continue, or none if the block ended
   */
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

  /**
   * Record the raw HTML line content and update the finished flag when the closing pattern matches.
   *
   * @param line source line to append
   */
  @Override
  public void addLine(SourceLine line) {
    content.add(line.getContent());
    if (closingPattern != null && closingPattern.matches(line.getContent())) {
      finished = true;
    }
  }

  /** Commit the collected HTML content into the block literal. */
  @Override
  public void closeBlock() {
    block.setLiteral(content.getString());
    content = null;
  }

  /**
   * Recognizes the start of an HTML block.
   *
   * <p>The factory inspects the non-indented portion of the line and selects the first matching
   * block type. Some block types capture a closing condition so the parser can stop as soon as the
   * terminating line appears.
   */
  public static class Factory extends AbstractBlockParserFactory {

    /**
     * Try to start an HTML block at the current line.
     *
     * @param state current parser state
     * @param matchedBlockParser most recent matched block parser
     * @return a parser start when the line matches one of the HTML block patterns, otherwise none
     */
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

  /**
   * Determine whether the candidate content matches one of the seven HTML block shapes.
   *
   * @param blockType HTML block type number from the CommonMark classification
   * @param content line content starting at the first non-space character
   * @return {@code true} when the line matches the requested block type
   */
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

  /**
   * Build the optional closing predicate for HTML block types that terminate on a specific marker.
   *
   * @param blockType HTML block type number
   * @return line condition for the closing marker, or {@code null} when the block is open-ended
   */
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

  /**
   * Check whether the supplied content starts with a named HTML tag that belongs to the provided
   * allowlist.
   *
   * @param content line content beginning with {@code <}
   * @param names allowed tag names
   * @param allowClosingTag whether a leading {@code </name>} form is accepted
   * @return {@code true} when the content begins with an allowed tag name
   */
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

  /**
   * Check whether the supplied content contains a closing tag for one of the named raw HTML tags.
   *
   * @param content line content to inspect
   * @param names tag names to search for
   * @return {@code true} when a matching closing tag is present
   */
  private static boolean containsNamedClosingTag(String content, Set<String> names) {
    String lower = content.toLowerCase(Locale.ROOT);
    for (String tagName : names) {
      if (lower.contains("</" + tagName + ">")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check whether the entire line is a single HTML tag with only trailing whitespace after it.
   *
   * @param content line content to inspect
   * @return {@code true} when the line contains exactly one valid tag
   */
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

  /**
   * Parse a tag at the supplied position.
   *
   * @param content content to inspect
   * @param index index of the opening {@code <}
   * @return the index immediately after the tag, or {@code -1} when the content is not a tag
   */
  private static int parseHtmlTag(String content, int index) {
    if (index >= content.length() || content.charAt(index) != '<') {
      return -1;
    }

    if (index + 1 < content.length() && content.charAt(index + 1) == '/') {
      return parseClosingTag(content, index + 2);
    }
    return parseOpenTag(content, index + 1);
  }

  /**
   * Parse a closing tag of the form {@code </name>}.
   *
   * @param content content to inspect
   * @param index index after the {@code </} prefix
   * @return index immediately after the tag, or {@code -1} when the content is invalid
   */
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

  /**
   * Parse an opening tag with optional attributes and an optional trailing slash.
   *
   * @param content content to inspect
   * @param index index immediately after the opening {@code <}
   * @return index immediately after the tag, or {@code -1} when the content is invalid
   */
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

  /**
   * Parse a quoted or unquoted attribute value.
   *
   * @param content content to inspect
   * @param index index of the first value character
   * @return index immediately after the value, or {@code -1} when the value is malformed
   */
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

  /**
   * Advance past any HTML whitespace characters.
   *
   * @param content content to inspect
   * @param index starting position
   * @return first non-whitespace index at or after {@code index}
   */
  private static int skipHtmlWhitespace(String content, int index) {
    while (index < content.length() && isHtmlWhitespace(content.charAt(index))) {
      index++;
    }
    return index;
  }

  /**
   * @return {@code true} when the character is an ASCII upper-case letter
   */
  private static boolean isAsciiUpperCase(char c) {
    return c >= 'A' && c <= 'Z';
  }

  /**
   * @return {@code true} when the character can start an HTML tag or attribute name
   */
  private static boolean isTagNameStart(char c) {
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
  }

  /**
   * @return {@code true} when the character can continue an HTML tag name
   */
  private static boolean isTagNameContinue(char c) {
    return isTagNameStart(c) || (c >= '0' && c <= '9') || c == '-';
  }

  /**
   * @return {@code true} when the character can start an HTML attribute name
   */
  private static boolean isAttributeStart(char c) {
    return isTagNameStart(c) || c == '_' || c == ':';
  }

  /**
   * @return {@code true} when the character can continue an HTML attribute name
   */
  private static boolean isAttributeContinue(char c) {
    return isAttributeStart(c) || (c >= '0' && c <= '9') || c == '.' || c == '-';
  }

  /**
   * Determine whether a character terminates an unquoted HTML attribute value.
   *
   * @param c character to inspect
   * @return {@code true} when the character ends an unquoted attribute value
   */
  private static boolean isAttributeValueEnd(char c) {
    return isHtmlWhitespace(c)
        || c == '"'
        || c == '\''
        || c == '='
        || c == '<'
        || c == '>'
        || c == '`';
  }

  /**
   * Determine whether a character is HTML whitespace.
   *
   * @param c character to inspect
   * @return {@code true} when the character is one of the HTML whitespace code points
   */
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

  /** Predicate used to determine whether a given line closes a particular HTML block. */
  @FunctionalInterface
  private interface LineCondition {
    boolean matches(CharSequence line);
  }
}
