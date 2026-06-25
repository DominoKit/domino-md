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
package org.dominokit.markdown.internal.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.NodeRenderer;

public class NodeRendererMap {

  private final List<NodeRenderer> nodeRenderers = new ArrayList<>();
  private final Map<Class<? extends Node>, NodeRenderer> renderers = new HashMap<>(32);

  /**
   * Set the renderer for each {@link NodeRenderer#getNodeTypes()}, unless there was already a
   * renderer set (first wins).
   */
  public void add(NodeRenderer nodeRenderer) {
    nodeRenderers.add(nodeRenderer);
    for (var nodeType : nodeRenderer.getNodeTypes()) {
      // The first node renderer for a node type "wins".
      renderers.putIfAbsent(nodeType, nodeRenderer);
    }
  }

  public void render(Node node) {
    var nodeRenderer = renderers.get(node.getClass());
    if (nodeRenderer != null) {
      nodeRenderer.render(node);
    }
  }

  public void beforeRoot(Node node) {
    nodeRenderers.forEach(r -> r.beforeRoot(node));
  }

  public void afterRoot(Node node) {
    nodeRenderers.forEach(r -> r.afterRoot(node));
  }
}
