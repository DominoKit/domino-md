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
package org.dominokit.markdown.ext.task.list.items;

import org.dominokit.markdown.Extension;
import org.dominokit.markdown.ext.task.list.items.internal.TaskListItemElementNodeRenderer;
import org.dominokit.markdown.ext.task.list.items.internal.TaskListItemHtmlNodeRenderer;
import org.dominokit.markdown.ext.task.list.items.internal.TaskListItemPostProcessor;
import org.dominokit.markdown.ext.task.list.items.internal.TaskListItemTextContentNodeRenderer;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.elemental2.Elemental2Renderer;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.dominokit.markdown.renderer.text.TextContentRenderer;

/** Extension for GitHub-style task list items. */
public final class TaskListItemsExtension
    implements Parser.ParserExtension,
        HtmlRenderer.HtmlRendererExtension,
        Elemental2Renderer.Elemental2RendererExtension,
        TextContentRenderer.TextContentRendererExtension {

  private TaskListItemsExtension() {}

  public static Extension create() {
    return new TaskListItemsExtension();
  }

  @Override
  public void extend(Parser.Builder parserBuilder) {
    parserBuilder.postProcessor(new TaskListItemPostProcessor());
  }

  @Override
  public void extend(HtmlRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(TaskListItemHtmlNodeRenderer::new);
  }

  @Override
  public void extend(Elemental2Renderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(TaskListItemElementNodeRenderer::new);
  }

  @Override
  public void extend(TextContentRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(TaskListItemTextContentNodeRenderer::new);
  }
}
