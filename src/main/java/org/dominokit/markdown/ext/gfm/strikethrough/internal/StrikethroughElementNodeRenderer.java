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
package org.dominokit.markdown.ext.gfm.strikethrough.internal;

import elemental2.dom.Element;
import java.util.Map;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.elemental2.ElementNodeRenderer;
import org.dominokit.markdown.renderer.elemental2.ElementNodeRendererContext;

/**
 * Elemental2 renderer for GFM strikethrough nodes.
 *
 * <p>The renderer emits a {@code <del>} element, applies any configured attribute providers, and
 * then renders the children into that element.
 */
public class StrikethroughElementNodeRenderer extends StrikethroughNodeRenderer
    implements ElementNodeRenderer {

  private final ElementNodeRendererContext context;

  /**
   * Create a renderer bound to the active Elemental2 rendering context.
   *
   * @param context rendering context used for element creation and child traversal
   */
  public StrikethroughElementNodeRenderer(ElementNodeRendererContext context) {
    this.context = context;
  }

  /**
   * Render the strikethrough wrapper as a DOM {@code del} element.
   *
   * @param node strikethrough node to render
   */
  @Override
  public void render(Node node) {
    Element element = context.getDocument().createElement("del");
    for (var attribute : context.extendAttributes(node, "del", Map.of()).entrySet()) {
      if (attribute.getValue() != null) {
        element.setAttribute(attribute.getKey(), attribute.getValue());
      }
    }
    context.append(element);
    context.renderChildren(node, element);
  }
}
