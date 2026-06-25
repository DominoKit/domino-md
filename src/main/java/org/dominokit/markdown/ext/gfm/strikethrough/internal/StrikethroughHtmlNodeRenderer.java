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
package org.dominokit.markdown.ext.gfm.strikethrough.internal;

import java.util.Map;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.html.HtmlNodeRendererContext;
import org.dominokit.markdown.renderer.html.HtmlWriter;

public class StrikethroughHtmlNodeRenderer extends StrikethroughNodeRenderer {

  private final HtmlNodeRendererContext context;
  private final HtmlWriter html;

  public StrikethroughHtmlNodeRenderer(HtmlNodeRendererContext context) {
    this.context = context;
    this.html = context.getWriter();
  }

  @Override
  public void render(Node node) {
    html.tag("del", context.extendAttributes(node, "del", Map.of()));
    renderChildren(node);
    html.tag("/del");
  }

  private void renderChildren(Node parent) {
    Node node = parent.getFirstChild();
    while (node != null) {
      Node next = node.getNext();
      context.render(node);
      node = next;
    }
  }
}
