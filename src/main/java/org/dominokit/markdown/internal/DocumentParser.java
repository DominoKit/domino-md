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

import java.util.*;
import org.dominokit.markdown.internal.util.Parsing;
import org.dominokit.markdown.node.*;
import org.dominokit.markdown.parser.IncludeSourceSpans;
import org.dominokit.markdown.parser.InlineParserFactory;
import org.dominokit.markdown.parser.SourceLine;
import org.dominokit.markdown.parser.SourceLines;
import org.dominokit.markdown.parser.beta.InlineContentParserFactory;
import org.dominokit.markdown.parser.beta.LinkProcessor;
import org.dominokit.markdown.parser.block.*;
import org.dominokit.markdown.parser.delimiter.DelimiterProcessor;
import org.dominokit.markdown.text.Characters;

/**
 * Stateful block parser that turns source lines into a document tree.
 *
 * <p>This class walks the input line by line, keeps track of the currently open block parsers, and
 * decides when lines continue an existing block, start a new block, or become plain text. Once the
 * block tree has been built, it performs inline parsing across the collected block contents and
 * returns the final {@link Document}.
 */
public class DocumentParser implements ParserState {

  private static final Set<Class<? extends Block>> CORE_FACTORY_TYPES =
      new LinkedHashSet<>(
          List.of(
              BlockQuote.class,
              Heading.class,
              FencedCodeBlock.class,
              HtmlBlock.class,
              ThematicBreak.class,
              ListBlock.class,
              IndentedCodeBlock.class));

  private static final Map<Class<? extends Block>, BlockParserFactory> NODES_TO_CORE_FACTORIES;

  static {
    Map<Class<? extends Block>, BlockParserFactory> map = new HashMap<>();
    map.put(BlockQuote.class, new BlockQuoteParser.Factory());
    map.put(Heading.class, new HeadingParser.Factory());
    map.put(FencedCodeBlock.class, new FencedCodeBlockParser.Factory());
    map.put(HtmlBlock.class, new HtmlBlockParser.Factory());
    map.put(ThematicBreak.class, new ThematicBreakParser.Factory());
    map.put(ListBlock.class, new ListBlockParser.Factory());
    map.put(IndentedCodeBlock.class, new IndentedCodeBlockParser.Factory());
    NODES_TO_CORE_FACTORIES = Collections.unmodifiableMap(map);
  }

  private SourceLine line;

  /** Line index (0-based) */
  private int lineIndex = -1;

  /** current index (offset) in input line (0-based) */
  private int index = 0;

  /** current column of input line (tab causes column to go to next 4-space tab stop) (0-based) */
  private int column = 0;

  /** if the current column is within a tab character (partially consumed tab) */
  private boolean columnIsInTab;

  private int nextNonSpace = 0;
  private int nextNonSpaceColumn = 0;
  private int indent = 0;
  private boolean blank;

  private final List<BlockParserFactory> blockParserFactories;
  private final InlineParserFactory inlineParserFactory;
  private final List<InlineContentParserFactory> inlineContentParserFactories;
  private final List<DelimiterProcessor> delimiterProcessors;
  private final List<LinkProcessor> linkProcessors;
  private final Set<Character> linkMarkers;
  private final IncludeSourceSpans includeSourceSpans;
  private final int maxOpenBlockParsers;
  private final DocumentBlockParser documentBlockParser;
  private final Definitions definitions = new Definitions();

  private final List<OpenBlockParser> openBlockParsers = new ArrayList<>();
  private final List<BlockParser> allBlockParsers = new ArrayList<>();

