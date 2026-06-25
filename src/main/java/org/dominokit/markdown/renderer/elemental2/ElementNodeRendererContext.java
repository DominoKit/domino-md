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
package org.dominokit.markdown.renderer.elemental2;

import elemental2.dom.Document;
import java.util.Map;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.html.UrlSanitizer;

/** Context exposed to Elemental2 DOM node renderers. */
public interface ElementNodeRendererContext {

  /** @return the document used to create DOM nodes for this render pass */
  Document getDocument();

  /**
   * @param url raw url value
   * @return encoded url depending on renderer configuration
   */
  String encodeUrl(String url);

  /**
   * Let extensions modify attributes for a rendered tag.
   *
   * @param node the markdown node being rendered
   * @param tagName the target tag name
   * @param attributes default attributes for that tag
   * @return the final attributes to apply
   */
  Map<String, String> extendAttributes(Node node, String tagName, Map<String, String> attributes);

  /**
   * Render a single markdown node into the current DOM container.
   *
   * @param node the node to render
   */
  void render(Node node);

  /**
   * Render all child markdown nodes into the current DOM container.
   *
   * @param parent the markdown parent whose children should be rendered
   */
  void renderChildren(Node parent);

  /**
   * Render all child markdown nodes into the specified DOM container.
   *
   * @param parent the markdown parent whose children should be rendered
   * @param container the DOM container that should receive the rendered children
   */
  void renderChildren(Node parent, elemental2.dom.Node container);

  /**
   * Append a DOM node to the current render container.
   *
   * @param node the DOM node to append
   */
  void append(elemental2.dom.Node node);

  /** @return configured soft break behavior */
  SoftBreakRendering softBreakRendering();

  /** @return whether a single document paragraph should omit the wrapping {@code <p>} */
  boolean shouldOmitSingleParagraphP();

  /** @return whether URLs should be sanitized before assignment */
  boolean shouldSanitizeUrls();

  /** @return sanitizer to use when {@link #shouldSanitizeUrls()} is true */
  UrlSanitizer urlSanitizer();

  /** @return configured raw HTML handling strategy, or {@code null} for safe fallback mode */
  RawHtmlHandler rawHtmlHandler();
}
