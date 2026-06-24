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

import java.util.regex.Pattern;
import org.dominokit.markdown.node.Block;
import org.dominokit.markdown.node.HtmlBlock;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.parser.SourceLine;
import org.dominokit.markdown.parser.block.AbstractBlockParser;
import org.dominokit.markdown.parser.block.AbstractBlockParserFactory;
import org.dominokit.markdown.parser.block.BlockContinue;
import org.dominokit.markdown.parser.block.BlockStart;
import org.dominokit.markdown.parser.block.MatchedBlockParser;
import org.dominokit.markdown.parser.block.ParserState;

public class HtmlBlockParser extends AbstractBlockParser {

  private static final String TAGNAME = "[A-Za-z][A-Za-z0-9-]*";
  private static final String ATTRIBUTENAME = "[a-zA-Z_:][a-zA-Z0-9:._-]*";
  private static final String UNQUOTEDVALUE = "[^\"'=<>`\\x00-\\x20]+";
  private static final String SINGLEQUOTEDVALUE = "'[^']*'";
  private static final String DOUBLEQUOTEDVALUE = "\"[^\"]*\"";
  private static final String ATTRIBUTEVALUE =
      "(?:" + UNQUOTEDVALUE + "|" + SINGLEQUOTEDVALUE + "|" + DOUBLEQUOTEDVALUE + ")";
  private static final String ATTRIBUTEVALUESPEC = "(?:" + "\\s*=" + "\\s*" + ATTRIBUTEVALUE + ")";
  private static final String ATTRIBUTE =
      "(?:" + "\\s+" + ATTRIBUTENAME + ATTRIBUTEVALUESPEC + "?)";
  private static final String OPENTAG = "<" + TAGNAME + ATTRIBUTE + "*" + "\\s*/?>";
  private static final String CLOSETAG = "</" + TAGNAME + "\\s*[>]";

  private static final Pattern[][] BLOCK_PATTERNS =
      new Pattern[][] {
        {null, null},
        {
          Pattern.compile("^<(?:script|pre|style|textarea)(?:\\s|>|$)", Pattern.CASE_INSENSITIVE),
          Pattern.compile("</(?:script|pre|style|textarea)>", Pattern.CASE_INSENSITIVE)
        },
        {Pattern.compile("^<!--"), Pattern.compile("-->")},
        {Pattern.compile("^<[?]"), Pattern.compile("\\?>")},
        {Pattern.compile("^<![A-Z]"), Pattern.compile(">")},
        {Pattern.compile("^<!\\[CDATA\\["), Pattern.compile("\\]\\]>")},
        {
          Pattern.compile(
              "^</?(?:"
                  + "address|article|aside|"
                  + "base|basefont|blockquote|body|"
                  + "caption|center|col|colgroup|"
                  + "dd|details|dialog|dir|div|dl|dt|"
                  + "fieldset|figcaption|figure|footer|form|frame|frameset|"
                  + "h1|h2|h3|h4|h5|h6|head|header|hr|html|"
                  + "iframe|"
                  + "legend|li|link|"
                  + "main|menu|menuitem|"
                  + "nav|noframes|"
                  + "ol|optgroup|option|"
                  + "p|param|"
                  + "search|section|summary|"
                  + "table|tbody|td|tfoot|th|thead|title|tr|track|"
                  + "ul"
                  + ")(?:\\s|[/]?[>]|$)",
              Pattern.CASE_INSENSITIVE),
          null
        },
        {
          Pattern.compile("^(?:" + OPENTAG + "|" + CLOSETAG + ")\\s*$", Pattern.CASE_INSENSITIVE),
          null
        }
      };

  private final HtmlBlock block = new HtmlBlock();
  private final Pattern closingPattern;
  private boolean finished;
  private BlockContent content = new BlockContent();

  private HtmlBlockParser(Pattern closingPattern) {
    this.closingPattern = closingPattern;
  }

  @Override
  public Block getBlock() {
    return block;
  }

  @Override
  public BlockContinue tryContinue(ParserState state) {
    if (finished) {
      return BlockContinue.none();
    }
    if (state.isBlank() && closingPattern == null) {
      return BlockContinue.none();
    }
    return BlockContinue.atIndex(state.getIndex());
  }

  @Override
  public void addLine(SourceLine line) {
    content.add(line.getContent());
    if (closingPattern != null && closingPattern.matcher(line.getContent()).find()) {
      finished = true;
    }
  }

  @Override
  public void closeBlock() {
    block.setLiteral(content.getString());
    content = null;
  }

  public static class Factory extends AbstractBlockParserFactory {

    @Override
    public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
      int nextNonSpace = state.getNextNonSpaceIndex();
      CharSequence line = state.getLine().getContent();

      if (state.getIndent() < 4 && line.charAt(nextNonSpace) == '<') {
        for (int blockType = 1; blockType <= 7; blockType++) {
          if (blockType == 7
              && (matchedBlockParser.getMatchedBlockParser().getBlock() instanceof Paragraph
                  || state.getActiveBlockParser().canHaveLazyContinuationLines())) {
            continue;
          }
          Pattern opener = BLOCK_PATTERNS[blockType][0];
          Pattern closer = BLOCK_PATTERNS[blockType][1];
          boolean matches = opener.matcher(line.subSequence(nextNonSpace, line.length())).find();
          if (matches) {
            return BlockStart.of(new HtmlBlockParser(closer)).atIndex(state.getIndex());
          }
        }
      }
      return BlockStart.none();
    }
  }
}
