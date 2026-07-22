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
 * An image node.
 *
 * <p>Images carry a destination and optional title, and their inline children represent the alt
 * text.
 */
public class Image extends Node {

  private String destination;
  private String title;

  /** Create an empty image node that can be populated later. */
  public Image() {}

  /**
   * Create an image node with its destination and title already set.
   *
   * @param destination image destination URL
   * @param title optional image title
   */
  public Image(String destination, String title) {
    this.destination = destination;
    this.title = title;
  }

  /** Dispatch this image to the visitor. */
  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  /**
   * @return the image destination
   */
  public String getDestination() {
    return destination;
  }

  /** Set the image destination. */
  public void setDestination(String destination) {
    this.destination = destination;
  }

  /**
   * @return the image title, or {@code null}
   */
  public String getTitle() {
    return title;
  }

  /** Set the image title. */
  public void setTitle(String title) {
    this.title = title;
  }

  /** Include the destination and title in debug output. */
  @Override
  protected String toStringAttributes() {
    return "destination=" + destination + ", title=" + title;
  }
}
