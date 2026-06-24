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
package org.dominokit.markdown.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.internal.DocumentParser;
import org.dominokit.markdown.internal.InlineParserImpl;
import org.dominokit.markdown.node.Block;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.parser.block.BlockParserFactory;

/** Parses Markdown input to an AST tree of nodes. */
public class Parser {

  private final List<BlockParserFactory> blockParserFactories;
  private final InlineParserFactory inlineParserFactory;
  private final List<PostProcessor> postProcessors;
  private final IncludeSourceSpans includeSourceSpans;
  private final int maxOpenBlockParsers;

  private Parser(Builder builder) {
    blockParserFactories =
        DocumentParser.calculateBlockParserFactories(
            builder.blockParserFactories, builder.enabledBlockTypes);
    inlineParserFactory = builder.getInlineParserFactory();
    postProcessors = builder.postProcessors;
    includeSourceSpans = builder.includeSourceSpans;
    maxOpenBlockParsers = builder.maxOpenBlockParsers;
    inlineParserFactory.create(
        new InlineParserContext() {
          // Validate that the inline parser factory can produce an instance during builder setup.
        });
  }

  public static Builder builder() {
    return new Builder();
  }

  public Node parse(String input) {
    Objects.requireNonNull(input, "input must not be null");
    DocumentParser documentParser = createDocumentParser();
    Node document = documentParser.parse(input);
    return postProcess(document);
  }

  private DocumentParser createDocumentParser() {
    return new DocumentParser(
        blockParserFactories, inlineParserFactory, includeSourceSpans, maxOpenBlockParsers);
  }

  private Node postProcess(Node document) {
    for (PostProcessor postProcessor : postProcessors) {
      document = postProcessor.process(document);
    }
    return document;
  }

  public static class Builder {
    private final List<BlockParserFactory> blockParserFactories = new ArrayList<>();
    private final List<PostProcessor> postProcessors = new ArrayList<>();
    private Set<Class<? extends Block>> enabledBlockTypes =
        DocumentParser.getDefaultBlockParserTypes();
    private InlineParserFactory inlineParserFactory;
    private IncludeSourceSpans includeSourceSpans = IncludeSourceSpans.NONE;
    private int maxOpenBlockParsers = Integer.MAX_VALUE;

    public Parser build() {
      return new Parser(this);
    }

    public Builder extensions(Iterable<? extends Extension> extensions) {
      Objects.requireNonNull(extensions, "extensions must not be null");
      for (Extension extension : extensions) {
        if (extension instanceof ParserExtension) {
          ((ParserExtension) extension).extend(this);
        }
      }
      return this;
    }

    public Builder enabledBlockTypes(Set<Class<? extends Block>> enabledBlockTypes) {
      Objects.requireNonNull(enabledBlockTypes, "enabledBlockTypes must not be null");
      DocumentParser.checkEnabledBlockTypes(enabledBlockTypes);
      this.enabledBlockTypes = enabledBlockTypes;
      return this;
    }

    public Builder includeSourceSpans(IncludeSourceSpans includeSourceSpans) {
      this.includeSourceSpans = includeSourceSpans;
      return this;
    }

    public Builder maxOpenBlockParsers(int maxOpenBlockParsers) {
      if (maxOpenBlockParsers < 0) {
        throw new IllegalArgumentException("maxOpenBlockParsers must be >= 0");
      }
      this.maxOpenBlockParsers = maxOpenBlockParsers;
      return this;
    }

    public Builder customBlockParserFactory(BlockParserFactory blockParserFactory) {
      Objects.requireNonNull(blockParserFactory, "blockParserFactory must not be null");
      blockParserFactories.add(blockParserFactory);
      return this;
    }

    public Builder postProcessor(PostProcessor postProcessor) {
      Objects.requireNonNull(postProcessor, "postProcessor must not be null");
      postProcessors.add(postProcessor);
      return this;
    }

    public Builder inlineParserFactory(InlineParserFactory inlineParserFactory) {
      this.inlineParserFactory = inlineParserFactory;
      return this;
    }

    private InlineParserFactory getInlineParserFactory() {
      return inlineParserFactory != null ? inlineParserFactory : InlineParserImpl::new;
    }
  }

  /** Extension hook for the parser builder. */
  public interface ParserExtension extends Extension {
    void extend(Builder parserBuilder);
  }
}
