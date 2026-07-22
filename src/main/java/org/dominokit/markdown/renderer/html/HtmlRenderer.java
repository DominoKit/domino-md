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
package org.dominokit.markdown.renderer.html;

import java.util.*;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.internal.renderer.NodeRendererMap;
import org.dominokit.markdown.internal.util.Escaping;
import org.dominokit.markdown.node.*;
import org.dominokit.markdown.renderer.Renderer;

/**
 * Renders a markdown node tree to HTML.
 *
 * <p>The renderer is configurable through a builder so callers can control HTML escaping, URL
 * sanitization, percent-encoding, soft-break rendering, and extension hooks for custom node and
 * attribute renderers. Rendering is single-pass and writes directly to the supplied {@link
 * Appendable}.
 */
public class HtmlRenderer implements Renderer {

  private final String softbreak;
  private final boolean escapeHtml;
  private final boolean percentEncodeUrls;
  private final boolean omitSingleParagraphP;
  private final boolean sanitizeUrls;
  private final UrlSanitizer urlSanitizer;
  private final List<AttributeProviderFactory> attributeProviderFactories;
  private final List<HtmlNodeRendererFactory> nodeRendererFactories;

  /**
   * Capture the builder state into an immutable renderer instance.
   *
   * <p>The constructor copies the builder collections so later builder mutations do not affect
   * already-created renderers. The built-in core renderer is appended last so custom node renderers
   * can override core behavior when they register earlier in the builder.
   *
   * @param builder renderer configuration source
   */
  private HtmlRenderer(Builder builder) {
    this.softbreak = builder.softbreak;
    this.escapeHtml = builder.escapeHtml;
    this.percentEncodeUrls = builder.percentEncodeUrls;
    this.omitSingleParagraphP = builder.omitSingleParagraphP;
    this.sanitizeUrls = builder.sanitizeUrls;
    this.urlSanitizer = builder.urlSanitizer;
    this.attributeProviderFactories = new ArrayList<>(builder.attributeProviderFactories);

    this.nodeRendererFactories = new ArrayList<>(builder.nodeRendererFactories.size() + 1);
    this.nodeRendererFactories.addAll(builder.nodeRendererFactories);
    // Add as last. This means clients can override the rendering of core nodes if they want.
    this.nodeRendererFactories.add(CoreHtmlNodeRenderer::new);
  }

  /** Create a new builder for configuring an {@link HtmlRenderer}. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Render a node tree into the supplied appendable.
   *
   * <p>Each call creates a fresh render context so renderer state, attribute providers, and node
   * renderers do not leak between invocations.
   *
   * @param node the markdown node tree to render
   * @param output destination for the generated HTML
   */
  @Override
  public void render(Node node, Appendable output) {
    Objects.requireNonNull(node, "node must not be null");
    RendererContext context = new RendererContext(new HtmlWriter(output));
    context.beforeRoot(node);
    context.render(node);
    context.afterRoot(node);
  }

  /**
   * Render a node tree to a new string.
   *
   * @param node the markdown node tree to render
   * @return the rendered HTML
   */
  @Override
  public String render(Node node) {
    Objects.requireNonNull(node, "node must not be null");
    StringBuilder sb = new StringBuilder();
    render(node, sb);
    return sb.toString();
  }

  /**
   * Builder for configuring an {@link HtmlRenderer}.
   *
   * <p>The builder collects renderer flags, factories, and extensions before producing an immutable
   * renderer instance.
   */
  public static class Builder {

    private String softbreak = "\n";
    private boolean escapeHtml = false;
    private boolean sanitizeUrls = false;
    private UrlSanitizer urlSanitizer = new DefaultUrlSanitizer();
    private boolean percentEncodeUrls = false;
    private boolean omitSingleParagraphP = false;
    private List<AttributeProviderFactory> attributeProviderFactories = new ArrayList<>();
    private List<HtmlNodeRendererFactory> nodeRendererFactories = new ArrayList<>();

    /** Build the configured {@link HtmlRenderer}. */
    public HtmlRenderer build() {
      return new HtmlRenderer(this);
    }

