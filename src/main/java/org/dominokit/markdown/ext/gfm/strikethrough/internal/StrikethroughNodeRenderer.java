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

import java.util.Set;
import org.dominokit.markdown.ext.gfm.strikethrough.Strikethrough;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.NodeRenderer;

/**
 * Shared node-renderer base for the GFM strikethrough extension.
 *
 * <p>All concrete renderers in this package handle the same node type and only differ in how they
 * emit the opening and closing markup or text. This base class centralizes the handled node set.
 */
abstract class StrikethroughNodeRenderer implements NodeRenderer {

  /**
   * @return the strikethrough node type handled by this renderer family
   */
  @Override
  public Set<Class<? extends Node>> getNodeTypes() {
    return Set.of(Strikethrough.class);
  }
}
