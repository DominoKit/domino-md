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
package org.dominokit.markdown.renderer.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.internal.renderer.NodeRendererMap;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.Renderer;

/**
 * Renders markdown nodes to plain text content with minimal markup-like additions.
 *
 * <p>The renderer is intentionally lossy: it preserves enough structure for readability but
 * discards syntax details that do not matter in plain text.
 */
public class TextContentRenderer implements Renderer {

  private final LineBreakRendering lineBreakRendering;
  private final List<TextContentNodeRendererFactory> nodeRendererFactories;

  /**
   * Create a renderer from the supplied builder configuration.
   *
   * @param builder renderer configuration state
   */
  private TextContentRenderer(Builder builder) {
    this.lineBreakRendering = builder.lineBreakRendering;
    this.nodeRendererFactories = new ArrayList<>(builder.nodeRendererFactories.size() + 1);
    this.nodeRendererFactories.addAll(builder.nodeRendererFactories);
    this.nodeRendererFactories.add(CoreTextContentNodeRenderer::new);
  }

  /** Create a new builder for configuring a {@link TextContentRenderer}. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Render a node tree into the supplied appendable.
   *
   * @param node the markdown node tree to render
   * @param output destination for the generated text
   */
  @Override
  public void render(Node node, Appendable output) {
    Objects.requireNonNull(node, "node must not be null");
    Objects.requireNonNull(output, "output must not be null");
    RendererContext context =
        new RendererContext(new TextContentWriter(output, lineBreakRendering));
    context.beforeRoot(node);
    context.render(node);
    context.afterRoot(node);
  }

  /**
   * Render a node tree to a string.
   *
   * @param node the markdown node tree to render
   * @return the rendered plain text
   */
  @Override
  public String render(Node node) {
    Objects.requireNonNull(node, "node must not be null");
    StringBuilder sb = new StringBuilder();
    render(node, sb);
    return sb.toString();
  }

  /**
   * Builder for configuring a {@link TextContentRenderer}.
   *
   * <p>The builder collects line-break behavior, custom renderers, and extensions before creating
   * an immutable renderer instance.
   */
  public static class Builder {

    private final List<TextContentNodeRendererFactory> nodeRendererFactories = new ArrayList<>();
    private LineBreakRendering lineBreakRendering = LineBreakRendering.COMPACT;

    /** Build the configured {@link TextContentRenderer}. */
    public TextContentRenderer build() {
      return new TextContentRenderer(this);
    }

    /**
     * Configure how line breaks are rendered.
     *
     * @param lineBreakRendering the mode to use
     * @return this builder for chaining
     */
    public Builder lineBreakRendering(LineBreakRendering lineBreakRendering) {
      this.lineBreakRendering =
          Objects.requireNonNull(lineBreakRendering, "lineBreakRendering must not be null");
      return this;
    }

    /**
     * @param stripNewlines true to flatten text, false to keep compact block separation
     * @return this builder for chaining
     * @deprecated Use {@link #lineBreakRendering(LineBreakRendering)} instead.
     */
    @Deprecated
    public Builder stripNewlines(boolean stripNewlines) {
      this.lineBreakRendering =
          stripNewlines ? LineBreakRendering.STRIP : LineBreakRendering.COMPACT;
      return this;
    }

    /**
     * Add a factory for creating a node renderer.
     *
     * @param nodeRendererFactory the renderer factory to add
     * @return this builder for chaining
     */
    public Builder nodeRendererFactory(TextContentNodeRendererFactory nodeRendererFactory) {
      this.nodeRendererFactories.add(
          Objects.requireNonNull(nodeRendererFactory, "nodeRendererFactory must not be null"));
      return this;
    }

    /**
     * Apply text-renderer-specific extensions.
     *
     * @param extensions extensions to inspect for {@link TextContentRendererExtension} hooks
     * @return this builder for chaining
     */
    public Builder extensions(Iterable<? extends Extension> extensions) {
      Objects.requireNonNull(extensions, "extensions must not be null");
      for (Extension extension : extensions) {
        if (extension instanceof TextContentRendererExtension) {
          ((TextContentRendererExtension) extension).extend(this);
        }
      }
      return this;
    }

    /**
     * Apply a single text renderer extension.
     *
     * @param extension extension to apply
     * @return this builder for chaining
     */
    public Builder withExtension(Extension extension) {
      return extensions(List.of(Objects.requireNonNull(extension, "extension must not be null")));
    }

    /**
     * Apply one or more text renderer extensions.
     *
     * @param extensions extensions to apply
     * @return this builder for chaining
     */
    public Builder withExtensions(Extension... extensions) {
      Objects.requireNonNull(extensions, "extensions must not be null");
      return extensions(List.of(extensions));
    }
  }

  /** Extension contract for {@link TextContentRenderer}. */
  public interface TextContentRendererExtension extends Extension {
    /** Contribute renderer configuration to the supplied builder. */
    void extend(Builder rendererBuilder);
  }

  private final class RendererContext implements TextContentNodeRendererContext {
    private final TextContentWriter textContentWriter;
    private final NodeRendererMap nodeRendererMap = new NodeRendererMap();

    /**
     * Create a render-pass context backed by the supplied writer.
     *
     * @param textContentWriter destination for generated text
     */
    private RendererContext(TextContentWriter textContentWriter) {
      this.textContentWriter = textContentWriter;
      for (TextContentNodeRendererFactory factory : nodeRendererFactories) {
        nodeRendererMap.add(factory.create(this));
      }
    }

    @Override
    /**
     * @return the configured line-break mode
     */
    public LineBreakRendering lineBreakRendering() {
      return lineBreakRendering;
    }

    @Override
    @Deprecated
    /**
     * @return whether line breaks are stripped
     */
    public boolean stripNewlines() {
      return lineBreakRendering == LineBreakRendering.STRIP;
    }

    @Override
    /**
     * @return the writer used to emit text content
     */
    public TextContentWriter getWriter() {
      return textContentWriter;
    }

    @Override
    /** Render a node using the renderer map associated with this context. */
    public void render(Node node) {
      nodeRendererMap.render(node);
    }

    /** Notify registered renderers that root rendering is about to start. */
    private void beforeRoot(Node node) {
      nodeRendererMap.beforeRoot(node);
    }

    /** Notify registered renderers that root rendering has completed. */
    private void afterRoot(Node node) {
      nodeRendererMap.afterRoot(node);
    }
  }
}
