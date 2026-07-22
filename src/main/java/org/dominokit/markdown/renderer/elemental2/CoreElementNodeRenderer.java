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
import elemental2.dom.Element;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.dominokit.markdown.node.AbstractVisitor;
import org.dominokit.markdown.node.BlockQuote;
import org.dominokit.markdown.node.BulletList;
import org.dominokit.markdown.node.Code;
import org.dominokit.markdown.node.Emphasis;
import org.dominokit.markdown.node.FencedCodeBlock;
import org.dominokit.markdown.node.HardLineBreak;
import org.dominokit.markdown.node.Heading;
import org.dominokit.markdown.node.HtmlBlock;
import org.dominokit.markdown.node.HtmlInline;
import org.dominokit.markdown.node.Image;
import org.dominokit.markdown.node.IndentedCodeBlock;
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.ListBlock;
import org.dominokit.markdown.node.ListItem;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.OrderedList;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.node.SoftLineBreak;
import org.dominokit.markdown.node.StrongEmphasis;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.node.ThematicBreak;

/**
 * Core renderer that maps the built-in markdown node types to Elemental2 DOM nodes.
 *
 * <p>This renderer is intentionally opinionated: it knows how to translate the stock node set into
 * native DOM elements, handles the renderer configuration flags exposed by {@link
 * Elemental2Renderer}, and delegates all child traversal back through the active rendering context
 * so nested nodes inherit the same attribute, raw HTML, and soft-break behavior.
 */
public class CoreElementNodeRenderer extends AbstractVisitor implements ElementNodeRenderer {

  private final ElementNodeRendererContext context;
  private final Document document;

  /**
   * Create a renderer bound to the active Elemental2 rendering context.
   *
   * @param context the render-pass context used for DOM creation, attribute extension, and child
   *     traversal
   */
  public CoreElementNodeRenderer(ElementNodeRendererContext context) {
    this.context = context;
    this.document = context.getDocument();
  }

  /**
   * Return the node types handled by this renderer.
   *
   * <p>The set mirrors the built-in node hierarchy so the renderer can participate as the default
   * fallback for common markdown structures while still allowing extensions to contribute their own
   * specialized renderers.
   */
  @Override
  public Set<Class<? extends Node>> getNodeTypes() {
    return Set.of(
        org.dominokit.markdown.node.Document.class,
        Heading.class,
        Paragraph.class,
        BlockQuote.class,
        BulletList.class,
        FencedCodeBlock.class,
        HtmlBlock.class,
        ThematicBreak.class,
        IndentedCodeBlock.class,
        Link.class,
        ListItem.class,
        OrderedList.class,
        Image.class,
        Emphasis.class,
        StrongEmphasis.class,
        Text.class,
        Code.class,
        HtmlInline.class,
        SoftLineBreak.class,
        HardLineBreak.class);
  }

  /**
   * Dispatch the supplied node to the matching visit method.
   *
   * @param node the node to render
   */
  @Override
  public void render(Node node) {
    node.accept(this);
  }

  /** Render the document by delegating traversal to the child nodes. */
  @Override
  public void visit(org.dominokit.markdown.node.Document document) {
    visitChildren(document);
  }

  /** Render a heading as an {@code h1}..{@code h6} element based on its markdown level. */
  @Override
  public void visit(Heading heading) {
    appendContainer("h" + heading.getLevel(), heading);
  }

  /**
   * Render a paragraph unless the current configuration says it should be flattened.
   *
   * <p>Paragraphs are omitted inside tight lists and, optionally, when the root document contains
   * exactly one paragraph.
   */
  @Override
  public void visit(Paragraph paragraph) {
    if (shouldOmitParagraph(paragraph)) {
      visitChildren(paragraph);
      return;
    }

    appendContainer("p", paragraph);
  }

  /** Render a block quote using a {@code blockquote} wrapper element. */
  @Override
  public void visit(BlockQuote blockQuote) {
    appendContainer("blockquote", blockQuote);
  }

