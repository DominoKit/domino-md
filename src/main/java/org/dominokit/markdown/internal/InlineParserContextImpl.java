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
package org.dominokit.markdown.internal;

import java.util.List;
import java.util.Set;
import org.dominokit.markdown.node.LinkReferenceDefinition;
import org.dominokit.markdown.parser.InlineParserContext;
import org.dominokit.markdown.parser.beta.InlineContentParserFactory;
import org.dominokit.markdown.parser.beta.LinkProcessor;
import org.dominokit.markdown.parser.delimiter.DelimiterProcessor;

public class InlineParserContextImpl implements InlineParserContext {

  private final List<InlineContentParserFactory> inlineContentParserFactories;
  private final List<DelimiterProcessor> delimiterProcessors;
  private final List<LinkProcessor> linkProcessors;
  private final Set<Character> linkMarkers;
  private final Definitions definitions;

  public InlineParserContextImpl(
      List<InlineContentParserFactory> inlineContentParserFactories,
      List<DelimiterProcessor> delimiterProcessors,
      List<LinkProcessor> linkProcessors,
      Set<Character> linkMarkers,
      Definitions definitions) {
    this.inlineContentParserFactories = inlineContentParserFactories;
    this.delimiterProcessors = delimiterProcessors;
    this.linkProcessors = linkProcessors;
    this.linkMarkers = linkMarkers;
    this.definitions = definitions;
  }

  @Override
  public List<InlineContentParserFactory> getCustomInlineContentParserFactories() {
    return inlineContentParserFactories;
  }

  @Override
  public List<DelimiterProcessor> getCustomDelimiterProcessors() {
    return delimiterProcessors;
  }

  @Override
  public List<LinkProcessor> getCustomLinkProcessors() {
    return linkProcessors;
  }

  @Override
  public Set<Character> getCustomLinkMarkers() {
    return linkMarkers;
  }

  @Override
  public LinkReferenceDefinition getLinkReferenceDefinition(String label) {
    return definitions.getDefinition(LinkReferenceDefinition.class, label);
  }

  @Override
  public <D> D getDefinition(Class<D> type, String label) {
    return definitions.getDefinition(type, label);
  }
}
