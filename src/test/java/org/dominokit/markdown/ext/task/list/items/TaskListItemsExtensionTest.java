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
package org.dominokit.markdown.ext.task.list.items;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.junit.Test;

public class TaskListItemsExtensionTest {

  private static final Set<Extension> EXTENSIONS = Set.of(TaskListItemsExtension.create());
  private static final String HTML_CHECKED = "<input type=\"checkbox\" disabled=\"\" checked=\"\">";
  private static final String HTML_UNCHECKED = "<input type=\"checkbox\" disabled=\"\">";

  @Test
  public void checkedAndUncheckedTasksShouldRenderCheckboxInputs() {
    assertThat(render("- [x] this is *done*\n"))
        .isEqualTo("<ul>\n<li>" + HTML_CHECKED + " this is <em>done</em></li>\n</ul>\n");

    assertThat(render("- [ ] do this\n"))
        .isEqualTo("<ul>\n<li>" + HTML_UNCHECKED + " do this</li>\n</ul>\n");
  }

  @Test
  public void nestedTaskListsShouldKeepExistingListStructure() {
    assertThat(render("- [x] foo\n  - [ ] bar\n  - [x] baz\n- [ ] bim"))
        .isEqualTo(
            "<ul>\n"
                + "<li>"
                + HTML_CHECKED
                + " foo\n"
                + "<ul>\n"
                + "<li>"
                + HTML_UNCHECKED
                + " bar</li>\n"
                + "<li>"
                + HTML_CHECKED
                + " baz</li>\n"
                + "</ul>\n"
                + "</li>\n"
                + "<li>"
                + HTML_UNCHECKED
                + " bim</li>\n"
                + "</ul>\n");
  }

  @Test
  public void invalidTaskMarkersShouldStayLiteralText() {
    assertThat(render("- [x]no space\n")).isEqualTo("<ul>\n<li>[x]no space</li>\n</ul>\n");
    assertThat(render("* [] neither is this\n"))
        .isEqualTo("<ul>\n<li>[] neither is this</li>\n</ul>\n");
    assertThat(render("* [x]  \n")).isEqualTo("<ul>\n<li>[x]</li>\n</ul>\n");
  }

  private static String render(String markdown) {
    Parser parser = Parser.builder().extensions(EXTENSIONS).build();
    HtmlRenderer renderer = HtmlRenderer.builder().extensions(EXTENSIONS).build();
    return renderer.render(parser.parse(markdown));
  }
}
