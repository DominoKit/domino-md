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
package org.dominokit.markdown.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.dominokit.markdown.internal.InlineParserImpl;
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.LinkReferenceDefinition;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.beta.InlineContentParserFactory;
import org.dominokit.markdown.parser.beta.LinkProcessor;
import org.dominokit.markdown.parser.delimiter.DelimiterProcessor;
import org.junit.Test;

public class InlineParserContextTest {

  @Test
  public void definitionLookupShouldReceiveOriginalLabelAndNormalizeInternally() {
    CapturingInlineParserFactory inlineParserFactory = new CapturingInlineParserFactory();
    Parser parser = Parser.builder().inlineParserFactory(inlineParserFactory).build();

    Node document = parser.parse("[link with special label][FooBarBaz]\n\n[foobarbaz]: /url");
    Paragraph paragraph = (Paragraph) document.getFirstChild();
    Link link = (Link) paragraph.getFirstChild();

    assertThat(inlineParserFactory.lookups).containsExactly("FooBarBaz");
    assertThat(link.getDestination()).isEqualTo("/url");
    assertThat(((Text) link.getFirstChild()).getLiteral()).isEqualTo("link with special label");
    assertThat(document.getLastChild()).isInstanceOf(LinkReferenceDefinition.class);
  }

  private static class CapturingInlineParserFactory implements InlineParserFactory {
    private final List<String> lookups = new ArrayList<>();

    @Override
    public InlineParser create(InlineParserContext inlineParserContext) {
      InlineParserContext wrappedContext =
          new InlineParserContext() {
            @Override
            public List<InlineContentParserFactory> getCustomInlineContentParserFactories() {
              return inlineParserContext.getCustomInlineContentParserFactories();
            }

            @Override
            public List<DelimiterProcessor> getCustomDelimiterProcessors() {
              return inlineParserContext.getCustomDelimiterProcessors();
            }

            @Override
            public List<LinkProcessor> getCustomLinkProcessors() {
              return inlineParserContext.getCustomLinkProcessors();
            }

            @Override
            public Set<Character> getCustomLinkMarkers() {
              return inlineParserContext.getCustomLinkMarkers();
            }

            @Override
            @Deprecated
            public LinkReferenceDefinition getLinkReferenceDefinition(String label) {
              return getDefinition(LinkReferenceDefinition.class, label);
            }

            @Override
            public <D> D getDefinition(Class<D> type, String label) {
              lookups.add(label);
              return inlineParserContext.getDefinition(type, label);
            }
          };

      return new InlineParserImpl(wrappedContext);
    }
  }
}
