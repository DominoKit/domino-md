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

import org.dominokit.markdown.internal.inline.LinkResultImpl;
import org.dominokit.markdown.node.Node;

/**
 * Decision returned by a {@link LinkProcessor}.
 *
 * <p>Processors can either decline a link, wrap the parsed text in a node, or replace the entire
 * construct with a different node.
 */
public interface LinkResult {
  /** @return a result indicating that the processor did not handle the link */
  static LinkResult none() {
    return null;
  }

  /**
   * Wrap the link text in a node. This is the normal behavior for links, e.g. for this:
   *
   * <pre><code>
   * [my *text*](destination)
   * </code></pre>
   *
   * The text is {@code my *text*}, a text node and emphasis. The text is wrapped in a {@link
   * org.dominokit.markdown.node.Link} node, which means the text is added as child nodes to it.
   *
   * @param node the node to which the link text nodes will be added as child nodes
   * @param position the position to continue parsing from
   * @return a wrapping result
   */
  static LinkResult wrapTextIn(Node node, Position position) {
    return new LinkResultImpl(LinkResultImpl.Type.WRAP, node, position);
  }

  /**
   * Replace the link with a node. E.g. for this:
   *
   * <pre><code>
   * [^foo]
   * </code></pre>
   *
   * The processor could decide to create a {@code FootnoteReference} node instead which replaces
   * the link.
   *
   * @param node the node to replace the link with
   * @param position the position to continue parsing from
   * @return a replacing result
   */
  static LinkResult replaceWith(Node node, Position position) {
    return new LinkResultImpl(LinkResultImpl.Type.REPLACE, node, position);
  }

  /**
   * Include the marker, if present, in the processing decision.
   *
   * @return this result configured to include the marker
   */
  LinkResult includeMarker();
}
