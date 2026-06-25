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
package org.dominokit.markdown.ext.task.list.items.internal;

import org.dominokit.markdown.ext.task.list.items.TaskListItemMarker;
import org.dominokit.markdown.node.AbstractVisitor;
import org.dominokit.markdown.node.ListItem;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.Paragraph;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.PostProcessor;

public class TaskListItemPostProcessor implements PostProcessor {

  @Override
  public Node process(Node node) {
    node.accept(new TaskListItemVisitor());
    return node;
  }

  private static final class TaskListItemVisitor extends AbstractVisitor {

    @Override
    public void visit(ListItem listItem) {
      Node child = listItem.getFirstChild();
      if (child instanceof Paragraph) {
        Node node = child.getFirstChild();
        if (node instanceof Text) {
          Text text = (Text) node;
          TaskMarker taskMarker = parseTaskMarker(text.getLiteral());
          if (taskMarker != null) {
            listItem.prependChild(new TaskListItemMarker(taskMarker.checked));
            text.setLiteral(taskMarker.remainingText);
          }
        }
      }

      visitChildren(listItem);
    }
  }

  private static TaskMarker parseTaskMarker(String literal) {
    if (literal == null || literal.length() < 5 || literal.charAt(0) != '[') {
      return null;
    }
    char marker = literal.charAt(1);
    if ((marker != 'x' && marker != 'X' && marker != ' ') || literal.charAt(2) != ']') {
      return null;
    }

    int index = 3;
    if (index >= literal.length() || !Character.isWhitespace(literal.charAt(index))) {
      return null;
    }

    while (index < literal.length() && Character.isWhitespace(literal.charAt(index))) {
      index++;
    }
    if (index == literal.length()) {
      return null;
    }

    return new TaskMarker(marker == 'x' || marker == 'X', literal.substring(index));
  }

  private static final class TaskMarker {
    private final boolean checked;
    private final String remainingText;

    private TaskMarker(boolean checked, String remainingText) {
      this.checked = checked;
      this.remainingText = remainingText;
    }
  }
}
