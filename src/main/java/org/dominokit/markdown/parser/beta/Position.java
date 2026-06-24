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
package org.dominokit.markdown.parser.beta;

/**
 * Position within a {@link Scanner}. This is intentionally kept opaque so as not to expose the
 * internal structure of the Scanner.
 */
public class Position {

  final int lineIndex;
  final int index;

  Position(int lineIndex, int index) {
    this.lineIndex = lineIndex;
    this.index = index;
  }
}
