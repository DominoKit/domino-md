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
package org.dominokit.markdown.renderer.elemental2;

/**
 * Strategy for rendering raw HTML nodes into DOM nodes.
 *
 * <p>Handlers may return {@code null} to fall back to the renderer's safe text fallback.
 */
public interface RawHtmlHandler {

  /**
   * Render inline raw HTML.
   *
   * @param literal raw inline HTML from the parsed markdown
   * @return rendered DOM node, or {@code null} to use the safe fallback
   */
  elemental2.dom.Node renderInline(String literal);

  /**
   * Render block raw HTML.
   *
   * @param literal raw block HTML from the parsed markdown
   * @return rendered DOM node, or {@code null} to use the safe fallback
   */
  elemental2.dom.Node renderBlock(String literal);
}
