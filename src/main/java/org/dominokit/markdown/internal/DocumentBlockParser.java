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

import org.dominokit.markdown.node.Block;
import org.dominokit.markdown.node.Document;
import org.dominokit.markdown.parser.block.AbstractBlockParser;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.ParserState;

/**
 * Root block parser that owns the document node.
 *
 * <p>The document parser is always open, always a container, and always able to contain any child
 * block. Its only continuation rule is to keep the current line position aligned so nested block
 * parsers can attach to the document root.
 */
public class DocumentBlockParser extends AbstractBlockParser {

  private final Document document = new Document();

  /**
   * @return {@code true}; the document is a container block
   */
  @Override
  public boolean isContainer() {
    return true;
  }

  /**
   * @return {@code true}; the document can contain any block type
   */
  @Override
  public boolean canContain(Block block) {
    return true;
  }

  /**
   * @return the root document node being built
   */
  @Override
  public Document getBlock() {
    return document;
  }

  /**
   * Continue parsing from the current line index without consuming additional characters.
   *
   * @param state current parser state
   * @return a continuation positioned at the current index
   */
  @Override
  public BlockContinue tryContinue(ParserState state) {
    return BlockContinue.atIndex(state.getIndex());
  }
}
