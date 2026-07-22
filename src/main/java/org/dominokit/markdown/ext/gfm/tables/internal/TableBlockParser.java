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
package org.dominokit.markdown.ext.gfm.tables.internal;

import java.util.ArrayList;
import java.util.List;
import org.dominokit.markdown.ext.gfm.tables.TableBlock;
import org.dominokit.markdown.ext.gfm.tables.TableBody;
import org.dominokit.markdown.ext.gfm.tables.TableCell;
import org.dominokit.markdown.ext.gfm.tables.TableHead;
import org.dominokit.markdown.ext.gfm.tables.TableRow;
import org.dominokit.markdown.node.Block;
import org.dominokit.markdown.node.SourceSpan;
import org.dominokit.markdown.parser.InlineParser;
import org.dominokit.markdown.parser.SourceLine;
import org.dominokit.markdown.parser.SourceLines;
import org.dominokit.markdown.parser.block.AbstractBlockParser;
import org.dominokit.markdown.parser.block.AbstractBlockParserFactory;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.BlockStart;
import org.dominokit.markdown.parser.block.MatchedBlockParser;
import org.dominokit.markdown.parser.block.ParserState;
import org.dominokit.markdown.text.Characters;

/**
 * Block parser for GitHub-style pipe tables.
 *
 * <p>The parser first records the header row, then validates the separator line and uses the
 * separator metadata to parse the remaining body rows into table cells.
 */
public class TableBlockParser extends AbstractBlockParser {

  private final TableBlock block = new TableBlock();
  private final List<SourceLine> rowLines = new ArrayList<>();
  private final List<TableCellInfo> columns;
  private boolean canHaveLazyContinuationLines = true;

  /** Create a table parser for the parsed separator metadata and header line. */
  private TableBlockParser(List<TableCellInfo> columns, SourceLine headerLine) {
    this.columns = columns;
    this.rowLines.add(headerLine);
  }

  @Override
  /**
   * @return whether the current table can accept lazy continuation lines
   */
  public boolean canHaveLazyContinuationLines() {
    return canHaveLazyContinuationLines;
  }

  @Override
  /**
   * @return the table block being built
   */
  public Block getBlock() {
    return block;
  }

  @Override
  /** Continue the table only when the current line still looks like a table row. */
  public BlockContinue tryContinue(ParserState state) {
    CharSequence content = state.getLine().getContent();
    int pipe = Characters.find('|', content, state.getNextNonSpaceIndex());
    if (pipe == -1) {
      return BlockContinue.none();
    }
    if (pipe == state.getNextNonSpaceIndex()
        && Characters.skipSpaceTab(content, pipe + 1, content.length()) == content.length()) {
      canHaveLazyContinuationLines = false;
      return BlockContinue.none();
    }
    return BlockContinue.atIndex(state.getIndex());
  }

  @Override
  /** Record another table row line. */
  public void addLine(SourceLine line) {
    rowLines.add(line);
  }

  @Override
  /** Convert the collected table lines into table head/body/cell nodes and parse inline content. */
  public void parseInlines(InlineParser inlineParser) {
    List<SourceSpan> sourceSpans = block.getSourceSpans();

    SourceSpan headerSourceSpan = !sourceSpans.isEmpty() ? sourceSpans.get(0) : null;
    TableHead head = new TableHead();
    if (headerSourceSpan != null) {
      head.addSourceSpan(headerSourceSpan);
    }
    block.appendChild(head);

    TableRow headerRow = new TableRow();
    headerRow.setSourceSpans(head.getSourceSpans());
    head.appendChild(headerRow);

    List<SourceLine> headerCells = split(rowLines.get(0));
    int headerColumns = headerCells.size();
    for (int i = 0; i < headerColumns; i++) {
      TableCell tableCell = parseCell(headerCells.get(i), i, inlineParser);
      tableCell.setHeader(true);
      headerRow.appendChild(tableCell);
    }

    TableBody body = null;
    for (int rowIndex = 2; rowIndex < rowLines.size(); rowIndex++) {
      SourceLine rowLine = rowLines.get(rowIndex);
      SourceSpan sourceSpan = rowIndex < sourceSpans.size() ? sourceSpans.get(rowIndex) : null;
      List<SourceLine> cells = split(rowLine);

      TableRow row = new TableRow();
      if (sourceSpan != null) {
        row.addSourceSpan(sourceSpan);
      }

      for (int i = 0; i < headerColumns; i++) {
        SourceLine cell = i < cells.size() ? cells.get(i) : SourceLine.of("", null);
        row.appendChild(parseCell(cell, i, inlineParser));
      }

      if (body == null) {
        body = new TableBody();
        block.appendChild(body);
      }
      body.appendChild(row);
      body.addSourceSpan(sourceSpan);
    }
  }

