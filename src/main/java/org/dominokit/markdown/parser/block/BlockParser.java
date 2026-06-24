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
 * <p>Implementations should subclass {@link AbstractBlockParser} instead of implementing this
 * directly.
 */
public interface BlockParser {

  /**
   * Return true if the block that is parsed is a container (contains other blocks), or false if
   * it's a leaf.
   */
  boolean isContainer();

  /**
   * Return true if the block can have lazy continuation lines.
   *
   * <p>Lazy continuation lines are lines that were rejected by this {@link
   * #tryContinue(ParserState)} but didn't match any other block parsers either.
   *
   * <p>If true is returned here, those lines will get added via {@link #addLine(SourceLine)}. For
   * false, the block is closed instead.
   */
  boolean canHaveLazyContinuationLines();

  boolean canContain(Block childBlock);

  Block getBlock();

  BlockContinue tryContinue(ParserState parserState);

  /**
   * Add the part of a line that belongs to this block parser to parse (i.e. without any container
   * block markers). Note that the line will only include a {@link SourceLine#getSourceSpan()} if
   * source spans are enabled for inlines.
   */
  void addLine(SourceLine line);

  /**
   * Add a source span of the currently parsed block. The default implementation in {@link
   * AbstractBlockParser} adds it to the block. Unless you have some complicated parsing where you
   * need to check source positions, you don't need to override this.
   *
   * @since 0.16.0
   */
  void addSourceSpan(SourceSpan sourceSpan);

  /**
   * Return definitions parsed by this parser. The definitions returned here can later be accessed
   * during inline parsing via {@link
   * org.dominokit.markdown.parser.InlineParserContext#getDefinition}.
   */
  List<DefinitionMap<?>> getDefinitions();

  void closeBlock();

  void parseInlines(InlineParser inlineParser);
}
