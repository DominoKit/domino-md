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
package org.dominokit.markdown.ext.task.list.items.internal;

import org.dominokit.markdown.ext.task.list.items.TaskListItemMarker;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.markdown.MarkdownNodeRendererContext;
import org.dominokit.markdown.renderer.markdown.MarkdownWriter;

/** Renders task-list marker nodes back to Markdown. */
public class TaskListItemMarkdownNodeRenderer extends TaskListItemNodeRenderer {

  private final MarkdownNodeRendererContext context;
  private final MarkdownWriter writer;

  /**
   * Create a renderer bound to the active Markdown rendering context.
   *
   * @param context rendering context used for markdown emission
   */
  public TaskListItemMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
    this.context = context;
    this.writer = context.getWriter();
  }

  @Override
  /** Write the checkbox marker and render the remaining children. */
  public void render(Node node) {
    TaskListItemMarker marker = (TaskListItemMarker) node;
    writer.raw("[" + (marker.isChecked() ? "x" : " ") + "] ");
    renderChildren(node);
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
