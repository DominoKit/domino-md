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
package org.dominokit.markdown.ext.gfm.strikethrough;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.node.SourceSpan;
import org.dominokit.markdown.parser.IncludeSourceSpans;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.junit.Test;

public class StrikethroughExtensionTest {

  private static final Set<Extension> EXTENSIONS = Set.of(StrikethroughExtension.create());

  @Test
  public void oneOrTwoTildesShouldRenderAsStrikethrough() {
    assertThat(render("~foo~")).isEqualTo("<p><del>foo</del></p>\n");
    assertThat(render("~~foo~~")).isEqualTo("<p><del>foo</del></p>\n");
  }

  @Test
  public void unmatchedOrTooManyTildesShouldStayLiteral() {
    assertThat(render("foo ~~~~")).isEqualTo("<p>foo ~~~~</p>\n");
    assertThat(render("~~foo")).isEqualTo("<p>~~foo</p>\n");
    assertThat(render("~~foo~~~")).isEqualTo("<p>~~foo~~~</p>\n");
  }

  @Test
  public void requireTwoTildesOptionShouldLeaveSingleTildeForOtherProcessors() {
    Parser parser =
        Parser.builder()
            .extensions(Set.of(StrikethroughExtension.builder().requireTwoTildes(true).build()))
            .build();

    assertThat(render(parser, "~foo~ ~~bar~~")).isEqualTo("<p>~foo~ <del>bar</del></p>\n");
  }

  @Test
  public void sourceSpansShouldCoverWrappedRegion() {
    Parser parser =
        Parser.builder()
            .extensions(EXTENSIONS)
            .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
            .build();

    Node document = parser.parse("hey ~~there~~\n");
    Paragraph paragraph = (Paragraph) document.getFirstChild();
    Node strikethrough = paragraph.getLastChild();

    assertThat(strikethrough).isInstanceOf(Strikethrough.class);
    assertThat(strikethrough.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 4, 4, 9)));
  }

  private static String render(String markdown) {
    return render(Parser.builder().extensions(EXTENSIONS).build(), markdown);
  }

  private static String render(Parser parser, String markdown) {
    HtmlRenderer renderer = HtmlRenderer.builder().extensions(EXTENSIONS).build();
    return renderer.render(parser.parse(markdown));
  }
}
