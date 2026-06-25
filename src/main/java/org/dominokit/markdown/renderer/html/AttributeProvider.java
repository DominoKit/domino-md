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
import org.dominokit.markdown.node.Node;

/** Extension point for adding/changing attributes on HTML tags for a node. */
public interface AttributeProvider {

  /**
   * Set the attributes for a HTML tag of the specified node by modifying the provided map.
   *
   * <p>This allows to change or even remove default attributes. With great power comes great
   * responsibility.
   *
   * <p>The attribute key and values will be escaped (preserving character entities), so don't
   * escape them here, otherwise they will be double-escaped.
   *
   * <p>This method may be called multiple times for the same node, if the node is rendered using
   * multiple nested tags (e.g. code blocks).
   *
   * @param node the node to set attributes for
   * @param tagName the HTML tag name that these attributes are for (e.g. {@code h1}, {@code pre},
   *     {@code code}).
   * @param attributes the attributes, with any default attributes already set in the map
   */
  void setAttributes(Node node, String tagName, Map<String, String> attributes);
}
