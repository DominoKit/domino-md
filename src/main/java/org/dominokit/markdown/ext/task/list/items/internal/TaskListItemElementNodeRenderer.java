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

import elemental2.dom.Element;
import java.util.LinkedHashMap;
import java.util.Map;
import org.dominokit.markdown.ext.task.list.items.TaskListItemMarker;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.elemental2.ElementNodeRenderer;
import org.dominokit.markdown.renderer.elemental2.ElementNodeRendererContext;

public class TaskListItemElementNodeRenderer extends TaskListItemNodeRenderer
    implements ElementNodeRenderer {

  private final ElementNodeRendererContext context;

  public TaskListItemElementNodeRenderer(ElementNodeRendererContext context) {
    this.context = context;
  }

  @Override
  public void render(Node node) {
    TaskListItemMarker marker = (TaskListItemMarker) node;
    Map<String, String> attributes = new LinkedHashMap<>();
    attributes.put("type", "checkbox");
    attributes.put("disabled", "");
    if (marker.isChecked()) {
      attributes.put("checked", "");
    }

    Element input = context.getDocument().createElement("input");
    for (var attribute : context.extendAttributes(node, "input", attributes).entrySet()) {
      if (attribute.getValue() != null) {
        input.setAttribute(attribute.getKey(), attribute.getValue());
      }
    }

    context.append(input);
    context.append(context.getDocument().createTextNode(" "));
  }
}
