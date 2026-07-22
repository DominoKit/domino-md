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
package org.dominokit.markdown.text;

import java.util.BitSet;
import java.util.Set;

/**
 * Bit-set backed matcher for ASCII characters.
 *
 * <p>The matcher is optimized for the small character sets that commonly appear in Markdown
 * tokenization and escaping rules.
 */
public class AsciiMatcher implements CharMatcher {
  private final BitSet set;

  private AsciiMatcher(Builder builder) {
    this.set = builder.set;
  }

  /**
   * @return whether the ASCII character is contained in the configured set
   */
  @Override
  public boolean matches(char c) {
    return set.get(c);
  }

  /**
   * @return a builder initialized with the current character set
   */
  public Builder newBuilder() {
    return new Builder((BitSet) set.clone());
  }

  /**
   * @return a new empty builder
   */
  public static Builder builder() {
    return new Builder(new BitSet());
  }

  /**
   * Create a builder initialized from an existing matcher.
   *
   * @param matcher the matcher to clone
   * @return a builder seeded with the same character set
   */
  public static Builder builder(AsciiMatcher matcher) {
    return new Builder((BitSet) matcher.set.clone());
  }

  /** Builder for {@link AsciiMatcher}. */
  public static class Builder {
    private final BitSet set;

    private Builder(BitSet set) {
      this.set = set;
    }

    /** Add one ASCII character to the matcher. */
    public Builder c(char c) {
      if (c > 127) {
        throw new IllegalArgumentException("Can only match ASCII characters");
      }
      set.set(c);
      return this;
    }

    /** Add every character in the string to the matcher. */
    public Builder anyOf(String s) {
      for (int i = 0; i < s.length(); i++) {
        c(s.charAt(i));
      }
      return this;
    }

    /** Add every character in the set to the matcher. */
    public Builder anyOf(Set<Character> characters) {
      for (Character c : characters) {
        c(c);
      }
      return this;
    }

    /** Add an inclusive ASCII character range to the matcher. */
    public Builder range(char from, char toInclusive) {
      for (char c = from; c <= toInclusive; c++) {
        c(c);
      }
      return this;
    }

    /**
     * @return the built matcher
     */
    public AsciiMatcher build() {
      return new AsciiMatcher(this);
    }
  }
}
