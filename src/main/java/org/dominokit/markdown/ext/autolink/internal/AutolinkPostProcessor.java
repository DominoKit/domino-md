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

/** Post-processes plain text nodes and converts recognized URLs and email addresses into links. */
public class AutolinkPostProcessor implements PostProcessor {

  private final Set<AutolinkType> linkTypes;

  /**
   * Create a post processor for the supplied autolink types.
   *
   * @param linkTypes autolink categories that should be recognized
   */
  public AutolinkPostProcessor(Set<AutolinkType> linkTypes) {
    Objects.requireNonNull(linkTypes, "linkTypes must not be null");
    if (linkTypes.isEmpty()) {
      throw new IllegalArgumentException("linkTypes must not be empty");
    }
    this.linkTypes = EnumSet.copyOf(linkTypes);
  }

  /**
   * Walk the document tree and linkify eligible text nodes in place.
   *
   * @param node root node to post-process
   * @return the same node instance after any replacements have been applied
   */
  @Override
  public Node process(Node node) {
    node.accept(new AutolinkVisitor());
    return node;
  }

  /**
   * Replace portions of a text node with link nodes for any recognized autolinks.
   *
   * <p>The method preserves non-link text verbatim, splits the original text node into literal
   * fragments around each match, and then replaces each match with a link node that reuses the
   * original source span information when possible.
   *
   * @param originalTextNode text node to inspect and potentially rewrite
   */
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

  /**
   * Extract all autolink matches from a literal string.
   *
   * @param literal text to inspect
   * @return ordered list of non-overlapping autolink matches
   */
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

  /**
   * Find the next autolink candidate after the supplied index.
   *
   * @param literal text to inspect
   * @param startIndex index at which scanning should begin
   * @return the next valid autolink match, or {@code null} when none are present
   */
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

  /**
   * Parse a URL autolink candidate.
   *
   * <p>The parser recognizes explicit scheme-based URLs and validates the token boundaries so the
   * resulting link does not swallow surrounding punctuation.
   */
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

  /**
   * Parse a {@code www.}-prefixed autolink candidate.
   *
   * <p>These matches are promoted to {@code http://} links after host validation succeeds.
   */
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

  /**
   * Parse an email autolink candidate.
   *
   * @param literal text to inspect
   * @param startIndex index where the candidate begins
   * @return a link match when the token is a valid email address, otherwise {@code null}
   */
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

  /**
   * Scan a URL scheme prefix.
   *
   * @param literal text to inspect
   * @param startIndex index of the first scheme character
   * @return index immediately after the scheme, or {@code -1} when the prefix is invalid
   */
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

  /**
   * Scan the maximal raw URL token.
   *
   * <p>Scanning stops at whitespace or angle brackets because those characters cannot appear in an
   * autolink token.
   */
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

  /**
   * Scan the maximal raw email token.
   *
   * <p>Scanning stops at whitespace, angle brackets, or characters that are not valid in the
   * email-local-part grammar used by the autolink parser.
   */
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

  /**
   * Trim trailing punctuation that is not part of the actual URL.
   *
   * @param literal text being scanned
   * @param startIndex index where the candidate begins
   * @param endIndex exclusive end of the untrimmed candidate
   * @return exclusive end of the trimmed candidate
   */
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

  /**
   * Trim trailing punctuation that is not part of the actual email address.
   *
   * @param literal text being scanned
   * @param startIndex index where the candidate begins
   * @param endIndex exclusive end of the untrimmed candidate
   * @return exclusive end of the trimmed candidate
   */
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

  /**
   * Detect a trailing unmatched bracket or parenthesis at the end of a candidate.
   *
   * @param literal text being scanned
   * @param startIndex index where the candidate begins
   * @param endIndex exclusive end of the current candidate
   * @return {@code true} when the trailing bracket should be dropped
   */
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

  /**
   * Determine whether a URL-like token can start at the given index.
   *
   * @param literal text being scanned
   * @param index candidate start position
   * @return {@code true} when the character sequence can begin a URL autolink
   */
  private boolean isUrlStart(String literal, int index) {
    return isBoundaryBefore(literal, index) && isAsciiLetter(literal.charAt(index));
  }

  /**
   * Determine whether a {@code www.}-style URL can start at the given index.
   *
   * @param literal text being scanned
   * @param index candidate start position
   * @return {@code true} when the sequence can begin a {@code www.} autolink
   */
  private boolean isWwwStart(String literal, int index) {
    return isBoundaryBefore(literal, index)
        && index + 4 <= literal.length()
        && literal.regionMatches(true, index, "www.", 0, 4);
  }

