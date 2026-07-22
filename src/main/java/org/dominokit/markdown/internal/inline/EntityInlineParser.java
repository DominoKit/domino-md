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
package org.dominokit.markdown.internal.inline;

import java.util.Set;
import org.dominokit.markdown.internal.util.Html5Entities;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.beta.*;
import org.dominokit.markdown.text.AsciiMatcher;

/**
 * Attempts to parse an HTML entity or numeric character reference.
 *
 * <p>The parser recognizes decimal entities, hexadecimal entities, and named HTML5 entities.
 */
public class EntityInlineParser implements InlineContentParser {

  private static final AsciiMatcher hex =
      AsciiMatcher.builder().range('0', '9').range('A', 'F').range('a', 'f').build();
  private static final AsciiMatcher dec = AsciiMatcher.builder().range('0', '9').build();
  private static final AsciiMatcher entityStart =
      AsciiMatcher.builder().range('A', 'Z').range('a', 'z').build();
  private static final AsciiMatcher entityContinue =
      entityStart.newBuilder().range('0', '9').build();

  /**
   * Parse an entity reference at the current position.
   *
   * @param inlineParserState the current inline parser state
   * @return a parsed text node containing the decoded entity, or none when no entity is present
   */
  @Override
  public ParsedInline tryParse(InlineParserState inlineParserState) {
    Scanner scanner = inlineParserState.scanner();
    Position start = scanner.position();
    // Skip `&`
    scanner.next();

    char c = scanner.peek();
    if (c == '#') {
      // Numeric
      scanner.next();
      if (scanner.next('x') || scanner.next('X')) {
        int digits = scanner.match(hex);
        if (1 <= digits && digits <= 6 && scanner.next(';')) {
          return entity(scanner, start);
        }
      } else {
        int digits = scanner.match(dec);
        if (1 <= digits && digits <= 7 && scanner.next(';')) {
          return entity(scanner, start);
        }
      }
    } else if (entityStart.matches(c)) {
      scanner.match(entityContinue);
      if (scanner.next(';')) {
        return entity(scanner, start);
      }
    }

    return ParsedInline.none();
  }

  /**
   * Convert the consumed entity text into a decoded text node.
   *
   * @param scanner scanner positioned after the entity
   * @param start position where the entity started
   * @return parsed text node containing the decoded entity
   */
  private ParsedInline entity(Scanner scanner, Position start) {
    String text = scanner.getSource(start, scanner.position()).getContent();
    return ParsedInline.of(new Text(Html5Entities.entityToString(text)), scanner.position());
  }

  public static class Factory implements InlineContentParserFactory {

    /**
     * @return the trigger character set for entity parsing
     */
    @Override
    public Set<Character> getTriggerCharacters() {
      return Set.of('&');
    }

    /**
     * @return a new entity parser
     */
    @Override
    public InlineContentParser create() {
      return new EntityInlineParser();
    }
  }
}
