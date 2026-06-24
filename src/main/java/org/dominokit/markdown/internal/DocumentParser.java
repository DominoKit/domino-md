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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dominokit.markdown.internal.util.Parsing;
import org.dominokit.markdown.node.Block;
import org.dominokit.markdown.node.BlockQuote;
import org.dominokit.markdown.node.Document;
import org.dominokit.markdown.node.FencedCodeBlock;
import org.dominokit.markdown.node.Heading;
import org.dominokit.markdown.node.HtmlBlock;
import org.dominokit.markdown.node.IndentedCodeBlock;
import org.dominokit.markdown.node.ListBlock;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.node.SourceSpan;
import org.dominokit.markdown.node.ThematicBreak;
import org.dominokit.markdown.parser.IncludeSourceSpans;
import org.dominokit.markdown.parser.InlineParserFactory;
import org.dominokit.markdown.parser.SourceLine;
import org.dominokit.markdown.parser.SourceLines;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.BlockParser;
import org.dominokit.markdown.parser.block.BlockParserFactory;
import org.dominokit.markdown.parser.block.BlockStart;
import org.dominokit.markdown.parser.block.MatchedBlockParser;
import org.dominokit.markdown.parser.block.ParserState;
import org.dominokit.markdown.text.Characters;

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
  private int lineIndex = -1;
  private int index;
  private int column;
  private boolean columnIsInTab;
  private int nextNonSpace;
  private int nextNonSpaceColumn;
  private int indent;
  private boolean blank;

  private final List<BlockParserFactory> blockParserFactories;
  private final InlineParserFactory inlineParserFactory;
  private final IncludeSourceSpans includeSourceSpans;
  private final int maxOpenBlockParsers;
  private final DocumentBlockParser documentBlockParser;

  private final List<OpenBlockParser> openBlockParsers = new ArrayList<>();
  private final List<BlockParser> allBlockParsers = new ArrayList<>();

  public DocumentParser(
      List<BlockParserFactory> blockParserFactories,
      InlineParserFactory inlineParserFactory,
      IncludeSourceSpans includeSourceSpans,
      int maxOpenBlockParsers) {
    this.blockParserFactories = blockParserFactories;
    this.inlineParserFactory = inlineParserFactory;
    this.includeSourceSpans = includeSourceSpans;
    this.maxOpenBlockParsers = maxOpenBlockParsers;

    documentBlockParser = new DocumentBlockParser();
    activateBlockParser(new OpenBlockParser(documentBlockParser, 0));
  }

  public static Set<Class<? extends Block>> getDefaultBlockParserTypes() {
    return CORE_FACTORY_TYPES;
  }

  public static List<BlockParserFactory> calculateBlockParserFactories(
      List<BlockParserFactory> customBlockParserFactories,
      Set<Class<? extends Block>> enabledBlockTypes) {
    List<BlockParserFactory> list = new ArrayList<>(customBlockParserFactories);
    for (Class<? extends Block> blockType : enabledBlockTypes) {
      list.add(NODES_TO_CORE_FACTORIES.get(blockType));
    }
    return list;
  }

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
      parseLine(input.substring(lineStart), lineStart);
    }

    return finalizeAndProcess();
  }

  @Override
  public SourceLine getLine() {
    return line;
  }

  @Override
  public int getIndex() {
    return index;
  }

  @Override
  public int getNextNonSpaceIndex() {
    return nextNonSpace;
  }

  @Override
  public int getColumn() {
    return column;
  }

  @Override
  public int getIndent() {
    return indent;
  }

  @Override
  public boolean isBlank() {
    return blank;
  }

  @Override
  public BlockParser getActiveBlockParser() {
    return openBlockParsers.get(openBlockParsers.size() - 1).blockParser;
  }

  private void parseLine(String rawLine, int inputIndex) {
    setLine(rawLine, inputIndex);

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
        }

        if (blockContinue.getNewIndex() != -1) {
          setNewIndex(blockContinue.getNewIndex());
        } else if (blockContinue.getNewColumn() != -1) {
          setNewColumn(blockContinue.getNewColumn());
        }
        matches++;
      } else {
        break;
      }
    }

    int unmatchedBlocks = openBlockParsers.size() - matches;
    BlockParser blockParser = openBlockParsers.get(matches - 1).blockParser;
    boolean startedNewBlock = false;
    int lastIndex = index;

    boolean tryBlockStarts =
        blockParser.getBlock() instanceof Paragraph || blockParser.isContainer();
    while (tryBlockStarts) {
      lastIndex = index;
      findNextNonSpace();

      if (isBlank() || (indent < 4 && Characters.isLetter(this.line.getContent(), nextNonSpace))) {
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
        BlockParser activeBlockParser = getActiveBlockParser();
        if (activeBlockParser instanceof ParagraphParser) {
          ParagraphParser paragraphParser = (ParagraphParser) activeBlockParser;
          int lines =
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

    if (!startedNewBlock && !isBlank() && getActiveBlockParser().canHaveLazyContinuationLines()) {
      openBlockParsers.get(openBlockParsers.size() - 1).sourceIndex = lastIndex;
      addLine();
    } else {
      if (unmatchedBlocks > 0) {
        closeBlockParsers(unmatchedBlocks);
      }

      if (!blockParser.isContainer()) {
        addLine();
      } else if (!isBlank()) {
        ParagraphParser paragraphParser = new ParagraphParser();
        addChild(new OpenBlockParser(paragraphParser, lastIndex));
        addLine();
      } else {
        addSourceSpans();
      }
    }
  }

  private void setLine(String rawLine, int inputIndex) {
    lineIndex++;
    index = 0;
    column = 0;
    columnIsInTab = false;

    String lineContent = prepareLine(rawLine);
    SourceSpan sourceSpan = null;
    if (includeSourceSpans != IncludeSourceSpans.NONE) {
      sourceSpan = SourceSpan.of(lineIndex, 0, inputIndex, lineContent.length());
    }
    line = SourceLine.of(lineContent, sourceSpan);
  }

  private void findNextNonSpace() {
    int i = index;
    int cols = column;

    blank = true;
    while (i < line.getContent().length()) {
      char c = line.getContent().charAt(i);
      if (c == ' ') {
        i++;
        cols++;
        continue;
      }
      if (c == '\t') {
        i++;
        cols += 4 - (cols % 4);
        continue;
      }
      blank = false;
      break;
    }

    nextNonSpace = i;
    nextNonSpaceColumn = cols;
    indent = nextNonSpaceColumn - column;
  }

  private void setNewIndex(int newIndex) {
    if (newIndex >= nextNonSpace) {
      index = nextNonSpace;
      column = nextNonSpaceColumn;
    }
    while (index < newIndex && index != line.getContent().length()) {
      advance();
    }
    columnIsInTab = false;
  }

  private void setNewColumn(int newColumn) {
    if (newColumn >= nextNonSpaceColumn) {
      index = nextNonSpace;
      column = nextNonSpaceColumn;
    }
    while (column < newColumn && index != line.getContent().length()) {
      advance();
    }
    if (column > newColumn) {
      index--;
      column = newColumn;
      columnIsInTab = true;
    } else {
      columnIsInTab = false;
    }
  }

  private void advance() {
    char c = line.getContent().charAt(index);
    index++;
    if (c == '\t') {
      column += Parsing.columnsToNextTabStop(column);
    } else {
      column++;
    }
  }

  private void addLine() {
    CharSequence content;
    if (columnIsInTab) {
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
      sourceSpan = line.getSourceSpan().subSpan(index);
    }
    getActiveBlockParser().addLine(SourceLine.of(content, sourceSpan));
    addSourceSpans();
  }

  private void addSourceSpans() {
    if (includeSourceSpans != IncludeSourceSpans.NONE) {
      for (int i = 1; i < openBlockParsers.size(); i++) {
        OpenBlockParser openBlockParser = openBlockParsers.get(i);
        int blockIndex = Math.min(openBlockParser.sourceIndex, index);
        int length = line.getContent().length() - blockIndex;
        if (length != 0) {
          openBlockParser.blockParser.addSourceSpan(line.getSourceSpan().subSpan(blockIndex));
        }
      }
    }
  }

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

  private void processInlines() {
    InlineParserContextImpl context = new InlineParserContextImpl();
    var inlineParser = inlineParserFactory.create(context);
    for (BlockParser blockParser : allBlockParsers) {
      blockParser.parseInlines(inlineParser);
    }
  }

  private void addChild(OpenBlockParser openBlockParser) {
    while (!getActiveBlockParser().canContain(openBlockParser.blockParser.getBlock())) {
      closeBlockParsers(1);
    }
    getActiveBlockParser().getBlock().appendChild(openBlockParser.blockParser.getBlock());
    activateBlockParser(openBlockParser);
  }

  private void activateBlockParser(OpenBlockParser openBlockParser) {
    openBlockParsers.add(openBlockParser);
  }

  private OpenBlockParser deactivateBlockParser() {
    return openBlockParsers.remove(openBlockParsers.size() - 1);
  }

  private List<SourceSpan> replaceParagraphLines(int lines, ParagraphParser paragraphParser) {
    List<SourceSpan> sourceSpans = paragraphParser.removeLines(lines);
    closeBlockParsers(1);
    return sourceSpans;
  }

  private List<SourceSpan> prepareActiveBlockParserForReplacement(BlockParser blockParser) {
    deactivateBlockParser();
    blockParser.closeBlock();
    blockParser.getBlock().unlink();
    return blockParser.getBlock().getSourceSpans();
  }

  private Document finalizeAndProcess() {
    closeBlockParsers(openBlockParsers.size());
    processInlines();
    return documentBlockParser.getBlock();
  }

  private void closeBlockParsers(int count) {
    for (int i = 0; i < count; i++) {
      BlockParser blockParser = deactivateBlockParser().blockParser;
      blockParser.closeBlock();
      allBlockParsers.add(blockParser);
    }
  }

  private static String prepareLine(String line) {
    return line.indexOf('\0') == -1 ? line : line.replace('\0', '\uFFFD');
  }

  private static class MatchedBlockParserImpl implements MatchedBlockParser {

    private final BlockParser matchedBlockParser;

    private MatchedBlockParserImpl(BlockParser matchedBlockParser) {
      this.matchedBlockParser = matchedBlockParser;
    }

    @Override
    public BlockParser getMatchedBlockParser() {
      return matchedBlockParser;
    }

    @Override
    public SourceLines getParagraphLines() {
      if (matchedBlockParser instanceof ParagraphParser) {
        return ((ParagraphParser) matchedBlockParser).getParagraphLines();
      }
      return SourceLines.empty();
    }
  }

  private static class OpenBlockParser {
    private final BlockParser blockParser;
    private int sourceIndex;

    private OpenBlockParser(BlockParser blockParser, int sourceIndex) {
      this.blockParser = blockParser;
      this.sourceIndex = sourceIndex;
    }
  }
}
