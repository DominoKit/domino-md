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
package org.dominokit.markdown.internal.inline;

import org.dominokit.markdown.node.Image;
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.LinkReferenceDefinition;
import org.dominokit.markdown.parser.InlineParserContext;
import org.dominokit.markdown.parser.beta.LinkInfo;
import org.dominokit.markdown.parser.beta.LinkProcessor;
import org.dominokit.markdown.parser.beta.LinkResult;
import org.dominokit.markdown.parser.beta.Scanner;

public class CoreLinkProcessor implements LinkProcessor {

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

  private static LinkResult process(
      LinkInfo linkInfo, Scanner scanner, String destination, String title) {
    if (linkInfo.marker() != null && linkInfo.marker().getLiteral().equals("!")) {
      return LinkResult.wrapTextIn(new Image(destination, title), scanner.position())
          .includeMarker();
    }
    return LinkResult.wrapTextIn(new Link(destination, title), scanner.position());
  }
}
