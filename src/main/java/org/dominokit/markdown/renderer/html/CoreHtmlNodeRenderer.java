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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.dominokit.markdown.node.*;
import org.dominokit.markdown.renderer.NodeRenderer;

/**
 * Core HTML node renderer for the built-in markdown node set.
 *
 * <p>This renderer is registered last so custom HTML node renderers can override specific node
 * types before the built-in fallback handles the remaining ones. It translates the stock AST into
 * HTML tags, delegates attribute customization to the renderer context, and preserves source line
 * structure where the HTML output needs block boundaries.
 */
public class CoreHtmlNodeRenderer extends AbstractVisitor implements NodeRenderer {

  protected final HtmlNodeRendererContext context;
  private final HtmlWriter html;

  /**
   * Create a renderer bound to the current HTML rendering context.
   *
   * @param context render-time context used for HTML output, attribute extension, and recursion
   */
  public CoreHtmlNodeRenderer(HtmlNodeRendererContext context) {
    this.context = context;
    this.html = context.getWriter();
  }

  @Override
  /**
   * Return the node types handled by the built-in HTML renderer.
   *
   * <p>The returned set mirrors the standard markdown node hierarchy so the renderer can act as the
   * default fallback for the core syntax.
   */
  public Set<Class<? extends Node>> getNodeTypes() {
    return Set.of(
        Document.class,
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

  /** Render the supplied node by dispatching to the matching visit method. */
  @Override
  public void render(Node node) {
    node.accept(this);
  }

  /** Render the document by delegating to its child nodes. */
  @Override
  public void visit(Document document) {
    // No rendering itself
    visitChildren(document);
  }

  /** Render headings as block-level heading tags. */
  @Override
  public void visit(Heading heading) {
    String htag = "h" + heading.getLevel();
    html.line();
    html.tag(htag, getAttrs(heading, htag));
    visitChildren(heading);
    html.tag('/' + htag);
    html.line();
  }

  /**
   * Render paragraphs, omitting the wrapper in tight lists or when configured for a lone root
   * paragraph.
   */
  @Override
  public void visit(Paragraph paragraph) {
    boolean omitP =
        isInTightList(paragraph)
            || //
            (context.shouldOmitSingleParagraphP()
                && paragraph.getParent() instanceof Document
                && //
                paragraph.getPrevious() == null
                && paragraph.getNext() == null);
    if (!omitP) {
      html.line();
      html.tag("p", getAttrs(paragraph, "p"));
    }
    visitChildren(paragraph);
    if (!omitP) {
      html.tag("/p");
      html.line();
    }
  }

  /** Render a block quote as a {@code blockquote} element. */
  @Override
  public void visit(BlockQuote blockQuote) {
    html.line();
    html.tag("blockquote", getAttrs(blockQuote, "blockquote"));
    html.line();
    visitChildren(blockQuote);
    html.line();
    html.tag("/blockquote");
    html.line();
  }

  /** Render an unordered list as a {@code ul} element. */
  @Override
  public void visit(BulletList bulletList) {
    renderListBlock(bulletList, "ul", getAttrs(bulletList, "ul"));
  }

  /**
   * Render fenced code blocks as {@code pre > code}.
   *
   * <p>If the fence info string includes a language token, it is preserved as a {@code language-*}
   * class on the inner {@code code} element.
   */
  @Override
  public void visit(FencedCodeBlock fencedCodeBlock) {
    String literal = fencedCodeBlock.getLiteral();
    Map<String, String> attributes = new LinkedHashMap<>();
    String info = fencedCodeBlock.getInfo();
    if (info != null && !info.isEmpty()) {
      int space = info.indexOf(" ");
      String language;
      if (space == -1) {
        language = info;
      } else {
        language = info.substring(0, space);
      }
      attributes.put("class", "language-" + language);
    }
    renderCodeBlock(literal, fencedCodeBlock, attributes);
  }

  /**
   * Render HTML blocks either as escaped paragraphs or raw HTML depending on the renderer
   * configuration.
   */
  @Override
  public void visit(HtmlBlock htmlBlock) {
    html.line();
    if (context.shouldEscapeHtml()) {
      html.tag("p", getAttrs(htmlBlock, "p"));
      html.text(htmlBlock.getLiteral());
      html.tag("/p");
    } else {
      html.raw(htmlBlock.getLiteral());
    }
    html.line();
  }

  /** Render thematic breaks as self-closing {@code hr} tags. */
  @Override
  public void visit(ThematicBreak thematicBreak) {
    html.line();
    html.tag("hr", getAttrs(thematicBreak, "hr"), true);
    html.line();
  }

  /** Render indented code blocks using the same {@code pre > code} structure as fenced blocks. */
  @Override
  public void visit(IndentedCodeBlock indentedCodeBlock) {
    renderCodeBlock(indentedCodeBlock.getLiteral(), indentedCodeBlock, Map.of());
  }

  /**
   * Render links as anchor elements.
   *
   * <p>The destination may be sanitized and percent-encoded before being written into the final
   * {@code href} attribute.
   */
  @Override
  public void visit(Link link) {
    Map<String, String> attrs = new LinkedHashMap<>();
    String url = link.getDestination();

    if (context.shouldSanitizeUrls()) {
      url = context.urlSanitizer().sanitizeLinkUrl(url);
      attrs.put("rel", "nofollow");
    }

    url = context.encodeUrl(url);
    attrs.put("href", url);
    if (link.getTitle() != null) {
      attrs.put("title", link.getTitle());
    }
    html.tag("a", getAttrs(link, "a", attrs));
    visitChildren(link);
    html.tag("/a");
  }

  /** Render list items as {@code li} elements. */
  @Override
  public void visit(ListItem listItem) {
    html.tag("li", getAttrs(listItem, "li"));
    visitChildren(listItem);
    html.tag("/li");
    html.line();
  }

  /** Render ordered lists as {@code ol} elements and preserve the starting number when needed. */
  @Override
  public void visit(OrderedList orderedList) {
    int start = orderedList.getMarkerStartNumber() != null ? orderedList.getMarkerStartNumber() : 1;
    Map<String, String> attrs = new LinkedHashMap<>();
    if (start != 1) {
      attrs.put("start", String.valueOf(start));
    }
    renderListBlock(orderedList, "ol", getAttrs(orderedList, "ol", attrs));
  }

  /** Render images as void {@code img} elements and derive the alt text from inline children. */
  @Override
  public void visit(Image image) {
    String url = image.getDestination();

    AltTextVisitor altTextVisitor = new AltTextVisitor();
    image.accept(altTextVisitor);
    String altText = altTextVisitor.getAltText();

    Map<String, String> attrs = new LinkedHashMap<>();
    if (context.shouldSanitizeUrls()) {
      url = context.urlSanitizer().sanitizeImageUrl(url);
    }

    attrs.put("src", context.encodeUrl(url));
    attrs.put("alt", altText);
    if (image.getTitle() != null) {
      attrs.put("title", image.getTitle());
    }

    html.tag("img", getAttrs(image, "img", attrs), true);
  }

  /** Render emphasis using an {@code em} element. */
  @Override
  public void visit(Emphasis emphasis) {
    html.tag("em", getAttrs(emphasis, "em"));
    visitChildren(emphasis);
    html.tag("/em");
  }

  /** Render strong emphasis using a {@code strong} element. */
  @Override
  public void visit(StrongEmphasis strongEmphasis) {
    html.tag("strong", getAttrs(strongEmphasis, "strong"));
    visitChildren(strongEmphasis);
    html.tag("/strong");
  }

  /** Render text as escaped HTML text. */
  @Override
  public void visit(Text text) {
    html.text(text.getLiteral());
  }

  /** Render inline code using a {@code code} element. */
  @Override
  public void visit(Code code) {
    html.tag("code", getAttrs(code, "code"));
    html.text(code.getLiteral());
    html.tag("/code");
  }

  /** Render inline HTML as raw HTML or escaped text depending on configuration. */
  @Override
  public void visit(HtmlInline htmlInline) {
    if (context.shouldEscapeHtml()) {
      html.text(htmlInline.getLiteral());
    } else {
      html.raw(htmlInline.getLiteral());
    }
  }

  /**
   * Render soft line breaks using the configured soft-break fragment.
   *
   * <p>The renderer can preserve line breaks as text, normalize them to spaces, or emit actual
   * {@code br} elements depending on the configured behavior.
   */
  @Override
  public void visit(SoftLineBreak softLineBreak) {
    html.raw(context.getSoftbreak());
  }

  /** Render hard line breaks as {@code br} elements. */
  @Override
  public void visit(HardLineBreak hardLineBreak) {
    html.tag("br", getAttrs(hardLineBreak, "br"), true);
    html.line();
  }

  /** Delegate child traversal to the active renderer context. */
  @Override
  protected void visitChildren(Node parent) {
    Node node = parent.getFirstChild();
    while (node != null) {
      Node next = node.getNext();
      context.render(node);
      node = next;
    }
  }

  /**
   * Render a code block as a {@code pre > code} pair.
   *
   * <p>The outer {@code pre} and inner {@code code} tags may receive different attributes, and the
   * literal content is emitted as escaped HTML text.
   */
  private void renderCodeBlock(String literal, Node node, Map<String, String> attributes) {
    html.line();
    html.tag("pre", getAttrs(node, "pre"));
    html.tag("code", getAttrs(node, "code", attributes));
    html.text(literal);
    html.tag("/code");
    html.tag("/pre");
    html.line();
  }

  /**
   * Render an ordered or unordered list block.
   *
   * @param listBlock list node whose children should be rendered
   * @param tagName HTML tag name for the list
   * @param attributes resolved attributes to apply to the list element
   */
  private void renderListBlock(
      ListBlock listBlock, String tagName, Map<String, String> attributes) {
    html.line();
    html.tag(tagName, attributes);
    html.line();
    visitChildren(listBlock);
    html.line();
    html.tag('/' + tagName);
    html.line();
  }

  /**
   * Determine whether the paragraph is inside a tight list.
   *
   * <p>The paragraph only needs to inspect its grandparent because list items are nested directly
   * under the list block.
   */
  private boolean isInTightList(Paragraph paragraph) {
    Node parent = paragraph.getParent();
    if (parent != null) {
      Node gramps = parent.getParent();
      if (gramps instanceof ListBlock) {
        ListBlock list = (ListBlock) gramps;
        return list.isTight();
      }
    }
    return false;
  }

  /**
   * Resolve the final attributes for a tag, allowing attribute providers to contribute.
   *
   * @param node the markdown node being rendered
   * @param tagName the HTML tag name to render
   * @return the final attribute map for the element
   */
  private Map<String, String> getAttrs(Node node, String tagName) {
    return getAttrs(node, tagName, Map.of());
  }

  /**
   * Resolve the final attributes for a tag with default attributes.
   *
   * @param node the markdown node being rendered
   * @param tagName the HTML tag name to render
   * @param defaultAttributes renderer-supplied attributes
   * @return the final attribute map for the element
   */
  private Map<String, String> getAttrs(
      Node node, String tagName, Map<String, String> defaultAttributes) {
    return context.extendAttributes(node, tagName, defaultAttributes);
  }

  /**
   * Collect alternative text from an image's inline children.
   *
   * <p>Soft and hard line breaks become newline characters so the resulting alt text remains
   * readable and deterministic.
   */
  private static class AltTextVisitor extends AbstractVisitor {

    private final StringBuilder sb = new StringBuilder();

    /**
     * @return the accumulated alt text built from the image's inline children
     */
    String getAltText() {
      return sb.toString();
    }

    /** Append literal text to the image alt text. */
    @Override
    public void visit(Text text) {
      sb.append(text.getLiteral());
    }

    /** Append inline code text to the image alt text. */
    @Override
    public void visit(Code code) {
      sb.append(code.getLiteral());
    }

    /** Normalize a soft line break to a newline in the image alt text. */
    @Override
    public void visit(SoftLineBreak softLineBreak) {
      sb.append('\n');
    }

    /** Normalize a hard line break to a newline in the image alt text. */
    @Override
    public void visit(HardLineBreak hardLineBreak) {
      sb.append('\n');
    }
  }
}
