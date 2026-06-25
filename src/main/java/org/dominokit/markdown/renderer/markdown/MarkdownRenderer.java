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

/** Renders nodes back to canonical CommonMark-style Markdown text. */
public class MarkdownRenderer implements Renderer {

  private final List<MarkdownNodeRendererFactory> nodeRendererFactories;

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

  /**
   * Create a new builder for configuring a {@link MarkdownRenderer}.
   *
   * @return a builder
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public void render(Node node, Appendable output) {
    Objects.requireNonNull(node, "node must not be null");
    Objects.requireNonNull(output, "output must not be null");
    RendererContext context = new RendererContext(new MarkdownWriter(output));
    context.beforeRoot(node);
    context.render(node);
    context.afterRoot(node);
  }

  @Override
  public String render(Node node) {
    Objects.requireNonNull(node, "node must not be null");
    StringBuilder sb = new StringBuilder();
    render(node, sb);
    return sb.toString();
  }

  /** Builder for configuring a {@link MarkdownRenderer}. */
  public static class Builder {

    private final List<MarkdownNodeRendererFactory> nodeRendererFactories = new ArrayList<>();

    /** @return the configured {@link MarkdownRenderer} */
    public MarkdownRenderer build() {
      return new MarkdownRenderer(this);
    }

    /**
     * Add a factory for creating a node renderer.
     *
     * @param nodeRendererFactory the factory to add
     * @return {@code this}
     */
    public Builder nodeRendererFactory(MarkdownNodeRendererFactory nodeRendererFactory) {
      this.nodeRendererFactories.add(
          Objects.requireNonNull(nodeRendererFactory, "nodeRendererFactory must not be null"));
      return this;
    }

    /**
     * @param extensions extensions to use on this Markdown renderer
     * @return {@code this}
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
  }

  /** Extension contract for {@link MarkdownRenderer}. */
  public interface MarkdownRendererExtension extends Extension {
    void extend(Builder rendererBuilder);
  }

  private final class RendererContext implements MarkdownNodeRendererContext {
    private final MarkdownWriter writer;
    private final NodeRendererMap nodeRendererMap = new NodeRendererMap();
    private final Set<Character> additionalTextEscapes;

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
    public MarkdownWriter getWriter() {
      return writer;
    }

    @Override
    public void render(Node node) {
      nodeRendererMap.render(node);
    }

    @Override
    public Set<Character> getSpecialCharacters() {
      return additionalTextEscapes;
    }

    private void beforeRoot(Node node) {
      nodeRendererMap.beforeRoot(node);
    }

    private void afterRoot(Node node) {
      nodeRendererMap.afterRoot(node);
    }
  }
}