  /**
   * Determine whether an email address can start at the given index.
   *
   * @param literal text being scanned
   * @param index candidate start position
   * @return {@code true} when the sequence can begin an email autolink
   */
  private boolean isEmailStart(String literal, int index) {
    return isBoundaryBefore(literal, index) && isEmailLocalPartChar(literal.charAt(index));
  }

  /**
   * Check that the current position is preceded by a non-word boundary.
   *
   * @param literal text being scanned
   * @param index candidate start position
   * @return {@code true} when the candidate is not embedded in a larger identifier
   */
  private boolean isBoundaryBefore(String literal, int index) {
    if (index == 0) {
      return true;
    }
    char previous = literal.charAt(index - 1);
    return !(isAsciiLetterOrDigit(previous) || previous == '_' || previous == '@');
  }

  /**
   * Validate that the candidate looks like a domain name.
   *
   * @param text host or domain text
   * @return {@code true} when the text is segmented into valid DNS labels
   */
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

  /**
   * Extract the host portion from a URL-like string.
   *
   * @param text URL-like string that may contain a path or query string
   * @return substring before the first path, query, or fragment separator
   */
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

  /**
   * Validate that the candidate is a plausible email address.
   *
   * <p>The local part must satisfy the autolink character rules and the domain must contain at
   * least one dot-separated valid DNS label.
   *
   * @param text candidate email address
   * @return {@code true} when the token is a valid email autolink
   */
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

  /**
   * Validate a single DNS label.
   *
   * @param value full domain string
   * @param start label start index
   * @param end label end index
   * @return {@code true} when the slice is a valid DNS label
   */
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

  /**
   * @return whether the character is an ASCII letter
   */
  private boolean isAsciiLetter(char c) {
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
  }

  /**
   * @return whether the character is an ASCII letter or digit
   */
  private boolean isAsciiLetterOrDigit(char c) {
    return isAsciiLetter(c) || (c >= '0' && c <= '9');
  }

  /**
   * @return whether the character is valid inside a URL scheme
   */
  private boolean isSchemeChar(char c) {
    return isAsciiLetterOrDigit(c) || c == '.' || c == '+' || c == '-';
  }

  /**
   * @return whether the character is valid in an email local part
   */
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

  /**
   * Create a text node with source-span slicing applied.
   *
   * @param text literal text for the node
   * @param sourceSpan source span to slice, or {@code null} when spans are unavailable
   * @param beginIndex start index relative to the source span
   * @param endIndex end index relative to the source span
   * @return a text node carrying the corresponding sliced source span when possible
   */
  private Text createTextNode(String text, SourceSpan sourceSpan, int beginIndex, int endIndex) {
    Text textNode = new Text(text);
    if (sourceSpan != null) {
      textNode.addSourceSpan(sourceSpan.subSpan(beginIndex, endIndex));
    }
    return textNode;
  }

  /**
   * Insert a node after the current insertion point and return the inserted node.
   *
   * @param insertAfter node after which the new node should be inserted
   * @param node node to insert
   * @return the inserted node
   */
  private Node insertAfter(Node insertAfter, Node node) {
    insertAfter.insertAfter(node);
    return node;
  }

  /**
   * Visitor that linkifies only text outside existing links.
   *
   * <p>The visitor tracks link nesting depth so nested text inside existing anchors is left
   * untouched while top-level text nodes are scanned for autolinks.
   */
  private final class AutolinkVisitor extends AbstractVisitor {
    private int inLink;

    /** Enter a link node and recurse into its children without rewriting nested text. */
    @Override
    public void visit(Link link) {
      inLink++;
      super.visit(link);
      inLink--;
    }

    /**
     * Linkify text nodes that are not already inside a link.
     *
     * @param text text node being visited
     */
    @Override
    public void visit(Text text) {
      if (inLink == 0) {
        linkify(text);
      }
    }
  }

  /** Immutable description of a detected autolink match. */
  private static final class LinkMatch {
    private final int beginIndex;
    private final int endIndex;
    private final String text;
    private final String destination;

    /**
     * Capture a single autolink match.
     *
     * @param beginIndex start index in the source literal
     * @param endIndex exclusive end index in the source literal
     * @param text text to display in the generated link
     * @param destination final link destination
     */
    private LinkMatch(int beginIndex, int endIndex, String text, String destination) {
      this.beginIndex = beginIndex;
      this.endIndex = endIndex;
      this.text = text;
      this.destination = destination;
    }
  }
}
