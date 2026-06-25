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
package org.dominokit.markdown.ext.autolink.internal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.dominokit.markdown.ext.autolink.AutolinkType;
import org.dominokit.markdown.node.AbstractVisitor;
import org.dominokit.markdown.node.Link;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.SourceSpan;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.PostProcessor;

public class AutolinkPostProcessor implements PostProcessor {

  private final Set<AutolinkType> linkTypes;

  public AutolinkPostProcessor(Set<AutolinkType> linkTypes) {
    Objects.requireNonNull(linkTypes, "linkTypes must not be null");
    if (linkTypes.isEmpty()) {
      throw new IllegalArgumentException("linkTypes must not be empty");
    }
    this.linkTypes = EnumSet.copyOf(linkTypes);
  }

  @Override
  public Node process(Node node) {
    node.accept(new AutolinkVisitor());
    return node;
  }

  private void linkify(Text originalTextNode) {
    String literal = originalTextNode.getLiteral();
    List<LinkMatch> matches = extractMatches(literal);
    if (matches.isEmpty()) {
      return;
    }

    Node insertAfter = originalTextNode;
    List<SourceSpan> sourceSpans = originalTextNode.getSourceSpans();
    SourceSpan sourceSpan = sourceSpans.size() == 1 ? sourceSpans.get(0) : null;

    int index = 0;
    for (LinkMatch match : matches) {
      if (match.beginIndex > index) {
        insertAfter =
            insertAfter(
                insertAfter,
                createTextNode(
                    literal.substring(index, match.beginIndex),
                    sourceSpan,
                    index,
                    match.beginIndex));
      }

      Text linkText = createTextNode(match.text, sourceSpan, match.beginIndex, match.endIndex);
      Link link = new Link(match.destination, null);
      link.appendChild(linkText);
      link.setSourceSpans(linkText.getSourceSpans());
      insertAfter = insertAfter(insertAfter, link);
      index = match.endIndex;
    }

    if (index < literal.length()) {
      insertAfter(
          insertAfter,
          createTextNode(literal.substring(index), sourceSpan, index, literal.length()));
    }

    originalTextNode.unlink();
  }

  private List<LinkMatch> extractMatches(String literal) {
    List<LinkMatch> matches = new ArrayList<>();
    int index = 0;
    while (index < literal.length()) {
      LinkMatch match = findNextMatch(literal, index);
      if (match == null) {
        break;
      }
      matches.add(match);
      index = match.endIndex;
    }
    return matches;
  }

  private LinkMatch findNextMatch(String literal, int startIndex) {
    for (int i = startIndex; i < literal.length(); i++) {
      if (linkTypes.contains(AutolinkType.URL) && isUrlStart(literal, i)) {
        LinkMatch match = parseUrlMatch(literal, i);
        if (match != null) {
          return match;
        }
      }

      if (linkTypes.contains(AutolinkType.WWW) && isWwwStart(literal, i)) {
        LinkMatch match = parseWwwMatch(literal, i);
        if (match != null) {
          return match;
        }
      }

      if (linkTypes.contains(AutolinkType.EMAIL) && isEmailStart(literal, i)) {
        LinkMatch match = parseEmailMatch(literal, i);
        if (match != null) {
          return match;
        }
      }
    }
    return null;
  }

  private LinkMatch parseUrlMatch(String literal, int startIndex) {
    int schemeEnd = scanScheme(literal, startIndex);
    if (schemeEnd == -1 || schemeEnd + 3 >= literal.length()) {
      return null;
    }
    if (literal.charAt(schemeEnd) != ':'
        || literal.charAt(schemeEnd + 1) != '/'
        || literal.charAt(schemeEnd + 2) != '/') {
      return null;
    }

    int endIndex = scanUrlToken(literal, startIndex);
    if (endIndex <= schemeEnd + 3) {
      return null;
    }

    int trimmedEnd = trimUrlEnd(literal, startIndex, endIndex);
    if (trimmedEnd <= schemeEnd + 3) {
      return null;
    }

    String text = literal.substring(startIndex, trimmedEnd);
    return new LinkMatch(startIndex, trimmedEnd, text, text);
  }

  private LinkMatch parseWwwMatch(String literal, int startIndex) {
    int endIndex = scanUrlToken(literal, startIndex);
    int trimmedEnd = trimUrlEnd(literal, startIndex, endIndex);
    if (trimmedEnd <= startIndex + 4) {
      return null;
    }

    String text = literal.substring(startIndex, trimmedEnd);
    String remainder = text.substring(4);
    String host = extractHost(remainder);
    if (host.indexOf('.') == -1) {
      return null;
    }
    if (!isValidDomainLike(host)) {
      return null;
    }
    return new LinkMatch(startIndex, trimmedEnd, text, "http://" + text);
  }

  private LinkMatch parseEmailMatch(String literal, int startIndex) {
    int endIndex = scanEmailToken(literal, startIndex);
    if (endIndex <= startIndex) {
      return null;
    }

    int trimmedEnd = trimEmailEnd(literal, startIndex, endIndex);
    if (trimmedEnd <= startIndex) {
      return null;
    }

    String text = literal.substring(startIndex, trimmedEnd);
    if (!isEmailAddress(text)) {
      return null;
    }

    return new LinkMatch(startIndex, trimmedEnd, text, "mailto:" + text);
  }

  private int scanScheme(String literal, int startIndex) {
    if (!isAsciiLetter(literal.charAt(startIndex))) {
      return -1;
    }
    int index = startIndex + 1;
    while (index < literal.length() && isSchemeChar(literal.charAt(index))) {
      index++;
    }
    int schemeLength = index - startIndex;
    if (schemeLength < 2 || schemeLength > 32) {
      return -1;
    }
    return index;
  }

