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
package org.dominokit.markdown.parser.beta;

import org.dominokit.markdown.parser.InlineParserContext;

/**
 * An interface to decide how links/images are handled.
 *
 * <p>Implementations need to be registered with a parser via {@link
 * org.dominokit.markdown.parser.Parser.Builder#linkProcessor}. Then, when inline parsing is run,
 * each parsed link/image is passed to the processor. This includes links like these:
 *
 * <p>
 *
 * <pre><code>
 * [text](destination)
 * [text]
 * [text][]
 * [text][label]
 * </code></pre>
 *
 * And images:
 *
 * <pre><code>
 * ![text](destination)
 * ![text]
 * ![text][]
 * ![text][label]
 * </code></pre>
 *
 * See {@link LinkInfo} for accessing various parts of the parsed link/image.
 *
 * <p>The processor can then inspect the link/image and decide what to do with it by returning the
 * appropriate {@link LinkResult}. If it returns {@link LinkResult#none()}, the next registered
 * processor is tried. If none of them apply, the link is handled as it normally would.
 */
public interface LinkProcessor {

  /**
   * @param linkInfo information about the parsed link/image
   * @param scanner the scanner at the current position after the parsed link/image
   * @param context context for inline parsing
   * @return what to do with the link/image, e.g. do nothing (try the next processor), wrap the text
   *     in a node, or replace the link/image with a node
   */
  LinkResult process(LinkInfo linkInfo, Scanner scanner, InlineParserContext context);
}
