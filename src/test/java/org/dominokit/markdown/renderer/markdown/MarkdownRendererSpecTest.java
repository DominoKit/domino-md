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
package org.dominokit.markdown.renderer.markdown;

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
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.parser.Parser;
import org.dominokit.markdown.renderer.html.HtmlRenderer;
import org.junit.Test;

/**
 * Tests Markdown rendering against the checked-in CommonMark examples by asserting semantic HTML
 * equivalence after a Markdown round trip.
 */
public class MarkdownRendererSpecTest {

  private static final String SPEC_RESOURCE = "/spec.txt";
  private static final Pattern SECTION_PATTERN = Pattern.compile("#{1,6} *(.*)");
  private static final String EXAMPLE_START_MARKER = "```````````````````````````````` example";
  private static final Parser PARSER = Parser.builder().build();
  private static final MarkdownRenderer MARKDOWN_RENDERER = MarkdownRenderer.builder().build();
  private static final HtmlRenderer HTML_RENDERER =
      HtmlRenderer.builder().percentEncodeUrls(true).build();

  @Test
  public void markdownRoundTripShouldPreserveCommonMarkSpecHtml() {
    List<ExampleFailure> failures = new ArrayList<>();

    for (Example example : readExamples()) {
      String markdown = MARKDOWN_RENDERER.render(parse(example.source));
      String renderedHtml = renderHtml(markdown);
      if (!showTabs(renderedHtml).equals(showTabs(example.html))) {
        failures.add(new ExampleFailure(example, markdown, renderedHtml));
      }
    }

    if (!failures.isEmpty()) {
      StringBuilder details = new StringBuilder();
      details.append("Markdown renderer spec mismatches: ").append(failures.size()).append('\n');
      for (int i = 0; i < Math.min(10, failures.size()); i++) {
        ExampleFailure failure = failures.get(i);
        details
            .append('\n')
            .append(failure.example)
            .append('\n')
            .append("Source:\n")
            .append(showTabs(failure.example.source))
            .append("Markdown:\n")
            .append(showTabs(failure.markdown))
            .append("Expected HTML:\n")
            .append(showTabs(failure.example.html))
            .append("Actual HTML:\n")
            .append(showTabs(failure.actualHtml));
      }
      fail(details.toString());
    }
  }

  private static Node parse(String source) {
    return PARSER.parse(source);
  }

  private static String renderHtml(String source) {
    return HTML_RENDERER.render(parse(source)).replace("\t", "\u2192");
  }

  private static String showTabs(String value) {
    return value.replace("\t", "\u2192");
  }

  private static List<Example> readExamples() {
    try (InputStream stream =
            Objects.requireNonNull(
                MarkdownRendererSpecTest.class.getResourceAsStream(SPEC_RESOURCE),
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

    private Example(
        String filename,
        String section,
        String info,
        int exampleNumber,
        String source,
        String html) {
      this.filename = filename;
      this.section = section;
      this.info = info;
      this.exampleNumber = exampleNumber;
      this.source = source;
      this.html = html;
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

  private static final class ExampleFailure {
    private final Example example;
    private final String markdown;
    private final String actualHtml;

    private ExampleFailure(Example example, String markdown, String actualHtml) {
      this.example = example;
      this.markdown = markdown;
      this.actualHtml = actualHtml;
    }
  }
}
