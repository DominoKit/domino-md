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
package org.dominokit.markdown.renderer.text;

/**
 * Controls how line breaks are rendered in plain-text output.
 *
 * <p>The modes range from fully flattened text to separated blocks with blank lines preserved.
 */
public enum LineBreakRendering {
  /**
   * Strip all line breaks within blocks and between blocks, resulting in all the text in a single
   * line.
   */
  STRIP,
  /** Use single line breaks between blocks, not a blank line. */
  COMPACT,
  /** Separate blocks by a blank line and preserve loose-list separation. */
  SEPARATE_BLOCKS,
}
