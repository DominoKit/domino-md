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
package org.dominokit.markdown.internal.inline;

import org.dominokit.markdown.node.Image;
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.LinkReferenceDefinition;
import org.dominokit.markdown.parser.InlineParserContext;
import org.dominokit.markdown.parser.beta.LinkInfo;
import org.dominokit.markdown.parser.beta.LinkProcessor;
import org.dominokit.markdown.parser.beta.LinkResult;
import org.dominokit.markdown.parser.beta.Scanner;

/**
 * Default link processor that turns parsed link metadata into link or image nodes.
 *
 * <p>The processor first resolves inline links, then falls back to link-reference definitions when
 * the input is a reference link. When the parsed marker is {@code !}, the processor creates an
 * image node instead of a link node and instructs the parser to keep the marker in the input.
 */
public class CoreLinkProcessor implements LinkProcessor {

  /**
   * Resolve a parsed link description into a link or image node.
   *
   * @param linkInfo parsed link metadata
   * @param scanner scanner positioned after the closing delimiter
   * @param context inline parser context used to resolve reference definitions
   * @return a link-processing result, or none when the reference cannot be resolved
   */
  @Override
  public LinkResult process(LinkInfo linkInfo, Scanner scanner, InlineParserContext context) {
    if (linkInfo.destination() != null) {
      // Inline link
      return process(linkInfo, scanner, linkInfo.destination(), linkInfo.title());
    }

    var label = linkInfo.label();
    var ref = label != null && !label.isEmpty() ? label : linkInfo.text();
    var def = context.getDefinition(LinkReferenceDefinition.class, ref);
    if (def != null) {
      // Reference link
      return process(linkInfo, scanner, def.getDestination(), def.getTitle());
    }
    return LinkResult.none();
  }

  /**
   * Create the final link-processing result after the destination and title have been resolved.
   *
   * @param linkInfo parsed link metadata
   * @param scanner scanner positioned after the closing delimiter
   * @param destination resolved link destination
   * @param title resolved link title
   * @return a result that wraps the parsed text in a link or image node
   */
  private static LinkResult process(
      LinkInfo linkInfo, Scanner scanner, String destination, String title) {
    if (linkInfo.marker() != null && linkInfo.marker().getLiteral().equals("!")) {
      return LinkResult.wrapTextIn(new Image(destination, title), scanner.position())
          .includeMarker();
    }
    return LinkResult.wrapTextIn(new Link(destination, title), scanner.position());
  }
}
