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
package org.dominokit.markdown.ext.task.list.items.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import org.dominokit.markdown.ext.task.list.items.TaskListItemMarker;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.html.HtmlNodeRendererContext;
import org.dominokit.markdown.renderer.html.HtmlWriter;

/** Renders task-list marker nodes as disabled checkbox inputs. */
public class TaskListItemHtmlNodeRenderer extends TaskListItemNodeRenderer {

  private final HtmlNodeRendererContext context;
  private final HtmlWriter html;

  /**
   * Create a renderer bound to the active HTML rendering context.
   *
   * @param context rendering context used for HTML emission
   */
  public TaskListItemHtmlNodeRenderer(HtmlNodeRendererContext context) {
    this.context = context;
    this.html = context.getWriter();
  }

  @Override
  /** Render the checkbox input and trailing space. */
  public void render(Node node) {
    TaskListItemMarker marker = (TaskListItemMarker) node;
    Map<String, String> attributes = new LinkedHashMap<>();
    attributes.put("type", "checkbox");
    attributes.put("disabled", "");
    if (marker.isChecked()) {
      attributes.put("checked", "");
    }
    html.tag("input", context.extendAttributes(node, "input", attributes));
    html.text(" ");
  }
}
