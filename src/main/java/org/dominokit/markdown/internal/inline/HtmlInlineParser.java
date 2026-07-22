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
package org.dominokit.markdown.internal.inline;

import java.util.Set;
import org.dominokit.markdown.node.HtmlInline;
import org.dominokit.markdown.parser.beta.*;
import org.dominokit.markdown.text.AsciiMatcher;

/**
 * Attempt to parse inline HTML.
 *
 * <p>The parser recognizes the CommonMark inline HTML forms: tags, closing tags, processing
 * instructions, comments, CDATA sections, and declarations.
 */
public class HtmlInlineParser implements InlineContentParser {

  private static final AsciiMatcher asciiLetter =
      AsciiMatcher.builder().range('A', 'Z').range('a', 'z').build();

  // spec: A tag name consists of an ASCII letter followed by zero or more ASCII letters, digits, or
  // hyphens (-).
  private static final AsciiMatcher tagNameStart = asciiLetter;
  private static final AsciiMatcher tagNameContinue =
      tagNameStart.newBuilder().range('0', '9').c('-').build();

  // spec: An attribute name consists of an ASCII letter, _, or :, followed by zero or more ASCII
  // letters, digits,
  // _, ., :, or -. (Note: This is the XML specification restricted to ASCII. HTML5 is laxer.)
  private static final AsciiMatcher attributeStart = asciiLetter.newBuilder().c('_').c(':').build();
  private static final AsciiMatcher attributeContinue =
      attributeStart.newBuilder().range('0', '9').c('.').c('-').build();
  // spec: An unquoted attribute value is a nonempty string of characters not including whitespace,
  // ", ', =, <, >, or `.
  private static final AsciiMatcher attributeValueEnd =
      AsciiMatcher.builder()
          .c(' ')
          .c('\t')
          .c('\n')
          .c('\u000B')
          .c('\f')
          .c('\r')
          .c('"')
          .c('\'')
          .c('=')
          .c('<')
          .c('>')
          .c('`')
          .build();

  /**
   * Try to parse an inline HTML fragment at the current position.
   *
   * @param inlineParserState the current inline parser state
   * @return a parsed HTML node, or none when the input is not inline HTML
   */
  @Override
  public ParsedInline tryParse(InlineParserState inlineParserState) {
    Scanner scanner = inlineParserState.scanner();
    Position start = scanner.position();
    // Skip over `<`
    scanner.next();

    char c = scanner.peek();
    if (tagNameStart.matches(c)) {
      if (tryOpenTag(scanner)) {
        return htmlInline(start, scanner);
      }
    } else if (c == '/') {
      if (tryClosingTag(scanner)) {
        return htmlInline(start, scanner);
      }
    } else if (c == '?') {
      if (tryProcessingInstruction(scanner)) {
        return htmlInline(start, scanner);
      }
    } else if (c == '!') {
      // comment, declaration or CDATA
      scanner.next();
      c = scanner.peek();
      if (c == '-') {
        if (tryComment(scanner)) {
          return htmlInline(start, scanner);
        }
      } else if (c == '[') {
        if (tryCdata(scanner)) {
          return htmlInline(start, scanner);
        }
      } else if (asciiLetter.matches(c)) {
        if (tryDeclaration(scanner)) {
          return htmlInline(start, scanner);
        }
      }
    }

    return ParsedInline.none();
  }

  /**
   * Convert the consumed HTML literal into an inline HTML node.
   *
   * @param start position where the HTML fragment started
   * @param scanner scanner positioned after the HTML fragment
   * @return parsed inline HTML result
   */
  private static ParsedInline htmlInline(Position start, Scanner scanner) {
    String text = scanner.getSource(start, scanner.position()).getContent();
    HtmlInline node = new HtmlInline();
    node.setLiteral(text);
    return ParsedInline.of(node, scanner.position());
  }

