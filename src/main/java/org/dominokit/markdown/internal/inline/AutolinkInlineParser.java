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
import java.util.regex.Pattern;
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.SourceLines;
import org.dominokit.markdown.parser.beta.*;

/** Attempt to parse an autolink (URL or email in pointy brackets). */
public class AutolinkInlineParser implements InlineContentParser {

  private static final Pattern URI =
      Pattern.compile("^[a-zA-Z][a-zA-Z0-9.+-]{1,31}:[^<>\u0000-\u0020]*$");

  private static final Pattern EMAIL =
      Pattern.compile(
          "^([a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*)$");

  @Override
  public ParsedInline tryParse(InlineParserState inlineParserState) {
    Scanner scanner = inlineParserState.scanner();
    scanner.next();
    Position textStart = scanner.position();
    if (scanner.find('>') > 0) {
      SourceLines textSource = scanner.getSource(textStart, scanner.position());
      String content = textSource.getContent();
      scanner.next();

      String destination = null;
      if (URI.matcher(content).matches()) {
        destination = content;
      } else if (EMAIL.matcher(content).matches()) {
        destination = "mailto:" + content;
      }

      if (destination != null) {
        Link link = new Link(destination, null);
        Text text = new Text(content);
        text.setSourceSpans(textSource.getSourceSpans());
        link.appendChild(text);
        return ParsedInline.of(link, scanner.position());
      }
    }
    return ParsedInline.none();
  }

  public static class Factory implements InlineContentParserFactory {
    @Override
    public Set<Character> getTriggerCharacters() {
      return Set.of('<');
    }

    @Override
    public InlineContentParser create() {
      return new AutolinkInlineParser();
    }
  }
}
