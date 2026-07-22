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
package org.dominokit.markdown.parser.beta;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.dominokit.markdown.node.SourceSpan;
import org.dominokit.markdown.parser.SourceLine;
import org.dominokit.markdown.parser.SourceLines;
import org.junit.Test;

public class ScannerTest {

  @Test
  public void nextShouldAdvanceThroughSingleLine() {
    Scanner scanner = new Scanner(List.of(SourceLine.of("foo bar", null)), 0, 4);

    assertThat(scanner.peek()).isEqualTo('b');
    scanner.next();
    assertThat(scanner.peek()).isEqualTo('a');
    scanner.next();
    assertThat(scanner.peek()).isEqualTo('r');
    scanner.next();
    assertThat(scanner.peek()).isEqualTo('\0');
  }

  @Test
  public void scannerShouldExposeVirtualNewlinesBetweenLines() {
    Scanner scanner =
        new Scanner(List.of(SourceLine.of("ab", null), SourceLine.of("cde", null)), 0, 0);

    assertThat(scanner.hasNext()).isTrue();
    assertThat(scanner.peekPreviousCodePoint()).isEqualTo('\0');
    assertThat(scanner.peek()).isEqualTo('a');
    scanner.next();

    assertThat(scanner.hasNext()).isTrue();
    assertThat(scanner.peekPreviousCodePoint()).isEqualTo('a');
    assertThat(scanner.peek()).isEqualTo('b');
    scanner.next();

    assertThat(scanner.hasNext()).isTrue();
    assertThat(scanner.peekPreviousCodePoint()).isEqualTo('b');
    assertThat(scanner.peek()).isEqualTo('\n');
    scanner.next();

    assertThat(scanner.hasNext()).isTrue();
    assertThat(scanner.peekPreviousCodePoint()).isEqualTo('\n');
    assertThat(scanner.peek()).isEqualTo('c');
    scanner.next();

    assertThat(scanner.hasNext()).isTrue();
    assertThat(scanner.peekPreviousCodePoint()).isEqualTo('c');
    assertThat(scanner.peek()).isEqualTo('d');
    scanner.next();

    assertThat(scanner.hasNext()).isTrue();
    assertThat(scanner.peekPreviousCodePoint()).isEqualTo('d');
    assertThat(scanner.peek()).isEqualTo('e');
    scanner.next();

    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.peekPreviousCodePoint()).isEqualTo('e');
    assertThat(scanner.peek()).isEqualTo('\0');
  }

  @Test
  public void codePointMethodsShouldHandleSupplementaryCharacters() {
    Scanner scanner = new Scanner(List.of(SourceLine.of("\uD83D\uDE0A", null)), 0, 0);

    assertThat(scanner.hasNext()).isTrue();
    assertThat(scanner.peekPreviousCodePoint()).isEqualTo('\0');
    assertThat(scanner.peekCodePoint()).isEqualTo(128522);
    scanner.next();
    scanner.next();

    assertThat(scanner.hasNext()).isFalse();
    assertThat(scanner.peekPreviousCodePoint()).isEqualTo(128522);
    assertThat(scanner.peekCodePoint()).isEqualTo('\0');
  }

  @Test
  public void getSourceShouldPreserveContentAndSourceSpansAcrossLines() {
    Scanner scanner =
        new Scanner(
            List.of(
                SourceLine.of("ab", SourceSpan.of(10, 3, 13, 2)),
                SourceLine.of("cde", SourceSpan.of(11, 4, 20, 3))),
            0,
            0);

    Position start = scanner.position();

    scanner.next();
    assertSourceLines(
        scanner.getSource(start, scanner.position()), "a", SourceSpan.of(10, 3, 13, 1));

    Position afterA = scanner.position();

    scanner.next();
    assertSourceLines(
        scanner.getSource(start, scanner.position()), "ab", SourceSpan.of(10, 3, 13, 2));

    Position afterB = scanner.position();

    scanner.next();
    assertSourceLines(
        scanner.getSource(start, scanner.position()), "ab\n", SourceSpan.of(10, 3, 13, 2));

    scanner.next();
    assertSourceLines(
        scanner.getSource(start, scanner.position()),
        "ab\nc",
        SourceSpan.of(10, 3, 13, 2),
        SourceSpan.of(11, 4, 20, 1));

    scanner.next();
    assertSourceLines(
        scanner.getSource(start, scanner.position()),
        "ab\ncd",
        SourceSpan.of(10, 3, 13, 2),
        SourceSpan.of(11, 4, 20, 2));

    scanner.next();
    assertSourceLines(
        scanner.getSource(start, scanner.position()),
        "ab\ncde",
        SourceSpan.of(10, 3, 13, 2),
        SourceSpan.of(11, 4, 20, 3));

    assertSourceLines(
        scanner.getSource(afterA, scanner.position()),
        "b\ncde",
        SourceSpan.of(10, 4, 14, 1),
        SourceSpan.of(11, 4, 20, 3));

    assertSourceLines(
        scanner.getSource(afterB, scanner.position()), "\ncde", SourceSpan.of(11, 4, 20, 3));
  }

  @Test
  public void nextStringShouldOnlyMatchWithinCurrentLine() {
    Scanner scanner =
        Scanner.of(
            SourceLines.of(List.of(SourceLine.of("hey ya", null), SourceLine.of("hi", null))));

    assertThat(scanner.next("hoy")).isFalse();
    assertThat(scanner.next("hey")).isTrue();
    assertThat(scanner.next(' ')).isTrue();
    assertThat(scanner.next("yo")).isFalse();
    assertThat(scanner.next("ya")).isTrue();
    assertThat(scanner.next(" ")).isFalse();
  }

  private static void assertSourceLines(
      SourceLines sourceLines, String expectedContent, SourceSpan... expectedSourceSpans) {
    assertThat(sourceLines.getContent()).isEqualTo(expectedContent);
    assertThat(sourceLines.getSourceSpans()).isEqualTo(List.of(expectedSourceSpans));
  }
}
