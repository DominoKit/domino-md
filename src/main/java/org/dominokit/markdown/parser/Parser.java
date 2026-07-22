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
package org.dominokit.markdown.parser;

import java.util.*;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.internal.Definitions;
import org.dominokit.markdown.internal.DocumentParser;
import org.dominokit.markdown.internal.InlineParserContextImpl;
import org.dominokit.markdown.internal.InlineParserImpl;
import org.dominokit.markdown.node.*;
import org.dominokit.markdown.parser.beta.InlineContentParserFactory;
import org.dominokit.markdown.parser.beta.LinkInfo;
import org.dominokit.markdown.parser.beta.LinkProcessor;
import org.dominokit.markdown.parser.beta.LinkResult;
import org.dominokit.markdown.parser.block.BlockParserFactory;
import org.dominokit.markdown.parser.delimiter.DelimiterProcessor;

/**
 * Configurable entry point for turning Markdown text into an AST.
 *
 * <p>The parser orchestrates block parsing, inline parsing, reference-definition collection, and
 * post-processing. Use {@link #builder()} to assemble a parser with custom extensions or additional
 * syntax handlers.
 *
 * <p>Example:
 *
 * <pre><code>
 * Parser parser = Parser.builder().build();
 * Node document = parser.parse("input text");
 * </code></pre>
 */
public class Parser {

  private final List<BlockParserFactory> blockParserFactories;
  private final List<InlineContentParserFactory> inlineContentParserFactories;
  private final List<DelimiterProcessor> delimiterProcessors;
  private final List<LinkProcessor> linkProcessors;
  private final Set<Character> linkMarkers;
  private final InlineParserFactory inlineParserFactory;
  private final List<PostProcessor> postProcessors;
  private final IncludeSourceSpans includeSourceSpans;
  private final int maxOpenBlockParsers;

  /**
   * Capture the builder state into an immutable parser instance.
   *
   * <p>The constructor copies the builder configuration and eagerly validates the inline parser
   * setup so invalid combinations fail fast instead of surfacing later during parsing.
   *
   * @param builder parser configuration source
   */
  private Parser(Builder builder) {
    this.blockParserFactories =
        DocumentParser.calculateBlockParserFactories(
            builder.blockParserFactories, builder.enabledBlockTypes);
    this.inlineParserFactory = builder.getInlineParserFactory();
    this.postProcessors = builder.postProcessors;
    this.inlineContentParserFactories = builder.inlineContentParserFactories;
    this.delimiterProcessors = builder.delimiterProcessors;
    this.linkProcessors = builder.linkProcessors;
    this.linkMarkers = builder.linkMarkers;
    this.includeSourceSpans = builder.includeSourceSpans;
    this.maxOpenBlockParsers = builder.maxOpenBlockParsers;

    // Try to construct an inline parser. Invalid configuration might result in an exception, which
    // we want to
    // detect as soon as possible.
    var context =
        new InlineParserContextImpl(
            inlineContentParserFactories,
            delimiterProcessors,
            linkProcessors,
            linkMarkers,
            new Definitions());
    this.inlineParserFactory.create(context);
  }

  /**
   * Create a new builder for configuring a {@link Parser}.
   *
   * @return a builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Parse the specified input text into a tree of nodes.
   *
   * <p>This method is thread-safe (a new parser state is used for each invocation).
   *
   * @param input the text to parse - must not be null
   * @return the root node
   */
  public Node parse(String input) {
    Objects.requireNonNull(input, "input must not be null");
    DocumentParser documentParser = createDocumentParser();
    Node document = documentParser.parse(input);
    return postProcess(document);
  }

  /**
   * Create a fresh internal parser state for one parse invocation.
   *
   * <p>Each call gets its own document parser so the public {@link Parser} instance remains
   * reusable and thread-safe.
   */
  private DocumentParser createDocumentParser() {
    return new DocumentParser(
        blockParserFactories,
        inlineParserFactory,
        inlineContentParserFactories,
        delimiterProcessors,
        linkProcessors,
        linkMarkers,
        includeSourceSpans,
        maxOpenBlockParsers);
  }

  /**
   * Apply configured post-processors in registration order.
   *
   * <p>Post-processors receive the output of the preceding stage, so each one can transform the
   * document tree before the next one runs.
   */
  private Node postProcess(Node document) {
    for (PostProcessor postProcessor : postProcessors) {
      document = postProcessor.process(document);
    }
    return document;
  }

