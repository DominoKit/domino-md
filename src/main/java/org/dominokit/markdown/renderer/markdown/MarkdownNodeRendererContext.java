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
package org.dominokit.markdown.renderer.markdown;

import java.util.Set;
import org.dominokit.markdown.node.Node;

/**
 * Rendering context exposed to Markdown node renderers.
 *
 * <p>The context is scoped to a single render pass and provides access to the writer and to any
 * custom characters that should be escaped when rendering plain text.
 */
public interface MarkdownNodeRendererContext {

  /**
   * @return the writer to use
   */
  MarkdownWriter getWriter();

  /**
   * Render the specified node and its children using the configured renderers.
   *
   * <p>This is primarily intended for child traversal. Passing the current node would recurse
   * forever.
   *
   * @param node the node to render
   */
  void render(Node node);

  /**
   * @return additional ASCII special characters that should be escaped in normal text
   */
  Set<Character> getSpecialCharacters();
}