  /**
   * Create a parser configured with the supplied block, inline, delimiter, and link handlers.
   *
   * @param blockParserFactories block parser factories to use
   * @param inlineParserFactory inline parser factory to use
   * @param inlineContentParserFactories inline-content parser factories to use
   * @param delimiterProcessors delimiter processors to use
   * @param linkProcessors link processors to use
   * @param linkMarkers custom link marker characters
   * @param includeSourceSpans source-span collection mode
   * @param maxOpenBlockParsers maximum number of open non-document block parsers
   */
  public DocumentParser(
      List<BlockParserFactory> blockParserFactories,
      InlineParserFactory inlineParserFactory,
      List<InlineContentParserFactory> inlineContentParserFactories,
      List<DelimiterProcessor> delimiterProcessors,
      List<LinkProcessor> linkProcessors,
      Set<Character> linkMarkers,
      IncludeSourceSpans includeSourceSpans,
      int maxOpenBlockParsers) {
    this.blockParserFactories = blockParserFactories;
    this.inlineParserFactory = inlineParserFactory;
    this.inlineContentParserFactories = inlineContentParserFactories;
    this.delimiterProcessors = delimiterProcessors;
    this.linkProcessors = linkProcessors;
    this.linkMarkers = linkMarkers;
    this.includeSourceSpans = includeSourceSpans;
    this.maxOpenBlockParsers = maxOpenBlockParsers;

    this.documentBlockParser = new DocumentBlockParser();
    activateBlockParser(new OpenBlockParser(documentBlockParser, 0));
  }

  /**
   * Return the set of block node types that the built-in parser knows how to create.
   *
   * <p>The parser uses this to validate configuration and to materialize the built-in factories in
   * a stable order.
   */
  public static Set<Class<? extends Block>> getDefaultBlockParserTypes() {
    return CORE_FACTORY_TYPES;
  }

  /**
   * Combine custom block parser factories with the built-in factories for the enabled block types.
   *
   * <p>Custom factories are inserted first so they can override the behavior of core syntax when
   * they match the same input.
   */
  public static List<BlockParserFactory> calculateBlockParserFactories(
      List<BlockParserFactory> customBlockParserFactories,
      Set<Class<? extends Block>> enabledBlockTypes) {
    // By having the custom factories come first, extensions are able to change behavior of core
    // syntax.
    List<BlockParserFactory> list = new ArrayList<>(customBlockParserFactories);
    for (Class<? extends Block> blockType : enabledBlockTypes) {
      list.add(NODES_TO_CORE_FACTORIES.get(blockType));
    }
    return list;
  }

  /**
   * Validate that the enabled block types are all recognized by the built-in parser.
   *
   * <p>Unknown types are rejected early with a descriptive exception so misconfiguration fails at
   * builder time instead of halfway through parsing.
   */
  public static void checkEnabledBlockTypes(Set<Class<? extends Block>> enabledBlockTypes) {
    for (Class<? extends Block> enabledBlockType : enabledBlockTypes) {
      if (!NODES_TO_CORE_FACTORIES.containsKey(enabledBlockType)) {
        throw new IllegalArgumentException(
            "Can't enable block type "
                + enabledBlockType
                + ", possible options are: "
                + NODES_TO_CORE_FACTORIES.keySet());
      }
    }
  }

  /**
   * Parse the full input string into a document tree.
   *
   * <p>The method advances one source line at a time, then finalizes any remaining open blocks and
   * runs inline parsing on the completed block tree.
   */
  public Document parse(String input) {
    int lineStart = 0;
    int lineBreak;
    while ((lineBreak = Characters.findLineBreak(input, lineStart)) != -1) {
      String line = input.substring(lineStart, lineBreak);
      parseLine(line, lineStart);
      if (lineBreak + 1 < input.length()
          && input.charAt(lineBreak) == '\r'
          && input.charAt(lineBreak + 1) == '\n') {
        lineStart = lineBreak + 2;
      } else {
        lineStart = lineBreak + 1;
      }
    }
    if (!input.isEmpty() && (lineStart == 0 || lineStart < input.length())) {
      String line = input.substring(lineStart);
      parseLine(line, lineStart);
    }

    return finalizeAndProcess();
  }

  /**
   * @return the current logical source line
   */
  @Override
  public SourceLine getLine() {
    return line;
  }

  /**
   * @return the current character index within the source line
   */
  @Override
  public int getIndex() {
    return index;
  }

  /**
   * @return the index of the next non-space character on the line
   */
  @Override
  public int getNextNonSpaceIndex() {
    return nextNonSpace;
  }

  /**
   * @return the current visual column
   */
  @Override
  public int getColumn() {
    return column;
  }

  /**
   * @return the indentation measured from the current line start
   */
  @Override
  public int getIndent() {
    return indent;
  }

  /**
   * @return whether the current line is blank
   */
  @Override
  public boolean isBlank() {
    return blank;
  }

