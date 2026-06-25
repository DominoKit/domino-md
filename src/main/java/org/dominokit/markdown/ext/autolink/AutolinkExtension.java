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
package org.dominokit.markdown.ext.autolink;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import org.dominokit.markdown.Extension;
import org.dominokit.markdown.ext.autolink.internal.AutolinkPostProcessor;
import org.dominokit.markdown.parser.Parser;

/** Extension for automatically turning plain URLs and email addresses into links. */
public final class AutolinkExtension implements Parser.ParserExtension {

  private final Set<AutolinkType> linkTypes;

  /** Creates the default autolink extension configuration. */
  public AutolinkExtension() {
    this(new Builder());
  }

  private AutolinkExtension(Builder builder) {
    this.linkTypes = builder.linkTypes;
  }

  public static Extension create() {
    return builder().build();
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public void extend(Parser.Builder parserBuilder) {
    parserBuilder.postProcessor(new AutolinkPostProcessor(linkTypes));
  }

  public static final class Builder {
    private Set<AutolinkType> linkTypes = EnumSet.allOf(AutolinkType.class);

    public Builder linkTypes(AutolinkType... linkTypes) {
      Objects.requireNonNull(linkTypes, "linkTypes must not be null");
      return linkTypes(Set.of(linkTypes));
    }

    public Builder linkTypes(Set<AutolinkType> linkTypes) {
      Objects.requireNonNull(linkTypes, "linkTypes must not be null");
      if (linkTypes.isEmpty()) {
        throw new IllegalArgumentException("linkTypes must not be empty");
      }
      this.linkTypes = EnumSet.copyOf(linkTypes);
      return this;
    }

    public Extension build() {
      return new AutolinkExtension(this);
    }
  }
}
