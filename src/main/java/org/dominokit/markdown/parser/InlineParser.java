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
package org.dominokit.markdown.parser;

import org.dominokit.markdown.node.Node;

/**
 * Parser for inline content such as text, emphasis, and links.
 */
public interface InlineParser {

  /**
   * Parse inline content from the supplied source lines and append the resulting nodes to the
   * target block.
   */
  void parse(SourceLines lines, Node node);
}
