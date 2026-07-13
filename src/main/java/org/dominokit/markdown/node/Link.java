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
 * A link with a destination and an optional title.
 *
 * <p>The link node stores the resolved destination and title after inline parsing has interpreted
 * the source syntax.
 */
public class Link extends Node {

  private String destination;
  private String title;

  /** Create an empty link node that can be populated later. */
  public Link() {}

  /**
   * Create a link node with its destination and title already set.
   *
   * @param destination link destination URL
   * @param title optional link title
   */
  public Link(String destination, String title) {
    this.destination = destination;
    this.title = title;
  }

  /** Dispatch this link to the visitor. */
  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  /** @return the link destination */
  public String getDestination() {
    return destination;
  }

  /** Set the link destination. */
  public void setDestination(String destination) {
    this.destination = destination;
  }

  /** @return the link title, or {@code null} */
  public String getTitle() {
    return title;
  }

  /** Set the link title. */
  public void setTitle(String title) {
    this.title = title;
  }

  /** Include the destination and title in debug output. */
  @Override
  protected String toStringAttributes() {
    return "destination=" + destination + ", title=" + title;
  }
}