  /**
   * @return the block parser that is currently active
   */
  @Override
  public BlockParser getActiveBlockParser() {
    return openBlockParsers.get(openBlockParsers.size() - 1).blockParser;
  }

  /**
   * Analyze one logical input line and update the parser state.
   *
   * <p>The implementation first tries to continue the currently open container blocks. If that is
   * not possible, it searches for new block starts. Any remaining content on the line is then
   * either appended to the active block or used to create a paragraph.
   */
  private void parseLine(String ln, int inputIndex) {
    setLine(ln, inputIndex);

    // For each containing block, try to parse the associated line start.
    // The document will always match, so we can skip the first block parser and start at 1 matches
    int matches = 1;
    for (int i = 1; i < openBlockParsers.size(); i++) {
      OpenBlockParser openBlockParser = openBlockParsers.get(i);
      BlockParser blockParser = openBlockParser.blockParser;
      findNextNonSpace();

      BlockContinue result = blockParser.tryContinue(this);
      if (result instanceof BlockContinueImpl) {
        BlockContinueImpl blockContinue = (BlockContinueImpl) result;
        openBlockParser.sourceIndex = getIndex();
        if (blockContinue.isFinalize()) {
          addSourceSpans();
          closeBlockParsers(openBlockParsers.size() - i);
          return;
        } else {
          if (blockContinue.getNewIndex() != -1) {
            setNewIndex(blockContinue.getNewIndex());
          } else if (blockContinue.getNewColumn() != -1) {
            setNewColumn(blockContinue.getNewColumn());
          }
          matches++;
        }
      } else {
        break;
      }
    }

    int unmatchedBlocks = openBlockParsers.size() - matches;
    BlockParser blockParser = openBlockParsers.get(matches - 1).blockParser;
    boolean startedNewBlock = false;

    int lastIndex = index;

    // Unless last matched container is a code block, try new container starts,
    // adding children to the last matched container:
    boolean tryBlockStarts =
        blockParser.getBlock() instanceof Paragraph || blockParser.isContainer();
    while (tryBlockStarts) {
      lastIndex = index;
      findNextNonSpace();

      // this is a little performance optimization:
      if (isBlank()
          || (indent < Parsing.CODE_BLOCK_INDENT
              && Characters.isLetter(this.line.getContent(), nextNonSpace))) {
        setNewIndex(nextNonSpace);
        break;
      }

      BlockStartImpl blockStart = findBlockStart(blockParser);
      if (blockStart == null) {
        setNewIndex(nextNonSpace);
        break;
      }

      startedNewBlock = true;
      int sourceIndex = getIndex();

      // We're starting a new block. If we have any previous blocks that need to be closed, we need
      // to do it now.
      if (unmatchedBlocks > 0) {
        closeBlockParsers(unmatchedBlocks);
        unmatchedBlocks = 0;
      }

      if (blockStart.getNewIndex() != -1) {
        setNewIndex(blockStart.getNewIndex());
      } else if (blockStart.getNewColumn() != -1) {
        setNewColumn(blockStart.getNewColumn());
      }

      List<SourceSpan> replacedSourceSpans = null;
      if (blockStart.getReplaceParagraphLines() >= 1 || blockStart.isReplaceActiveBlockParser()) {
        var activeBlockParser = getActiveBlockParser();
        if (activeBlockParser instanceof ParagraphParser) {
          var paragraphParser = (ParagraphParser) activeBlockParser;
          var lines =
              blockStart.isReplaceActiveBlockParser()
                  ? Integer.MAX_VALUE
                  : blockStart.getReplaceParagraphLines();
          replacedSourceSpans = replaceParagraphLines(lines, paragraphParser);
        } else if (blockStart.isReplaceActiveBlockParser()) {
          replacedSourceSpans = prepareActiveBlockParserForReplacement(activeBlockParser);
        }
      }

      for (BlockParser newBlockParser : blockStart.getBlockParsers()) {
        addChild(new OpenBlockParser(newBlockParser, sourceIndex));
        if (replacedSourceSpans != null) {
          newBlockParser.getBlock().setSourceSpans(replacedSourceSpans);
        }
        blockParser = newBlockParser;
        tryBlockStarts = newBlockParser.isContainer();
      }
    }

    // What remains at the offset is a text line. Add the text to the
    // appropriate block.

    // First check for a lazy continuation line
    if (!startedNewBlock && !isBlank() && getActiveBlockParser().canHaveLazyContinuationLines()) {
      openBlockParsers.get(openBlockParsers.size() - 1).sourceIndex = lastIndex;
      // lazy paragraph continuation
      addLine();

    } else {

      // finalize any blocks not matched
      if (unmatchedBlocks > 0) {
        closeBlockParsers(unmatchedBlocks);
      }

      if (!blockParser.isContainer()) {
        addLine();
      } else if (!isBlank()) {
        // create paragraph container for line
        ParagraphParser paragraphParser = new ParagraphParser();
        addChild(new OpenBlockParser(paragraphParser, lastIndex));
        addLine();
      } else {
        // This can happen for a list item like this:
        // ```
        // *
        // list item
        // ```
        //
        // The first line does not start a paragraph yet, but we still want to record source
        // positions.
        addSourceSpans();
      }
    }
  }

