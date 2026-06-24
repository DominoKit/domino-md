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
package org.dominokit.markdown.parser.beta;

import java.util.Set;

/**
 * A factory for extending inline content parsing.
 *
 * <p>See {@link org.dominokit.markdown.parser.Parser.Builder#customInlineContentParserFactory} for
 * how to register it.
 */
public interface InlineContentParserFactory {

  /**
   * An inline content parser needs to have a special "trigger" character which activates it. When
   * this character is encountered during inline parsing, {@link InlineContentParser#tryParse} is
   * called with the current parser state. It can also register for more than one trigger character.
   */
  Set<Character> getTriggerCharacters();

  /**
   * Create an {@link InlineContentParser} that will do the parsing. Create is called once per text
   * snippet of inline content inside block structures, and then called each time a trigger
   * character is encountered.
   */
  InlineContentParser create();
}
