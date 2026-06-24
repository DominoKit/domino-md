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
package org.dominokit.markdown.node;

import java.util.ArrayList;
import java.util.List;

/** A list of source spans that can be added to. Takes care of merging adjacent source spans. */
public class SourceSpans {

  private List<SourceSpan> sourceSpans;

  public static SourceSpans empty() {
    return new SourceSpans();
  }

  public List<SourceSpan> getSourceSpans() {
    return sourceSpans != null ? sourceSpans : List.of();
  }

  public void addAllFrom(Iterable<? extends Node> nodes) {
    for (Node node : nodes) {
      addAll(node.getSourceSpans());
    }
  }

  public void addAll(List<SourceSpan> other) {
    if (other.isEmpty()) {
      return;
    }
    if (sourceSpans == null) {
      sourceSpans = new ArrayList<>();
    }
    if (sourceSpans.isEmpty()) {
      sourceSpans.addAll(other);
      return;
    }

    int lastIndex = sourceSpans.size() - 1;
    SourceSpan current = sourceSpans.get(lastIndex);
    SourceSpan next = other.get(0);
    if (current.getInputIndex() + current.getLength() == next.getInputIndex()) {
      sourceSpans.set(
          lastIndex,
          SourceSpan.of(
              current.getLineIndex(),
              current.getColumnIndex(),
              current.getInputIndex(),
              current.getLength() + next.getLength()));
      sourceSpans.addAll(other.subList(1, other.size()));
    } else {
      sourceSpans.addAll(other);
    }
  }
}
