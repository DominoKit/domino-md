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
package org.dominokit.markdown.renderer.html;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.dominokit.markdown.node.Document;
import org.dominokit.markdown.node.FencedCodeBlock;
import org.dominokit.markdown.node.Image;
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.NodeRenderer;
import org.junit.Test;

public class HtmlRendererTest {

  @Test
  public void htmlAllowingShouldNotEscapeInlineHtml() {
    String rendered =
        htmlAllowingRenderer()
            .render(parse("paragraph with <span id='foo' class=\"bar\">inline &amp; html</span>"));

    assertThat(rendered)
        .isEqualTo("<p>paragraph with <span id='foo' class=\"bar\">inline &amp; html</span></p>\n");
  }

  @Test
  public void htmlAllowingShouldNotEscapeBlockHtml() {
    String rendered =
        htmlAllowingRenderer().render(parse("<div id='foo' class=\"bar\">block &amp;</div>"));

    assertThat(rendered).isEqualTo("<div id='foo' class=\"bar\">block &amp;</div>\n");
  }

  @Test
  public void htmlEscapingShouldEscapeInlineHtml() {
    String rendered =
        htmlEscapingRenderer()
            .render(parse("paragraph with <span id='foo' class=\"bar\">inline &amp; html</span>"));

    assertThat(rendered)
        .isEqualTo(
            "<p>paragraph with &lt;span id='foo' class=&quot;bar&quot;&gt;inline &amp; html&lt;/span&gt;</p>\n");
  }

  @Test
  public void htmlEscapingShouldEscapeHtmlBlocks() {
    String rendered =
        htmlEscapingRenderer().render(parse("<div id='foo' class=\"bar\">block &amp;</div>"));

    assertThat(rendered)
        .isEqualTo(
            "<p>&lt;div id='foo' class=&quot;bar&quot;&gt;block &amp;amp;&lt;/div&gt;</p>\n");
  }

  @Test
  public void textEscapingShouldEscapeHtmlSpecialCharacters() {
    String rendered = defaultRenderer().render(parse("escaping: & < > \" '"));

    assertThat(rendered).isEqualTo("<p>escaping: &amp; &lt; &gt; &quot; '</p>\n");
  }

  @Test
  public void characterReferencesWithoutSemicolonsShouldBeEscapedInLinks() {
    String input =
        "[example](&#x6A&#x61&#x76&#x61&#x73&#x63&#x72&#x69&#x70&#x74&#x3A&#x61&#x6C&#x65&#x72&#x74&#x28&#x27&#x58&#x53&#x53&#x27&#x29)";

    String rendered = defaultRenderer().render(parse(input));

    assertThat(rendered)
        .isEqualTo(
            "<p><a href=\"&amp;#x6A&amp;#x61&amp;#x76&amp;#x61&amp;#x73&amp;#x63&amp;#x72&amp;#x69&amp;#x70&amp;#x74&amp;#x3A&amp;#x61&amp;#x6C&amp;#x65&amp;#x72&amp;#x74&amp;#x28&amp;#x27&amp;#x58&amp;#x53&amp;#x53&amp;#x27&amp;#x29\">example</a></p>\n");
  }

  @Test
  public void attributeEscapingShouldEscapeHrefValues() {
    Paragraph paragraph = new Paragraph();
    Link link = new Link();
    link.setDestination("&colon;");
    paragraph.appendChild(link);

    assertThat(defaultRenderer().render(paragraph))
        .isEqualTo("<p><a href=\"&amp;colon;\"></a></p>\n");
  }

  @Test
  public void rawUrlsShouldNotFilterDangerousProtocols() {
    Paragraph paragraph = new Paragraph();
    Link link = new Link();
    link.setDestination("javascript:alert(5);");
    paragraph.appendChild(link);

    assertThat(rawUrlsRenderer().render(paragraph))
        .isEqualTo("<p><a href=\"javascript:alert(5);\"></a></p>\n");
  }

  @Test
  public void sanitizedUrlsShouldSetRelNoFollowAndKeepSafeUrls() {
    Paragraph paragraph = new Paragraph();
    Link link = new Link();
    link.setDestination("/exampleUrl");
    paragraph.appendChild(link);

    assertThat(sanitizeUrlsRenderer().render(paragraph))
        .isEqualTo("<p><a rel=\"nofollow\" href=\"/exampleUrl\"></a></p>\n");
  }

  @Test
  public void sanitizedUrlsShouldFilterDangerousProtocols() {
    Paragraph paragraph = new Paragraph();
    Link link = new Link();
    link.setDestination("javascript:alert(5);");
    paragraph.appendChild(link);

    assertThat(sanitizeUrlsRenderer().render(paragraph))
        .isEqualTo("<p><a rel=\"nofollow\" href=\"\"></a></p>\n");
  }

