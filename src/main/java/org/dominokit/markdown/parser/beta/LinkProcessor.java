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
package org.dominokit.markdown.parser.beta;

import org.dominokit.markdown.parser.InlineParserContext;

/**
 * Hook that decides how links and images are handled.
 *
 * <p>Processors are invoked after the parser has identified a candidate link or image. They can
 * inspect the parsed link information and either wrap the link text in a node, replace the entire
 * construct, or decline to handle it.
 */
public interface LinkProcessor {

  /**
   * Inspect a parsed link or image and decide how it should be handled.
   *
   * @param linkInfo information about the parsed link/image
   * @param scanner the scanner at the current position after the parsed link/image
   * @param context context for inline parsing
   * @return what to do with the link/image
   */
  LinkResult process(LinkInfo linkInfo, Scanner scanner, InlineParserContext context);
}
