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

import java.util.Map;
import org.dominokit.markdown.node.Node;

/** Extension point for adding or changing DOM element attributes for a rendered node. */
public interface ElementAttributeProvider {

  /**
   * Set the attributes for a tag of the specified node by mutating the provided map.
   *
   * @param node the node being rendered
   * @param tagName the tag name these attributes apply to
   * @param attributes the default attributes, ready to be changed or removed
   */
  void setAttributes(Node node, String tagName, Map<String, String> attributes);
}
