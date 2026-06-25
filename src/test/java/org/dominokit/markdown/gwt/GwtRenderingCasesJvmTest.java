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
package org.dominokit.markdown.gwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.junit.Test;

public class GwtRenderingCasesJvmTest {

  private static final Parser PARSER = Parser.builder().build();
  private static final HtmlRenderer RENDERER =
      HtmlRenderer.builder().percentEncodeUrls(true).build();

  @Test
  public void sharedBrowserCasesShouldMatchOnTheJvm() {
    for (GwtRenderingCases.RenderingCase testCase : GwtRenderingCases.CASES) {
      assertThat(RENDERER.render(PARSER.parse(testCase.markdown()))).isEqualTo(testCase.html());
    }
  }
}
