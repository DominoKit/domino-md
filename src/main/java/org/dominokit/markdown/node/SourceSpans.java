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

/**
 * Mutable accumulator for source spans.
 *
 * <p>The class keeps spans in insertion order and merges only the narrow case where the last stored
 * span touches the first span in the next batch. That is sufficient for the parser's span
 * accumulation patterns without paying a merge cost for every append.
 */
public class SourceSpans {

  private List<SourceSpan> sourceSpans;

  /**
   * @return a new, empty span accumulator
   */
  public static SourceSpans empty() {
    return new SourceSpans();
  }

  /**
   * @return the accumulated spans, or an empty list if none have been added
   */
  public List<SourceSpan> getSourceSpans() {
    return sourceSpans != null ? sourceSpans : List.of();
  }

  /**
   * Add the spans from each node in the supplied iterable.
   *
   * <p>This is a convenience for merging child-node spans into a parent span list during inline
   * reconstruction.
   */
  public void addAllFrom(Iterable<? extends Node> nodes) {
    for (Node node : nodes) {
      addAll(node.getSourceSpans());
    }
  }

  /**
   * Add a batch of spans to the accumulator.
   *
   * <p>If the new batch begins exactly where the current tail ends, the spans are coalesced into a
   * single span. Otherwise the batch is appended as-is.
   */
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
