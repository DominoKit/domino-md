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
 * Allows http, https, mailto, and data protocols for url. Also allows protocol relative urls, and
 * relative urls. Implementation based on
 * https://github.com/OWASP/java-html-sanitizer/blob/f07e44b034a45d94d6fd010279073c38b6933072/src/main/java/org/owasp/html/FilterUrlByProtocolAttributePolicy.java
 */
public class DefaultUrlSanitizer implements UrlSanitizer {
  private Set<String> protocols;

  public DefaultUrlSanitizer() {
    this(List.of("http", "https", "mailto", "data"));
  }

  public DefaultUrlSanitizer(Collection<String> protocols) {
    this.protocols = new HashSet<>(protocols);
  }

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

  @Override
  public String sanitizeImageUrl(String url) {
    return sanitizeLinkUrl(url);
  }

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