    /**
     * Configure the HTML that should be emitted for soft line breaks.
     *
     * <p>Use {@code "\n"} to preserve source line wrapping invisibly, {@code " "} to normalize soft
     * breaks to spaces, or {@code "<br>"} / {@code "<br />"} to materialize them as visible breaks.
     *
     * @param softbreak HTML fragment to emit for soft line breaks
     * @return this builder for chaining
     */
    public Builder softbreak(String softbreak) {
      this.softbreak = softbreak;
      return this;
    }

    /**
     * Enable or disable HTML escaping for raw HTML nodes.
     *
     * <p>When enabled, {@link HtmlInline} and {@link HtmlBlock} are emitted as text instead of
     * being interpreted as markup. This only affects literal HTML nodes, not ordinary text content
     * inside other markdown structures.
     *
     * @param escapeHtml {@code true} to escape raw HTML nodes
     * @return this builder for chaining
     */
    public Builder escapeHtml(boolean escapeHtml) {
      this.escapeHtml = escapeHtml;
      return this;
    }

    /**
     * Enable or disable URL sanitization for links and images.
     *
     * @param sanitizeUrls {@code true} to sanitize link and image destinations before rendering
     * @return this builder for chaining
     * @since 0.14.0
     */
    public Builder sanitizeUrls(boolean sanitizeUrls) {
      this.sanitizeUrls = sanitizeUrls;
      return this;
    }

    /**
     * Set the sanitizer used when URL sanitization is enabled.
     *
     * @param urlSanitizer sanitizer to apply to link and image destinations
     * @return this builder for chaining
     * @since 0.14.0
     */
    public Builder urlSanitizer(UrlSanitizer urlSanitizer) {
      this.urlSanitizer = urlSanitizer;
      return this;
    }

    /**
     * Enable or disable percent-encoding of link and image URLs.
     *
     * <p>Percent-encoding is applied after any URL sanitization step so the final attribute value
     * is suitable for HTML output.
     *
     * @param percentEncodeUrls {@code true} to percent-encode URLs before emission
     * @return this builder for chaining
     */
    public Builder percentEncodeUrls(boolean percentEncodeUrls) {
      this.percentEncodeUrls = percentEncodeUrls;
      return this;
    }

    /**
     * Configure whether a single top-level paragraph should omit its wrapping {@code <p>} tag.
     *
     * @param omitSingleParagraphP {@code true} to drop the wrapper around a lone root paragraph
     * @return this builder for chaining
     */
    public Builder omitSingleParagraphP(boolean omitSingleParagraphP) {
      this.omitSingleParagraphP = omitSingleParagraphP;
      return this;
    }

    /**
     * Register an attribute provider factory.
     *
     * @param attributeProviderFactory factory that creates per-render attribute providers
     * @return this builder for chaining
     */
    public Builder attributeProviderFactory(AttributeProviderFactory attributeProviderFactory) {
      Objects.requireNonNull(attributeProviderFactory, "attributeProviderFactory must not be null");
      this.attributeProviderFactories.add(attributeProviderFactory);
      return this;
    }

    /**
     * Register a node renderer factory.
     *
     * <p>Factories are consulted in registration order; custom factories added earlier can override
     * the built-in renderer that is appended internally at the end of the list.
     *
     * @param nodeRendererFactory factory for creating a node renderer
     * @return this builder for chaining
     */
    public Builder nodeRendererFactory(HtmlNodeRendererFactory nodeRendererFactory) {
      Objects.requireNonNull(nodeRendererFactory, "nodeRendererFactory must not be null");
      this.nodeRendererFactories.add(nodeRendererFactory);
      return this;
    }

    /**
     * Apply HTML-renderer-specific extensions.
     *
     * @param extensions extensions to inspect for {@link HtmlRendererExtension} hooks
     * @return this builder for chaining
     */
    public Builder extensions(Iterable<? extends Extension> extensions) {
      Objects.requireNonNull(extensions, "extensions must not be null");
      for (Extension extension : extensions) {
        if (extension instanceof HtmlRendererExtension) {
          HtmlRendererExtension htmlRendererExtension = (HtmlRendererExtension) extension;
          htmlRendererExtension.extend(this);
        }
      }
      return this;
    }

    /**
     * Apply a single HTML renderer extension.
     *
     * @param extension extension to apply
     * @return this builder for chaining
     */
    public Builder withExtension(Extension extension) {
      return extensions(List.of(Objects.requireNonNull(extension, "extension must not be null")));
    }

