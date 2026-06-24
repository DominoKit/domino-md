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

/** A fenced code block. */
public class FencedCodeBlock extends Block {

  private String fenceCharacter;
  private Integer openingFenceLength;
  private Integer closingFenceLength;
  private int fenceIndent;
  private String info;
  private String literal;

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  public String getFenceCharacter() {
    return fenceCharacter;
  }

  public void setFenceCharacter(String fenceCharacter) {
    this.fenceCharacter = fenceCharacter;
  }

  public Integer getOpeningFenceLength() {
    return openingFenceLength;
  }

  public void setOpeningFenceLength(Integer openingFenceLength) {
    if (openingFenceLength != null && openingFenceLength < 3) {
      throw new IllegalArgumentException("openingFenceLength needs to be >= 3");
    }
    checkFenceLengths(openingFenceLength, closingFenceLength);
    this.openingFenceLength = openingFenceLength;
  }

  public Integer getClosingFenceLength() {
    return closingFenceLength;
  }

  public void setClosingFenceLength(Integer closingFenceLength) {
    if (closingFenceLength != null && closingFenceLength < 3) {
      throw new IllegalArgumentException("closingFenceLength needs to be >= 3");
    }
    checkFenceLengths(openingFenceLength, closingFenceLength);
    this.closingFenceLength = closingFenceLength;
  }

  public int getFenceIndent() {
    return fenceIndent;
  }

  public void setFenceIndent(int fenceIndent) {
    this.fenceIndent = fenceIndent;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public String getLiteral() {
    return literal;
  }

  public void setLiteral(String literal) {
    this.literal = literal;
  }

  @Deprecated
  public char getFenceChar() {
    return fenceCharacter != null && !fenceCharacter.isEmpty() ? fenceCharacter.charAt(0) : '\0';
  }

  @Deprecated
  public void setFenceChar(char fenceChar) {
    this.fenceCharacter = fenceChar != '\0' ? String.valueOf(fenceChar) : null;
  }

  @Deprecated
  public int getFenceLength() {
    return openingFenceLength != null ? openingFenceLength : 0;
  }

  @Deprecated
  public void setFenceLength(int fenceLength) {
    this.openingFenceLength = fenceLength != 0 ? fenceLength : null;
  }

  private static void checkFenceLengths(Integer openingFenceLength, Integer closingFenceLength) {
    if (openingFenceLength != null
        && closingFenceLength != null
        && closingFenceLength < openingFenceLength) {
      throw new IllegalArgumentException(
          "fence lengths required to be: closingFenceLength >= openingFenceLength");
    }
  }
}
