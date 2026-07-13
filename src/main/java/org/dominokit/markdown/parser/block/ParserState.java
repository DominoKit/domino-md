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

import org.dominokit.markdown.parser.SourceLine;

/**
 * Snapshot of the current block-parsing state.
 *
 * <p>Block parser factories use this view to inspect the current line, indentation, and active
 * parser stack without mutating parser internals.
 */
public interface ParserState {

  /** @return the current logical source line */
  SourceLine getLine();

  /** @return the current character index within the source line */
  int getIndex();

  /** @return the index of the next non-space character on the line */
  int getNextNonSpaceIndex();

  /** @return the current visual column */
  int getColumn();

  /** @return the indentation measured from the current line start */
  int getIndent();

  /** @return whether the current line is blank */
  boolean isBlank();

  /** @return the block parser that is currently active */
  BlockParser getActiveBlockParser();
}
