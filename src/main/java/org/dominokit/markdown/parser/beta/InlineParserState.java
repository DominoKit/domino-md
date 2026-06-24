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

public interface InlineParserState {

  /**
   * Return a scanner for the input for the current position (on the trigger character that the
   * inline parser was added for).
   *
   * <p>Note that this always returns the same instance, if you want to backtrack you need to use
   * {@link Scanner#position()} and {@link Scanner#setPosition(Position)}.
   */
  Scanner scanner();
}
