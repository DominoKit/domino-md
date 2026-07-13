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

import java.util.Set;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.ext.task.list.items.internal.TaskListItemElementNodeRenderer;
import org.dominokit.markdown.ext.task.list.items.internal.TaskListItemHtmlNodeRenderer;
import org.dominokit.markdown.ext.task.list.items.internal.TaskListItemMarkdownNodeRenderer;
import org.dominokit.markdown.ext.task.list.items.internal.TaskListItemPostProcessor;
import org.dominokit.markdown.ext.task.list.items.internal.TaskListItemTextContentNodeRenderer;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.NodeRenderer;
import org.dominokit.markdown.renderer.elemental2.Elemental2Renderer;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.dominokit.markdown.renderer.markdown.MarkdownNodeRendererContext;
import org.dominokit.markdown.renderer.markdown.MarkdownNodeRendererFactory;
import org.dominokit.markdown.renderer.markdown.MarkdownRenderer;
import org.dominokit.markdown.renderer.text.TextContentRenderer;

/**
 * Extension for GitHub-style task list items.
 *
 * <p>The extension adds a post-processor that rewrites list items into task markers and registers
 * the corresponding renderers so the checkbox syntax can round-trip across output formats.
 */
public final class TaskListItemsExtension
    implements Parser.ParserExtension,
        HtmlRenderer.HtmlRendererExtension,
        Elemental2Renderer.Elemental2RendererExtension,
        TextContentRenderer.TextContentRendererExtension,
        MarkdownRenderer.MarkdownRendererExtension {

  /**
   * Create the default task list items extension configuration.
   *
   * <p>The default configuration has no tunable options and exists primarily for discovery and
   * simple instantiation.
   */
  public TaskListItemsExtension() {}

  /**
   * Create the default task list items extension instance.
   *
   * @return a ready-to-install extension
   */
  public static Extension create() {
    return new TaskListItemsExtension();
  }

  /**
   * Register the task-list post processor.
   *
   * @param parserBuilder parser builder to extend
   */
  @Override
  public void extend(Parser.Builder parserBuilder) {
    parserBuilder.postProcessor(new TaskListItemPostProcessor());
  }

  /**
   * Register the HTML node renderer for task list items.
   *
   * @param rendererBuilder HTML renderer builder to extend
   */
  @Override
  public void extend(HtmlRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(TaskListItemHtmlNodeRenderer::new);
  }

  /**
   * Register the Elemental2 node renderer for task list items.
   *
   * @param rendererBuilder DOM renderer builder to extend
   */
  @Override
  public void extend(Elemental2Renderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(TaskListItemElementNodeRenderer::new);
  }

  /**
   * Register the plain-text node renderer for task list items.
   *
   * @param rendererBuilder text-content renderer builder to extend
   */
  @Override
  public void extend(TextContentRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(TaskListItemTextContentNodeRenderer::new);
  }

  /**
   * Register the Markdown node renderer for task list items.
   *
   * @param rendererBuilder Markdown renderer builder to extend
   */
  @Override
  public void extend(MarkdownRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(
        new MarkdownNodeRendererFactory() {
          @Override
          public NodeRenderer create(MarkdownNodeRendererContext context) {
            return new TaskListItemMarkdownNodeRenderer(context);
          }

          @Override
          public Set<Character> getSpecialCharacters() {
            return Set.of();
          }
        });
  }
}
