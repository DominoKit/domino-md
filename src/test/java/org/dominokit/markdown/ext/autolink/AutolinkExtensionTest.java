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
package org.dominokit.markdown.ext.autolink;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.node.SoftLineBreak;
import org.dominokit.markdown.node.SourceSpan;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.IncludeSourceSpans;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.junit.Test;

public class AutolinkExtensionTest {

  private static final Set<Extension> EXTENSIONS = Set.of(AutolinkExtension.create());

  @Test
  public void urlsAndEmailsShouldRenderAsLinks() {
    assertThat(render("foo http://one.org/ bar http://two.org/"))
        .isEqualTo(
            "<p>foo <a href=\"http://one.org/\">http://one.org/</a> bar <a href=\"http://two.org/\">http://two.org/</a></p>\n");
    assertThat(render("foo@example.com"))
        .isEqualTo("<p><a href=\"mailto:foo@example.com\">foo@example.com</a></p>\n");
  }

  @Test
  public void trickyTrailingPunctuationAndBalancedParensShouldBeHandled() {
    assertThat(
            render(
                "http://example.com/one. Example 2 (see http://example.com/two). Example 3: http://example.com/foo_(bar)"))
        .isEqualTo(
            "<p><a href=\"http://example.com/one\">http://example.com/one</a>. "
                + "Example 2 (see <a href=\"http://example.com/two\">http://example.com/two</a>). "
                + "Example 3: <a href=\"http://example.com/foo_(bar)\">http://example.com/foo_(bar)</a></p>\n");
  }

  @Test
  public void wwwSupportShouldBeConfigurable() {
    assertThat(render("www.example.com"))
        .isEqualTo("<p><a href=\"http://www.example.com\">www.example.com</a></p>\n");

    Parser parser =
        Parser.builder()
            .extensions(
                Set.of(
                    AutolinkExtension.builder()
                        .linkTypes(AutolinkType.URL, AutolinkType.EMAIL)
                        .build()))
            .build();
    HtmlRenderer renderer = HtmlRenderer.builder().build();

    assertThat(renderer.render(parser.parse("www.example.com")))
        .isEqualTo("<p>www.example.com</p>\n");
  }

  @Test
  public void existingLinksShouldNotBeRewrittenAndSourceSpansShouldBePreserved() {
    Parser parser =
        Parser.builder()
            .extensions(EXTENSIONS)
            .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
            .build();
    Node document =
        parser.parse(
            "abc\nhttp://example.com/one\ndef http://example.com/two\n<http://example.com/three>");

    Paragraph paragraph = (Paragraph) document.getFirstChild();
    Text abc = (Text) paragraph.getFirstChild();
    Link one = (Link) abc.getNext().getNext();
    Text def = (Text) one.getNext().getNext();
    Link two = (Link) def.getNext();

    assertThat(abc.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 0, 0, 3)));
    assertThat(abc.getNext()).isInstanceOf(SoftLineBreak.class);
    assertThat(one.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(1, 0, 4, 22)));
    assertThat(def.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 0, 27, 4)));
    assertThat(two.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 4, 31, 22)));

    Link inlineAutolink = (Link) paragraph.getLastChild();
    assertThat(inlineAutolink.getDestination()).isEqualTo("http://example.com/three");
  }

  private static String render(String markdown) {
    Parser parser = Parser.builder().extensions(EXTENSIONS).build();
    HtmlRenderer renderer = HtmlRenderer.builder().build();
    return renderer.render(parser.parse(markdown));
  }
}
