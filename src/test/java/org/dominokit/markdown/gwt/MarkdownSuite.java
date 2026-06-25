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
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import java.util.Set;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.ext.autolink.AutolinkExtension;
import org.dominokit.markdown.ext.gfm.strikethrough.StrikethroughExtension;
import org.dominokit.markdown.ext.gfm.tables.TablesExtension;
import org.dominokit.markdown.ext.task.list.items.TaskListItemsExtension;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.elemental2.ElementNodeRenderer;
import org.dominokit.markdown.renderer.elemental2.Elemental2Renderer;
import org.dominokit.markdown.renderer.elemental2.SoftBreakRendering;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.dominokit.markdown.renderer.html.UrlSanitizer;

public class MarkdownSuite extends GWTTestCase {

  private static final Parser PARSER = Parser.builder().build();
  private static final HtmlRenderer RENDERER =
      HtmlRenderer.builder().percentEncodeUrls(true).build();
  private static final Elemental2Renderer ELEMENTAL2_RENDERER =
      Elemental2Renderer.builder().percentEncodeUrls(true).build();
  private static final Set<Extension> ENGINE_EXTENSIONS =
      Set.of(
          StrikethroughExtension.create(),
          TaskListItemsExtension.create(),
          TablesExtension.create(),
          AutolinkExtension.create());

  @Override
  public String getModuleName() {
    return "org.dominokit.MarkdownTest";
  }

  public void testSharedRenderingCasesMatchInTranspiledOutput() {
    for (GwtRenderingCases.RenderingCase testCase : GwtRenderingCases.CASES) {
      assertEquals(testCase.html(), RENDERER.render(PARSER.parse(testCase.markdown())));
    }
  }

  public void testElemental2RendererShouldRenderSafeDomCases() {
    for (Elemental2RenderingCases.RenderingCase testCase : Elemental2RenderingCases.CASES) {
      assertEquals(
          testCase.html(),
          serialize(ELEMENTAL2_RENDERER.render(PARSER.parse(testCase.markdown()))));
    }
  }

  public void testElemental2RendererShouldSupportRawHtmlHandlerOverride() {
    Elemental2Renderer renderer =
        Elemental2Renderer.builder()
            .rawHtmlHandler(
                new org.dominokit.markdown.renderer.elemental2.RawHtmlHandler() {
                  @Override
                  public elemental2.dom.Node renderInline(String literal) {
                    Element code = DomGlobal.document.createElement("code");
                    code.setAttribute("data-inline-raw", "true");
                    code.textContent = literal;
                    return code;
                  }

                  @Override
                  public elemental2.dom.Node renderBlock(String literal) {
                    Element section = DomGlobal.document.createElement("section");
                    section.setAttribute("data-block-raw", "true");
                    section.textContent = literal;
                    return section;
                  }
                })
            .build();

    assertEquals(
        "<p>before <code data-inline-raw=\"true\">&lt;span&gt;</code> after</p>",
        serialize(renderer.render(PARSER.parse("before <span> after\n"))));
    assertEquals(
        "<section data-block-raw=\"true\">&lt;div&gt;block&lt;/div&gt;</section>",
        serialize(renderer.render(PARSER.parse("<div>block</div>\n"))));
  }

  public void testElemental2RendererShouldSupportAttributeProviders() {
    Elemental2Renderer renderer =
        Elemental2Renderer.builder()
            .attributeProviderFactory(
                context ->
                    (node, tagName, attributes) -> {
                      if (node instanceof org.dominokit.markdown.node.FencedCodeBlock
                          && "pre".equals(tagName)) {
                        attributes.put("data-code-block", "fenced");
                      } else if (node instanceof org.dominokit.markdown.node.FencedCodeBlock
                          && "code".equals(tagName)) {
                        attributes.remove("class");
                        attributes.put("data-info", "custom");
                      }
                    })
            .build();

    assertEquals(
        "<pre data-code-block=\"fenced\"><code data-info=\"custom\">content\n</code></pre>",
        serialize(renderer.render(PARSER.parse("```java\ncontent\n```\n"))));
  }

  public void testElemental2RendererShouldSupportCustomNodeRenderers() {
    Elemental2Renderer renderer =
        Elemental2Renderer.builder()
            .nodeRendererFactory(
                context ->
                    new ElementNodeRenderer() {
                      @Override
                      public java.util.Set<Class<? extends org.dominokit.markdown.node.Node>>
                          getNodeTypes() {
                        return java.util.Set.of(org.dominokit.markdown.node.Link.class);
                      }

                      @Override
                      public void render(org.dominokit.markdown.node.Node node) {
                        context.append(DomGlobal.document.createTextNode("test"));
                      }
                    })
            .build();

    assertEquals("<p>foo test</p>", serialize(renderer.render(PARSER.parse("foo [bar](/url)"))));
  }