  /** Render an unordered list using a {@code ul} element. */
  @Override
  public void visit(BulletList bulletList) {
    appendContainer("ul", bulletList);
  }

  /**
   * Render a fenced code block as a {@code pre > code} structure.
   *
   * <p>If the fence info string contains a language hint, the hint is converted into a {@code
   * language-*} class on the inner {@code code} element so downstream syntax highlighters can
   * detect it.
   */
  @Override
  public void visit(FencedCodeBlock fencedCodeBlock) {
    Map<String, String> attributes = new LinkedHashMap<>();
    String info = fencedCodeBlock.getInfo();
    if (info != null && !info.isEmpty()) {
      int space = info.indexOf(' ');
      String language = space == -1 ? info : info.substring(0, space);
      attributes.put("class", "language-" + language);
    }
    appendCodeBlock(fencedCodeBlock, fencedCodeBlock.getLiteral(), attributes);
  }

  /**
   * Render raw HTML blocks using the configured raw HTML handler when available.
   *
   * <p>When no handler accepts the literal, the renderer falls back to a plain paragraph that
   * contains the literal text verbatim, preserving the content without interpreting it as DOM.
   */
  @Override
  public void visit(HtmlBlock htmlBlock) {
    elemental2.dom.Node rawNode = tryRawHtmlBlock(htmlBlock.getLiteral());
    if (rawNode != null) {
      context.append(rawNode);
      return;
    }

    Element paragraph = createElement("p", htmlBlock, Map.of());
    paragraph.appendChild(document.createTextNode(htmlBlock.getLiteral()));
    context.append(paragraph);
  }

  /** Render a thematic break as an {@code hr} element. */
  @Override
  public void visit(ThematicBreak thematicBreak) {
    context.append(createElement("hr", thematicBreak, Map.of()));
  }

  /** Render an indented code block using the same {@code pre > code} structure as fenced code. */
  @Override
  public void visit(IndentedCodeBlock indentedCodeBlock) {
    appendCodeBlock(indentedCodeBlock, indentedCodeBlock.getLiteral(), Map.of());
  }

  /**
   * Render a link as an anchor element.
   *
   * <p>The destination may be sanitized and percent-encoded depending on the renderer
   * configuration. Any configured attribute providers can further adjust the resulting attributes
   * before the children are rendered into the anchor.
   */
  @Override
  public void visit(Link link) {
    String url = link.getDestination();
    Map<String, String> attributes = new LinkedHashMap<>();
    if (context.shouldSanitizeUrls()) {
      url = context.urlSanitizer().sanitizeLinkUrl(url);
      attributes.put("rel", "nofollow");
    }
    attributes.put("href", context.encodeUrl(url));
    if (link.getTitle() != null) {
      attributes.put("title", link.getTitle());
    }

    Element anchor = createElement("a", link, attributes);
    context.append(anchor);
    context.renderChildren(link, anchor);
  }

  /** Render a list item as an {@code li} element. */
  @Override
  public void visit(ListItem listItem) {
    appendContainer("li", listItem);
  }

  /**
   * Render an ordered list as an {@code ol} element.
   *
   * <p>If the list starts at a number other than 1, the HTML {@code start} attribute is emitted so
   * the rendered list preserves the markdown numbering semantics.
   */
  @Override
  public void visit(OrderedList orderedList) {
    int start = orderedList.getMarkerStartNumber() != null ? orderedList.getMarkerStartNumber() : 1;
    Map<String, String> attributes = new LinkedHashMap<>();
    if (start != 1) {
      attributes.put("start", String.valueOf(start));
    }

    Element list = createElement("ol", orderedList, attributes);
    context.append(list);
    context.renderChildren(orderedList, list);
  }

