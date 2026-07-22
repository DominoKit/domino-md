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

import java.util.Map;
import org.dominokit.markdown.node.Image;
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.Node;

/**
 * Rendering context exposed to HTML node renderers.
 *
 * <p>Renderers use this context to write HTML, delegate rendering of child nodes, and inspect the
 * active renderer configuration. The context is render-pass scoped, so implementations should treat
 * it as ephemeral and not retain it after rendering completes.
 */
public interface HtmlNodeRendererContext {

  /**
   * Encode a URL according to the renderer configuration.
   *
   * @param url the raw URL value
   * @return the encoded URL when percent-encoding is enabled, otherwise the original value
   */
  String encodeUrl(String url);

  /**
   * Let extensions modify the HTML tag attributes.
   *
   * <p>Implementations should treat the provided map as mutable and return the final merged
   * attribute set after all registered providers have had a chance to contribute.
   *
   * @param node the node for which the attributes are applied
   * @param tagName the HTML tag name that these attributes are for
   * @param attributes the attributes that were calculated by the renderer
   * @return the extended attributes with added, updated, or removed entries
   */
  Map<String, String> extendAttributes(Node node, String tagName, Map<String, String> attributes);

  /**
   * @return the HTML writer to use
   */
  HtmlWriter getWriter();

  /**
   * @return HTML that should be rendered for a soft line break
   */
  String getSoftbreak();

  /**
   * Render the specified node and its children using the configured renderers.
   *
   * <p>This should be used to render child nodes. Passing the node that is currently being rendered
   * would recurse forever.
   *
   * @param node the node to render
   */
  void render(Node node);

  /**
   * @return whether HTML blocks and tags should be escaped or not
   */
  boolean shouldEscapeHtml();

  /**
   * @return whether documents that only contain a single paragraph should be rendered without the
   *     {@code <p>} tag
   */
  boolean shouldOmitSingleParagraphP();

  /**
   * @return true if the {@link UrlSanitizer} should be used
   * @since 0.14.0
   */
  boolean shouldSanitizeUrls();

  /**
   * @return sanitizer to use for securing {@link Link} href and {@link Image} src if {@link
   *     #shouldSanitizeUrls()} is true
   * @since 0.14.0
   */
  UrlSanitizer urlSanitizer();
}
