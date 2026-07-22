/*
 * Copyright © 2026 Dominokit
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

import org.dominokit.markdown.node.Image;
import org.dominokit.markdown.node.Link;

/**
 * Sanitizes urls for img and a elements by whitelisting protocols. This is intended to prevent XSS
 * payloads like [Click this totally safe url](javascript:document.xss=true;)
 *
 * <p>Implementation based on
 * https://github.com/OWASP/java-html-sanitizer/blob/f07e44b034a45d94d6fd010279073c38b6933072/src/main/java/org/owasp/html/FilterUrlByProtocolAttributePolicy.java
 *
 * @since 0.14.0
 */
public interface UrlSanitizer {
  /**
   * Sanitize a url for use in the href attribute of a {@link Link}.
   *
   * @param url Link to sanitize
   * @return Sanitized link
   */
  String sanitizeLinkUrl(String url);

  /**
   * Sanitize a url for use in the src attribute of a {@link Image}.
   *
   * @param url Link to sanitize
   * @return Sanitized link {@link Image}
   */
  String sanitizeImageUrl(String url);
}
