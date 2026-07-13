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
package org.dominokit.markdown.renderer.html;

import java.util.*;

/**
 * Default URL sanitizer for HTML rendering.
 *
 * <p>By default this sanitizer allows {@code http}, {@code https}, {@code mailto}, and
 * {@code data} URLs, as well as relative and protocol-relative URLs. The implementation is based
 * on the URL filtering rules used by OWASP's HTML sanitizer.
 */
public class DefaultUrlSanitizer implements UrlSanitizer {
  private Set<String> protocols;

  /** Create a sanitizer with the default protocol allowlist. */
  public DefaultUrlSanitizer() {
    this(List.of("http", "https", "mailto", "data"));
  }

  /**
   * Create a sanitizer with a custom protocol allowlist.
   *
   * @param protocols allowed URL protocols, compared case-insensitively
   */
  public DefaultUrlSanitizer(Collection<String> protocols) {
    this.protocols = new HashSet<>(protocols);
  }

  /**
   * Sanitize a link destination.
   *
   * <p>The method trims HTML whitespace, then scans for a protocol delimiter. If a protocol is
   * present and not allowlisted, the URL is rejected by returning an empty string.
   *
   * @param url the raw link destination
   * @return the sanitized URL, or an empty string when the protocol is not allowed
   */
  @Override
  public String sanitizeLinkUrl(String url) {
    url = stripHtmlSpaces(url);
    protocol_loop:
    for (int i = 0, n = url.length(); i < n; ++i) {
      switch (url.charAt(i)) {
        case '/':
        case '#':
        case '?': // No protocol.
          break protocol_loop;
        case ':':
          String protocol = url.substring(0, i).toLowerCase();
          if (!protocols.contains(protocol)) {
            return "";
          }
          break protocol_loop;
      }
    }
    return url;
  }

  /**
   * Sanitize an image destination.
   *
   * <p>Image and link destinations share the same URL policy.
   *
   * @param url the raw image destination
   * @return the sanitized URL, or an empty string when the protocol is not allowed
   */
  @Override
  public String sanitizeImageUrl(String url) {
    return sanitizeLinkUrl(url);
  }

  /** Trim leading and trailing HTML whitespace from the supplied string. */
  private String stripHtmlSpaces(String s) {
    int i = 0, n = s.length();
    for (; n > i; --n) {
      if (!isHtmlSpace(s.charAt(n - 1))) {
        break;
      }
    }
    for (; i < n; ++i) {
      if (!isHtmlSpace(s.charAt(i))) {
        break;
      }
    }
    if (i == 0 && n == s.length()) {
      return s;
    }
    return s.substring(i, n);
  }

  /**
   * Check whether a code point counts as HTML whitespace.
   *
   * @param ch the character to test
   * @return {@code true} when the character is considered HTML whitespace
   */
  private boolean isHtmlSpace(int ch) {
    switch (ch) {
      case ' ':
      case '\t':
      case '\n':
      case '\u000c':
      case '\r':
        return true;
      default:
        return false;
    }
  }
}