  @Test
  public void customUrlSanitizerShouldControlRenderedHref() {
    HtmlRenderer renderer =
        HtmlRenderer.builder()
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

    assertThat(renderer.render(parse("[link](docs)")))
        .isEqualTo("<p><a rel=\"nofollow\" href=\"/safe?from=docs\">link</a></p>\n");
    assertThat(renderer.render(parse("![alt](icon.png)")))
        .isEqualTo("<p><img src=\"/image?from=icon.png\" alt=\"alt\" /></p>\n");
  }

  @Test
  public void percentEncodeUrlShouldPreserveEncodedPartsAndEncodeUtf8() {
    HtmlRenderer renderer = HtmlRenderer.builder().percentEncodeUrls(true).build();

    assertThat(renderer.render(parse("[a](foo%20bar)")))
        .isEqualTo("<p><a href=\"foo%20bar\">a</a></p>\n");
    assertThat(renderer.render(parse("[a](foo%)"))).isEqualTo("<p><a href=\"foo%25\">a</a></p>\n");
    assertThat(renderer.render(parse("[a](!*'();:@&=+$,/?#[])")))
        .isEqualTo("<p><a href=\"!*'();:@&amp;=+$,/?#%5B%5D\">a</a></p>\n");
    assertThat(renderer.render(parse("[a](ä)"))).isEqualTo("<p><a href=\"%C3%A4\">a</a></p>\n");
  }

  @Test
  public void attributeProviderShouldOverrideCodeBlockAttributes() {
    AttributeProviderFactory custom =
        context ->
            (node, tagName, attributes) -> {
              if (node instanceof FencedCodeBlock && tagName.equals("code")) {
                FencedCodeBlock fencedCodeBlock = (FencedCodeBlock) node;
                attributes.remove("class");
                attributes.put("data-custom", fencedCodeBlock.getInfo());
              } else if (node instanceof FencedCodeBlock && tagName.equals("pre")) {
                attributes.put("data-code-block", "fenced");
              }
            };

    HtmlRenderer renderer = HtmlRenderer.builder().attributeProviderFactory(custom).build();

    assertThat(renderer.render(parse("```info\ncontent\n```")))
        .isEqualTo(
            "<pre data-code-block=\"fenced\"><code data-custom=\"info\">content\n</code></pre>\n");
    assertThat(renderer.render(parse("```evil\"\ncontent\n```")))
        .isEqualTo(
            "<pre data-code-block=\"fenced\"><code data-custom=\"evil&quot;\">content\n</code></pre>\n");
  }

  @Test
  public void attributeProviderShouldBeAbleToReplaceImageAltAttribute() {
    AttributeProviderFactory custom =
        context ->
            (node, tagName, attributes) -> {
              if (node instanceof Image) {
                attributes.remove("alt");
                attributes.put("test", "hey");
              }
            };

    HtmlRenderer renderer = HtmlRenderer.builder().attributeProviderFactory(custom).build();

    assertThat(renderer.render(parse("![foo](/url)\n")))
        .isEqualTo("<p><img src=\"/url\" test=\"hey\" /></p>\n");
  }

  @Test
  public void attributeProviderFactoryShouldCreateFreshInstanceForEachRender() {
    AttributeProviderFactory factory =
        context ->
            new AttributeProvider() {
              private int index = 0;

              @Override
              public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
                attributes.put("key", String.valueOf(index));
                index++;
              }
            };

    HtmlRenderer renderer = HtmlRenderer.builder().attributeProviderFactory(factory).build();

    String firstPass = renderer.render(parse("text node"));
    String secondPass = renderer.render(parse("text node"));