  /**
   * Render an image as a self-contained {@code img} element.
   *
   * <p>The renderer derives the alt text by walking the image's inline children, normalizing line
   * breaks to newline characters so the resulting attribute matches markdown accessibility
   * expectations.
   */
  @Override
  public void visit(Image image) {
    String url = image.getDestination();
    if (context.shouldSanitizeUrls()) {
      url = context.urlSanitizer().sanitizeImageUrl(url);
    }

    AltTextVisitor altTextVisitor = new AltTextVisitor();
    image.accept(altTextVisitor);

    Map<String, String> attributes = new LinkedHashMap<>();
    attributes.put("src", context.encodeUrl(url));
    attributes.put("alt", altTextVisitor.getAltText());
    if (image.getTitle() != null) {
      attributes.put("title", image.getTitle());
    }

    context.append(createElement("img", image, attributes));
  }

  /** Render emphasis using an {@code em} element. */
  @Override
  public void visit(Emphasis emphasis) {
    appendContainer("em", emphasis);
  }

  /** Render strong emphasis using a {@code strong} element. */
  @Override
  public void visit(StrongEmphasis strongEmphasis) {
    appendContainer("strong", strongEmphasis);
  }

  /** Render a text node as a DOM text node without additional escaping. */
  @Override
  public void visit(Text text) {
    context.append(document.createTextNode(text.getLiteral()));
  }

  /** Render inline code as a {@code code} element containing the literal text. */
  @Override
  public void visit(Code code) {
    Element codeElement = createElement("code", code, Map.of());
    codeElement.appendChild(document.createTextNode(code.getLiteral()));
    context.append(codeElement);
  }

  /**
   * Render inline raw HTML using the configured handler when possible.
   *
   * <p>If the handler declines the literal, the renderer preserves the source by emitting it as a
   * text node rather than attempting to interpret it as HTML.
   */
  @Override
  public void visit(HtmlInline htmlInline) {
    elemental2.dom.Node rawNode = tryRawHtmlInline(htmlInline.getLiteral());
    if (rawNode != null) {
      context.append(rawNode);
      return;
    }

    context.append(document.createTextNode(htmlInline.getLiteral()));
  }

  /**
   * Render a soft line break according to the configured break policy.
   *
   * <p>The renderer can emit a {@code br} element, a space text node, or a literal newline text
   * node so consumers can choose between HTML fidelity and whitespace normalization.
   */
  @Override
  public void visit(SoftLineBreak softLineBreak) {
    if (context.softBreakRendering() == SoftBreakRendering.BR_ELEMENT) {
      context.append(createElement("br", softLineBreak, Map.of()));
      return;
    }

    String literal = context.softBreakRendering() == SoftBreakRendering.SPACE_TEXT ? " " : "\n";
    context.append(document.createTextNode(literal));
  }

  /** Render a hard line break as a {@code br} element. */
  @Override
  public void visit(HardLineBreak hardLineBreak) {
    context.append(createElement("br", hardLineBreak, Map.of()));
  }

  /** Delegate child traversal to the current rendering context. */
  @Override
  protected void visitChildren(Node parent) {
    context.renderChildren(parent);
  }

  /**
   * Append a {@code pre > code} block for literal code content.
   *
   * @param node the source markdown node used for attribute extension
   * @param literal the code content to insert without interpretation
   * @param codeAttributes attributes to apply to the inner {@code code} element
   */
  private void appendCodeBlock(Node node, String literal, Map<String, String> codeAttributes) {
    Element pre = createElement("pre", node, Map.of());
    Element code = createElement("code", node, codeAttributes);
    code.appendChild(document.createTextNode(literal));
    pre.appendChild(code);
    context.append(pre);
  }

  /**
   * Append a container element and render the node's children inside it.
   *
   * @param tagName the HTML tag to create
   * @param node the source markdown node used for extension hooks
   */
  private void appendContainer(String tagName, Node node) {
    Element element = createElement(tagName, node, Map.of());
    context.append(element);
    context.renderChildren(node, element);
  }

