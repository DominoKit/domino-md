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
package org.dominokit.markdown.extensions.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.dominokit.markdown.renderer.markdown.MarkdownRenderer;
import org.dominokit.markdown.renderer.text.TextContentRenderer;
import org.junit.Test;

public class ExtensionDiscoveryTest {

  @Test
  public void bundledDiscoveryShouldReturnDeterministicExtensionOrder() {
    assertThat(extensionClassNames(ExtensionDiscovery.load()))
        .containsExactly(
            "org.dominokit.markdown.ext.gfm.strikethrough.StrikethroughExtension",
            "org.dominokit.markdown.ext.task.list.items.TaskListItemsExtension",
            "org.dominokit.markdown.ext.gfm.tables.TablesExtension",
            "org.dominokit.markdown.ext.autolink.AutolinkExtension");
  }

  @Test
  public void bundledDiscoveryShouldReturnFreshExtensionInstances() {
    List<Extension> first = ExtensionDiscovery.load();
    List<Extension> second = ExtensionDiscovery.load();

    assertThat(first).hasSameSizeAs(second);
    for (int i = 0; i < first.size(); i++) {
      assertThat(first.get(i)).isNotSameAs(second.get(i));
      assertThat(first.get(i).getClass()).isEqualTo(second.get(i).getClass());
    }
  }

  @Test
  public void discoveredExtensionsShouldWireParserAndRenderers() {
    List<Extension> extensions = ExtensionDiscovery.load();
    Parser parser = Parser.builder().extensions(extensions).build();
    HtmlRenderer htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();
    MarkdownRenderer markdownRenderer = MarkdownRenderer.builder().extensions(extensions).build();
    TextContentRenderer textRenderer = TextContentRenderer.builder().extensions(extensions).build();

    Node strikethrough = parser.parse("~~foo~~");
    Node taskList = parser.parse("- [x] task\n");
    Node table = parser.parse("A|B\n---|---\n1|2");
    Node autolink = parser.parse("www.example.com");

    assertThat(htmlRenderer.render(strikethrough)).isEqualTo("<p><del>foo</del></p>\n");
    assertThat(markdownRenderer.render(taskList)).isEqualTo("- [x] task\n");
    assertThat(textRenderer.render(table)).isEqualTo("A | B\n1 | 2");
    assertThat(htmlRenderer.render(autolink))
        .isEqualTo("<p><a href=\"http://www.example.com\">www.example.com</a></p>\n");
  }

  private static List<String> extensionClassNames(List<Extension> extensions) {
    return extensions.stream()
        .map(extension -> extension.getClass().getName())
        .collect(Collectors.toList());
  }
}
