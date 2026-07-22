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
package org.dominokit.markdown.ext.gfm.strikethrough;

import java.util.Set;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.ext.gfm.strikethrough.internal.StrikethroughDelimiterProcessor;
import org.dominokit.markdown.ext.gfm.strikethrough.internal.StrikethroughElementNodeRenderer;
import org.dominokit.markdown.ext.gfm.strikethrough.internal.StrikethroughHtmlNodeRenderer;
import org.dominokit.markdown.ext.gfm.strikethrough.internal.StrikethroughMarkdownNodeRenderer;
import org.dominokit.markdown.ext.gfm.strikethrough.internal.StrikethroughTextContentNodeRenderer;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.NodeRenderer;
import org.dominokit.markdown.renderer.elemental2.Elemental2Renderer;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.dominokit.markdown.renderer.markdown.MarkdownNodeRendererContext;
import org.dominokit.markdown.renderer.markdown.MarkdownNodeRendererFactory;
import org.dominokit.markdown.renderer.markdown.MarkdownRenderer;
import org.dominokit.markdown.renderer.text.TextContentRenderer;

/** Extension for GitHub-style strikethrough using {@code ~} or {@code ~~}. */
public final class StrikethroughExtension
    implements Parser.ParserExtension,
        HtmlRenderer.HtmlRendererExtension,
        Elemental2Renderer.Elemental2RendererExtension,
        TextContentRenderer.TextContentRendererExtension,
        MarkdownRenderer.MarkdownRendererExtension {

  private final boolean requireTwoTildes;

  /**
   * Create the default strikethrough extension configuration.
   *
   * <p>The default configuration accepts both single-tilde and double-tilde delimiters and
   * delegates to the builder so future options stay centralized in one place.
   */
  public StrikethroughExtension() {
    this(new Builder());
  }

  /**
   * Create an extension from a builder snapshot.
   *
   * @param builder configuration source
   */
  private StrikethroughExtension(Builder builder) {
    this.requireTwoTildes = builder.requireTwoTildes;
  }

  /**
   * Create the default strikethrough extension instance.
   *
   * @return a ready-to-install extension using the default builder configuration
   */
  public static Extension create() {
    return builder().build();
  }

  /**
   * Create a builder for configuring strikethrough behavior.
   *
   * @return new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Register the delimiter processor used to recognize strikethrough runs.
   *
   * @param parserBuilder parser builder to extend
   */
  @Override
  public void extend(Parser.Builder parserBuilder) {
    parserBuilder.customDelimiterProcessor(new StrikethroughDelimiterProcessor(requireTwoTildes));
  }

  /**
   * Register the HTML node renderer for strikethrough nodes.
   *
   * @param rendererBuilder HTML renderer builder to extend
   */
  @Override
  public void extend(HtmlRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(StrikethroughHtmlNodeRenderer::new);
  }

  /**
   * Register the Elemental2 node renderer for strikethrough nodes.
   *
   * @param rendererBuilder Elemental2 renderer builder to extend
   */
  @Override
  public void extend(Elemental2Renderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(StrikethroughElementNodeRenderer::new);
  }

  /**
   * Register the plain-text node renderer for strikethrough nodes.
   *
   * @param rendererBuilder text-content renderer builder to extend
   */
  @Override
  public void extend(TextContentRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(StrikethroughTextContentNodeRenderer::new);
  }

  /**
   * Register the Markdown node renderer for strikethrough nodes.
   *
   * @param rendererBuilder Markdown renderer builder to extend
   */
  @Override
  public void extend(MarkdownRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(
        new MarkdownNodeRendererFactory() {
          @Override
          public NodeRenderer create(MarkdownNodeRendererContext context) {
            return new StrikethroughMarkdownNodeRenderer(context);
          }

          @Override
          public Set<Character> getSpecialCharacters() {
            return Set.of('~');
          }
        });
  }

  /** Builder for {@link StrikethroughExtension}. */
  public static final class Builder {
    private boolean requireTwoTildes;

    /**
     * Require the opening and closing delimiter to use at least two tildes.
     *
     * @param requireTwoTildes whether single tildes should be rejected
     * @return this builder for chaining
     */
    public Builder requireTwoTildes(boolean requireTwoTildes) {
      this.requireTwoTildes = requireTwoTildes;
      return this;
    }

    /**
     * Build the configured extension.
     *
     * @return immutable extension instance
     */
    public Extension build() {
      return new StrikethroughExtension(this);
    }
  }
}
