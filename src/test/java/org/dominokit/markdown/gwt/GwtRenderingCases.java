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

import java.util.List;

final class GwtRenderingCases {

  static final List<RenderingCase> CASES =
      List.of(
          new RenderingCase("# Hello\n", "<h1>Hello</h1>\n"),
          new RenderingCase("### ###\n", "<h3></h3>\n"),
          new RenderingCase("[a](ä)\n", "<p><a href=\"%C3%A4\">a</a></p>\n"),
          new RenderingCase("![foo &auml;](/img)\n", "<p><img src=\"/img\" alt=\"foo ä\" /></p>\n"),
          new RenderingCase(
              "paragraph with <span>x</span>\n", "<p>paragraph with <span>x</span></p>\n"),
          new RenderingCase(
              "```java\ncode\n```\n", "<pre><code class=\"language-java\">code\n</code></pre>\n"),
          new RenderingCase("- one\n- two\n", "<ul>\n<li>one</li>\n<li>two</li>\n</ul>\n"));

  private GwtRenderingCases() {}

  static final class RenderingCase {
    private final String markdown;
    private final String html;

    private RenderingCase(String markdown, String html) {
      this.markdown = markdown;
      this.html = html;
    }

    String markdown() {
      return markdown;
    }

    String html() {
      return html;
    }
  }
}
