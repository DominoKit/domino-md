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
package org.dominokit.markdown.internal;

import java.util.LinkedList;
import java.util.ListIterator;
import org.dominokit.markdown.parser.delimiter.DelimiterProcessor;
import org.dominokit.markdown.parser.delimiter.DelimiterRun;

/**
 * An implementation of DelimiterProcessor that dispatches all calls to two or more other
 * DelimiterProcessors depending on the length of the delimiter run. All child DelimiterProcessors
 * must have different minimum lengths. A given delimiter run is dispatched to the child with the
 * largest acceptable minimum length. If no child is applicable, the one with the largest minimum
 * length is chosen.
 */
class StaggeredDelimiterProcessor implements DelimiterProcessor {

  private final char delim;
  private int minLength = 0;
  private LinkedList<DelimiterProcessor> processors =
      new LinkedList<>(); // in reverse getMinLength order

  /**
   * Create a staggered processor for a single delimiter character.
   *
   * @param delim delimiter character handled by the processor set
   */
  StaggeredDelimiterProcessor(char delim) {
    this.delim = delim;
  }

  /**
   * @return the delimiter character accepted on the opening side
   */
  @Override
  public char getOpeningCharacter() {
    return delim;
  }

  /**
   * @return the delimiter character accepted on the closing side
   */
  @Override
  public char getClosingCharacter() {
    return delim;
  }

  /**
   * @return the minimum delimiter run length across all registered child processors
   */
  @Override
  public int getMinLength() {
    return minLength;
  }

  /**
   * Register a child delimiter processor.
   *
   * <p>Processors are kept in descending minimum-length order so the first processor whose
   * threshold fits the current delimiter run wins.
   *
   * @param dp processor to register
   */
  void add(DelimiterProcessor dp) {
    final int len = dp.getMinLength();
    ListIterator<DelimiterProcessor> it = processors.listIterator();
    boolean added = false;
    while (it.hasNext()) {
      DelimiterProcessor p = it.next();
      int pLen = p.getMinLength();
      if (len > pLen) {
        it.previous();
        it.add(dp);
        added = true;
        break;
      } else if (len == pLen) {
        throw new IllegalArgumentException(
            "Cannot add two delimiter processors for char '"
                + delim
                + "' and minimum length "
                + len
                + "; conflicting processors: "
                + p
                + ", "
                + dp);
      }
    }
    if (!added) {
      processors.add(dp);
      this.minLength = len;
    }
  }

  /**
   * Select the child processor that should handle a run of the supplied length.
   *
   * @param len delimiter run length
   * @return the best matching processor
   */
  private DelimiterProcessor findProcessor(int len) {
    for (DelimiterProcessor p : processors) {
      if (p.getMinLength() <= len) {
        return p;
      }
    }
    return processors.getFirst();
  }

  /**
   * Dispatch processing to the best matching child processor.
   *
   * @param openingRun opening delimiter run
   * @param closingRun closing delimiter run
   * @return the number of delimiters consumed
   */
  @Override
  public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
    return findProcessor(openingRun.length()).process(openingRun, closingRun);
  }
}
