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
package org.dominokit.markdown.ext.classes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.dominokit.markdown.node.FencedCodeBlock;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.junit.Test;

public class MarkdownClassExtensionTest {

  @Test
  public void customClassRulesShouldMergeWithGeneratedHtmlAttributes() {
    Parser parser = Parser.builder().build();
    HtmlRenderer renderer =
        HtmlRenderer.builder()
            .extensions(
                List.of(
                    MarkdownClassExtension.builder()
                        .classes("base", "shared")
                        .nodeClasses(Paragraph.class, "node-paragraph")
                        .tagClasses("p", "tag-p")
                        .nodeClasses(FencedCodeBlock.class, "node-code-block")
                        .tagClasses("pre", "tag-pre")
                        .tagClasses("code", "tag-code")
                        .build()))
            .build();

    assertThat(renderer.render(parser.parse("paragraph")))
        .isEqualTo("<p class=\"base shared node-paragraph tag-p\">paragraph</p>\n");
    assertThat(renderer.render(parser.parse("```java\ncode\n```\n")))
        .isEqualTo(
            "<pre class=\"base shared node-code-block tag-pre\">"
                + "<code class=\"language-java base shared node-code-block tag-code\">code\n</code></pre>\n");
  }
}
