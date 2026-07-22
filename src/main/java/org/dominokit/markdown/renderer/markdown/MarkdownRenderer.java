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
package org.dominokit.markdown.renderer.markdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.internal.renderer.NodeRendererMap;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.Renderer;

/**
 * Renders a markdown node tree back to canonical CommonMark-style Markdown text.
 *
 * <p>The renderer preserves semantic structure instead of exact source formatting. It rebuilds a
 * textual representation from the AST, applying the configured renderers and escape rules to
 * produce stable Markdown output.
 */
public class MarkdownRenderer implements Renderer {

  private final List<MarkdownNodeRendererFactory> nodeRendererFactories;

  /**
   * Capture the builder state into an immutable renderer instance.
   *
   * <p>The constructor copies the builder list so later builder mutations do not affect the
   * renderer. The built-in core renderer is appended last so custom Markdown node renderers can
   * override core behavior when registered earlier.
   *
   * @param builder renderer configuration source
   */
  private MarkdownRenderer(Builder builder) {
    this.nodeRendererFactories = new ArrayList<>(builder.nodeRendererFactories.size() + 1);
    this.nodeRendererFactories.addAll(builder.nodeRendererFactories);
    this.nodeRendererFactories.add(
        new MarkdownNodeRendererFactory() {
          @Override
          public org.dominokit.markdown.renderer.NodeRenderer create(
              MarkdownNodeRendererContext context) {
            return new CoreMarkdownNodeRenderer(context);
          }

          @Override
          public Set<Character> getSpecialCharacters() {
            return Set.of();
          }
        });
  }

  /** Create a new builder for configuring a {@link MarkdownRenderer}. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Render a node tree into the supplied appendable.
   *
   * @param node the markdown node tree to render
   * @param output destination for the generated Markdown text
   */
  @Override
  public void render(Node node, Appendable output) {
    Objects.requireNonNull(node, "node must not be null");
    Objects.requireNonNull(output, "output must not be null");
    RendererContext context = new RendererContext(new MarkdownWriter(output));
    context.beforeRoot(node);
    context.render(node);
    context.afterRoot(node);
  }

  /**
   * Render a node tree to a string.
   *
   * @param node the markdown node tree to render
   * @return the rendered Markdown
   */
  @Override
  public String render(Node node) {
    Objects.requireNonNull(node, "node must not be null");
    StringBuilder sb = new StringBuilder();
    render(node, sb);
    return sb.toString();
  }

  /**
   * Builder for configuring a {@link MarkdownRenderer}.
   *
   * <p>The builder collects custom node renderers and extensions before creating an immutable
   * renderer instance.
   */
  public static class Builder {

    private final List<MarkdownNodeRendererFactory> nodeRendererFactories = new ArrayList<>();

    /** Build the configured {@link MarkdownRenderer}. */
    public MarkdownRenderer build() {
      return new MarkdownRenderer(this);
    }

    /**
     * Register a node renderer factory.
     *
     * @param nodeRendererFactory factory used to create renderers during each render pass
     * @return this builder for chaining
     */
    public Builder nodeRendererFactory(MarkdownNodeRendererFactory nodeRendererFactory) {
      this.nodeRendererFactories.add(
          Objects.requireNonNull(nodeRendererFactory, "nodeRendererFactory must not be null"));
      return this;
    }

    /**
     * Apply Markdown-renderer-specific extensions.
     *
     * @param extensions extensions to inspect for {@link MarkdownRendererExtension} hooks
     * @return this builder for chaining
     */
    public Builder extensions(Iterable<? extends Extension> extensions) {
      Objects.requireNonNull(extensions, "extensions must not be null");
      for (Extension extension : extensions) {
        if (extension instanceof MarkdownRendererExtension) {
          ((MarkdownRendererExtension) extension).extend(this);
        }
      }
      return this;
    }

    /**
     * Apply a single Markdown renderer extension.
     *
     * @param extension extension to apply
     * @return this builder for chaining
     */
    public Builder withExtension(Extension extension) {
      return extensions(List.of(Objects.requireNonNull(extension, "extension must not be null")));
    }

    /**
     * Apply one or more Markdown renderer extensions.
     *
     * @param extensions extensions to apply
     * @return this builder for chaining
     */
    public Builder withExtensions(Extension... extensions) {
      Objects.requireNonNull(extensions, "extensions must not be null");
      return extensions(List.of(extensions));
    }
  }

  /** Extension contract for {@link MarkdownRenderer}. */
  public interface MarkdownRendererExtension extends Extension {
    /** Contribute renderer configuration to the supplied builder. */
    void extend(Builder rendererBuilder);
  }

  private final class RendererContext implements MarkdownNodeRendererContext {
    private final MarkdownWriter writer;
    private final NodeRendererMap nodeRendererMap = new NodeRendererMap();
    private final Set<Character> additionalTextEscapes;

    /**
     * Create a render-pass context backed by the supplied writer.
     *
     * @param writer destination for generated Markdown text
     */
    private RendererContext(MarkdownWriter writer) {
      this.writer = writer;
      Set<Character> escapes = new HashSet<>();
      for (MarkdownNodeRendererFactory factory : nodeRendererFactories) {
        escapes.addAll(factory.getSpecialCharacters());
      }
      this.additionalTextEscapes = Collections.unmodifiableSet(escapes);

      for (MarkdownNodeRendererFactory factory : nodeRendererFactories) {
        nodeRendererMap.add(factory.create(this));
      }
    }

    @Override
    /**
     * @return the writer used to emit Markdown
     */
    public MarkdownWriter getWriter() {
      return writer;
    }

    @Override
    /** Render a node using the renderer map associated with this context. */
    public void render(Node node) {
      nodeRendererMap.render(node);
    }

    @Override
    /**
     * @return additional text characters that should be escaped
     */
    public Set<Character> getSpecialCharacters() {
      return additionalTextEscapes;
    }

    /** Notify node renderers that a root render pass is about to begin. */
    private void beforeRoot(Node node) {
      nodeRendererMap.beforeRoot(node);
    }

    /** Notify node renderers that a root render pass has completed. */
    private void afterRoot(Node node) {
      nodeRendererMap.afterRoot(node);
    }
  }
}
