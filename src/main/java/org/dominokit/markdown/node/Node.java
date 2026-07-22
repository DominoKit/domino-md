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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base type for every node in the Markdown abstract syntax tree.
 *
 * <p>The tree is represented as a doubly linked sibling list with explicit parent/child references.
 * A node may have any number of children, but each node has at most one parent and at most one
 * predecessor and successor sibling. The root document node is the only node without a parent.
 *
 * <p>All tree mutation methods first detach the node being moved from its current location. This
 * keeps the tree structurally consistent and makes the operations safe to use when re-parenting
 * nodes between different parts of the document.
 */
public abstract class Node {

  private Node parent;
  private Node firstChild;
  private Node lastChild;
  private Node previous;
  private Node next;
  private List<SourceSpan> sourceSpans;

  /**
   * Dispatch this node to a visitor.
   *
   * <p>Concrete subclasses forward to the type-specific visitor method so callers can implement
   * node-specific behavior without manual {@code instanceof} checks.
   *
   * @param visitor the visitor to accept
   */
  public abstract void accept(Visitor visitor);

  /**
   * @return the next sibling node, or {@code null} if this is the last sibling
   */
  public Node getNext() {
    return next;
  }

  /**
   * @return the previous sibling node, or {@code null} if this is the first sibling
   */
  public Node getPrevious() {
    return previous;
  }

  /**
   * @return the first child, or {@code null} if this node has no children
   */
  public Node getFirstChild() {
    return firstChild;
  }

  /**
   * @return the last child, or {@code null} if this node has no children
   */
  public Node getLastChild() {
    return lastChild;
  }

  /**
   * @return the parent node, or {@code null} if this node is detached or is the root
   */
  public Node getParent() {
    return parent;
  }

  /**
   * Set the parent reference without updating sibling links.
   *
   * <p>This is protected because callers must preserve the bidirectional parent/sibling invariants;
   * public tree mutation methods take care of that bookkeeping.
   */
  protected void setParent(Node parent) {
    this.parent = parent;
  }

  /**
   * Append a child node to the end of this node's child list.
   *
   * <p>If the child already belongs to another location in the tree, it is unlinked first. The
   * child is then attached as the new last child and its sibling pointers are updated to match.
   *
   * @param child the node to append
   */
  public void appendChild(Node child) {
    child.unlink();
    child.setParent(this);
    if (lastChild != null) {
      lastChild.next = child;
      child.previous = lastChild;
    } else {
      firstChild = child;
    }
    lastChild = child;
  }

  /**
   * Insert a child node at the beginning of this node's child list.
   *
   * <p>The same unlink-and-reattach behavior as {@link #appendChild(Node)} applies.
   *
   * @param child the node to prepend
   */
  public void prependChild(Node child) {
    child.unlink();
    child.setParent(this);
    if (firstChild != null) {
      firstChild.previous = child;
      child.next = firstChild;
      firstChild = child;
    } else {
      firstChild = child;
      lastChild = child;
    }
  }

  /**
   * Detach this node from its parent and sibling chain.
   *
   * <p>After unlinking, the node becomes standalone: parent, previous, and next are all cleared.
   */
  public void unlink() {
    if (previous != null) {
      previous.next = next;
    } else if (parent != null) {
      parent.firstChild = next;
    }

    if (next != null) {
      next.previous = previous;
    } else if (parent != null) {
      parent.lastChild = previous;
    }

    parent = null;
    previous = null;
    next = null;
  }

  /**
   * Insert a sibling immediately after this node.
   *
   * <p>The sibling is unlinked from its previous location before being attached. Parent and sibling
   * pointers are updated so the surrounding list remains consistent.
   *
   * @param sibling the node to insert after this node
   */
  public void insertAfter(Node sibling) {
    sibling.unlink();
    sibling.next = next;
    if (sibling.next != null) {
      sibling.next.previous = sibling;
    }
    sibling.previous = this;
    next = sibling;
    sibling.parent = parent;
    if (sibling.next == null && sibling.parent != null) {
      sibling.parent.lastChild = sibling;
    }
  }

  /**
   * Insert a sibling immediately before this node.
   *
   * <p>The sibling is unlinked from its previous location before being attached. Parent and sibling
   * pointers are updated so the surrounding list remains consistent.
   *
   * @param sibling the node to insert before this node
   */
  public void insertBefore(Node sibling) {
    sibling.unlink();
    sibling.previous = previous;
    if (sibling.previous != null) {
      sibling.previous.next = sibling;
    }
    sibling.next = this;
    previous = sibling;
    sibling.parent = parent;
    if (sibling.previous == null && sibling.parent != null) {
      sibling.parent.firstChild = sibling;
    }
  }

  /**
   * Return the source spans associated with this node.
   *
   * <p>The returned list is immutable. An empty list means that source tracking was disabled or no
   * spans were recorded for this node.
   */
  public List<SourceSpan> getSourceSpans() {
    return sourceSpans != null ? Collections.unmodifiableList(sourceSpans) : List.of();
  }

  /**
   * Replace the current source-span list.
   *
   * <p>Empty input clears the stored spans entirely so the node remains in the cheaper "no-span"
   * state.
   *
   * @param sourceSpans source spans to associate with this node
   */
  public void setSourceSpans(List<SourceSpan> sourceSpans) {
    if (sourceSpans.isEmpty()) {
      this.sourceSpans = null;
    } else {
      this.sourceSpans = new ArrayList<>(sourceSpans);
    }
  }

  /**
   * Append a single source span to this node.
   *
   * <p>Spans are stored in insertion order and are not merged here; callers that need coalescing
   * should use {@link SourceSpans}.
   *
   * @param sourceSpan the span to record
   */
  public void addSourceSpan(SourceSpan sourceSpan) {
    if (sourceSpans == null) {
      sourceSpans = new ArrayList<>();
    }
    sourceSpans.add(sourceSpan);
  }

  /** Render the node type and any subclass-specific attributes for debugging. */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" + toStringAttributes() + "}";
  }

  /**
   * Subclasses append their diagnostic attributes here.
   *
   * <p>The default implementation returns an empty string so subclasses only need to override it
   * when they have additional state worth exposing in {@link #toString()}.
   */
  protected String toStringAttributes() {
    return "";
  }
}
