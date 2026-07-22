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
package org.dominokit.markdown.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.dominokit.markdown.node.Code;
import org.dominokit.markdown.node.CustomNode;
import org.dominokit.markdown.node.Emphasis;
import org.dominokit.markdown.node.HardLineBreak;
import org.dominokit.markdown.node.HtmlInline;
import org.dominokit.markdown.node.Image;
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.LinkReferenceDefinition;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.Nodes;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.node.SoftLineBreak;
import org.dominokit.markdown.node.StrongEmphasis;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.delimiter.DelimiterProcessor;
import org.dominokit.markdown.parser.delimiter.DelimiterRun;
import org.junit.Test;

public class InlineSyntaxTest {

  @Test
  public void emphasisAndStrongEmphasisShouldWrapInlineChildren() {
    Paragraph paragraph = onlyChild(parse("a *b* **c**"), Paragraph.class);

    assertThat(((Text) paragraph.getFirstChild()).getLiteral()).isEqualTo("a ");
    assertThat(paragraph.getFirstChild().getNext()).isInstanceOf(Emphasis.class);
    assertThat(((Text) paragraph.getFirstChild().getNext().getFirstChild()).getLiteral())
        .isEqualTo("b");
    assertThat(paragraph.getLastChild()).isInstanceOf(StrongEmphasis.class);
    assertThat(((Text) paragraph.getLastChild().getFirstChild()).getLiteral()).isEqualTo("c");
  }

  @Test
  public void codeSpansShouldCollapseWrappingSpacesPerSpec() {
    Paragraph paragraph = onlyChild(parse("a ` code ` b"), Paragraph.class);
    Code code = findFirst(paragraph, Code.class);

    assertThat(code.getLiteral()).isEqualTo("code");
  }

  @Test
  public void backslashEscapesAndSoftLineBreaksShouldBeParsed() {
    Paragraph paragraph = onlyChild(parse("a\\*b\nnext"), Paragraph.class);

    assertThat(((Text) paragraph.getFirstChild()).getLiteral()).isEqualTo("a*b");
    assertThat(paragraph.getFirstChild().getNext()).isInstanceOf(SoftLineBreak.class);
    assertThat(((Text) paragraph.getLastChild()).getLiteral()).isEqualTo("next");
  }

  @Test
  public void twoTrailingSpacesShouldCreateHardLineBreak() {
    Paragraph paragraph = onlyChild(parse("a  \nnext"), Paragraph.class);

    assertThat(((Text) paragraph.getFirstChild()).getLiteral()).isEqualTo("a");
    assertThat(paragraph.getFirstChild().getNext()).isInstanceOf(HardLineBreak.class);
    assertThat(((Text) paragraph.getLastChild()).getLiteral()).isEqualTo("next");
  }

  @Test
  public void inlineLinksShouldCaptureDestinationAndTitle() {
    Paragraph paragraph = onlyChild(parse("[x](url \"t\")"), Paragraph.class);
    Link link = onlyChild(paragraph, Link.class);

    assertThat(link.getDestination()).isEqualTo("url");
    assertThat(link.getTitle()).isEqualTo("t");
    assertThat(((Text) link.getFirstChild()).getLiteral()).isEqualTo("x");
  }

  @Test
  public void referenceImagesShouldUseCollectedDefinitions() {
    Node document = parse("![alt][id]\n\n[id]: /img 'title'");
    Paragraph paragraph = firstChild(document, Paragraph.class);
    Image image = onlyChild(paragraph, Image.class);

    assertThat(image.getDestination()).isEqualTo("/img");
    assertThat(image.getTitle()).isEqualTo("title");
    assertThat(((Text) image.getFirstChild()).getLiteral()).isEqualTo("alt");
    assertThat(document.getLastChild()).isInstanceOf(LinkReferenceDefinition.class);
  }

  @Test
  public void referenceLinksShouldUseCollectedDefinitions() {
    Node document = parse("[ref]\n\n[ref]: /dest");
    Paragraph paragraph = firstChild(document, Paragraph.class);
    Link link = onlyChild(paragraph, Link.class);

    assertThat(link.getDestination()).isEqualTo("/dest");
    assertThat(link.getTitle()).isNull();
    assertThat(((Text) link.getFirstChild()).getLiteral()).isEqualTo("ref");
    assertThat(document.getLastChild()).isInstanceOf(LinkReferenceDefinition.class);
  }

  @Test
  public void entitiesShouldResolveToLiteralCharacters() {
    Paragraph paragraph = onlyChild(parse("&amp; &#35; &Aacute;"), Paragraph.class);

    assertThat(((Text) paragraph.getFirstChild()).getLiteral()).isEqualTo("& # Á");
  }

  @Test
  public void inlineHtmlShouldProduceHtmlInlineNodes() {
    Paragraph paragraph = onlyChild(parse("<span>x</span>"), Paragraph.class);

    assertThat(paragraph.getFirstChild()).isInstanceOf(HtmlInline.class);
    assertThat(((Text) paragraph.getFirstChild().getNext()).getLiteral()).isEqualTo("x");
    assertThat(paragraph.getLastChild()).isInstanceOf(HtmlInline.class);
  }

  @Test
  public void underscoreDelimiterRulesShouldPreventIntrawordEmphasis() {
    Paragraph paragraph = onlyChild(parse("foo_bar_baz"), Paragraph.class);

    assertThat(((Text) paragraph.getFirstChild()).getLiteral()).isEqualTo("foo_bar_baz");
    assertThat(paragraph.getFirstChild()).isSameAs(paragraph.getLastChild());
  }

  @Test
  public void customDelimiterProcessorShouldIntegrateWithInlineParsing() {
    Parser parser = Parser.builder().customDelimiterProcessor(new PlusDelimiterProcessor()).build();
    Paragraph paragraph = onlyChild((Node) parser.parse("a +b+ c"), Paragraph.class);

    assertThat(((Text) paragraph.getFirstChild()).getLiteral()).isEqualTo("a ");
    assertThat(paragraph.getFirstChild().getNext()).isInstanceOf(PlusDelimited.class);
    assertThat(((Text) paragraph.getFirstChild().getNext().getFirstChild()).getLiteral())
        .isEqualTo("b");
    assertThat(((Text) paragraph.getLastChild()).getLiteral()).isEqualTo(" c");
  }

  private static Node parse(String markdown) {
    return Parser.builder().build().parse(markdown);
  }

  private static <T extends Node> T onlyChild(Node parent, Class<T> type) {
    assertThat(parent.getFirstChild()).isInstanceOf(type);
    assertThat(parent.getFirstChild()).isSameAs(parent.getLastChild());
    return type.cast(parent.getFirstChild());
  }

  private static <T extends Node> T firstChild(Node parent, Class<T> type) {
    assertThat(parent.getFirstChild()).isInstanceOf(type);
    return type.cast(parent.getFirstChild());
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

  private static class PlusDelimited extends CustomNode {}

  private static class PlusDelimiterProcessor implements DelimiterProcessor {
    @Override
    public char getOpeningCharacter() {
      return '+';
    }

    @Override
    public char getClosingCharacter() {
      return '+';
    }

    @Override
    public int getMinLength() {
      return 1;
    }

    @Override
    public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
      PlusDelimited plusDelimited = new PlusDelimited();
      Node opener = openingRun.getOpener();
      for (Node node : Nodes.between(opener, closingRun.getCloser())) {
        plusDelimited.appendChild(node);
      }
      opener.insertAfter(plusDelimited);
      return 1;
    }
  }
}
