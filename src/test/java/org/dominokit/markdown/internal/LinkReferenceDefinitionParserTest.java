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
package org.dominokit.markdown.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.dominokit.markdown.internal.LinkReferenceDefinitionParser.State;
import org.dominokit.markdown.node.LinkReferenceDefinition;
import org.dominokit.markdown.parser.SourceLine;
import org.junit.Test;

public class LinkReferenceDefinitionParserTest {

  private final LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();

  @Test
  public void startLabelShouldEnterLabelState() {
    assertState("[", State.LABEL, "[");
  }

  @Test
  public void nonReferenceParagraphShouldLockParserIntoParagraphState() {
    assertParagraph("a");

    parse("a");
    parse("[");

    assertThat(parser.getState()).isEqualTo(State.PARAGRAPH);
    assertParagraphLines("a\n[", parser);
  }

  @Test
  public void emptyLabelsShouldBeRejected() {
    assertParagraph("[]: /");
    assertParagraph("[ ]: /");
    assertParagraph("[ \t\n\u000B\f\r ]: /");
  }

  @Test
  public void labelShouldRequireColonImmediatelyAfterClosingBracket() {
    assertParagraph("[foo] : /");
  }

  @Test
  public void labelShouldTransitionToDestinationState() {
    assertState("[foo]:", State.DESTINATION, "[foo]:");
    assertState("[ foo ]:", State.DESTINATION, "[ foo ]:");
  }

  @Test
  public void invalidNestedLabelShouldFallBackToParagraph() {
    assertParagraph("[foo[]:");
  }

  @Test
  public void multilineLabelShouldBeAccepted() {
    parse("[two");
    assertThat(parser.getState()).isEqualTo(State.LABEL);

    parse("lines]:");
    assertThat(parser.getState()).isEqualTo(State.DESTINATION);

    parse("/url");
    assertThat(parser.getState()).isEqualTo(State.START_TITLE);
    assertDefinition(parser.getDefinitions().get(0), "two\nlines", "/url", null);
  }

  @Test
  public void labelMayStartOnFollowingLine() {
    parse("[");
    assertThat(parser.getState()).isEqualTo(State.LABEL);

    parse("weird]:");
    assertThat(parser.getState()).isEqualTo(State.DESTINATION);

    parse("/url");
    assertThat(parser.getState()).isEqualTo(State.START_TITLE);
    assertDefinition(parser.getDefinitions().get(0), "\nweird", "/url", null);
  }

  @Test
  public void destinationShouldCreateDefinitionAndClearParagraphLines() {
    parse("[foo]: /url");
    assertThat(parser.getState()).isEqualTo(State.START_TITLE);
    assertParagraphLines("", parser);

    assertThat(parser.getDefinitions()).hasSize(1);
    assertDefinition(parser.getDefinitions().get(0), "foo", "/url", null);

    parse("[bar]: </url2>");
    assertDefinition(parser.getDefinitions().get(1), "bar", "/url2", null);
  }

  @Test
  public void invalidDestinationShouldFallBackToParagraph() {
    assertParagraph("[foo]: <bar<>");
  }

  @Test
  public void titleShouldBeCaptured() {
    parse("[foo]: /url 'title'");

    assertThat(parser.getState()).isEqualTo(State.START_DEFINITION);
    assertParagraphLines("", parser);
    assertThat(parser.getDefinitions()).hasSize(1);
    assertDefinition(parser.getDefinitions().get(0), "foo", "/url", "title");
  }

  @Test
  public void whitespaceOnlyLineAfterDestinationShouldFinishDefinition() {
    parse("[foo]: /url");
    assertThat(parser.getState()).isEqualTo(State.START_TITLE);
    assertParagraphLines("", parser);

    parse("   ");

    assertThat(parser.getState()).isEqualTo(State.START_DEFINITION);
    assertParagraphLines("   ", parser);
    assertThat(parser.getDefinitions()).hasSize(1);
    assertDefinition(parser.getDefinitions().get(0), "foo", "/url", null);
  }

  @Test
  public void multilineTitleShouldAccumulateUntilClosed() {
    parse("[foo]: /url 'two");
    assertThat(parser.getState()).isEqualTo(State.TITLE);
    assertParagraphLines("[foo]: /url 'two", parser);
    assertThat(parser.getDefinitions()).isEmpty();

    parse("lines");
    assertThat(parser.getState()).isEqualTo(State.TITLE);
    assertParagraphLines("[foo]: /url 'two\nlines", parser);
    assertThat(parser.getDefinitions()).isEmpty();

    parse("'");

    assertThat(parser.getState()).isEqualTo(State.START_DEFINITION);
    assertParagraphLines("", parser);
    assertThat(parser.getDefinitions()).hasSize(1);
    assertDefinition(parser.getDefinitions().get(0), "foo", "/url", "two\nlines\n");
  }

  @Test
  public void multilineTitleMayStartAtEndOfLine() {
    parse("[foo]: /url '");
    assertThat(parser.getState()).isEqualTo(State.TITLE);

    parse("title'");
    assertThat(parser.getState()).isEqualTo(State.START_DEFINITION);
    assertDefinition(parser.getDefinitions().get(0), "foo", "/url", "\ntitle");
  }

  @Test
  public void invalidTrailingContentAfterTitleShouldKeepDefinitionWithoutTitle() {
    parse("[foo]: /url");
    assertThat(parser.getState()).isEqualTo(State.START_TITLE);

    parse("\"title\" bad");

    assertThat(parser.getState()).isEqualTo(State.PARAGRAPH);
    assertDefinition(parser.getDefinitions().get(0), "foo", "/url", null);
  }

  @Test
  public void invalidParenTitleShouldKeepDefinitionWithoutTitle() {
    parse("[foo]: /url");
    assertThat(parser.getState()).isEqualTo(State.START_TITLE);

    parse("(title");
    assertThat(parser.getState()).isEqualTo(State.TITLE);

    parse("foo(");

    assertThat(parser.getState()).isEqualTo(State.PARAGRAPH);
    assertDefinition(parser.getDefinitions().get(0), "foo", "/url", null);
  }

  @Test
  public void invalidTitlesShouldFallBackToParagraph() {
    assertParagraph("[foo]: /url (invalid(");
    assertParagraph("[foo]: </url>'title'");
    assertParagraph("[foo]: /url 'title' INVALID");
  }

  private void parse(String content) {
    parser.parse(SourceLine.of(content, null));
  }

  private static void assertParagraph(String input) {
    assertState(input, State.PARAGRAPH, input);
  }

  private static void assertState(String input, State state, String paragraphContent) {
    LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();
    parser.parse(SourceLine.of(input, null));
    assertThat(parser.getState()).isEqualTo(state);
    assertParagraphLines(paragraphContent, parser);
  }

  private static void assertDefinition(
      LinkReferenceDefinition definition, String label, String destination, String title) {
    assertThat(definition.getLabel()).isEqualTo(label);
    assertThat(definition.getDestination()).isEqualTo(destination);
    assertThat(definition.getTitle()).isEqualTo(title);
  }

  private static void assertParagraphLines(
      String expectedContent, LinkReferenceDefinitionParser parser) {
    assertThat(parser.getParagraphLines().getContent()).isEqualTo(expectedContent);
  }
}