  /**
   * Attempt to scan an inline HTML opening tag.
   *
   * @param scanner inline scanner
   * @return {@code true} if a valid opening tag was consumed
   */
  private static boolean tryOpenTag(Scanner scanner) {
    // spec: An open tag consists of a < character, a tag name, zero or more attributes, optional
    // whitespace,
    // an optional / character, and a > character.
    scanner.next();
    scanner.match(tagNameContinue);
    boolean whitespace = scanner.whitespace() >= 1;
    // spec: An attribute consists of whitespace, an attribute name, and an optional attribute value
    // specification.
    while (whitespace && scanner.match(attributeStart) >= 1) {
      scanner.match(attributeContinue);
      // spec: An attribute value specification consists of optional whitespace, a = character,
      // optional whitespace, and an attribute value.
      whitespace = scanner.whitespace() >= 1;
      if (scanner.next('=')) {
        scanner.whitespace();
        char valueStart = scanner.peek();
        if (valueStart == '\'') {
          scanner.next();
          if (scanner.find('\'') < 0) {
            return false;
          }
          scanner.next();
        } else if (valueStart == '"') {
          scanner.next();
          if (scanner.find('"') < 0) {
            return false;
          }
          scanner.next();
        } else {
          if (scanner.find(attributeValueEnd) <= 0) {
            return false;
          }
        }

        // Whitespace is required between attributes
        whitespace = scanner.whitespace() >= 1;
      }
    }

    scanner.next('/');
    return scanner.next('>');
  }

  /**
   * Attempt to scan an inline HTML closing tag.
   *
   * @param scanner inline scanner
   * @return {@code true} if a valid closing tag was consumed
   */
  private static boolean tryClosingTag(Scanner scanner) {
    // spec: A closing tag consists of the string </, a tag name, optional whitespace, and the
    // character >.
    scanner.next();
    if (scanner.match(tagNameStart) >= 1) {
      scanner.match(tagNameContinue);
      scanner.whitespace();
      return scanner.next('>');
    }
    return false;
  }

  /**
   * Attempt to scan a processing instruction.
   *
   * @param scanner inline scanner
   * @return {@code true} if a valid processing instruction was consumed
   */
  private static boolean tryProcessingInstruction(Scanner scanner) {
    // spec: A processing instruction consists of the string <?, a string of characters not
    // including the string ?>,
    // and the string ?>.
    scanner.next();
    while (scanner.find('?') > 0) {
      scanner.next();
      if (scanner.next('>')) {
        return true;
      }
    }
    return false;
  }

  /**
   * Attempt to scan an HTML comment.
   *
   * @param scanner inline scanner
   * @return {@code true} if a valid comment was consumed
   */
  private static boolean tryComment(Scanner scanner) {
    // spec: An [HTML comment](@) consists of `<!-->`, `<!--->`, or  `<!--`, a string of
    // characters not including the string `-->`, and `-->` (see the
    // [HTML
    // spec](https://html.spec.whatwg.org/multipage/parsing.html#markup-declaration-open-state)).

    // Skip first `-`
    scanner.next();
    if (!scanner.next('-')) {
      return false;
    }

    if (scanner.next('>') || scanner.next("->")) {
      return true;
    }

    while (scanner.find('-') >= 0) {
      if (scanner.next("-->")) {
        return true;
      } else {
        scanner.next();
      }
    }

    return false;
  }

  /**
   * Attempt to scan a CDATA section.
   *
   * @param scanner inline scanner
   * @return {@code true} if a valid CDATA section was consumed
   */
  private static boolean tryCdata(Scanner scanner) {
    // spec: A CDATA section consists of the string <![CDATA[, a string of characters not including
    // the string ]]>,
    // and the string ]]>.

    // Skip `[`
    scanner.next();

    if (scanner.next("CDATA[")) {
      while (scanner.find(']') >= 0) {
        if (scanner.next("]]>")) {
          return true;
        } else {
          scanner.next();
        }
      }
    }

    return false;
  }

  /**
   * Attempt to scan an HTML declaration.
   *
   * @param scanner inline scanner
   * @return {@code true} if a valid declaration was consumed
   */
  private static boolean tryDeclaration(Scanner scanner) {
    // spec: A declaration consists of the string <!, an ASCII letter, zero or more characters not
    // including
    // the character >, and the character >.
    scanner.match(asciiLetter);
    if (scanner.whitespace() <= 0) {
      return false;
    }
    if (scanner.find('>') >= 0) {
      scanner.next();
      return true;
    }
    return false;
  }

  public static class Factory implements InlineContentParserFactory {

    /** Return the single trigger character that can start inline HTML. */
    @Override
    public Set<Character> getTriggerCharacters() {
      return Set.of('<');
    }

    /** Create a new inline HTML parser instance. */
    @Override
    public InlineContentParser create() {
      return new HtmlInlineParser();
    }
  }
}
