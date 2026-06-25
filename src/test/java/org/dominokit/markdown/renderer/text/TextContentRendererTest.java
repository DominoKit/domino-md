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
package org.dominokit.markdown.renderer.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.ext.autolink.AutolinkExtension;
import org.dominokit.markdown.ext.gfm.strikethrough.StrikethroughExtension;
import org.dominokit.markdown.ext.gfm.tables.TablesExtension;
import org.dominokit.markdown.ext.task.list.items.TaskListItemsExtension;
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.NodeRenderer;
import org.junit.Test;

public class TextContentRendererTest {

  private static final Set<Extension> EXTENSIONS =
      Set.of(
          StrikethroughExtension.create(),
          TaskListItemsExtension.create(),
          TablesExtension.create(),
          AutolinkExtension.create());

  @Test
  public void compactRenderingShouldKeepSingleBlockBreaksAndReadableInlineMarkers() {
    TextContentRenderer renderer = TextContentRenderer.builder().build();

    assertThat(renderer.render(parse("# Heading\n\nParagraph with `code` and [link](docs).")))
        .isEqualTo("Heading\nParagraph with \"code\" and \"link\" (docs).");
    assertThat(renderer.render(parse("3) three\n4) four\n"))).isEqualTo("3) three\n4) four");
    assertThat(renderer.render(parse("> quote\n\n---\n"))).isEqualTo("\u00ABquote\u00BB\n***");
  }

  @Test
  public void lineBreakModesShouldControlSoftAndBlockBreaks() {
    Node node = parse("one\ntwo\n\nthree");

    assertThat(TextContentRenderer.builder().build().render(node)).isEqualTo("one\ntwo\nthree");
    assertThat(
            TextContentRenderer.builder()
                .lineBreakRendering(LineBreakRendering.SEPARATE_BLOCKS)
                .build()
                .render(node))
        .isEqualTo("one\ntwo\n\nthree");
    assertThat(
            TextContentRenderer.builder()
                .lineBreakRendering(LineBreakRendering.STRIP)
                .build()
                .render(node))
        .isEqualTo("one two three");
  }

  @Test
  public void deprecatedStripNewlinesBuilderShouldMapToStripMode() {
    TextContentRenderer renderer = TextContentRenderer.builder().stripNewlines(true).build();

    assertThat(renderer.render(parse("alpha\nbeta\n\ngamma"))).isEqualTo("alpha beta gamma");
  }

  @Test
  public void customNodeRendererShouldOverrideCoreRenderer() {
    TextContentRenderer renderer =
        TextContentRenderer.builder()
            .nodeRendererFactory(
                context ->
                    new NodeRenderer() {
                      @Override
                      public java.util.Set<Class<? extends Node>> getNodeTypes() {
                        return java.util.Set.of(Link.class);
                      }

                      @Override
                      public void render(Node node) {
                        context.getWriter().write("[custom-link]");
                      }
                    })
            .build();

    assertThat(renderer.render(parse("before [label](docs) after")))
        .isEqualTo("before [custom-link] after");
  }

  @Test
  public void extensionsShouldRenderReadablePlainText() {
    Parser parser = Parser.builder().extensions(EXTENSIONS).build();
    TextContentRenderer renderer = TextContentRenderer.builder().extensions(EXTENSIONS).build();

    assertThat(renderer.render(parser.parse("~~foo~~"))).isEqualTo("foo");
    assertThat(renderer.render(parser.parse("- [x] task\n"))).isEqualTo("- [x] task");
    assertThat(renderer.render(parser.parse("A|B\n---|---\n1|2"))).isEqualTo("A | B\n1 | 2");
    assertThat(renderer.render(parser.parse("www.example.com")))
        .isEqualTo("\"www.example.com\" (http://www.example.com)");
  }

  @Test
  public void separateBlocksModeShouldKeepTableRowsCompactButSeparateFollowingBlocks() {
    Parser parser = Parser.builder().extensions(Set.of(TablesExtension.create())).build();
    TextContentRenderer renderer =
        TextContentRenderer.builder()
            .extensions(Set.of(TablesExtension.create()))
            .lineBreakRendering(LineBreakRendering.SEPARATE_BLOCKS)
            .build();

    assertThat(renderer.render(parser.parse("A|B\n---|---\n1|2\n\nnext")))
        .isEqualTo("A | B\n1 | 2\n\nnext");
  }

  private static Node parse(String markdown) {
    return Parser.builder().build().parse(markdown);
  }
}
