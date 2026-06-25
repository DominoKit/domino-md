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

/**
 * Extension for GFM strikethrough using {@code ~} or {@code ~~}.
 *
 * <p>The parsed strikethrough text regions are turned into {@link Strikethrough} nodes.
 */
public final class StrikethroughExtension
    implements Parser.ParserExtension,
        HtmlRenderer.HtmlRendererExtension,
        Elemental2Renderer.Elemental2RendererExtension,
        TextContentRenderer.TextContentRendererExtension,
        MarkdownRenderer.MarkdownRendererExtension {

  private final boolean requireTwoTildes;

  /** Creates the default strikethrough extension configuration. */
  public StrikethroughExtension() {
    this(new Builder());
  }

  private StrikethroughExtension(Builder builder) {
    this.requireTwoTildes = builder.requireTwoTildes;
  }

  public static Extension create() {
    return builder().build();
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public void extend(Parser.Builder parserBuilder) {
    parserBuilder.customDelimiterProcessor(new StrikethroughDelimiterProcessor(requireTwoTildes));
  }

  @Override
  public void extend(HtmlRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(StrikethroughHtmlNodeRenderer::new);
  }

  @Override
  public void extend(Elemental2Renderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(StrikethroughElementNodeRenderer::new);
  }

  @Override
  public void extend(TextContentRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(StrikethroughTextContentNodeRenderer::new);
  }

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

  public static final class Builder {
    private boolean requireTwoTildes;

    public Builder requireTwoTildes(boolean requireTwoTildes) {
      this.requireTwoTildes = requireTwoTildes;
      return this;
    }

    public Extension build() {
      return new StrikethroughExtension(this);
    }
  }
}
