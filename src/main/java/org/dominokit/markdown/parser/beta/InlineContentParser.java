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
package org.dominokit.markdown.parser.beta;

/**
 * Parser for a type of inline content.
 *
 * <p>Each parser instance is created by an {@link InlineContentParserFactory} and is scoped to a
 * single inline parsing pass, which allows implementations to keep per-pass state if needed.
 */
public interface InlineContentParser {

  /**
   * Try to parse inline content starting from the current position. Note that the character at the
   * current position is one of {@link InlineContentParserFactory#getTriggerCharacters()} of the
   * factory that created this parser.
   *
   * <p>For a given inline content snippet that is being parsed, this method can be called multiple
   * times: each time a trigger character is encountered.
   *
   * @param inlineParserState the current state of the inline parser
   * @return the result of parsing; can indicate that this parser is not interested or that parsing
   *     was successful
   */
  ParsedInline tryParse(InlineParserState inlineParserState);
}
