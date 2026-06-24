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

  StaggeredDelimiterProcessor(char delim) {
    this.delim = delim;
  }

  @Override
  public char getOpeningCharacter() {
    return delim;
  }

  @Override
  public char getClosingCharacter() {
    return delim;
  }

  @Override
  public int getMinLength() {
    return minLength;
  }

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

  private DelimiterProcessor findProcessor(int len) {
    for (DelimiterProcessor p : processors) {
      if (p.getMinLength() <= len) {
        return p;
      }
    }
    return processors.getFirst();
  }

  @Override
  public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
    return findProcessor(openingRun.length()).process(openingRun, closingRun);
  }
}
