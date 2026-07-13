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

import java.util.Objects;
import org.dominokit.markdown.internal.util.Parsing;
import org.dominokit.markdown.node.Block;
import org.dominokit.markdown.node.BulletList;
import org.dominokit.markdown.node.ListBlock;
import org.dominokit.markdown.node.ListItem;
import org.dominokit.markdown.node.OrderedList;
import org.dominokit.markdown.parser.block.AbstractBlockParser;
import org.dominokit.markdown.parser.block.AbstractBlockParserFactory;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.BlockParser;
import org.dominokit.markdown.parser.block.BlockStart;
import org.dominokit.markdown.parser.block.MatchedBlockParser;
import org.dominokit.markdown.parser.block.ParserState;

/**
 * Block parser for ordered and unordered list containers.
 *
 * <p>The parser owns the list block itself, tracks blank-line state so it can decide whether the
 * list remains tight, and cooperates with child {@link ListItemParser} instances to reconstruct
 * list item indentation and nesting.
 */
public class ListBlockParser extends AbstractBlockParser {

  private final ListBlock block;
  private boolean hadBlankLine;
  private int linesAfterBlank;

  /**
   * Create a parser for the supplied list block.
   *
   * @param block list block to populate
   */
  public ListBlockParser(ListBlock block) {
    this.block = block;
  }

  /** @return {@code true}; list blocks are containers */
  @Override
  public boolean isContainer() {
    return true;
  }

  /**
   * List blocks can only contain list items, and a blank line can retroactively mark the list as
   * loose when followed by another item.
   */
  @Override
  public boolean canContain(Block childBlock) {
    if (childBlock instanceof ListItem) {
      if (hadBlankLine && linesAfterBlank == 1) {
        block.setTight(false);
        hadBlankLine = false;
      }
      return true;
    }
    return false;
  }

  /** @return the list block being built */
  @Override
  public Block getBlock() {
    return block;
  }

  /**
   * Keep the list open across blank lines so later items can decide whether the list is tight.
   *
   * @param state current parser state
   * @return a continuation at the current line index
   */
  @Override
  public BlockContinue tryContinue(ParserState state) {
    if (state.isBlank()) {
      hadBlankLine = true;
      linesAfterBlank = 0;
    } else if (hadBlankLine) {
      linesAfterBlank++;
    }
    return BlockContinue.atIndex(state.getIndex());
  }

  /**
   * Parse a possible list marker from the current line.
   *
   * <p>The result includes the list block type and the column where the list item content should
   * begin.
   */
  private static ListData parseList(
      CharSequence line, int markerIndex, int markerColumn, boolean inParagraph) {
    ListMarkerData listMarker = parseListMarker(line, markerIndex);
    if (listMarker == null) {
      return null;
    }
    ListBlock listBlock = listMarker.listBlock;

    int indexAfterMarker = listMarker.indexAfterMarker;
    int markerLength = indexAfterMarker - markerIndex;
    int columnAfterMarker = markerColumn + markerLength;
    int contentColumn = columnAfterMarker;

    boolean hasContent = false;
    for (int i = indexAfterMarker; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '\t') {
        contentColumn += Parsing.columnsToNextTabStop(contentColumn);
      } else if (c == ' ') {
        contentColumn++;
      } else {
        hasContent = true;
        break;
      }
    }

    if (inParagraph) {
      if (listBlock instanceof OrderedList
          && ((OrderedList) listBlock).getMarkerStartNumber() != 1) {
        return null;
      }
      if (!hasContent) {
        return null;
      }
    }

    if (!hasContent || (contentColumn - columnAfterMarker) > Parsing.CODE_BLOCK_INDENT) {
      contentColumn = columnAfterMarker + 1;
    }

