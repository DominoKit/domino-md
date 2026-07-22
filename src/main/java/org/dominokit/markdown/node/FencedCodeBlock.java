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
package org.dominokit.markdown.node;

/**
 * A fenced code block.
 *
 * <p>The block stores both the opening and closing fence metadata so renderers can reproduce the
 * source structure or inspect how the block was written.
 */
public class FencedCodeBlock extends Block {

  private String fenceCharacter;
  private Integer openingFenceLength;
  private Integer closingFenceLength;
  private int fenceIndent;
  private String info;
  private String literal;

  /** Dispatch this fenced code block to the visitor. */
  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  /**
   * @return the fence character, such as {@code `} or {@code ~}
   */
  public String getFenceCharacter() {
    return fenceCharacter;
  }

  /** Set the fence character. */
  public void setFenceCharacter(String fenceCharacter) {
    this.fenceCharacter = fenceCharacter;
  }

  /**
   * @return the opening fence length, or {@code null}
   */
  public Integer getOpeningFenceLength() {
    return openingFenceLength;
  }

  /** Set the opening fence length. */
  public void setOpeningFenceLength(Integer openingFenceLength) {
    if (openingFenceLength != null && openingFenceLength < 3) {
      throw new IllegalArgumentException("openingFenceLength needs to be >= 3");
    }
    checkFenceLengths(openingFenceLength, closingFenceLength);
    this.openingFenceLength = openingFenceLength;
  }

  /**
   * @return the closing fence length, or {@code null}
   */
  public Integer getClosingFenceLength() {
    return closingFenceLength;
  }

  /** Set the closing fence length. */
  public void setClosingFenceLength(Integer closingFenceLength) {
    if (closingFenceLength != null && closingFenceLength < 3) {
      throw new IllegalArgumentException("closingFenceLength needs to be >= 3");
    }
    checkFenceLengths(openingFenceLength, closingFenceLength);
    this.closingFenceLength = closingFenceLength;
  }

  /**
   * @return the indentation that preceded the opening fence
   */
  public int getFenceIndent() {
    return fenceIndent;
  }

  /** Set the indentation that preceded the opening fence. */
  public void setFenceIndent(int fenceIndent) {
    this.fenceIndent = fenceIndent;
  }

  /**
   * @return the info string, or {@code null}
   */
  public String getInfo() {
    return info;
  }

  /** Set the info string. */
  public void setInfo(String info) {
    this.info = info;
  }

  /**
   * @return the literal code content
   */
  public String getLiteral() {
    return literal;
  }

  /** Set the literal code content. */
  public void setLiteral(String literal) {
    this.literal = literal;
  }

  /**
   * @return the fence character as a single char, or {@code '\0'} if absent
   * @deprecated use {@link #getFenceCharacter()} instead
   */
  @Deprecated
  public char getFenceChar() {
    return fenceCharacter != null && !fenceCharacter.isEmpty() ? fenceCharacter.charAt(0) : '\0';
  }

  /**
   * Set the fence character using a single char.
   *
   * @deprecated use {@link #setFenceCharacter(String)} instead
   */
  @Deprecated
  public void setFenceChar(char fenceChar) {
    this.fenceCharacter = fenceChar != '\0' ? String.valueOf(fenceChar) : null;
  }

  /**
   * @return the opening fence length, or {@code 0} when unset
   * @deprecated use {@link #getOpeningFenceLength()} instead
   */
  @Deprecated
  public int getFenceLength() {
    return openingFenceLength != null ? openingFenceLength : 0;
  }

  /**
   * Set the opening fence length.
   *
   * @deprecated use {@link #setOpeningFenceLength(Integer)} instead
   */
  @Deprecated
  public void setFenceLength(int fenceLength) {
    this.openingFenceLength = fenceLength != 0 ? fenceLength : null;
  }

  /** Validate that the closing fence is at least as long as the opening fence. */
  private static void checkFenceLengths(Integer openingFenceLength, Integer closingFenceLength) {
    if (openingFenceLength != null
        && closingFenceLength != null
        && closingFenceLength < openingFenceLength) {
      throw new IllegalArgumentException(
          "fence lengths required to be: closingFenceLength >= openingFenceLength");
    }
  }
}
