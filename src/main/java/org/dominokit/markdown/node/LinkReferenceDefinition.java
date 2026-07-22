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

/**
 * A link reference definition block.
 *
 * <p>These definitions are collected during block parsing and resolved later when inline links are
 * processed.
 */
public class LinkReferenceDefinition extends Block {

  private String label;
  private String destination;
  private String title;

  /** Create an empty definition. */
  public LinkReferenceDefinition() {}

  /** Create a fully populated definition. */
  public LinkReferenceDefinition(String label, String destination, String title) {
    this.label = label;
    this.destination = destination;
    this.title = title;
  }

  /**
   * @return the reference label
   */
  public String getLabel() {
    return label;
  }

  /** Set the reference label. */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * @return the destination URI
   */
  public String getDestination() {
    return destination;
  }

  /** Set the destination URI. */
  public void setDestination(String destination) {
    this.destination = destination;
  }

  /**
   * @return the optional title, or {@code null}
   */
  public String getTitle() {
    return title;
  }

  /** Set the optional title. */
  public void setTitle(String title) {
    this.title = title;
  }

  /** Dispatch this definition to the visitor. */
  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
