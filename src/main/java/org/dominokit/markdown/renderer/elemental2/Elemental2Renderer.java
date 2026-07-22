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
package org.dominokit.markdown.renderer.elemental2;

import elemental2.dom.Document;
import elemental2.dom.DocumentFragment;
import elemental2.dom.DomGlobal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.internal.renderer.NodeRendererMap;
import org.dominokit.markdown.internal.util.Escaping;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.html.DefaultUrlSanitizer;
import org.dominokit.markdown.renderer.html.UrlSanitizer;

/**
 * Renders a markdown node tree into Elemental2 DOM nodes.
 *
 * <p>The renderer composes a render pass from a document, a stack of active DOM containers, one or
 * more node renderers, and optional attribute providers. The result is always a detached {@link
 * DocumentFragment}, which keeps rendering side-effect free until the caller inserts the fragment
 * into the live DOM.
 */
public final class Elemental2Renderer {

  private final SoftBreakRendering softBreakRendering;
  private final boolean sanitizeUrls;
  private final UrlSanitizer urlSanitizer;
  private final boolean percentEncodeUrls;
  private final boolean omitSingleParagraphP;
  private final RawHtmlHandler rawHtmlHandler;
  private final List<ElementAttributeProviderFactory> attributeProviderFactories;
  private final List<ElementNodeRendererFactory> nodeRendererFactories;

  /**
   * Capture the builder state into an immutable renderer instance.
   *
   * <p>The constructor copies the builder collections so subsequent builder mutations do not affect
   * this renderer. The built-in core renderer is appended last so custom renderers can override
   * core node handling when they are registered earlier.
   *
   * @param builder renderer configuration source
   */
  private Elemental2Renderer(Builder builder) {
    this.softBreakRendering = builder.softBreakRendering;
    this.sanitizeUrls = builder.sanitizeUrls;
    this.urlSanitizer = builder.urlSanitizer;
    this.percentEncodeUrls = builder.percentEncodeUrls;
    this.omitSingleParagraphP = builder.omitSingleParagraphP;
    this.rawHtmlHandler = builder.rawHtmlHandler;
    this.attributeProviderFactories = new ArrayList<>(builder.attributeProviderFactories);

    this.nodeRendererFactories = new ArrayList<>(builder.nodeRendererFactories.size() + 1);
    this.nodeRendererFactories.addAll(builder.nodeRendererFactories);
    this.nodeRendererFactories.add(CoreElementNodeRenderer::new);
  }

  /** Create a new builder for configuring an {@link Elemental2Renderer}. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Render a node tree to a detached {@link DocumentFragment}.
   *
   * <p>The renderer creates a fresh document-backed render context for each call so concurrent
   * invocations do not share mutable state. All node renderers and attribute providers are
   * instantiated from the builder configuration at the start of the pass.
   *
   * @param node the markdown node tree to render
   * @return detached fragment containing the rendered DOM subtree
   */
  public DocumentFragment render(Node node) {
    Objects.requireNonNull(node, "node must not be null");
    RendererContext context = new RendererContext(DomGlobal.document);
    context.beforeRoot(node);
    context.render(node);
    context.afterRoot(node);
    return context.root;
  }

  /**
   * Builder for configuring an {@link Elemental2Renderer}.
   *
   * <p>The builder accumulates renderer options, extension hooks, and factory registrations. The
   * produced renderer is immutable after construction.
   */
  public static final class Builder {

    private SoftBreakRendering softBreakRendering = SoftBreakRendering.NEWLINE_TEXT;
    private boolean sanitizeUrls = false;
    private UrlSanitizer urlSanitizer = new DefaultUrlSanitizer();
    private boolean percentEncodeUrls = false;
    private boolean omitSingleParagraphP = false;
    private RawHtmlHandler rawHtmlHandler;
    private final List<ElementAttributeProviderFactory> attributeProviderFactories =
        new ArrayList<>();
    private final List<ElementNodeRendererFactory> nodeRendererFactories = new ArrayList<>();

    /** Create the configured renderer instance. */
    public Elemental2Renderer build() {
      return new Elemental2Renderer(this);
    }