  public void testElemental2RendererShouldSanitizeUrls() {
    Elemental2Renderer renderer =
        Elemental2Renderer.builder()
            .sanitizeUrls(true)
            .urlSanitizer(
                new UrlSanitizer() {
                  @Override
                  public String sanitizeLinkUrl(String url) {
                    return "/safe?from=" + url;
                  }

                  @Override
                  public String sanitizeImageUrl(String url) {
                    return "/image?from=" + url;
                  }
                })
            .build();

    assertEquals(
        "<p><a rel=\"nofollow\" href=\"/safe?from=docs\">link</a></p>",
        serialize(renderer.render(PARSER.parse("[link](docs)"))));
    assertEquals(
        "<p><img src=\"/image?from=icon.png\" alt=\"alt\"></p>",
        serialize(renderer.render(PARSER.parse("![alt](icon.png)"))));
  }

  public void testElemental2RendererShouldExposeSoftBreakModes() {
    Elemental2Renderer brRenderer =
        Elemental2Renderer.builder().softBreakRendering(SoftBreakRendering.BR_ELEMENT).build();
    Elemental2Renderer spaceRenderer =
        Elemental2Renderer.builder().softBreakRendering(SoftBreakRendering.SPACE_TEXT).build();

    assertEquals("<p>foo<br>bar</p>", serialize(brRenderer.render(PARSER.parse("foo\nbar"))));
    assertEquals("<p>foo bar</p>", serialize(spaceRenderer.render(PARSER.parse("foo\nbar"))));
  }

  public void testElemental2RendererShouldOmitSingleParagraphWhenConfigured() {
    Elemental2Renderer renderer = Elemental2Renderer.builder().omitSingleParagraphP(true).build();

    assertEquals("hi <em>there</em>", serialize(renderer.render(PARSER.parse("hi *there*"))));
  }

  public void testHtmlRendererShouldSupportExtensionSetInBrowserBuild() {
    Parser parser = Parser.builder().extensions(ENGINE_EXTENSIONS).build();
    HtmlRenderer renderer = HtmlRenderer.builder().extensions(ENGINE_EXTENSIONS).build();

    assertEquals("<p><del>foo</del></p>\n", renderer.render(parser.parse("~~foo~~")));
    assertEquals(
        "<ul>\n<li><input type=\"checkbox\" disabled=\"\" checked=\"\"> task</li>\n</ul>\n",
        renderer.render(parser.parse("- [x] task\n")));
    assertEquals(
        "<table>\n<thead>\n<tr>\n<th>A</th>\n<th>B</th>\n</tr>\n</thead>\n<tbody>\n<tr>\n<td>1</td>\n<td>2</td>\n</tr>\n</tbody>\n</table>\n",
        renderer.render(parser.parse("A|B\n---|---\n1|2")));
    assertEquals(
        "<p><a href=\"http://www.example.com\">www.example.com</a></p>\n",
        renderer.render(parser.parse("www.example.com")));
  }

  public void testElemental2RendererShouldSupportExtensionSetInBrowserBuild() {
    Parser parser = Parser.builder().extensions(ENGINE_EXTENSIONS).build();
    Elemental2Renderer renderer =
        Elemental2Renderer.builder().percentEncodeUrls(true).extensions(ENGINE_EXTENSIONS).build();

    assertEquals("<p><del>foo</del></p>", serialize(renderer.render(parser.parse("~~foo~~"))));
    assertEquals(
        "<ul><li><input type=\"checkbox\" value=\"on\" disabled=\"\" checked=\"\"> task</li></ul>",
        serialize(renderer.render(parser.parse("- [x] task\n"))));
    assertEquals(
        "<table><thead><tr><th>A</th><th>B</th></tr></thead><tbody><tr><td>1</td><td>2</td></tr></tbody></table>",
        serialize(renderer.render(parser.parse("A|B\n---|---\n1|2"))));
    assertEquals(
        "<p><a href=\"http://www.example.com\">www.example.com</a></p>",
        serialize(renderer.render(parser.parse("www.example.com"))));
  }

  private static String serialize(elemental2.dom.DocumentFragment fragment) {
    Element container = DomGlobal.document.createElement("div");
    container.appendChild(fragment);
    return container.innerHTML;
  }
}
