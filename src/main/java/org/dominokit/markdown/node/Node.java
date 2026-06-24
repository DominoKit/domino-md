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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The base class of all Markdown AST nodes ({@link Block} and inlines).
 *
 * <p>A node can have multiple children, and a parent (except for the root node).
 */
public abstract class Node {

  private Node parent;
  private Node firstChild;
  private Node lastChild;
  private Node previous;
  private Node next;
  private List<SourceSpan> sourceSpans;

  public abstract void accept(Visitor visitor);

  public Node getNext() {
    return next;
  }

  public Node getPrevious() {
    return previous;
  }

  public Node getFirstChild() {
    return firstChild;
  }

  public Node getLastChild() {
    return lastChild;
  }

  public Node getParent() {
    return parent;
  }

  protected void setParent(Node parent) {
    this.parent = parent;
  }

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

  public List<SourceSpan> getSourceSpans() {
    return sourceSpans != null ? Collections.unmodifiableList(sourceSpans) : List.of();
  }

  public void setSourceSpans(List<SourceSpan> sourceSpans) {
    if (sourceSpans.isEmpty()) {
      this.sourceSpans = null;
    } else {
      this.sourceSpans = new ArrayList<>(sourceSpans);
    }
  }

  public void addSourceSpan(SourceSpan sourceSpan) {
    if (sourceSpans == null) {
      sourceSpans = new ArrayList<>();
    }
    sourceSpans.add(sourceSpan);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" + toStringAttributes() + "}";
  }

  protected String toStringAttributes() {
    return "";
  }
}
