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
 * Parser for a specific block node.
 *
 * <p>Implementations should usually extend {@link AbstractBlockParser} rather than implement this
 * interface directly, because the abstract base class already handles most of the bookkeeping
 * around source spans and block finalization.
 */
public interface BlockParser {

  /**
   * @return whether this block can contain other blocks
   */
  boolean isContainer();

  /**
   * @return whether this block accepts lazy continuation lines
   *     <p>Lazy continuation lines are lines that were rejected by this {@link
   *     #tryContinue(ParserState)} but didn't match any other block parsers either.
   *     <p>If true is returned here, those lines will get added via {@link #addLine(SourceLine)}.
   *     For false, the block is closed instead.
   */
  boolean canHaveLazyContinuationLines();

  /**
   * @return whether this block can contain the supplied child block
   */
  boolean canContain(Block childBlock);

  /**
   * @return the block node being built by this parser
   */
  Block getBlock();

  /**
   * Decide whether the parser can continue on the current line.
   *
   * @param parserState the current parser state
   * @return a continuation result, or {@code null} if the block should stop
   */
  BlockContinue tryContinue(ParserState parserState);

  /**
   * Add the part of a line that belongs to this block parser to parse.
   *
   * <p>The supplied line excludes any container markers or indentation that were already consumed
   * by the block parser. The line only carries a {@link SourceLine#getSourceSpan()} when source
   * spans are enabled for inline parsing.
   *
   * @param line the line content that belongs to the block
   */
  void addLine(SourceLine line);

  /**
   * Add a source span of the currently parsed block.
   *
   * <p>The default implementation in {@link AbstractBlockParser} adds the span to the current block
   * node.
   *
   * @param sourceSpan source span to add
   * @since 0.16.0
   */
  void addSourceSpan(SourceSpan sourceSpan);

  /**
   * Return definitions parsed by this parser.
   *
   * <p>The definitions returned here can later be accessed during inline parsing via {@link
   * org.dominokit.markdown.parser.InlineParserContext#getDefinition}.
   *
   * @return any definition maps produced by this block parser
   */
  List<DefinitionMap<?>> getDefinitions();

  /** Finalize the block after all lines have been consumed. */
  void closeBlock();

  void parseInlines(InlineParser inlineParser);
}
