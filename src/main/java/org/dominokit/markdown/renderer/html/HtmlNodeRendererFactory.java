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

import org.dominokit.markdown.renderer.NodeRenderer;

/**
 * Factory for HTML node renderers.
 *
 * <p>Factories are instantiated once per render pass and receive the active context so they can
 * create renderers with access to the current writer, attribute providers, and configuration
 * flags.
 */
public interface HtmlNodeRendererFactory {

  /**
   * Create a new node renderer for the specified rendering context.
   *
   * @param context the context for rendering
   * @return a node renderer
   */
  NodeRenderer create(HtmlNodeRendererContext context);
}
