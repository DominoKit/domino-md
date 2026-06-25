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
package org.dominokit.markdown.internal.inline;

import java.util.Set;
import org.dominokit.markdown.internal.util.Escaping;
import org.dominokit.markdown.node.HardLineBreak;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.beta.*;

/**
 * Parse a backslash-escaped special character, adding either the escaped character, a hard line
 * break (if the backslash is followed by a newline), or a literal backslash to the block's
 * children.
 */
public class BackslashInlineParser implements InlineContentParser {

  @Override
  public ParsedInline tryParse(InlineParserState inlineParserState) {
    Scanner scanner = inlineParserState.scanner();
    // Backslash
    scanner.next();

    char next = scanner.peek();
    if (next == '\n') {
      scanner.next();
      return ParsedInline.of(new HardLineBreak(), scanner.position());
    } else if (Escaping.isEscapable(next)) {
      scanner.next();
      return ParsedInline.of(new Text(String.valueOf(next)), scanner.position());
    } else {
      return ParsedInline.of(new Text("\\"), scanner.position());
    }
  }

  public static class Factory implements InlineContentParserFactory {
    @Override
    public Set<Character> getTriggerCharacters() {
      return Set.of('\\');
    }

    @Override
    public InlineContentParser create() {
      return new BackslashInlineParser();
    }
  }
}
