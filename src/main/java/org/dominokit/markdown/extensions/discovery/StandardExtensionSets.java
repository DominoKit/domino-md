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
package org.dominokit.markdown.extensions.discovery;

import java.util.List;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.ext.autolink.AutolinkExtension;
import org.dominokit.markdown.ext.gfm.strikethrough.StrikethroughExtension;
import org.dominokit.markdown.ext.gfm.tables.TablesExtension;
import org.dominokit.markdown.ext.task.list.items.TaskListItemsExtension;

/** Standard extension combinations that ship with the engine. */
public final class StandardExtensionSets {

  private StandardExtensionSets() {}

  /**
   * Returns the bundled GFM-oriented extension set in a deterministic order.
   *
   * <p>The returned list contains fresh extension instances and can be reused across the parser and
   * renderer builders.
   */
  public static List<Extension> coreGfm() {
    return List.of(
        StrikethroughExtension.create(),
        TaskListItemsExtension.create(),
        TablesExtension.create(),
        AutolinkExtension.create());
  }
}
