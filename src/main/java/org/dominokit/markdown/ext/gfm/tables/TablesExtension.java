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
package org.dominokit.markdown.ext.gfm.tables;

import java.util.Set;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.ext.gfm.tables.internal.TableBlockParser;
import org.dominokit.markdown.ext.gfm.tables.internal.TableElementNodeRenderer;
import org.dominokit.markdown.ext.gfm.tables.internal.TableHtmlNodeRenderer;
import org.dominokit.markdown.ext.gfm.tables.internal.TableMarkdownNodeRenderer;
import org.dominokit.markdown.ext.gfm.tables.internal.TableTextContentNodeRenderer;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.NodeRenderer;
import org.dominokit.markdown.renderer.elemental2.Elemental2Renderer;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.dominokit.markdown.renderer.markdown.MarkdownNodeRendererContext;
import org.dominokit.markdown.renderer.markdown.MarkdownNodeRendererFactory;
import org.dominokit.markdown.renderer.markdown.MarkdownRenderer;
import org.dominokit.markdown.renderer.text.TextContentRenderer;

/** Extension for GitHub-style pipe tables. */
public final class TablesExtension
    implements Parser.ParserExtension,
        HtmlRenderer.HtmlRendererExtension,
        Elemental2Renderer.Elemental2RendererExtension,
        TextContentRenderer.TextContentRendererExtension,
        MarkdownRenderer.MarkdownRendererExtension {

  /** Creates the default tables extension configuration. */
  public TablesExtension() {}

  public static Extension create() {
    return new TablesExtension();
  }

  @Override
  public void extend(Parser.Builder parserBuilder) {
    parserBuilder.customBlockParserFactory(new TableBlockParser.Factory());
  }

  @Override
  public void extend(HtmlRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(TableHtmlNodeRenderer::new);
  }

  @Override
  public void extend(Elemental2Renderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(TableElementNodeRenderer::new);
  }

  @Override
  public void extend(TextContentRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(TableTextContentNodeRenderer::new);
  }

  @Override
  public void extend(MarkdownRenderer.Builder rendererBuilder) {
    rendererBuilder.nodeRendererFactory(
        new MarkdownNodeRendererFactory() {
          @Override
          public NodeRenderer create(MarkdownNodeRendererContext context) {
            return new TableMarkdownNodeRenderer(context);
          }

          @Override
          public Set<Character> getSpecialCharacters() {
            return Set.of('|');
          }
        });
  }
}
