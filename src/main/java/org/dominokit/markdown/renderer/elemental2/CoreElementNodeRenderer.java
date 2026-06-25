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

/** The node renderer that renders all core markdown nodes to Elemental2 DOM nodes. */
public class CoreElementNodeRenderer extends AbstractVisitor implements ElementNodeRenderer {

  private final ElementNodeRendererContext context;
  private final Document document;

  public CoreElementNodeRenderer(ElementNodeRendererContext context) {
    this.context = context;
    this.document = context.getDocument();
  }

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

  @Override
  public void render(Node node) {
    node.accept(this);
  }

  @Override
  public void visit(org.dominokit.markdown.node.Document document) {
    visitChildren(document);
  }

  @Override
  public void visit(Heading heading) {
    appendContainer("h" + heading.getLevel(), heading);
  }

  @Override
  public void visit(Paragraph paragraph) {
    if (shouldOmitParagraph(paragraph)) {
      visitChildren(paragraph);
      return;
    }

    appendContainer("p", paragraph);
  }

  @Override
  public void visit(BlockQuote blockQuote) {
    appendContainer("blockquote", blockQuote);
  }

  @Override
  public void visit(BulletList bulletList) {
    appendContainer("ul", bulletList);
  }

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

  @Override
  public void visit(ThematicBreak thematicBreak) {
    context.append(createElement("hr", thematicBreak, Map.of()));
  }

  @Override
  public void visit(IndentedCodeBlock indentedCodeBlock) {
    appendCodeBlock(indentedCodeBlock, indentedCodeBlock.getLiteral(), Map.of());
  }

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

  @Override
  public void visit(ListItem listItem) {
    appendContainer("li", listItem);
  }

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

  @Override
  public void visit(Emphasis emphasis) {
    appendContainer("em", emphasis);
  }

  @Override
  public void visit(StrongEmphasis strongEmphasis) {
    appendContainer("strong", strongEmphasis);
  }

  @Override
  public void visit(Text text) {
    context.append(document.createTextNode(text.getLiteral()));
  }

  @Override
  public void visit(Code code) {
    Element codeElement = createElement("code", code, Map.of());
    codeElement.appendChild(document.createTextNode(code.getLiteral()));
    context.append(codeElement);
  }

  @Override
  public void visit(HtmlInline htmlInline) {
    elemental2.dom.Node rawNode = tryRawHtmlInline(htmlInline.getLiteral());
    if (rawNode != null) {
      context.append(rawNode);
      return;
    }

    context.append(document.createTextNode(htmlInline.getLiteral()));
  }

  @Override
  public void visit(SoftLineBreak softLineBreak) {
    if (context.softBreakRendering() == SoftBreakRendering.BR_ELEMENT) {
      context.append(createElement("br", softLineBreak, Map.of()));
      return;
    }

    String literal = context.softBreakRendering() == SoftBreakRendering.SPACE_TEXT ? " " : "\n";
    context.append(document.createTextNode(literal));
  }

  @Override
  public void visit(HardLineBreak hardLineBreak) {
    context.append(createElement("br", hardLineBreak, Map.of()));
  }

  @Override
  protected void visitChildren(Node parent) {
    context.renderChildren(parent);
  }

  private void appendCodeBlock(Node node, String literal, Map<String, String> codeAttributes) {
    Element pre = createElement("pre", node, Map.of());
    Element code = createElement("code", node, codeAttributes);
    code.appendChild(document.createTextNode(literal));
    pre.appendChild(code);
    context.append(pre);
  }

  private void appendContainer(String tagName, Node node) {
    Element element = createElement(tagName, node, Map.of());
    context.append(element);
    context.renderChildren(node, element);
  }

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

  private boolean shouldOmitParagraph(Paragraph paragraph) {
    return isInTightList(paragraph)
        || (context.shouldOmitSingleParagraphP()
            && paragraph.getParent() instanceof org.dominokit.markdown.node.Document
            && paragraph.getPrevious() == null
            && paragraph.getNext() == null);
  }

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

  private elemental2.dom.Node tryRawHtmlBlock(String literal) {
    RawHtmlHandler rawHtmlHandler = context.rawHtmlHandler();
    return rawHtmlHandler != null ? rawHtmlHandler.renderBlock(literal) : null;
  }

  private elemental2.dom.Node tryRawHtmlInline(String literal) {
    RawHtmlHandler rawHtmlHandler = context.rawHtmlHandler();
    return rawHtmlHandler != null ? rawHtmlHandler.renderInline(literal) : null;
  }

  private static final class AltTextVisitor extends AbstractVisitor {

    private final StringBuilder builder = new StringBuilder();

    @Override
    public void visit(Text text) {
      builder.append(text.getLiteral());
    }

    @Override
    public void visit(Code code) {
      builder.append(code.getLiteral());
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
      builder.append('\n');
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
      builder.append('\n');
    }

    private String getAltText() {
      return builder.toString();
    }
  }
}
