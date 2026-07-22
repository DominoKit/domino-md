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

import java.util.Set;
import org.dominokit.markdown.ext.task.list.items.TaskListItemMarker;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.NodeRenderer;

/** Base renderer for task-list marker nodes. */
abstract class TaskListItemNodeRenderer implements NodeRenderer {

  @Override
  /**
   * @return the task marker node type handled by this renderer
   */
  public Set<Class<? extends Node>> getNodeTypes() {
    return Set.of(TaskListItemMarker.class);
  }
}