  /**
   * Create an element with the renderer's extended attributes applied.
   *
   * <p>Default attributes are first merged with any registered attribute providers and then copied
   * into the newly created DOM element, skipping only {@code null} values so providers can remove
   * attributes by omission.
   *
   * @param tagName the tag name to create
   * @param node the markdown node being rendered
   * @param defaultAttributes base attributes supplied by the renderer
   * @return a freshly created DOM element with the final attribute set applied
   */
  private Element createElement(String tagName, Node node, Map<String, String> defaultAttributes) {
    Element element = document.createElement(tagName);
    Map<String, String> attributes = context.extendAttributes(node, tagName, defaultAttributes);
    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      if (entry.getValue() != null) {
        element.setAttribute(entry.getKey(), entry.getValue());
      }
    }
    return element;
  }

  /**
   * Determine whether a paragraph should be omitted in the current rendering mode.
   *
   * <p>Paragraphs are omitted when they appear inside tight lists or when the renderer is
   * configured to drop the wrapper for a single top-level paragraph.
   */
  private boolean shouldOmitParagraph(Paragraph paragraph) {
    return isInTightList(paragraph)
        || (context.shouldOmitSingleParagraphP()
            && paragraph.getParent() instanceof org.dominokit.markdown.node.Document
            && paragraph.getPrevious() == null
            && paragraph.getNext() == null);
  }

  /**
   * Check whether the paragraph is nested inside a tight list.
   *
   * <p>The check only needs to inspect the immediate grandparent because markdown list items own
   * the paragraph directly and the tightness flag lives on the enclosing list block.
   */
  private boolean isInTightList(Paragraph paragraph) {
    Node parent = paragraph.getParent();
    if (parent != null) {
      Node grandParent = parent.getParent();
      if (grandParent instanceof ListBlock) {
        return ((ListBlock) grandParent).isTight();
      }
    }
    return false;
  }

  /**
   * Try to render raw block HTML via the configured handler.
   *
   * @param literal the raw HTML text captured from the markdown source
   * @return a DOM node when the handler accepts the literal, otherwise {@code null}
   */
  private elemental2.dom.Node tryRawHtmlBlock(String literal) {
    RawHtmlHandler rawHtmlHandler = context.rawHtmlHandler();
    return rawHtmlHandler != null ? rawHtmlHandler.renderBlock(literal) : null;
  }

  /**
   * Try to render raw inline HTML via the configured handler.
   *
   * @param literal the raw inline HTML captured from the markdown source
   * @return a DOM node when the handler accepts the literal, otherwise {@code null}
   */
  private elemental2.dom.Node tryRawHtmlInline(String literal) {
    RawHtmlHandler rawHtmlHandler = context.rawHtmlHandler();
    return rawHtmlHandler != null ? rawHtmlHandler.renderInline(literal) : null;
  }

  /**
   * Visitor used to extract image alt text from nested inline content.
   *
   * <p>The visitor collapses the image's descendant text into a single string and normalizes both
   * soft and hard line breaks to newline characters so the final {@code alt} attribute remains
   * readable and deterministic.
   */
  private static final class AltTextVisitor extends AbstractVisitor {

    private final StringBuilder builder = new StringBuilder();

    /** Append literal text to the accumulated image alt text. */
    @Override
    public void visit(Text text) {
      builder.append(text.getLiteral());
    }

    /** Append inline code text to the accumulated image alt text. */
    @Override
    public void visit(Code code) {
      builder.append(code.getLiteral());
    }

    /** Normalize a soft line break to a newline in the accumulated alt text. */
    @Override
    public void visit(SoftLineBreak softLineBreak) {
      builder.append('\n');
    }

    /** Normalize a hard line break to a newline in the accumulated alt text. */
    @Override
    public void visit(HardLineBreak hardLineBreak) {
      builder.append('\n');
    }

    /**
     * @return the accumulated alt text built from the image's inline children
     */
    private String getAltText() {
      return builder.toString();
    }
  }
}
