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

import org.dominokit.markdown.ext.task.list.items.TaskListItemMarker;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.text.TextContentNodeRendererContext;
import org.dominokit.markdown.renderer.text.TextContentWriter;

public class TaskListItemTextContentNodeRenderer extends TaskListItemNodeRenderer {

  private final TextContentWriter textContent;

  public TaskListItemTextContentNodeRenderer(TextContentNodeRendererContext context) {
    this.textContent = context.getWriter();
  }

  @Override
  public void render(Node node) {
    TaskListItemMarker marker = (TaskListItemMarker) node;
    textContent.write(marker.isChecked() ? "[x] " : "[ ] ");
  }
}