  private int scanUrlToken(String literal, int startIndex) {
    int index = startIndex;
    while (index < literal.length()) {
      char c = literal.charAt(index);
      if (Character.isWhitespace(c) || c == '<' || c == '>') {
        break;
      }
      index++;
    }
    return index;
  }

  private int scanEmailToken(String literal, int startIndex) {
    int index = startIndex;
    while (index < literal.length()) {
      char c = literal.charAt(index);
      if (Character.isWhitespace(c) || c == '<' || c == '>') {
        break;
      }
      if (!(isEmailLocalPartChar(c) || c == '@' || c == '-')) {
        break;
      }
      index++;
    }
    return index;
  }

  private int trimUrlEnd(String literal, int startIndex, int endIndex) {
    int end = endIndex;
    while (end > startIndex) {
      char c = literal.charAt(end - 1);
      if (c == '.' || c == ',' || c == ':' || c == ';' || c == '!' || c == '?') {
        end--;
      } else if ((c == ')' || c == ']') && isUnbalancedTrailingBracket(literal, startIndex, end)) {
        end--;
      } else {
        break;
      }
    }
    return end;
  }

  private int trimEmailEnd(String literal, int startIndex, int endIndex) {
    int end = endIndex;
    while (end > startIndex) {
      char c = literal.charAt(end - 1);
      if (c == '.' || c == ',' || c == ':' || c == ';' || c == '!' || c == '?' || c == ')'
          || c == ']') {
        end--;
      } else {
        break;
      }
    }
    return end;
  }

  private boolean isUnbalancedTrailingBracket(String literal, int startIndex, int endIndex) {
    int parens = 0;
    int brackets = 0;
    for (int i = startIndex; i < endIndex; i++) {
      char c = literal.charAt(i);
      if (c == '(') {
        parens++;
      } else if (c == ')') {
        parens--;
      } else if (c == '[') {
        brackets++;
      } else if (c == ']') {
        brackets--;
      }
    }

    char trailing = literal.charAt(endIndex - 1);
    return (trailing == ')' && parens < 0) || (trailing == ']' && brackets < 0);
  }

  private boolean isUrlStart(String literal, int index) {
    return isBoundaryBefore(literal, index) && isAsciiLetter(literal.charAt(index));
  }

  private boolean isWwwStart(String literal, int index) {
    return isBoundaryBefore(literal, index)
        && index + 4 <= literal.length()
        && literal.regionMatches(true, index, "www.", 0, 4);
  }

  private boolean isEmailStart(String literal, int index) {
    return isBoundaryBefore(literal, index) && isEmailLocalPartChar(literal.charAt(index));
  }

  private boolean isBoundaryBefore(String literal, int index) {
    if (index == 0) {
      return true;
    }
    char previous = literal.charAt(index - 1);
    return !(isAsciiLetterOrDigit(previous) || previous == '_' || previous == '@');
  }

  private boolean isValidDomainLike(String text) {
    int start = 0;
    while (start < text.length()) {
      int dot = text.indexOf('.', start);
      int end = dot == -1 ? text.length() : dot;
      if (!isValidDomainLabel(text, start, end)) {
        return false;
      }
      if (dot == -1) {
        return true;
      }
      start = dot + 1;
    }
    return false;
  }

  private String extractHost(String text) {
    int end = text.length();
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == '/' || c == '?' || c == '#') {
        end = i;
        break;
      }
    }
    return text.substring(0, end);
  }

  private boolean isEmailAddress(String text) {
    int at = text.indexOf('@');
    if (at <= 0 || at != text.lastIndexOf('@') || at == text.length() - 1) {
      return false;
    }

    for (int i = 0; i < at; i++) {
      if (!isEmailLocalPartChar(text.charAt(i))) {
        return false;
      }
    }

    String domain = text.substring(at + 1);
    int start = 0;
    boolean sawDot = false;
    while (start < domain.length()) {
      int dot = domain.indexOf('.', start);
      int end = dot == -1 ? domain.length() : dot;
      if (!isValidDomainLabel(domain, start, end)) {
        return false;
      }
      if (dot == -1) {
        return sawDot;
      }
      sawDot = true;
      start = dot + 1;
    }
    return false;
  }

  private boolean isValidDomainLabel(String value, int start, int end) {
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

  private boolean isAsciiLetter(char c) {
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
  }

  private boolean isAsciiLetterOrDigit(char c) {
    return isAsciiLetter(c) || (c >= '0' && c <= '9');
  }

  private boolean isSchemeChar(char c) {
    return isAsciiLetterOrDigit(c) || c == '.' || c == '+' || c == '-';
  }

  private boolean isEmailLocalPartChar(char c) {
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

  private Text createTextNode(String text, SourceSpan sourceSpan, int beginIndex, int endIndex) {
    Text textNode = new Text(text);
    if (sourceSpan != null) {
      textNode.addSourceSpan(sourceSpan.subSpan(beginIndex, endIndex));
    }
    return textNode;
  }

  private Node insertAfter(Node insertAfter, Node node) {
    insertAfter.insertAfter(node);
    return node;
  }

  private final class AutolinkVisitor extends AbstractVisitor {
    private int inLink;

    @Override
    public void visit(Link link) {
      inLink++;
      super.visit(link);
      inLink--;
    }

    @Override
    public void visit(Text text) {
      if (inLink == 0) {
        linkify(text);
      }
    }
  }

  private static final class LinkMatch {
    private final int beginIndex;
    private final int endIndex;
    private final String text;
    private final String destination;

    private LinkMatch(int beginIndex, int endIndex, String text, String destination) {
      this.beginIndex = beginIndex;
      this.endIndex = endIndex;
      this.text = text;
      this.destination = destination;
    }
  }
}
