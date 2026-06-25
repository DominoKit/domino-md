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

import com.google.gwt.junit.client.GWTTestCase;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.html.HtmlRenderer;

public class MarkdownSuite extends GWTTestCase {

  private static final Parser PARSER = Parser.builder().build();
  private static final HtmlRenderer RENDERER =
      HtmlRenderer.builder().percentEncodeUrls(true).build();

  @Override
  public String getModuleName() {
    return "org.dominokit.MarkdownTest";
  }

  public void testSharedRenderingCasesMatchInTranspiledOutput() {
    for (GwtRenderingCases.RenderingCase testCase : GwtRenderingCases.CASES) {
      assertEquals(testCase.html(), RENDERER.render(PARSER.parse(testCase.markdown())));
    }
  }
}
