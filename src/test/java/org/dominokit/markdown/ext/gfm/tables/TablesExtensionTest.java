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
package org.dominokit.markdown.ext.gfm.tables;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.SourceSpan;
import org.dominokit.markdown.parser.IncludeSourceSpans;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.junit.Test;

public class TablesExtensionTest {

  private static final Set<Extension> EXTENSIONS = Set.of(TablesExtension.create());

  @Test
  public void validHeaderSeparatorAndBodyShouldRenderTable() {
    assertThat(render("Abc|Def\n---|---\n1|2"))
        .isEqualTo(
            "<table>\n"
                + "<thead>\n"
                + "<tr>\n"
                + "<th>Abc</th>\n"
                + "<th>Def</th>\n"
                + "</tr>\n"
                + "</thead>\n"
                + "<tbody>\n"
                + "<tr>\n"
                + "<td>1</td>\n"
                + "<td>2</td>\n"
                + "</tr>\n"
                + "</tbody>\n"
                + "</table>\n");
  }

  @Test
  public void invalidSeparatorShouldLeaveParagraphText() {
    assertThat(render("Abc|Def\n|-a-|---")).isEqualTo("<p>Abc|Def\n|-a-|---</p>\n");
    assertThat(render("Abc|Def\n---||---")).isEqualTo("<p>Abc|Def\n---||---</p>\n");
  }

  @Test
  public void pipeEscapesAndAlignmentShouldBePreserved() {
    assertThat(render("|A\\|B|C|\n|:---|---:|\n|1|2|"))
        .isEqualTo(
            "<table>\n"
                + "<thead>\n"
                + "<tr>\n"
                + "<th align=\"left\">A|B</th>\n"
                + "<th align=\"right\">C</th>\n"
                + "</tr>\n"
                + "</thead>\n"
                + "<tbody>\n"
                + "<tr>\n"
                + "<td align=\"left\">1</td>\n"
                + "<td align=\"right\">2</td>\n"
                + "</tr>\n"
                + "</tbody>\n"
                + "</table>\n");
  }

  @Test
  public void sourceSpansShouldFollowHeaderAndBodyRows() {
    Parser parser =
        Parser.builder()
            .extensions(EXTENSIONS)
            .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
            .build();
    Node document = parser.parse("A|B\n---|---\n1|2");

    TableBlock table = (TableBlock) document.getFirstChild();
    TableHead head = (TableHead) table.getFirstChild();
    TableRow headerRow = (TableRow) head.getFirstChild();
    TableCell firstHeader = (TableCell) headerRow.getFirstChild();
    TableBody body = (TableBody) head.getNext();
    TableRow bodyRow = (TableRow) body.getFirstChild();

    assertThat(firstHeader.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 0, 0, 1)));
    assertThat(bodyRow.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 0, 12, 3)));
  }

  private static String render(String markdown) {
    Parser parser = Parser.builder().extensions(EXTENSIONS).build();
    HtmlRenderer renderer = HtmlRenderer.builder().extensions(EXTENSIONS).build();
    return renderer.render(parser.parse(markdown));
  }
}
