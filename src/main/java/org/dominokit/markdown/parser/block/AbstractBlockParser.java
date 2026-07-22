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

import java.util.List;
import org.dominokit.markdown.node.Block;
import org.dominokit.markdown.node.DefinitionMap;
import org.dominokit.markdown.node.SourceSpan;
import org.dominokit.markdown.parser.InlineParser;
import org.dominokit.markdown.parser.SourceLine;

/**
 * Convenience base class for block parsers that do not need to override every lifecycle hook.
 *
 * <p>Concrete parsers can extend this class and only implement the behavior they need. The default
 * implementation treats the parser as a leaf block, accepts no child blocks, and performs no
 * additional work during line accumulation or closeout.
 */
public abstract class AbstractBlockParser implements BlockParser {

  /**
   * @return {@code false} because leaf parsers are not containers by default
   */
  @Override
  public boolean isContainer() {
    return false;
  }

  /**
   * @return {@code false} because leaf parsers do not accept lazy continuation lines by default
   */
  @Override
  public boolean canHaveLazyContinuationLines() {
    return false;
  }

  /**
   * @return {@code false} because leaf parsers do not accept child blocks by default
   */
  @Override
  public boolean canContain(Block childBlock) {
    return false;
  }

  /** Default no-op line accumulator. */
  @Override
  public void addLine(SourceLine line) {}

  /**
   * Forward a source span to the block produced by this parser.
   *
   * @param sourceSpan span to attach
   */
  @Override
  public void addSourceSpan(SourceSpan sourceSpan) {
    getBlock().addSourceSpan(sourceSpan);
  }

  /**
   * @return no definitions by default
   */
  @Override
  public List<DefinitionMap<?>> getDefinitions() {
    return List.of();
  }

  /** Default no-op close hook. */
  @Override
  public void closeBlock() {}

  /** Default no-op inline parsing hook. */
  @Override
  public void parseInlines(InlineParser inlineParser) {}
}
