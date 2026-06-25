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

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.dominokit.markdown.internal.util.Escaping;

public class HtmlWriter {

  private static final Map<String, String> NO_ATTRIBUTES = Map.of();

  private final Appendable buffer;
  private char lastChar = 0;

  public HtmlWriter(Appendable out) {
    Objects.requireNonNull(out, "out must not be null");
    this.buffer = out;
  }

  public void raw(String s) {
    append(s);
  }

  public void text(String text) {
    append(Escaping.escapeHtml(text));
  }

  public void tag(String name) {
    tag(name, NO_ATTRIBUTES);
  }

  public void tag(String name, Map<String, String> attrs) {
    tag(name, attrs, false);
  }

  public void tag(String name, Map<String, String> attrs, boolean voidElement) {
    append("<");
    append(name);
    if (attrs != null && !attrs.isEmpty()) {
      for (var attr : attrs.entrySet()) {
        append(" ");
        append(Escaping.escapeHtml(attr.getKey()));
        if (attr.getValue() != null) {
          append("=\"");
          append(Escaping.escapeHtml(attr.getValue()));
          append("\"");
        }
      }
    }
    if (voidElement) {
      append(" /");
    }

    append(">");
  }

  public void line() {
    if (lastChar != 0 && lastChar != '\n') {
      append("\n");
    }
  }

  protected void append(String s) {
    try {
      buffer.append(s);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    int length = s.length();
    if (length != 0) {
      lastChar = s.charAt(length - 1);
    }
  }
}
