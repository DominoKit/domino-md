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

import java.util.Iterator;

/** Utility methods for iterating over node ranges. */
public final class Nodes {

  /** Utility class; do not instantiate. */
  private Nodes() {}

  /**
   * Iterate over the siblings that appear strictly between two nodes.
   *
   * <p>The {@code start} node itself is excluded, and iteration stops before {@code end}.
   */
  public static Iterable<Node> between(Node start, Node end) {
    return new NodeIterable(start.getNext(), end);
  }

  /** Iterable view over a sibling range. */
  private static class NodeIterable implements Iterable<Node> {

    private final Node first;
    private final Node end;

    /**
     * Create an iterable view over a sibling range.
     *
     * @param first first node in the range, inclusive
     * @param end node at which iteration should stop, exclusive
     */
    private NodeIterable(Node first, Node end) {
      this.first = first;
      this.end = end;
    }

    /**
     * @return an iterator over the range
     */
    @Override
    public Iterator<Node> iterator() {
      return new NodeIterator(first, end);
    }
  }

  /** Iterator over a sibling range. */
  private static class NodeIterator implements Iterator<Node> {

    private Node node;
    private final Node end;

    /**
     * Create an iterator over a sibling range.
     *
     * @param first first node in the range, inclusive
     * @param end node at which iteration should stop, exclusive
     */
    private NodeIterator(Node first, Node end) {
      node = first;
      this.end = end;
    }

    /**
     * @return whether another node is available
     */
    @Override
    public boolean hasNext() {
      return node != null && node != end;
    }

    /**
     * @return the next node in the range
     */
    @Override
    public Node next() {
      Node result = node;
      node = node.getNext();
      return result;
    }

    /** Removal is not supported. */
    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