    assertThat(secondPass).isEqualTo(firstPass);
  }

  @Test
  public void withExtensionShouldRegisterAnHtmlRendererExtension() {
    HtmlRenderer renderer =
        HtmlRenderer.builder()
            .withExtension(
                new HtmlRenderer.HtmlRendererExtension() {
                  @Override
                  public void extend(HtmlRenderer.Builder builder) {
                    builder.attributeProviderFactory(
                        context ->
                            (node, tagName, attributes) -> {
                              if ("p".equals(tagName)) {
                                attributes.put("data-single", "true");
                              }
                            });
                  }
                })
            .build();

    assertThat(renderer.render(parse("paragraph")))
        .isEqualTo("<p data-single=\"true\">paragraph</p>\n");
  }

  @Test
  public void withExtensionsShouldRegisterMultipleHtmlRendererExtensions() {
    HtmlRenderer renderer =
        HtmlRenderer.builder()
            .withExtensions(
                new HtmlRenderer.HtmlRendererExtension() {
                  @Override
                  public void extend(HtmlRenderer.Builder builder) {
                    builder.attributeProviderFactory(
                        context ->
                            (node, tagName, attributes) -> {
                              if ("p".equals(tagName)) {
                                attributes.put("data-first", "true");
                              }
                            });
                  }
                },
                new HtmlRenderer.HtmlRendererExtension() {
                  @Override
                  public void extend(HtmlRenderer.Builder builder) {
                    builder.attributeProviderFactory(
                        context ->
                            (node, tagName, attributes) -> {
                              if ("p".equals(tagName)) {
                                attributes.put("data-second", "true");
                              }
                            });
                  }
                })
            .build();

    assertThat(renderer.render(parse("paragraph")))
        .isEqualTo("<p data-first=\"true\" data-second=\"true\">paragraph</p>\n");
  }

  @Test
  public void customNodeRendererShouldOverrideCoreLinkRendering() {
    HtmlNodeRendererFactory nodeRendererFactory =
        context ->
            new NodeRenderer() {
              @Override
              public Set<Class<? extends Node>> getNodeTypes() {
                return Set.of(Link.class);
              }

              @Override
              public void render(Node node) {
                context.getWriter().text("test");
              }
            };

    HtmlRenderer renderer = HtmlRenderer.builder().nodeRendererFactory(nodeRendererFactory).build();

    assertThat(renderer.render(parse("foo [bar](/url)"))).isEqualTo("<p>foo test</p>\n");
  }

  @Test
  public void orderedListStartZeroShouldRenderStartAttribute() {
    assertThat(defaultRenderer().render(parse("0. Test\n")))
        .isEqualTo("<ol start=\"0\">\n<li>Test</li>\n</ol>\n");
  }

  @Test
  public void imageAltTextShouldFlattenInlineChildrenAndBreaks() {
    assertThat(defaultRenderer().render(parse("![foo\nbar](/url)\n")))
        .isEqualTo("<p><img src=\"/url\" alt=\"foo\nbar\" /></p>\n");
    assertThat(defaultRenderer().render(parse("![foo  \nbar](/url)\n")))
        .isEqualTo("<p><img src=\"/url\" alt=\"foo\nbar\" /></p>\n");
    assertThat(defaultRenderer().render(parse("![foo &auml;](/url)\n")))
        .isEqualTo("<p><img src=\"/url\" alt=\"foo ä\" /></p>\n");
    assertThat(defaultRenderer().render(parse("![_foo_ **bar** [link](/url)](/url)\n")))
        .isEqualTo("<p><img src=\"/url\" alt=\"foo bar link\" /></p>\n");
    assertThat(defaultRenderer().render(parse("![`foo` bar](/url)\n")))
        .isEqualTo("<p><img src=\"/url\" alt=\"foo bar\" /></p>\n");
  }

  @Test
  public void documentContainingOnlyParagraphChildrenShouldRenderWithoutWrapperParagraph() {
    Node documentWithParagraph = parse("Here I have a test [link](http://www.google.com)");
    Node paragraph = documentWithParagraph.getFirstChild();

    Document document = new Document();
    Node child = paragraph.getFirstChild();
    while (child != null) {
      Node current = child;
      child = current.getNext();
      document.appendChild(current);
    }

    assertThat(defaultRenderer().render(document))
        .isEqualTo("Here I have a test <a href=\"http://www.google.com\">link</a>");
  }

  @Test
  public void omitSingleParagraphPShouldDropParagraphTagForSingleParagraphDocuments() {
    HtmlRenderer renderer = HtmlRenderer.builder().omitSingleParagraphP(true).build();

    assertThat(renderer.render(parse("hi *there*"))).isEqualTo("hi <em>there</em>");
  }

  @Test
  public void softbreakConfigurationShouldControlSoftLineBreakOutput() {
    HtmlRenderer renderer = HtmlRenderer.builder().softbreak("<br />").build();

    assertThat(renderer.render(parse("foo\nbar"))).isEqualTo("<p>foo<br />bar</p>\n");
  }

  @Test
  public void extensionsShouldBeAppliedThroughBuilderExtensionRegistration() {
    HtmlRenderer renderer =
        HtmlRenderer.builder()
            .extensions(
                Set.of(
                    (HtmlRenderer.HtmlRendererExtension)
                        builder ->
                            builder.attributeProviderFactory(
                                context ->
                                    (node, tagName, attributes) -> {
                                      if ("p".equals(tagName)) {
                                        attributes.put("data-ext", "true");
                                      }
                                    })))
            .build();

    assertThat(renderer.render(parse("hello"))).isEqualTo("<p data-ext=\"true\">hello</p>\n");
  }

  private static HtmlRenderer defaultRenderer() {
    return HtmlRenderer.builder().build();
  }

  private static HtmlRenderer htmlAllowingRenderer() {
    return HtmlRenderer.builder().escapeHtml(false).build();
  }

  private static HtmlRenderer sanitizeUrlsRenderer() {
    return HtmlRenderer.builder()
        .sanitizeUrls(true)
        .urlSanitizer(new DefaultUrlSanitizer())
        .build();
  }

  private static HtmlRenderer rawUrlsRenderer() {
    return HtmlRenderer.builder().sanitizeUrls(false).build();
  }

  private static HtmlRenderer htmlEscapingRenderer() {
    return HtmlRenderer.builder().escapeHtml(true).build();
  }

  private static Node parse(String source) {
    return Parser.builder().build().parse(source);
  }
}
