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
package org.dominokit.markdown.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.dominokit.markdown.node.CustomNode;
import org.dominokit.markdown.node.Heading;
import org.dominokit.markdown.node.Image;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.beta.InlineContentParser;
import org.dominokit.markdown.parser.beta.InlineContentParserFactory;
import org.dominokit.markdown.parser.beta.InlineParserState;
import org.dominokit.markdown.parser.beta.ParsedInline;
import org.junit.Test;

public class InlineContentParserTest {

  @Test
  public void customInlineContentParserShouldRunPerInlineSnippet() {
    Parser parser =
        Parser.builder().customInlineContentParserFactory(new DollarInlineParser.Factory()).build();
    Node document = parser.parse("Test: $hey *there*$ $you$\n\n# Heading $heading$\n");

    DollarInline first = findFirst(document, DollarInline.class);
    assertThat(first.getLiteral()).isEqualTo("hey *there*");

    DollarInline second = (DollarInline) document.getFirstChild().getLastChild();
    assertThat(second.getLiteral()).isEqualTo("you");

    Heading heading = findFirst(document, Heading.class);
    DollarInline third = (DollarInline) heading.getLastChild();
    assertThat(third.getLiteral()).isEqualTo("heading");

    assertThat(first.getIndex()).isEqualTo(0);
    assertThat(second.getIndex()).isEqualTo(1);
    assertThat(third.getIndex()).isEqualTo(0);
  }

  @Test
  public void bangInlineContentParserShouldNotInterfereWithImages() {
    Parser parser =
        Parser.builder().customInlineContentParserFactory(new BangInlineParser.Factory()).build();
    Node document = parser.parse("![image](url) !notimage");
    Image image = findFirst(document, Image.class);

    assertThat(image.getDestination()).isEqualTo("url");
    assertThat(((Text) image.getNext()).getLiteral()).isEqualTo(" ");
    assertThat(image.getNext().getNext()).isInstanceOf(BangInline.class);
    assertThat(((Text) image.getNext().getNext().getNext()).getLiteral()).isEqualTo("notimage");
  }

  private static <T extends Node> T findFirst(Node node, Class<T> type) {
    if (type.isInstance(node)) {
      return type.cast(node);
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      T found = findFirst(child, type);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private static class DollarInline extends CustomNode {
    private final String literal;
    private final int index;

    private DollarInline(String literal, int index) {
      this.literal = literal;
      this.index = index;
    }

    public String getLiteral() {
      return literal;
    }

    public int getIndex() {
      return index;
    }
  }

  private static class DollarInlineParser implements InlineContentParser {
    private int index;

    @Override
    public ParsedInline tryParse(InlineParserState inlineParserState) {
      var scanner = inlineParserState.scanner();
      scanner.next();
      var start = scanner.position();

      if (scanner.find('$') == -1) {
        return ParsedInline.none();
      }

      String content = scanner.getSource(start, scanner.position()).getContent();
      scanner.next();
      return ParsedInline.of(new DollarInline(content, index++), scanner.position());
    }

    private static class Factory implements InlineContentParserFactory {
      @Override
      public Set<Character> getTriggerCharacters() {
        return Set.of('$');
      }

      @Override
      public InlineContentParser create() {
        return new DollarInlineParser();
      }
    }
  }

  private static class BangInline extends CustomNode {}

  private static class BangInlineParser implements InlineContentParser {
    @Override
    public ParsedInline tryParse(InlineParserState inlineParserState) {
      var scanner = inlineParserState.scanner();
      scanner.next();
      return ParsedInline.of(new BangInline(), scanner.position());
    }

    private static class Factory implements InlineContentParserFactory {
      @Override
      public Set<Character> getTriggerCharacters() {
        return Set.of('!');
      }

      @Override
      public InlineContentParser create() {
        return new BangInlineParser();
      }
    }
  }
}
