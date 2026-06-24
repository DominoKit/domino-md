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
import org.dominokit.markdown.node.SourceSpan;
import org.dominokit.markdown.parser.InlineParser;
import org.dominokit.markdown.parser.SourceLine;

public abstract class AbstractBlockParser implements BlockParser {

  @Override
  public boolean isContainer() {
    return false;
  }

  @Override
  public boolean canHaveLazyContinuationLines() {
    return false;
  }

  @Override
  public boolean canContain(Block childBlock) {
    return false;
  }

  @Override
  public void addLine(SourceLine line) {}

  @Override
  public void addSourceSpan(SourceSpan sourceSpan) {
    getBlock().addSourceSpan(sourceSpan);
  }

  @Override
  public List<?> getDefinitions() {
    return List.of();
  }

  @Override
  public void closeBlock() {}

  @Override
  public void parseInlines(InlineParser inlineParser) {}
}