    /**
     * Configure how soft line breaks should be rendered.
     *
     * @param softBreakRendering strategy to use for soft line breaks
     * @return this builder for chaining
     */
    public Builder softBreakRendering(SoftBreakRendering softBreakRendering) {
      this.softBreakRendering =
          Objects.requireNonNull(softBreakRendering, "softBreakRendering must not be null");
      return this;
    }

    /**
     * Enable or disable URL sanitization for links and images.
     *
     * @param sanitizeUrls {@code true} to sanitize link and image destinations before assignment
     * @return this builder for chaining
     */
    public Builder sanitizeUrls(boolean sanitizeUrls) {
      this.sanitizeUrls = sanitizeUrls;
      return this;
    }

    /**
     * Set the sanitizer used when URL sanitization is enabled.
     *
     * @param urlSanitizer sanitizer implementation to use
     * @return this builder for chaining
     */
    public Builder urlSanitizer(UrlSanitizer urlSanitizer) {
      this.urlSanitizer = Objects.requireNonNull(urlSanitizer, "urlSanitizer must not be null");
      return this;
    }

    /**
     * Enable or disable percent-encoding of link and image URLs.
     *
     * @param percentEncodeUrls {@code true} to percent-encode URLs before assignment
     * @return this builder for chaining
     */
    public Builder percentEncodeUrls(boolean percentEncodeUrls) {
      this.percentEncodeUrls = percentEncodeUrls;
      return this;
    }

    /**
     * Omit the wrapping paragraph tag when the document only contains one paragraph.
     *
     * @param omitSingleParagraphP {@code true} to drop a single top-level paragraph wrapper
     * @return this builder for chaining
     */
    public Builder omitSingleParagraphP(boolean omitSingleParagraphP) {
      this.omitSingleParagraphP = omitSingleParagraphP;
      return this;
    }

    /**
     * Set a custom raw-HTML handler.
     *
     * <p>When no handler is configured, raw HTML content falls back to plain text behavior rather
     * than being interpreted as DOM.
     *
     * @param rawHtmlHandler handler used to materialize raw HTML into DOM nodes, or {@code null}
     * @return this builder for chaining
     */
    public Builder rawHtmlHandler(RawHtmlHandler rawHtmlHandler) {
      this.rawHtmlHandler = rawHtmlHandler;
      return this;
    }

    /**
     * Register an attribute provider factory.
     *
     * @param attributeProviderFactory factory that creates per-render-pass attribute providers
     * @return this builder for chaining
     */
    public Builder attributeProviderFactory(
        ElementAttributeProviderFactory attributeProviderFactory) {
      this.attributeProviderFactories.add(
          Objects.requireNonNull(
              attributeProviderFactory, "attributeProviderFactory must not be null"));
      return this;
    }

    /**
     * Register a node renderer factory.
     *
     * @param nodeRendererFactory factory that creates per-render-pass node renderers
     * @return this builder for chaining
     */
    public Builder nodeRendererFactory(ElementNodeRendererFactory nodeRendererFactory) {
      this.nodeRendererFactories.add(
          Objects.requireNonNull(nodeRendererFactory, "nodeRendererFactory must not be null"));
      return this;
    }

    /**
     * Apply all matching Elemental2 renderer extensions.
     *
     * <p>Only extensions implementing {@link Elemental2RendererExtension} participate in this
     * builder. Other extension types are ignored because they target different renderer families.
     *
     * @param extensions extensions to inspect for Elemental2-specific hooks
     * @return this builder for chaining
     */
    public Builder extensions(Iterable<? extends Extension> extensions) {
      Objects.requireNonNull(extensions, "extensions must not be null");
      for (Extension extension : extensions) {
        if (extension instanceof Elemental2RendererExtension) {
          ((Elemental2RendererExtension) extension).extend(this);
        }
      }
      return this;
    }

    /**
     * Apply a single Elemental2 renderer extension.
     *
     * @param extension extension to apply
     * @return this builder for chaining
     */
    public Builder withExtension(Extension extension) {
      return extensions(List.of(Objects.requireNonNull(extension, "extension must not be null")));
    }

    /**
     * Apply one or more Elemental2 renderer extensions.
     *
     * @param extensions extensions to apply
     * @return this builder for chaining
     */
    public Builder withExtensions(Extension... extensions) {
      Objects.requireNonNull(extensions, "extensions must not be null");
      return extensions(List.of(extensions));
    }
  }

