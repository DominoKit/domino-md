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
package org.dominokit.markdown.renderer.markdown;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.ext.autolink.AutolinkExtension;
import org.dominokit.markdown.ext.gfm.strikethrough.StrikethroughExtension;
import org.dominokit.markdown.ext.gfm.tables.TablesExtension;
import org.dominokit.markdown.ext.task.list.items.TaskListItemsExtension;
import org.dominokit.markdown.node.Document;
import org.dominokit.markdown.node.Emphasis;
import org.dominokit.markdown.node.FencedCodeBlock;
import org.dominokit.markdown.node.Heading;
import org.dominokit.markdown.node.ListItem;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.OrderedList;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.node.ThematicBreak;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.NodeRenderer;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.junit.Test;

public class MarkdownRendererTest {

  private static final Set<Extension> EXTENSIONS =
      Set.of(
          StrikethroughExtension.create(),
          TaskListItemsExtension.create(),
          TablesExtension.create(),
          AutolinkExtension.create());

  private static final Parser CORE_PARSER = Parser.builder().build();
  private static final MarkdownRenderer CORE_RENDERER = MarkdownRenderer.builder().build();

  @Test
  public void coreRoundTripShouldPreserveRepresentativeMarkdownCases() {
    List<String> cases =
        List.of(
            "___\n",
            "# foo\n",
            "Foo\nbar\n===\n",
            "```\ntest\n```\n",
            "    hi\n    code\n",
            "> # Foo\n> \n> bar\n> baz\n",
            "* foo\n  bar\n",
            " -    one\n\n     two\n",
            "1\\. Foo\n",
            "999\\) Foo\n",
            "`` `foo ``\n",
            "[link](/uri \"title\")\n",
            "![a](<b\\>c>)\n",
            "<del>*foo*</del>\n",
            "foo  \nbar\n",
            "foo\nbar\n");

    for (String input : cases) {
      assertThat(parseAndRender(CORE_PARSER, CORE_RENDERER, input)).isEqualTo(input);
    }
  }

  @Test
  public void thematicBreakWithoutLiteralShouldUseFallbackMarker() {
    ThematicBreak thematicBreak = new ThematicBreak();

    assertThat(CORE_RENDERER.render(thematicBreak)).isEqualTo("___");
  }

  @Test
  public void fencedCodeBlockFromAstShouldChooseSafeFenceLength() {
    Document doc = new Document();
    FencedCodeBlock codeBlock = new FencedCodeBlock();
    codeBlock.setLiteral("hi`\n```\n``test");
    doc.appendChild(codeBlock);

    assertThat(CORE_RENDERER.render(doc)).isEqualTo("````\nhi`\n```\n``test\n````\n");
  }

  @Test
  public void manualNestedEmphasisShouldChooseDifferentDelimiter() {
    Document doc = new Document();
    Paragraph paragraph = new Paragraph();
    doc.appendChild(paragraph);

    Emphasis outer = new Emphasis();
    paragraph.appendChild(outer);
    Emphasis inner = new Emphasis();
    outer.appendChild(inner);
    inner.appendChild(new Text("hi"));

    assertThat(CORE_RENDERER.render(doc)).isEqualTo("*_hi_*\n");
  }

  @Test
  public void orderedListFromAstShouldPreserveStoredStartNumberAndDelimiter() {
    Document doc = new Document();
    OrderedList list = new OrderedList();
    list.setMarkerStartNumber(2);
    list.setMarkerDelimiter(")");
    ListItem item = new ListItem();
    Paragraph paragraph = new Paragraph();
    paragraph.appendChild(new Text("Test"));
    item.appendChild(paragraph);
    list.appendChild(item);
    doc.appendChild(list);

    assertThat(CORE_RENDERER.render(doc)).isEqualTo("2) Test\n");
  }

  @Test
  public void customNodeRendererShouldOverrideCoreRenderer() {
    MarkdownNodeRendererFactory nodeRendererFactory =
        new MarkdownNodeRendererFactory() {
          @Override
          public NodeRenderer create(MarkdownNodeRendererContext context) {
            return new NodeRenderer() {
              @Override
              public Set<Class<? extends Node>> getNodeTypes() {
                return Set.of(Heading.class);
              }

              @Override
              public void render(Node node) {
                context.getWriter().raw("# Custom heading");
              }
            };
          }

          @Override
          public Set<Character> getSpecialCharacters() {
            return Set.of();
          }
        };

    MarkdownRenderer renderer =
        MarkdownRenderer.builder().nodeRendererFactory(nodeRendererFactory).build();

    assertThat(renderer.render(CORE_PARSER.parse("# Hello"))).isEqualTo("# Custom heading\n");
  }

  @Test
  public void extensionSetShouldBeIdempotentAndPreserveHtmlSemantics() {
    Parser parser = Parser.builder().extensions(EXTENSIONS).build();
    MarkdownRenderer renderer = MarkdownRenderer.builder().extensions(EXTENSIONS).build();
    HtmlRenderer htmlRenderer =
        HtmlRenderer.builder().extensions(EXTENSIONS).percentEncodeUrls(true).build();

    assertCanonical(parser, renderer, htmlRenderer, "~foo~\n", "~foo~\n");
    assertCanonical(
        parser, renderer, htmlRenderer, "- [x] this is *done*\n", "- [x] this is *done*\n");
    assertCanonical(
        parser,
        renderer,
        htmlRenderer,
        "|A\\|B|C|\n|:---|---:|\n|1|2|",
        "|A\\|B|C|\n|:---|---:|\n|1|2|\n");
    assertCanonical(
        parser,
        renderer,
        htmlRenderer,
        "www.example.com",
        "[www.example.com](http://www.example.com)\n");
  }

  private static void assertCanonical(
      Parser parser,
      MarkdownRenderer renderer,
      HtmlRenderer htmlRenderer,
      String input,
      String expectedCanonical) {
    Node original = parser.parse(input);
    String canonical = renderer.render(original);

    assertThat(canonical).isEqualTo(expectedCanonical);
    assertThat(renderer.render(parser.parse(canonical))).isEqualTo(canonical);
    assertThat(htmlRenderer.render(original))
        .isEqualTo(htmlRenderer.render(parser.parse(canonical)));
  }

  private static String parseAndRender(Parser parser, MarkdownRenderer renderer, String source) {
    return renderer.render(parser.parse(source));
  }
}
