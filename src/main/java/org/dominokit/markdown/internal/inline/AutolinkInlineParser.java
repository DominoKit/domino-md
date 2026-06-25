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
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.SourceLines;
import org.dominokit.markdown.parser.beta.*;

/** Attempt to parse an autolink (URL or email in pointy brackets). */
public class AutolinkInlineParser implements InlineContentParser {

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
      if (isUriAutolink(content)) {
        destination = content;
      } else if (isEmailAutolink(content)) {
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

  private static boolean isUriAutolink(String content) {
    if (content.length() < 3 || !isAsciiLetter(content.charAt(0))) {
      return false;
    }

    int index = 1;
    while (index < content.length() && isSchemeChar(content.charAt(index))) {
      index++;
    }

    int schemeLength = index;
    if (schemeLength < 2
        || schemeLength > 32
        || index >= content.length()
        || content.charAt(index) != ':') {
      return false;
    }

    for (int i = index + 1; i < content.length(); i++) {
      char c = content.charAt(i);
      if (c == '<' || c == '>' || c == 0 || c <= 0x20) {
        return false;
      }
    }
    return true;
  }

  private static boolean isEmailAutolink(String content) {
    int at = content.indexOf('@');
    if (at <= 0 || at != content.lastIndexOf('@') || at == content.length() - 1) {
      return false;
    }

    for (int i = 0; i < at; i++) {
      if (!isEmailLocalPartChar(content.charAt(i))) {
        return false;
      }
    }

    String domain = content.substring(at + 1);
    int start = 0;
    boolean sawDot = false;
    while (start < domain.length()) {
      int dot = domain.indexOf('.', start);
      int end = dot == -1 ? domain.length() : dot;
      if (!isValidDomainLabel(domain, start, end)) {
        return false;
      }
      if (dot == -1) {
        return true;
      }
      sawDot = true;
      start = dot + 1;
    }
    return sawDot;
  }

  private static boolean isValidDomainLabel(String value, int start, int end) {
    int length = end - start;
    if (length <= 0 || length > 63) {
      return false;
    }

    if (!isAsciiLetterOrDigit(value.charAt(start))
        || !isAsciiLetterOrDigit(value.charAt(end - 1))) {
      return false;
    }

    for (int i = start + 1; i < end - 1; i++) {
      char c = value.charAt(i);
      if (!isAsciiLetterOrDigit(c) && c != '-') {
        return false;
      }
    }

    return true;
  }

  private static boolean isAsciiLetter(char c) {
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
  }

  private static boolean isAsciiLetterOrDigit(char c) {
    return isAsciiLetter(c) || (c >= '0' && c <= '9');
  }

  private static boolean isSchemeChar(char c) {
    return isAsciiLetterOrDigit(c) || c == '.' || c == '+' || c == '-';
  }

  private static boolean isEmailLocalPartChar(char c) {
    return isAsciiLetterOrDigit(c)
        || c == '.'
        || c == '!'
        || c == '#'
        || c == '$'
        || c == '%'
        || c == '&'
        || c == '\''
        || c == '*'
        || c == '+'
        || c == '/'
        || c == '='
        || c == '?'
        || c == '^'
        || c == '_'
        || c == '`'
        || c == '{'
        || c == '|'
        || c == '}'
        || c == '~';
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