  /** Parse one table cell, trimming outer whitespace and applying column metadata. */
  private TableCell parseCell(SourceLine cell, int column, InlineParser inlineParser) {
    TableCell tableCell = new TableCell();
    SourceSpan sourceSpan = cell.getSourceSpan();
    if (sourceSpan != null) {
      tableCell.addSourceSpan(sourceSpan);
    }

    if (column < columns.size()) {
      TableCellInfo cellInfo = columns.get(column);
      tableCell.setAlignment(cellInfo.alignment);
      tableCell.setWidth(cellInfo.width);
    }

    CharSequence content = cell.getContent();
    int start = Characters.skipSpaceTab(content, 0, content.length());
    int end = Characters.skipSpaceTabBackwards(content, content.length() - 1, start);
    inlineParser.parse(SourceLines.of(cell.substring(start, end + 1)), tableCell);
    return tableCell;
  }

  /** Split a table row into cells while preserving cell source spans. */
  private static List<SourceLine> split(SourceLine line) {
    CharSequence row = line.getContent();
    int nonSpace = Characters.skipSpaceTab(row, 0, row.length());
    int cellStart = nonSpace;
    int cellEnd = row.length();

    if (nonSpace < row.length() && row.charAt(nonSpace) == '|') {
      cellStart = nonSpace + 1;
      int nonSpaceEnd = Characters.skipSpaceTabBackwards(row, row.length() - 1, cellStart);
      cellEnd = nonSpaceEnd + 1;
    }

    List<SourceLine> cells = new ArrayList<>();
    StringBuilder builder = new StringBuilder();
    for (int i = cellStart; i < cellEnd; i++) {
      char c = row.charAt(i);
      if (c == '\\') {
        if (i + 1 < cellEnd && row.charAt(i + 1) == '|') {
          builder.append('|');
          i++;
        } else {
          builder.append('\\');
        }
      } else if (c == '|') {
        String content = builder.toString();
        cells.add(SourceLine.of(content, line.substring(cellStart, i).getSourceSpan()));
        builder.setLength(0);
        cellStart = i + 1;
      } else {
        builder.append(c);
      }
    }

    if (builder.length() > 0) {
      String content = builder.toString();
      cells.add(
          SourceLine.of(
              content, line.substring(cellStart, line.getContent().length()).getSourceSpan()));
    }

    return cells;
  }

  /** Parse the table separator row into column metadata. */
  private static List<TableCellInfo> parseSeparator(CharSequence value) {
    List<TableCellInfo> columns = new ArrayList<>();
    int pipes = 0;
    boolean valid = false;
    int index = 0;
    int width = 0;

    while (index < value.length()) {
      char c = value.charAt(index);
      switch (c) {
        case '|':
          index++;
          pipes++;
          if (pipes > 1) {
            return null;
          }
          valid = true;
          break;
        case '-':
        case ':':
          if (pipes == 0 && !columns.isEmpty()) {
            return null;
          }

          boolean left = false;
          boolean right = false;
          if (c == ':') {
            left = true;
            index++;
            width++;
          }

          boolean haveDash = false;
          while (index < value.length() && value.charAt(index) == '-') {
            index++;
            width++;
            haveDash = true;
          }
          if (!haveDash) {
            return null;
          }

          if (index < value.length() && value.charAt(index) == ':') {
            right = true;
            index++;
            width++;
          }

          columns.add(new TableCellInfo(getAlignment(left, right), width));
          width = 0;
          pipes = 0;
          break;
        case ' ':
        case '\t':
          index++;
          break;
        default:
          return null;
      }
    }

    return valid ? columns : null;
  }

  /** Map separator markers to a column alignment. */
  private static TableCell.Alignment getAlignment(boolean left, boolean right) {
    if (left && right) {
      return TableCell.Alignment.CENTER;
    }
    if (left) {
      return TableCell.Alignment.LEFT;
    }
    if (right) {
      return TableCell.Alignment.RIGHT;
    }
    return null;
  }

  /** Factory for {@link TableBlockParser}. */
  public static class Factory extends AbstractBlockParserFactory {

    @Override
    /** Detect a table header followed by a valid separator line. */
    public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
      List<SourceLine> paragraphLines = matchedBlockParser.getParagraphLines().getLines();
      if (paragraphLines.isEmpty()) {
        return BlockStart.none();
      }

      SourceLine paragraphLine = paragraphLines.get(paragraphLines.size() - 1);
      if (Characters.find('|', paragraphLine.getContent(), 0) == -1) {
        return BlockStart.none();
      }

      SourceLine separatorLine =
          state.getLine().substring(state.getIndex(), state.getLine().getContent().length());
      List<TableCellInfo> columns = parseSeparator(separatorLine.getContent());
      if (columns == null || columns.isEmpty()) {
        return BlockStart.none();
      }

      List<SourceLine> headerCells = split(paragraphLine);
      if (columns.size() < headerCells.size()) {
        return BlockStart.none();
      }

      return BlockStart.of(new TableBlockParser(columns, paragraphLine))
          .atIndex(state.getIndex())
          .replaceParagraphLines(1);
    }
  }

  private static final class TableCellInfo {
    private final TableCell.Alignment alignment;
    private final int width;

    /**
     * Store the alignment and width metadata for one parsed table column.
     *
     * @param alignment column alignment derived from the separator row
     * @param width separator width used when reconstructing table column metadata
     */
    private TableCellInfo(TableCell.Alignment alignment, int width) {
      this.alignment = alignment;
      this.width = width;
    }
  }
}