  /** Builder for configuring a {@link Parser}. */
  public static class Builder {
    private final List<BlockParserFactory> blockParserFactories = new ArrayList<>();
    private final List<InlineContentParserFactory> inlineContentParserFactories = new ArrayList<>();
    private final List<DelimiterProcessor> delimiterProcessors = new ArrayList<>();
    private final List<LinkProcessor> linkProcessors = new ArrayList<>();
    private final List<PostProcessor> postProcessors = new ArrayList<>();
    private final Set<Character> linkMarkers = new HashSet<>();
    private Set<Class<? extends Block>> enabledBlockTypes =
        DocumentParser.getDefaultBlockParserTypes();
    private InlineParserFactory inlineParserFactory;
    private IncludeSourceSpans includeSourceSpans = IncludeSourceSpans.NONE;
    private int maxOpenBlockParsers = Integer.MAX_VALUE;

    /**
     * @return the configured {@link Parser}
     */
    public Parser build() {
      return new Parser(this);
    }

    /**
     * @param extensions extensions to use on this parser
     * @return {@code this}
     */
    public Builder extensions(Iterable<? extends Extension> extensions) {
      Objects.requireNonNull(extensions, "extensions must not be null");
      for (Extension extension : extensions) {
        if (extension instanceof ParserExtension) {
          ParserExtension parserExtension = (ParserExtension) extension;
          parserExtension.extend(this);
        }
      }
      return this;
    }

    /**
     * Apply a single parser extension.
     *
     * @param extension extension to apply
     * @return this builder for chaining
     */
    public Builder withExtension(Extension extension) {
      return extensions(List.of(Objects.requireNonNull(extension, "extension must not be null")));
    }

    /**
     * Apply one or more parser extensions.
     *
     * @param extensions extensions to apply
     * @return this builder for chaining
     */
    public Builder withExtensions(Extension... extensions) {
      Objects.requireNonNull(extensions, "extensions must not be null");
      return extensions(List.of(extensions));
    }

    /**
     * Describe the list of markdown features the parser will recognize and parse.
     *
     * <p>By default, CommonMark will recognize and parse the following set of "block" elements:
     *
     * <ul>
     *   <li>{@link Heading} ({@code #})
     *   <li>{@link HtmlBlock} ({@code <html></html>})
     *   <li>{@link ThematicBreak} (Horizontal Rule) ({@code ---})
     *   <li>{@link FencedCodeBlock} ({@code ```})
     *   <li>{@link IndentedCodeBlock}
     *   <li>{@link BlockQuote} ({@code >})
     *   <li>{@link ListBlock} (Ordered / Unordered List) ({@code 1. / *})
     * </ul>
     *
     * <p>To parse only a subset of the features listed above, pass a list of each feature's
     * associated {@link Block} class.
     *
     * <p>E.g., to only parse headings and lists:
     *
     * <pre>{@code
     * Parser.builder().enabledBlockTypes(Set.of(Heading.class, ListBlock.class));
     *
     * }</pre>
     *
     * @param enabledBlockTypes A list of block nodes the parser will parse. If this list is empty,
     *     the parser will not recognize any CommonMark core features.
     * @return {@code this}
     */
    public Builder enabledBlockTypes(Set<Class<? extends Block>> enabledBlockTypes) {
      Objects.requireNonNull(enabledBlockTypes, "enabledBlockTypes must not be null");
      DocumentParser.checkEnabledBlockTypes(enabledBlockTypes);
      this.enabledBlockTypes = enabledBlockTypes;
      return this;
    }

    /**
     * Whether to calculate source positions for parsed {@link Node Nodes}, see {@link
     * Node#getSourceSpans()}.
     *
     * <p>By default, source spans are disabled.
     *
     * @param includeSourceSpans which kind of source spans should be included
     * @return {@code this}
     * @since 0.16.0
     */
    public Builder includeSourceSpans(IncludeSourceSpans includeSourceSpans) {
      this.includeSourceSpans = includeSourceSpans;
      return this;
    }

    /**
     * Limit how many block parsers may be open at once while parsing.
     *
     * <p>Once the limit is reached, additional block starts are treated as plain text instead of
     * creating deeper nested block structure.
     *
     * <p>The document root parser is not counted. The default is unlimited, so callers that keep
     * using {@code Parser.builder().build()} preserve behavior.
     *
     * @param maxOpenBlockParsers maximum number of open non-document block parsers, must be zero or
     *     greater
     * @return {@code this}
     */
    public Builder maxOpenBlockParsers(int maxOpenBlockParsers) {
      if (maxOpenBlockParsers < 0) {
        throw new IllegalArgumentException("maxOpenBlockParsers must be >= 0");
      }
      this.maxOpenBlockParsers = maxOpenBlockParsers;
      return this;
    }

