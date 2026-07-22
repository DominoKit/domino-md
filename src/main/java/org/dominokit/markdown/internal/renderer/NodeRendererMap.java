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
package org.dominokit.markdown.internal.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.NodeRenderer;

/**
 * Resolves a node class to the first renderer that claims it.
 *
 * <p>The map preserves renderer registration order. The first renderer to register a particular
 * node type wins, which allows extensions to override core rendering by being added earlier in the
 * chain.
 */
public class NodeRendererMap {

  private final List<NodeRenderer> nodeRenderers = new ArrayList<>();
  private final Map<Class<? extends Node>, NodeRenderer> renderers = new HashMap<>(32);

  /**
   * Register a renderer and claim any node types it handles.
   *
   * <p>If a node type is already mapped, the existing renderer is kept. This is deliberate so that
   * the renderer list can be composed from multiple sources while still respecting "first wins"
   * precedence.
   */
  public void add(NodeRenderer nodeRenderer) {
    nodeRenderers.add(nodeRenderer);
    for (var nodeType : nodeRenderer.getNodeTypes()) {
      // The first node renderer for a node type "wins".
      renderers.putIfAbsent(nodeType, nodeRenderer);
    }
  }

  /**
   * Render the node with the registered renderer for its concrete class, if one exists.
   *
   * <p>Only exact class matches are used here because the renderer map is populated with the
   * concrete node types each renderer claims to support.
   *
   * @param node node to render
   */
  public void render(Node node) {
    var nodeRenderer = renderers.get(node.getClass());
    if (nodeRenderer != null) {
      nodeRenderer.render(node);
    }
  }

  /**
   * Notify every registered renderer before the root node is rendered.
   *
   * @param node root node about to be rendered
   */
  public void beforeRoot(Node node) {
    nodeRenderers.forEach(r -> r.beforeRoot(node));
  }

  /**
   * Notify every registered renderer after the root node has been rendered.
   *
   * @param node root node that was rendered
   */
  public void afterRoot(Node node) {
    nodeRenderers.forEach(r -> r.afterRoot(node));
  }
}
