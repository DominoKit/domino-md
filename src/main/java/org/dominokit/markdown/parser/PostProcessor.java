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
package org.dominokit.markdown.parser;

import org.dominokit.markdown.node.Node;

/**
 * Post-processes a parsed document.
 *
 * <p>Post-processors run after block and inline parsing have completed, which makes them suitable
 * for structural rewrites that are awkward to express during parsing itself.
 */
public interface PostProcessor {

  /**
   * Transform the parsed document and return the result.
   *
   * @param document parsed document tree
   * @return the transformed document tree
   */
  Node process(Node document);
}
