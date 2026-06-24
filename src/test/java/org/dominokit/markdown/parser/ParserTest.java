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
import java.util.concurrent.atomic.AtomicInteger;
import org.dominokit.markdown.node.BlockQuote;
import org.dominokit.markdown.node.BulletList;
import org.dominokit.markdown.node.Document;
import org.dominokit.markdown.node.FencedCodeBlock;
import org.dominokit.markdown.node.Heading;
import org.dominokit.markdown.node.HtmlBlock;
import org.dominokit.markdown.node.IndentedCodeBlock;
import org.dominokit.markdown.node.ListItem;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.OrderedList;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.node.SoftLineBreak;
import org.dominokit.markdown.node.SourceSpan;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.node.ThematicBreak;
import org.junit.Test;

public class ParserTest {

  @Test
  public void parseParagraphShouldPreservePlainTextAndSoftBreaks() {
    Paragraph paragraph = onlyChild(parse("hello\nworld"), Paragraph.class);

    assertThat(((Text) paragraph.getFirstChild()).getLiteral()).isEqualTo("hello");
    assertThat(paragraph.getFirstChild().getNext()).isInstanceOf(SoftLineBreak.class);
    assertThat(((Text) paragraph.getLastChild()).getLiteral()).isEqualTo("world");
  }

  @Test
  public void parseAtxHeadingShouldTrimClosingHashes() {
    Heading heading = onlyChild(parse("# Hello ###"), Heading.class);

    assertThat(heading.getLevel()).isEqualTo(1);
    assertThat(((Text) heading.getFirstChild()).getLiteral()).isEqualTo("Hello");
  }

  @Test
  public void parseSetextHeadingShouldReplaceParagraph() {
    Heading heading = onlyChild(parse("Hello\n---"), Heading.class);

    assertThat(heading.getLevel()).isEqualTo(2);
    assertThat(((Text) heading.getFirstChild()).getLiteral()).isEqualTo("Hello");
  }

  @Test
  public void parseThematicBreakShouldKeepLiteral() {
    ThematicBreak thematicBreak = onlyChild(parse("***"), ThematicBreak.class);

    assertThat(thematicBreak.getLiteral()).isEqualTo("***");
  }

  @Test
  public void parseFencedCodeBlockShouldCaptureFenceMetadataInfoAndLiteral() {
    FencedCodeBlock block = onlyChild(parse("```java\\*\ncode\n```\n"), FencedCodeBlock.class);

    assertThat(block.getFenceCharacter()).isEqualTo("`");
    assertThat(block.getOpeningFenceLength()).isEqualTo(3);
    assertThat(block.getClosingFenceLength()).isEqualTo(3);
    assertThat(block.getInfo()).isEqualTo("java*");
    assertThat(block.getLiteral()).isEqualTo("code\n");
  }

  @Test
  public void parseIndentedCodeBlockShouldPreserveBlankLinesAndTrimTrailingBlanks() {
    IndentedCodeBlock block = onlyChild(parse("    code\n\n    more\n"), IndentedCodeBlock.class);

    assertThat(block.getLiteral()).isEqualTo("code\n\nmore\n");
  }

  @Test
  public void parseBlockQuoteShouldContainParagraphContent() {
    BlockQuote blockQuote = onlyChild(parse("> quote\n> still"), BlockQuote.class);
    Paragraph paragraph = onlyChild(blockQuote, Paragraph.class);

    assertThat(((Text) paragraph.getFirstChild()).getLiteral()).isEqualTo("quote");
    assertThat(paragraph.getFirstChild().getNext()).isInstanceOf(SoftLineBreak.class);
    assertThat(((Text) paragraph.getLastChild()).getLiteral()).isEqualTo("still");
  }

  @Test
  public void parseBulletListShouldCaptureItemIndentAndLooseTightness() {
    BulletList list = onlyChild(parse("- one\n\n- two\n"), BulletList.class);
    ListItem firstItem = firstChild(list, ListItem.class);

    assertThat(list.getMarker()).isEqualTo("-");
    assertThat(list.isTight()).isFalse();
    assertThat(firstItem.getMarkerIndent()).isEqualTo(0);
    assertThat(firstItem.getContentIndent()).isEqualTo(2);
    assertThat(((Text) ((Paragraph) firstItem.getFirstChild()).getFirstChild()).getLiteral())
        .isEqualTo("one");
  }

  @Test
  public void parseOrderedListShouldCaptureStartAndDelimiter() {
    OrderedList list = onlyChild(parse("1. one\n2. two\n"), OrderedList.class);

    assertThat(list.getMarkerStartNumber()).isEqualTo(1);
    assertThat(list.getMarkerDelimiter()).isEqualTo(".");
    assertThat(list.isTight()).isTrue();
  }

  @Test
  public void parseHtmlBlockShouldKeepRawLiteral() {
    HtmlBlock htmlBlock = onlyChild(parse("<div>\ntext\n</div>\n"), HtmlBlock.class);

    assertThat(htmlBlock.getLiteral()).isEqualTo("<div>\ntext\n</div>");
  }

  @Test
  public void enabledBlockTypesShouldRestrictCoreFactories() {
    Parser parser = Parser.builder().enabledBlockTypes(Set.of(ThematicBreak.class)).build();
    Document document = parse(parser, "# head\n***\n");

    assertThat(document.getFirstChild()).isInstanceOf(Paragraph.class);
    assertThat(((Text) document.getFirstChild().getFirstChild()).getLiteral()).isEqualTo("# head");
    assertThat(document.getLastChild()).isInstanceOf(ThematicBreak.class);
  }

  @Test
  public void customInlineParserFactoryShouldControlInlineOutput() {
    AtomicInteger parseCalls = new AtomicInteger();
    Parser parser =
        Parser.builder()
            .inlineParserFactory(
                context ->
                    (lines, node) -> {
                      parseCalls.incrementAndGet();
                      node.appendChild(new Text("custom:" + lines.getContent()));
                    })
            .build();

    Heading heading = onlyChild(parse(parser, "# hi"), Heading.class);

    assertThat(parseCalls.get()).isEqualTo(1);
    assertThat(((Text) heading.getFirstChild()).getLiteral()).isEqualTo("custom:hi");
  }

  @Test
  public void includeSourceSpansShouldPopulateBlocksAndInlineText() {
    Parser parser =
        Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build();
    Heading heading = onlyChild(parse(parser, "Hello\n---"), Heading.class);

    assertThat(heading.getSourceSpans())
        .containsExactly(SourceSpan.of(0, 0, 0, 5), SourceSpan.of(1, 0, 6, 3));
    assertThat(heading.getFirstChild().getSourceSpans()).containsExactly(SourceSpan.of(0, 0, 0, 5));
  }

  private static Document parse(String markdown) {
    return parse(Parser.builder().build(), markdown);
  }

  private static Document parse(Parser parser, String markdown) {
    return (Document) parser.parse(markdown);
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
}