  /**
   * Prepare the parser state for a new line.
   *
   * <p>NUL characters are normalized to the Unicode replacement character so downstream parsing
   * sees a stable, spec-compliant input.
   */
  private void setLine(String ln, int inputIndex) {
    lineIndex++;
    index = 0;
    column = 0;
    columnIsInTab = false;

    String lineContent = prepareLine(ln);
    SourceSpan sourceSpan = null;
    if (includeSourceSpans != IncludeSourceSpans.NONE) {
      sourceSpan = SourceSpan.of(lineIndex, 0, inputIndex, lineContent.length());
    }
    this.line = SourceLine.of(lineContent, sourceSpan);
  }

  /**
   * Scan forward from the current index to the first non-space, non-tab character.
   *
   * <p>The method also computes the current indentation and whether the line is blank so later
   * block-start checks can reuse the result without rescanning.
   */
  private void findNextNonSpace() {
    int i = index;
    int cols = column;

    blank = true;
    int length = line.getContent().length();
    while (i < length) {
      char c = line.getContent().charAt(i);
      switch (c) {
        case ' ':
          i++;
          cols++;
          continue;
        case '\t':
          i++;
          cols += (4 - (cols % 4));
          continue;
      }
      blank = false;
      break;
    }

    nextNonSpace = i;
    nextNonSpaceColumn = cols;
    indent = nextNonSpaceColumn - column;
  }

  /**
   * Advance the current position to the requested character index.
   *
   * <p>Tab handling is preserved by advancing character by character when necessary so the current
   * column stays accurate.
   */
  private void setNewIndex(int newIndex) {
    if (newIndex >= nextNonSpace) {
      // We can start from here, no need to calculate tab stops again
      index = nextNonSpace;
      column = nextNonSpaceColumn;
    }
    int length = line.getContent().length();
    while (index < newIndex && index != length) {
      advance();
    }
    // If we're going to an index as opposed to a column, we're never within a tab
    columnIsInTab = false;
  }

  /**
   * Advance the current position to the requested visual column.
   *
   * <p>This is used by block parsers that reason in columns instead of raw character offsets, for
   * example when indentation spans a tab stop.
   */
  private void setNewColumn(int newColumn) {
    if (newColumn >= nextNonSpaceColumn) {
      // We can start from here, no need to calculate tab stops again
      index = nextNonSpace;
      column = nextNonSpaceColumn;
    }
    int length = line.getContent().length();
    while (column < newColumn && index != length) {
      advance();
    }
    if (column > newColumn) {
      // Last character was a tab and we overshot our target
      index--;
      column = newColumn;
      columnIsInTab = true;
    } else {
      columnIsInTab = false;
    }
  }

  /** Advance by one source character and update the current visual column accordingly. */
  private void advance() {
    char c = line.getContent().charAt(index);
    index++;
    if (c == '\t') {
      column += Parsing.columnsToNextTabStop(column);
    } else {
      column++;
    }
  }

