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
package org.dominokit.markdown.renderer.html;

import java.util.Map;
import org.dominokit.markdown.node.Node;

/**
 * Extension point for adding or changing attributes on HTML tags for a node.
 *
 * <p>Implementations may add, replace, or remove attributes before the final tag is emitted.
 * Attribute values are escaped by the renderer, so providers should supply raw text rather than
 * pre-escaped HTML entities.
 */
public interface AttributeProvider {

  /**
   * Mutate the attribute map for the tag currently being rendered.
   *
   * <p>This callback can be invoked multiple times for the same markdown node when that node is
   * rendered as multiple nested HTML tags, such as {@code pre > code}. Providers should therefore
   * key their logic on both the node and the target tag name.
   *
   * @param node the node to set attributes for
   * @param tagName the HTML tag name that these attributes are for
   * @param attributes the attributes, with any default attributes already set in the map
   */
  void setAttributes(Node node, String tagName, Map<String, String> attributes);
}
