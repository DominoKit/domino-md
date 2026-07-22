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
package org.dominokit.markdown.ext.dui;

import org.dominokit.markdown.Extension;
import org.dominokit.markdown.ext.classes.MarkdownClassExtension;
import org.dominokit.markdown.node.BlockQuote;
import org.dominokit.markdown.node.BulletList;
import org.dominokit.markdown.node.Code;
import org.dominokit.markdown.node.Emphasis;
import org.dominokit.markdown.node.FencedCodeBlock;
import org.dominokit.markdown.node.HardLineBreak;
import org.dominokit.markdown.node.Heading;
import org.dominokit.markdown.node.HtmlBlock;
import org.dominokit.markdown.node.Image;
import org.dominokit.markdown.node.IndentedCodeBlock;
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.ListItem;
import org.dominokit.markdown.node.OrderedList;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.node.SoftLineBreak;
import org.dominokit.markdown.node.StrongEmphasis;
import org.dominokit.markdown.node.ThematicBreak;
import org.dominokit.markdown.renderer.elemental2.Elemental2Renderer;
import org.dominokit.markdown.renderer.html.HtmlRenderer;

/**
 * Preset extension that applies the project's default {@code dui} class naming scheme.
 *
 * <p>The preset layers on top of {@link MarkdownClassExtension} so callers get the same class
 * merging behavior as the generic extension while avoiding manual configuration for the standard
 * markdown element set.
 */
public final class DuiClassExtension
    implements HtmlRenderer.HtmlRendererExtension, Elemental2Renderer.Elemental2RendererExtension {

  private final MarkdownClassExtension delegate;

  /**
   * Create the default preset configuration.
   *
   * <p>The preset assigns the fixed {@code dui} base class and {@code dui-md-*} node/tag classes to
   * the elements emitted by the core HTML and Elemental2 renderers.
   */
  public DuiClassExtension() {
    this.delegate = buildDelegate();
  }

  /**
   * Create a ready-to-install preset instance.
   *
   * @return extension instance with the default {@code dui} class mapping
   */
  public static Extension create() {
    return new DuiClassExtension();
  }

  /**
   * Register the preset with the HTML renderer.
   *
   * @param rendererBuilder HTML renderer builder to extend
   */
  @Override
  public void extend(HtmlRenderer.Builder rendererBuilder) {
    delegate.extend(rendererBuilder);
  }

  /**
   * Register the preset with the Elemental2 renderer.
   *
   * @param rendererBuilder DOM renderer builder to extend
   */
  @Override
  public void extend(Elemental2Renderer.Builder rendererBuilder) {
    delegate.extend(rendererBuilder);
  }

  /**
   * Assemble the fixed class configuration used by the preset.
   *
   * @return delegate extension with the built-in class mapping
   */
  private static MarkdownClassExtension buildDelegate() {
    MarkdownClassExtension.Builder builder = MarkdownClassExtension.builder().classes("dui");

    builder.nodeClasses(Paragraph.class, "dui-md-paragraph").tagClasses("p", "dui-md-p");
    builder.nodeClasses(Heading.class, "dui-md-heading");
    for (int level = 1; level <= 6; level++) {
      builder.tagClasses("h" + level, "dui-md-h" + level);
    }

    builder
        .nodeClasses(BlockQuote.class, "dui-md-blockquote")
        .tagClasses("blockquote", "dui-md-blockquote");
    builder.nodeClasses(BulletList.class, "dui-md-bullet-list").tagClasses("ul", "dui-md-ul");
    builder.nodeClasses(OrderedList.class, "dui-md-ordered-list").tagClasses("ol", "dui-md-ol");
    builder.nodeClasses(ListItem.class, "dui-md-list-item").tagClasses("li", "dui-md-li");
    builder.nodeClasses(Link.class, "dui-md-link").tagClasses("a", "dui-md-a");
    builder.nodeClasses(Image.class, "dui-md-image").tagClasses("img", "dui-md-img");
    builder.nodeClasses(Emphasis.class, "dui-md-emphasis").tagClasses("em", "dui-md-em");
    builder
        .nodeClasses(StrongEmphasis.class, "dui-md-strong-emphasis")
        .tagClasses("strong", "dui-md-strong");
    builder.nodeClasses(Code.class, "dui-md-code").tagClasses("code", "dui-md-code");
    builder.nodeClasses(FencedCodeBlock.class, "dui-md-fenced-code-block");
    builder.nodeClasses(IndentedCodeBlock.class, "dui-md-indented-code-block");
    builder.tagClasses("pre", "dui-md-pre");
    builder
        .nodeClasses(HardLineBreak.class, "dui-md-hard-line-break")
        .tagClasses("br", "dui-md-br");
    builder
        .nodeClasses(SoftLineBreak.class, "dui-md-soft-line-break")
        .tagClasses("br", "dui-md-br");
    builder.nodeClasses(ThematicBreak.class, "dui-md-thematic-break").tagClasses("hr", "dui-md-hr");
    builder.nodeClasses(HtmlBlock.class, "dui-md-html-block").tagClasses("p", "dui-md-p");

    return builder.build();
  }
}
