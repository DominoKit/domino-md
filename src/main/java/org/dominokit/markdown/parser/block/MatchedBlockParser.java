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
package org.dominokit.markdown.parser.block;

import org.dominokit.markdown.parser.SourceLines;

/**
 * View of the block parser that already matched the current line.
 *
 * <p>Block-start factories use this to inspect the currently matched parser and, when relevant,
 * the paragraph text it has accumulated so far.
 */
public interface MatchedBlockParser {

  /** @return the block parser that already matched the current line */
  BlockParser getMatchedBlockParser();

  /** @return paragraph lines collected by the matched parser, or an empty collection */
  SourceLines getParagraphLines();
}
