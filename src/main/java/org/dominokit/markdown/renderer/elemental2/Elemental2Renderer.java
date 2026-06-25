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

/** Renders a markdown node tree into Elemental2 DOM nodes. */
public final class Elemental2Renderer {

  private final SoftBreakRendering softBreakRendering;
  private final boolean sanitizeUrls;
  private final UrlSanitizer urlSanitizer;
  private final boolean percentEncodeUrls;
  private final boolean omitSingleParagraphP;
  private final RawHtmlHandler rawHtmlHandler;
  private final List<ElementAttributeProviderFactory> attributeProviderFactories;
  private final List<ElementNodeRendererFactory> nodeRendererFactories;

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

  /** Builder for configuring an {@link Elemental2Renderer}. */
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

    /** @return the configured renderer */
    public Elemental2Renderer build() {
      return new Elemental2Renderer(this);
    }

    public Builder softBreakRendering(SoftBreakRendering softBreakRendering) {
      this.softBreakRendering =
          Objects.requireNonNull(softBreakRendering, "softBreakRendering must not be null");
      return this;
    }

    public Builder sanitizeUrls(boolean sanitizeUrls) {
      this.sanitizeUrls = sanitizeUrls;
      return this;
    }

    public Builder urlSanitizer(UrlSanitizer urlSanitizer) {
      this.urlSanitizer = Objects.requireNonNull(urlSanitizer, "urlSanitizer must not be null");
      return this;
    }

    public Builder percentEncodeUrls(boolean percentEncodeUrls) {
      this.percentEncodeUrls = percentEncodeUrls;
      return this;
    }

    public Builder omitSingleParagraphP(boolean omitSingleParagraphP) {
      this.omitSingleParagraphP = omitSingleParagraphP;
      return this;
    }

    public Builder rawHtmlHandler(RawHtmlHandler rawHtmlHandler) {
      this.rawHtmlHandler = rawHtmlHandler;
      return this;
    }

    public Builder attributeProviderFactory(
        ElementAttributeProviderFactory attributeProviderFactory) {
      this.attributeProviderFactories.add(
          Objects.requireNonNull(
              attributeProviderFactory, "attributeProviderFactory must not be null"));
      return this;
    }

    public Builder nodeRendererFactory(ElementNodeRendererFactory nodeRendererFactory) {
      this.nodeRendererFactories.add(
          Objects.requireNonNull(nodeRendererFactory, "nodeRendererFactory must not be null"));
      return this;
    }

    public Builder extensions(Iterable<? extends Extension> extensions) {
      Objects.requireNonNull(extensions, "extensions must not be null");
      for (Extension extension : extensions) {
        if (extension instanceof Elemental2RendererExtension) {
          ((Elemental2RendererExtension) extension).extend(this);
        }
      }
      return this;
    }
  }

  /** Extension contract for {@link Elemental2Renderer}. */
  public interface Elemental2RendererExtension extends Extension {
    void extend(Builder rendererBuilder);
  }

  private final class RendererContext
      implements ElementNodeRendererContext, ElementAttributeProviderContext {

    private final Document document;
    private final DocumentFragment root;
    private final Deque<elemental2.dom.Node> containers = new ArrayDeque<>();
    private final List<ElementAttributeProvider> attributeProviders;
    private final NodeRendererMap nodeRendererMap = new NodeRendererMap();

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

    @Override
    public Document getDocument() {
      return document;
    }

    @Override
    public String encodeUrl(String url) {
      return percentEncodeUrls ? Escaping.percentEncodeUrl(url) : url;
    }

    @Override
    public Map<String, String> extendAttributes(
        Node node, String tagName, Map<String, String> attributes) {
      LinkedHashMap<String, String> result = new LinkedHashMap<>(attributes);
      for (ElementAttributeProvider attributeProvider : attributeProviders) {
        attributeProvider.setAttributes(node, tagName, result);
      }
      return result;
    }

    @Override
    public void render(Node node) {
      nodeRendererMap.render(node);
    }

    @Override
    public void renderChildren(Node parent) {
      Node node = parent.getFirstChild();
      while (node != null) {
        Node next = node.getNext();
        render(node);
        node = next;
      }
    }

    @Override
    public void renderChildren(Node parent, elemental2.dom.Node container) {
      containers.push(container);
      try {
        renderChildren(parent);
      } finally {
        containers.pop();
      }
    }

    @Override
    public void append(elemental2.dom.Node node) {
      containers.peek().appendChild(node);
    }

    @Override
    public SoftBreakRendering softBreakRendering() {
      return softBreakRendering;
    }

    @Override
    public boolean shouldOmitSingleParagraphP() {
      return omitSingleParagraphP;
    }

    @Override
    public boolean shouldSanitizeUrls() {
      return sanitizeUrls;
    }

    @Override
    public UrlSanitizer urlSanitizer() {
      return urlSanitizer;
    }

    @Override
    public RawHtmlHandler rawHtmlHandler() {
      return rawHtmlHandler;
    }

    private void beforeRoot(Node node) {
      nodeRendererMap.beforeRoot(node);
    }

    private void afterRoot(Node node) {
      nodeRendererMap.afterRoot(node);
    }
  }
}