  /**
   * Add the current line content to the active block parser.
   *
   * <p>If the parser is positioned in the middle of a tab, the remaining tab width is expanded to
   * spaces so block parsers receive a stable textual view of the line.
   */
  private void addLine() {
    CharSequence content;
    if (columnIsInTab) {
      // Our column is in a partially consumed tab. Expand the remaining columns (to the next tab
      // stop) to spaces.
      int afterTab = index + 1;
      CharSequence rest = line.getContent().subSequence(afterTab, line.getContent().length());
      int spaces = Parsing.columnsToNextTabStop(column);
      StringBuilder sb = new StringBuilder(spaces + rest.length());
      for (int i = 0; i < spaces; i++) {
        sb.append(' ');
      }
      sb.append(rest);
      content = sb.toString();
    } else if (index == 0) {
      content = line.getContent();
    } else {
      content = line.getContent().subSequence(index, line.getContent().length());
    }
    SourceSpan sourceSpan = null;
    if (includeSourceSpans == IncludeSourceSpans.BLOCKS_AND_INLINES
        && index < line.getSourceSpan().getLength()) {
      // Note that if we're in a partially-consumed tab the length of the source span and the
      // content don't match.
      sourceSpan = line.getSourceSpan().subSpan(index);
    }
    getActiveBlockParser().addLine(SourceLine.of(content, sourceSpan));
    addSourceSpans();
  }

  /**
   * Propagate the current line's source spans to every still-open block parser.
   *
   * <p>Each open block parser tracks the earliest source position at which the current line could
   * belong to it. This method records spans for the open blocks that still cover the current line.
   */
  private void addSourceSpans() {
    if (includeSourceSpans != IncludeSourceSpans.NONE) {
      // Don't add source spans for Document itself (it would get the whole source text), so start
      // at 1, not 0
      for (int i = 1; i < openBlockParsers.size(); i++) {
        var openBlockParser = openBlockParsers.get(i);
        // In case of a lazy continuation line, the index is less than where the block parser would
        // expect the
        // contents to start, so let's use whichever is smaller.
        int blockIndex = Math.min(openBlockParser.sourceIndex, index);
        int length = line.getContent().length() - blockIndex;
        if (length != 0) {
          openBlockParser.blockParser.addSourceSpan(line.getSourceSpan().subSpan(blockIndex));
        }
      }
    }
  }

  /**
   * Ask the registered block parser factories whether any of them can start a block here.
   *
   * <p>The search stops once a factory claims the line and returns a concrete start result.
   */
  private BlockStartImpl findBlockStart(BlockParser blockParser) {
    if (openBlockParsers.size() > maxOpenBlockParsers) {
      return null;
    }
    MatchedBlockParser matchedBlockParser = new MatchedBlockParserImpl(blockParser);
    for (BlockParserFactory blockParserFactory : blockParserFactories) {
      BlockStart result = blockParserFactory.tryStart(this, matchedBlockParser);
      if (result instanceof BlockStartImpl) {
        return (BlockStartImpl) result;
      }
    }
    return null;
  }

  /**
   * Run inline parsing over every block that collected raw text.
   *
   * <p>The block tree is already closed at this point, so the inline parser can safely consume the
   * stored block contents and attach inline children without affecting block structure.
   */
  private void processInlines() {
    var context =
        new InlineParserContextImpl(
            inlineContentParserFactories,
            delimiterProcessors,
            linkProcessors,
            linkMarkers,
            definitions);
    var inlineParser = inlineParserFactory.create(context);

    for (var blockParser : allBlockParsers) {
      blockParser.parseInlines(inlineParser);
    }
  }

  /**
   * Attach a newly started block to the nearest ancestor that can contain it.
   *
   * <p>If the current active block cannot contain the new block type, the parser closes blocks one
   * by one until it finds a valid parent.
   */
  private void addChild(OpenBlockParser openBlockParser) {
    while (!getActiveBlockParser().canContain(openBlockParser.blockParser.getBlock())) {
      closeBlockParsers(1);
    }

    getActiveBlockParser().getBlock().appendChild(openBlockParser.blockParser.getBlock());
    activateBlockParser(openBlockParser);
  }

  /** Push a block parser onto the stack of open parsers. */
  private void activateBlockParser(OpenBlockParser openBlockParser) {
    openBlockParsers.add(openBlockParser);
  }

  /** Pop the most recently opened block parser. */
  private OpenBlockParser deactivateBlockParser() {
    return openBlockParsers.remove(openBlockParsers.size() - 1);
  }