    return new ListData(listBlock, contentColumn);
  }

  /**
   * Parse a bullet or ordered list marker at the supplied position.
   *
   * @param line input line
   * @param index marker position within the line
   * @return parsed marker data, or {@code null} when the line does not start a list item
   */
  private static ListMarkerData parseListMarker(CharSequence line, int index) {
    char c = line.charAt(index);
    switch (c) {
      case '-':
      case '+':
      case '*':
        if (isSpaceTabOrEnd(line, index + 1)) {
          BulletList bulletList = new BulletList();
          bulletList.setMarker(String.valueOf(c));
          return new ListMarkerData(bulletList, index + 1);
        }
        return null;
      default:
        return parseOrderedList(line, index);
    }
  }

  /**
   * Parse an ordered-list marker such as {@code 1. } or {@code 2) }.
   *
   * @param line input line
   * @param index starting index of the potential marker
   * @return parsed marker data, or {@code null} when no valid ordered-list marker is present
   */
  private static ListMarkerData parseOrderedList(CharSequence line, int index) {
    int digits = 0;
    for (int i = index; i < line.length(); i++) {
      char c = line.charAt(i);
      switch (c) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          digits++;
          if (digits > 9) {
            return null;
          }
          break;
        case '.':
        case ')':
          if (digits >= 1 && isSpaceTabOrEnd(line, i + 1)) {
            String number = line.subSequence(index, i).toString();
            OrderedList orderedList = new OrderedList();
            orderedList.setMarkerStartNumber(Integer.parseInt(number));
            orderedList.setMarkerDelimiter(String.valueOf(c));
            return new ListMarkerData(orderedList, i + 1);
          }
          return null;
        default:
          return null;
      }
    }
    return null;
  }

  /**
   * Check whether the supplied position is whitespace or the end of the line.
   *
   * @param line input line
   * @param index position to inspect
   * @return {@code true} when the position is a space, tab, or end of input
   */
  private static boolean isSpaceTabOrEnd(CharSequence line, int index) {
    if (index < line.length()) {
      char c = line.charAt(index);
      return c == ' ' || c == '\t';
    }
    return true;
  }

  /**
   * Determine whether two list blocks should be treated as the same list.
   *
   * <p>Bullet lists compare their bullet marker, while ordered lists compare their delimiter.
   */
  private static boolean listsMatch(ListBlock a, ListBlock b) {
    if (a instanceof BulletList && b instanceof BulletList) {
      return Objects.equals(((BulletList) a).getMarker(), ((BulletList) b).getMarker());
    }
    if (a instanceof OrderedList && b instanceof OrderedList) {
      return Objects.equals(
          ((OrderedList) a).getMarkerDelimiter(), ((OrderedList) b).getMarkerDelimiter());
    }
    return false;
  }

  public static class Factory extends AbstractBlockParserFactory {

    /**
     * Attempt to start a list block at the current position.
     *
     * @param state parser state for the current line
     * @param matchedBlockParser the currently matched block parser chain
     * @return a block-start result when the line begins a list item, otherwise {@code none}
     */
    @Override
    public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
      BlockParser matched = matchedBlockParser.getMatchedBlockParser();

      if (state.getIndent() >= Parsing.CODE_BLOCK_INDENT) {
        return BlockStart.none();
      }
      int markerIndex = state.getNextNonSpaceIndex();
      int markerColumn = state.getColumn() + state.getIndent();
      boolean inParagraph = !matchedBlockParser.getParagraphLines().isEmpty();
      ListData listData =
          parseList(state.getLine().getContent(), markerIndex, markerColumn, inParagraph);
      if (listData == null) {
        return BlockStart.none();
      }

      int newColumn = listData.contentColumn;
      ListItemParser listItemParser =
          new ListItemParser(state.getIndent(), newColumn - state.getColumn());

      if (!(matched instanceof ListBlockParser)
          || !listsMatch((ListBlock) matched.getBlock(), listData.listBlock)) {
        ListBlockParser listBlockParser = new ListBlockParser(listData.listBlock);
        listData.listBlock.setTight(true);
        return BlockStart.of(listBlockParser, listItemParser).atColumn(newColumn);
      }
      return BlockStart.of(listItemParser).atColumn(newColumn);
    }
  }

  /** Parsed list metadata used to create a block and compute its content indentation. */
  private static class ListData {
    private final ListBlock listBlock;
    private final int contentColumn;

    private ListData(ListBlock listBlock, int contentColumn) {
      this.listBlock = listBlock;
      this.contentColumn = contentColumn;
    }
  }

  /** Parsed marker data describing the concrete list block and line offset after the marker. */
  private static class ListMarkerData {
    private final ListBlock listBlock;
    private final int indexAfterMarker;

    private ListMarkerData(ListBlock listBlock, int indexAfterMarker) {
      this.listBlock = listBlock;
      this.indexAfterMarker = indexAfterMarker;
    }
  }
}