    /**
     * Apply one or more HTML renderer extensions.
     *
     * @param extensions extensions to apply
     * @return this builder for chaining
     */
    public Builder withExtensions(Extension... extensions) {
      Objects.requireNonNull(extensions, "extensions must not be null");
      return extensions(List.of(extensions));
    }
  }

  /** Extension contract for {@link HtmlRenderer}. */
  public interface HtmlRendererExtension extends Extension {
    /** Contribute renderer configuration to the supplied builder. */
    void extend(Builder rendererBuilder);
  }

  private class RendererContext implements HtmlNodeRendererContext, AttributeProviderContext {

    private final HtmlWriter htmlWriter;
    private final List<AttributeProvider> attributeProviders;
    private final NodeRendererMap nodeRendererMap = new NodeRendererMap();

    /**
     * Create a render-pass context backed by the supplied writer.
     *
     * <p>The context instantiates attribute providers and node renderers eagerly so each render
     * call works with a fresh, isolated set of collaborators.
     */
    private RendererContext(HtmlWriter htmlWriter) {
      this.htmlWriter = htmlWriter;

      attributeProviders = new ArrayList<>(attributeProviderFactories.size());
      for (var attributeProviderFactory : attributeProviderFactories) {
        attributeProviders.add(attributeProviderFactory.create(this));
      }

      for (var factory : nodeRendererFactories) {
        var renderer = factory.create(this);
        nodeRendererMap.add(renderer);
      }
    }

    @Override
    /**
     * @return whether raw HTML nodes should be escaped
     */
    public boolean shouldEscapeHtml() {
      return escapeHtml;
    }

    @Override
    /**
     * @return whether a single root paragraph should omit the wrapper tag
     */
    public boolean shouldOmitSingleParagraphP() {
      return omitSingleParagraphP;
    }

    @Override
    /**
     * @return whether URLs should be sanitized before emission
     */
    public boolean shouldSanitizeUrls() {
      return sanitizeUrls;
    }

    @Override
    /**
     * @return the sanitizer used for links and images
     */
    public UrlSanitizer urlSanitizer() {
      return urlSanitizer;
    }

    @Override
    /**
     * Encode a URL if percent encoding is enabled.
     *
     * @param url the raw URL value
     * @return the encoded URL when configured, otherwise the original value
     */
    public String encodeUrl(String url) {
      if (percentEncodeUrls) {
        return Escaping.percentEncodeUrl(url);
      } else {
        return url;
      }
    }

    @Override
    /**
     * Merge default attributes with any registered HTML attribute providers.
     *
     * @param node the markdown node being rendered
     * @param tagName the HTML tag name being produced
     * @param attributes default attributes supplied by the renderer
     * @return the merged attribute map
     */
    public Map<String, String> extendAttributes(
        Node node, String tagName, Map<String, String> attributes) {
      Map<String, String> attrs = new LinkedHashMap<>(attributes);
      setCustomAttributes(node, tagName, attrs);
      return attrs;
    }

    @Override
    /**
     * @return the writer used to emit HTML
     */
    public HtmlWriter getWriter() {
      return htmlWriter;
    }

    @Override
    /**
     * @return the configured soft-break HTML fragment
     */
    public String getSoftbreak() {
      return softbreak;
    }

    @Override
    /** Render a node using the renderer map associated with this context. */
    public void render(Node node) {
      nodeRendererMap.render(node);
    }

    /** Notify node renderers that a root render pass is about to begin. */
    public void beforeRoot(Node node) {
      nodeRendererMap.beforeRoot(node);
    }

    /** Notify node renderers that a root render pass has completed. */
    public void afterRoot(Node node) {
      nodeRendererMap.afterRoot(node);
    }

    /**
     * Allow attribute providers to mutate the attribute map for a particular node and tag.
     *
     * @param node markdown node being rendered
     * @param tagName HTML tag name being produced
     * @param attrs mutable attribute map to update in place
     */
    private void setCustomAttributes(Node node, String tagName, Map<String, String> attrs) {
      for (AttributeProvider attributeProvider : attributeProviders) {
        attributeProvider.setAttributes(node, tagName, attrs);
      }
    }
  }
}
