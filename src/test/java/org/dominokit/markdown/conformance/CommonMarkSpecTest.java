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
package org.dominokit.markdown.conformance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dominokit.markdown.node.AbstractVisitor;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.node.Text;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.junit.Test;

public class CommonMarkSpecTest {

  private static final String SPEC_RESOURCE = "/spec.txt";
  private static final Pattern SECTION_PATTERN = Pattern.compile("#{1,6} *(.*)");
  private static final String EXAMPLE_START_MARKER = "```````````````````````````````` example";
  private static final Parser PARSER = Parser.builder().build();
  private static final HtmlRenderer RENDERER =
      HtmlRenderer.builder().percentEncodeUrls(true).build();

  @Test
  public void importedSpecSnapshotShouldContainExpectedCoreExampleCount() {
    assertThat(readExamples()).hasSize(652);
  }

  @Test
  public void specExamplesShouldRenderExpectedHtml() {
    List<Example> failures = new ArrayList<>();

    for (Example example : readExamples()) {
      String rendered = RENDERER.render(PARSER.parse(example.source));
      if (!showTabs(rendered).equals(showTabs(example.html))) {
        failures.add(example.withActual(rendered));
      }
    }

    if (!failures.isEmpty()) {
      StringBuilder details = new StringBuilder();
      details.append("CommonMark spec mismatches: ").append(failures.size()).append('\n');
      for (int i = 0; i < Math.min(10, failures.size()); i++) {
        Example failure = failures.get(i);
        details
            .append('\n')
            .append(failure)
            .append('\n')
            .append("Source:\n")
            .append(showTabs(failure.source))
            .append("Expected:\n")
            .append(showTabs(failure.html))
            .append("Actual:\n")
            .append(showTabs(failure.actualHtml));
      }
      fail(details.toString());
    }
  }

  @Test
  public void parsedSpecExamplesShouldNotCreateInvalidAdjacentTextNodes() {
    for (Example example : readExamples()) {
      Node document = PARSER.parse(example.source);
      document.accept(
          new AbstractVisitor() {
            @Override
            protected void visitChildren(Node parent) {
              if (parent instanceof Text && parent.getFirstChild() != null) {
                fail("Text node has children in " + example + ", literal: " + textLiteral(parent));
              }

              boolean lastText = false;
              for (Node node = parent.getFirstChild(); node != null; node = node.getNext()) {
                if (node instanceof Text) {
                  if (lastText) {
                    fail(
                        "Adjacent text nodes found in "
                            + example
                            + ", second literal: "
                            + textLiteral(node)
                            + '\n'
                            + showTabs(example.source));
                  }
                  lastText = true;
                } else {
                  lastText = false;
                }
              }

              super.visitChildren(parent);
            }
          });
    }
  }

  private static String textLiteral(Node node) {
    return ((Text) node).getLiteral();
  }

  private static String showTabs(String value) {
    return value.replace("\t", "\u2192");
  }

  private static List<Example> readExamples() {
    try (InputStream stream =
            Objects.requireNonNull(
                CommonMarkSpecTest.class.getResourceAsStream(SPEC_RESOURCE),
                "Missing test resource " + SPEC_RESOURCE);
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      List<Example> examples = new ArrayList<>();
      State state = State.BEFORE;
      String section = null;
      String info = "";
      int exampleNumber = 0;
      StringBuilder source = new StringBuilder();
      StringBuilder html = new StringBuilder();

      String line;
      while ((line = reader.readLine()) != null) {
        switch (state) {
          case BEFORE:
            Matcher matcher = SECTION_PATTERN.matcher(line);
            if (matcher.matches()) {
              section = matcher.group(1);
              exampleNumber = 0;
            }
            if (line.startsWith(EXAMPLE_START_MARKER)) {
              info = line.substring(EXAMPLE_START_MARKER.length()).trim();
              state = State.SOURCE;
              exampleNumber++;
            }
            break;
          case SOURCE:
            if (line.equals(".")) {
              state = State.HTML;
            } else {
              source.append(line.replace('\u2192', '\t')).append('\n');
            }
            break;
          case HTML:
            if (line.equals("````````````````````````````````")) {
              examples.add(
                  new Example(
                      "spec.txt",
                      section,
                      info,
                      exampleNumber,
                      source.toString(),
                      html.toString()));
              source = new StringBuilder();
              html = new StringBuilder();
              state = State.BEFORE;
            } else {
              html.append(line).append('\n');
            }
            break;
        }
      }

      return examples;
    } catch (IOException e) {
      throw new RuntimeException("Failed to read " + SPEC_RESOURCE, e);
    }
  }

  private enum State {
    BEFORE,
    SOURCE,
    HTML
  }

  private static final class Example {
    private final String filename;
    private final String section;
    private final String info;
    private final int exampleNumber;
    private final String source;
    private final String html;
    private final String actualHtml;

    private Example(
        String filename,
        String section,
        String info,
        int exampleNumber,
        String source,
        String html) {
      this(filename, section, info, exampleNumber, source, html, null);
    }

    private Example(
        String filename,
        String section,
        String info,
        int exampleNumber,
        String source,
        String html,
        String actualHtml) {
      this.filename = filename;
      this.section = section;
      this.info = info;
      this.exampleNumber = exampleNumber;
      this.source = source;
      this.html = html;
      this.actualHtml = actualHtml;
    }

    private Example withActual(String actualHtml) {
      return new Example(filename, section, info, exampleNumber, source, html, actualHtml);
    }

    @Override
    public String toString() {
      String suffix = info.isEmpty() ? "" : " [" + info + "]";
      return "File \""
          + filename
          + "\" section \""
          + section
          + "\" example "
          + exampleNumber
          + suffix;
    }
  }
}
