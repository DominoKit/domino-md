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
 * Visitor for Markdown AST nodes.
 *
 * <p>Implementations should subclass {@link AbstractVisitor} instead of implementing this directly.
 */
public interface Visitor {

  void visit(BlockQuote blockQuote);

  void visit(BulletList bulletList);

  void visit(Code code);

  void visit(CustomBlock customBlock);

  void visit(CustomNode customNode);

  void visit(Document document);

  void visit(Emphasis emphasis);

  void visit(FencedCodeBlock fencedCodeBlock);

  void visit(HardLineBreak hardLineBreak);

  void visit(Heading heading);

  void visit(HtmlBlock htmlBlock);

  void visit(HtmlInline htmlInline);

  void visit(Image image);

  void visit(IndentedCodeBlock indentedCodeBlock);

  void visit(Link link);

  void visit(LinkReferenceDefinition linkReferenceDefinition);

  void visit(ListItem listItem);

  void visit(OrderedList orderedList);

  void visit(Paragraph paragraph);

  void visit(SoftLineBreak softLineBreak);

  void visit(StrongEmphasis strongEmphasis);

  void visit(Text text);

  void visit(ThematicBreak thematicBreak);
}
