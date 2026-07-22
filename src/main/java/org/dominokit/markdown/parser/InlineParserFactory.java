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

/**
 * Factory for custom inline parsers.
 *
 * <p>Factories are consulted when inline parsing encounters one of their registered trigger
 * characters.
 */
public interface InlineParserFactory {

  /**
   * Create a new inline parser for the supplied context.
   *
   * @param inlineParserContext parser context for the current parse pass
   * @return a parser instance
   */
  InlineParser create(InlineParserContext inlineParserContext);
}