  /** Extension contract for {@link Elemental2Renderer}. */
  public interface Elemental2RendererExtension extends Extension {
    /** Contribute renderer configuration to the supplied builder. */
    void extend(Builder rendererBuilder);
  }

  private final class RendererContext
      implements ElementNodeRendererContext, ElementAttributeProviderContext {

    private final Document document;
    private final DocumentFragment root;
    private final Deque<elemental2.dom.Node> containers = new ArrayDeque<>();
    private final List<ElementAttributeProvider> attributeProviders;
    private final NodeRendererMap nodeRendererMap = new NodeRendererMap();

    /**
     * Create a render-pass context backed by the provided document.
     *
     * <p>The context owns the fragment that accumulates rendered nodes, the container stack used by
     * nested renderers, and the per-pass renderer/attribute-provider instances.
     */
    private RendererContext(Document document) {
      this.document = document;
      this.root = document.createDocumentFragment();
      this.containers.push(root);

      this.attributeProviders = new ArrayList<>(attributeProviderFactories.size());
      for (ElementAttributeProviderFactory factory : attributeProviderFactories) {
        this.attributeProviders.add(factory.create(this));
      }

      for (ElementNodeRendererFactory factory : nodeRendererFactories) {
        nodeRendererMap.add(factory.create(this));
      }
    }

    /**
     * @return the document used to create elements
     */
    @Override
    public Document getDocument() {
      return document;
    }

    /**
     * Encode a URL if percent-encoding is enabled.
     *
     * @param url the raw URL value
     * @return the encoded URL when configured, otherwise the original value
     */
    @Override
    public String encodeUrl(String url) {
      return percentEncodeUrls ? Escaping.percentEncodeUrl(url) : url;
    }

    /**
     * Merge default attributes with all registered Elemental2 attribute providers.
     *
     * <p>Providers are applied in registration order and may mutate the shared attribute map to
     * add, replace, or remove entries before the renderer creates the final DOM element.
     */
    @Override
    public Map<String, String> extendAttributes(
        Node node, String tagName, Map<String, String> attributes) {
      LinkedHashMap<String, String> result = new LinkedHashMap<>(attributes);
      for (ElementAttributeProvider attributeProvider : attributeProviders) {
        attributeProvider.setAttributes(node, tagName, result);
      }
      return result;
    }

    /** Render a single node into the current container. */
    @Override
    public void render(Node node) {
      nodeRendererMap.render(node);
    }

    /**
     * Render the children of the given markdown node into the current container.
     *
     * <p>The method walks the child list eagerly while caching {@code next} before each render so
     * renderers are free to mutate the tree without breaking traversal.
     */
    @Override
    public void renderChildren(Node parent) {
      Node node = parent.getFirstChild();
      while (node != null) {
        Node next = node.getNext();
        render(node);
        node = next;
      }
    }

    /**
     * Render the children of the given node into a specific DOM container.
     *
     * <p>The container is pushed on the active stack for the duration of the call so nested
     * renderers append into the correct DOM branch.
     */
    @Override
    public void renderChildren(Node parent, elemental2.dom.Node container) {
      containers.push(container);
      try {
        renderChildren(parent);
      } finally {
        containers.pop();
      }
    }

    /** Append a DOM node to the active container. */
    @Override
    public void append(elemental2.dom.Node node) {
      containers.peek().appendChild(node);
    }

    /**
     * @return the configured soft-break behavior
     */
    @Override
    public SoftBreakRendering softBreakRendering() {
      return softBreakRendering;
    }

    /**
     * @return whether a single paragraph should omit the wrapping {@code <p>} element
     */
    @Override
    public boolean shouldOmitSingleParagraphP() {
      return omitSingleParagraphP;
    }

    /**
     * @return whether URLs should be sanitized
     */
    @Override
    public boolean shouldSanitizeUrls() {
      return sanitizeUrls;
    }

    /**
     * @return the configured URL sanitizer
     */
    @Override
    public UrlSanitizer urlSanitizer() {
      return urlSanitizer;
    }

    /**
     * @return the configured raw HTML handler, or {@code null}
     */
    @Override
    public RawHtmlHandler rawHtmlHandler() {
      return rawHtmlHandler;
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