  /**
   * Replace paragraph lines with the contents of a new block that is taking over those lines.
   *
   * <p>This is used for constructs such as reference definitions that consume paragraph text as
   * part of a different block type.
   */
  private List<SourceSpan> replaceParagraphLines(int lines, ParagraphParser paragraphParser) {
    // Remove lines from paragraph as the new block is using them.
    // If all lines are used, this also unlinks the Paragraph block.
    var sourceSpans = paragraphParser.removeLines(lines);
    // Close the paragraph block parser, which will finalize it.
    closeBlockParsers(1);
    return sourceSpans;
  }

  /**
   * Prepare an active block parser to be replaced by a new block.
   *
   * <p>The block is finalized for source-span collection, then unlinked so the replacement block
   * can take its place in the tree.
   */
  private List<SourceSpan> prepareActiveBlockParserForReplacement(BlockParser blockParser) {
    // Note that we don't want to parse inlines here, as it's getting replaced.
    deactivateBlockParser();

    // Do this so that source positions are calculated, which we will carry over to the replacing
    // block.
    blockParser.closeBlock();
    blockParser.getBlock().unlink();
    return blockParser.getBlock().getSourceSpans();
  }

  /**
   * Finalize the document by closing all remaining open block parsers and then running inline
   * parsing.
   */
  private Document finalizeAndProcess() {
    closeBlockParsers(openBlockParsers.size());
    processInlines();
    return documentBlockParser.getBlock();
  }

  /**
   * Close and finalize a number of open block parsers from the top of the stack downward.
   *
   * <p>Closed parsers are preserved in {@link #allBlockParsers} so they can later participate in
   * inline parsing.
   */
  private void closeBlockParsers(int count) {
    for (int i = 0; i < count; i++) {
      BlockParser blockParser = deactivateBlockParser().blockParser;
      finalize(blockParser);
      // Remember for inline parsing. Note that a lot of blocks don't need inline parsing. We could
      // have a
      // separate interface (e.g. BlockParserWithInlines) so that we only have to remember those
      // that actually
      // have inlines to parse.
      allBlockParsers.add(blockParser);
    }
  }

  /**
   * Finalize a block parser.
   *
   * <p>Finalization includes collecting any link-reference definitions it exposes before its block
   * is closed.
   */
  private void finalize(BlockParser blockParser) {
    addDefinitionsFrom(blockParser);
    blockParser.closeBlock();
  }

  /** Copy the definitions exposed by a block parser into the parser-wide definition registry. */
  private void addDefinitionsFrom(BlockParser blockParser) {
    for (var definitionMap : blockParser.getDefinitions()) {
      definitions.addDefinitions(definitionMap);
    }
  }

  /**
   * Normalize the raw input line before parsing.
   *
   * <p>Null characters are replaced with the Unicode replacement character, matching the Markdown
   * parser's treatment of invalid code points.
   */
  private static String prepareLine(String line) {
    if (line.indexOf('\0') == -1) {
      return line;
    } else {
      return line.replace('\0', '\uFFFD');
    }
  }

  /** Adapter that exposes a matched block parser to block-start factories. */
  private static class MatchedBlockParserImpl implements MatchedBlockParser {

    private final BlockParser matchedBlockParser;

    public MatchedBlockParserImpl(BlockParser matchedBlockParser) {
      this.matchedBlockParser = matchedBlockParser;
    }

    /**
     * @return the parser that already matched the current line
     */
    @Override
    public BlockParser getMatchedBlockParser() {
      return matchedBlockParser;
    }

    /**
     * @return the paragraph lines if the matched parser is a paragraph, otherwise an empty
     *     collection
     */
    @Override
    public SourceLines getParagraphLines() {
      if (matchedBlockParser instanceof ParagraphParser) {
        ParagraphParser paragraphParser = (ParagraphParser) matchedBlockParser;
        return paragraphParser.getParagraphLines();
      }
      return SourceLines.empty();
    }
  }

  /** Track an open block parser together with the source index where it started. */
  private static class OpenBlockParser {
    private final BlockParser blockParser;
    private int sourceIndex;

    OpenBlockParser(BlockParser blockParser, int sourceIndex) {
      this.blockParser = blockParser;
      this.sourceIndex = sourceIndex;
    }
  }
}
