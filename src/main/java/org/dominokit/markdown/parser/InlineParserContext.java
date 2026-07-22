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

import java.util.List;
import java.util.Set;
import org.dominokit.markdown.node.LinkReferenceDefinition;
import org.dominokit.markdown.parser.beta.InlineContentParserFactory;
import org.dominokit.markdown.parser.beta.LinkProcessor;
import org.dominokit.markdown.parser.delimiter.DelimiterProcessor;

/**
 * Context exposed to inline parsing extensions.
 *
 * <p>The context provides access to custom parser registrations as well as the parsed
 * link-reference definitions collected during block parsing.
 */
public interface InlineParserContext {

  /**
   * @return custom inline content parsers that have been configured with {@link
   *     Parser.Builder#customInlineContentParserFactory(InlineContentParserFactory)}
   */
  List<InlineContentParserFactory> getCustomInlineContentParserFactories();

  /**
   * @return custom delimiter processors that have been configured with {@link
   *     Parser.Builder#customDelimiterProcessor(DelimiterProcessor)}
   */
  List<DelimiterProcessor> getCustomDelimiterProcessors();

  /**
   * @return custom link processors that have been configured with {@link
   *     Parser.Builder#linkProcessor}
   */
  List<LinkProcessor> getCustomLinkProcessors();

  /**
   * @return custom link markers that have been configured with {@link Parser.Builder#linkMarker}
   */
  Set<Character> getCustomLinkMarkers();

  /**
   * Look up a {@link LinkReferenceDefinition} for a given label.
   *
   * <p>Note that the passed in label does not need to be normalized; implementations are
   * responsible for doing the normalization before lookup.
   *
   * @param label the link label to look up
   * @return the definition if one exists, {@code null} otherwise
   * @deprecated use {@link #getDefinition} with {@link LinkReferenceDefinition} instead
   */
  @Deprecated
  LinkReferenceDefinition getLinkReferenceDefinition(String label);

  /**
   * Look up a definition of a type for a given label.
   *
   * <p>Note that the passed in label does not need to be normalized; implementations are
   * responsible for doing the normalization before lookup.
   *
   * @return the definition if one exists, null otherwise
   */
  <D> D getDefinition(Class<D> type, String label);
}