    /**
     * Add a custom block parser factory.
     *
     * <p>Note that custom factories are applied <em>before</em> the built-in factories. This is so
     * that extensions can change how some syntax is parsed that would otherwise be handled by
     * built-in factories. "With great power comes great responsibility."
     *
     * @param blockParserFactory a block parser factory implementation
     * @return {@code this}
     */
    public Builder customBlockParserFactory(BlockParserFactory blockParserFactory) {
      Objects.requireNonNull(blockParserFactory, "blockParserFactory must not be null");
      blockParserFactories.add(blockParserFactory);
      return this;
    }

    /**
     * Add a factory for a custom inline content parser, for extending inline parsing or overriding
     * built-in parsing.
     *
     * <p>Parsers are triggered by the characters returned from {@link
     * InlineContentParserFactory#getTriggerCharacters()}. It is possible to register multiple
     * parsers for the same character, or even for built-in trigger characters such as {@code `}.
     * Custom parsers are tried first in registration order, followed by the built-ins.
     *
     * @param inlineContentParserFactory factory to register
     * @return this builder for chaining
     */
    public Builder customInlineContentParserFactory(
        InlineContentParserFactory inlineContentParserFactory) {
      Objects.requireNonNull(inlineContentParserFactory, "inlineContentParser must not be null");
      inlineContentParserFactories.add(inlineContentParserFactory);
      return this;
    }

    /**
     * Add a custom delimiter processor for inline parsing.
     *
     * <p>Multiple delimiter processors with the same character can be added as long as their
     * minimum lengths differ. In that case, the shortest processor that can handle the current run
     * wins. If you need more control over how parsing is done, consider {@link
     * #customInlineContentParserFactory(InlineContentParserFactory)} instead.
     *
     * @param delimiterProcessor delimiter processor implementation to register
     * @return this builder for chaining
     */
    public Builder customDelimiterProcessor(DelimiterProcessor delimiterProcessor) {
      Objects.requireNonNull(delimiterProcessor, "delimiterProcessor must not be null");
      delimiterProcessors.add(delimiterProcessor);
      return this;
    }

    /**
     * Add a custom link or image processor for inline parsing.
     *
     * <p>Processors are tried in registration order. If none of them applies, the normal link
     * resolution behavior is used, so these processors can override built-in link parsing.
     *
     * @param linkProcessor link processor implementation to register
     * @return this builder for chaining
     */
    public Builder linkProcessor(LinkProcessor linkProcessor) {
      Objects.requireNonNull(linkProcessor, "linkProcessor must not be null");
      linkProcessors.add(linkProcessor);
      return this;
    }

    /**
     * Add a custom link marker for link processing.
     *
     * <p>A link marker is a character like {@code !} that changes the meaning of the following
     * bracket sequence. When a marker is present, the parsed {@link LinkInfo} includes it and link
     * processors may choose to preserve it via {@link LinkResult#includeMarker()}.
     *
     * @param linkMarker the marker character to add
     * @return this builder for chaining
     */
    public Builder linkMarker(Character linkMarker) {
      Objects.requireNonNull(linkMarker, "linkMarker must not be null");
      linkMarkers.add(linkMarker);
      return this;
    }

    /**
     * Register a post-processor that can transform the parsed AST after inline parsing completes.
     *
     * <p>Post-processors are executed in the order they are added.
     *
     * @param postProcessor the post-processing step to add
     * @return this builder for chaining
     */
    public Builder postProcessor(PostProcessor postProcessor) {
      Objects.requireNonNull(postProcessor, "postProcessor must not be null");
      postProcessors.add(postProcessor);
      return this;
    }

    /**
     * Override the factory used for inline markdown processing.
     *
     * <p>Provide a custom factory to change how inline syntax such as emphasis, code spans, links,
     * and images are parsed. When this method is not called, the default inline parser
     * implementation is used.
     *
     * @param inlineParserFactory custom inline parser factory
     * @return this builder for chaining
     */
    public Builder inlineParserFactory(InlineParserFactory inlineParserFactory) {
      this.inlineParserFactory = inlineParserFactory;
      return this;
    }

    /**
     * Resolve the inline parser factory to use for the final parser.
     *
     * <p>If the user did not supply one, the default {@link InlineParserImpl} implementation is
     * used.
     *
     * @return the inline parser factory that should back the parser instance
     */
    private InlineParserFactory getInlineParserFactory() {
      if (inlineParserFactory != null) {
        return inlineParserFactory;
      } else {
        return InlineParserImpl::new;
      }
    }
  }

  /** Extension contract for {@link Parser}. */
  public interface ParserExtension extends Extension {
    /** Contribute parser configuration to the supplied builder. */
    void extend(Builder parserBuilder);
  }
}
