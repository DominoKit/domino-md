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
package org.dominokit.markdown.ext.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.dominokit.markdown.node.Node;
import org.dominokit.markdown.renderer.elemental2.ElementAttributeProvider;
import org.dominokit.markdown.renderer.elemental2.ElementAttributeProviderContext;
import org.dominokit.markdown.renderer.elemental2.ElementAttributeProviderFactory;
import org.dominokit.markdown.renderer.elemental2.Elemental2Renderer;
import org.dominokit.markdown.renderer.html.AttributeProvider;
import org.dominokit.markdown.renderer.html.AttributeProviderContext;
import org.dominokit.markdown.renderer.html.AttributeProviderFactory;
import org.dominokit.markdown.renderer.html.HtmlRenderer;

/**
 * Optional renderer extension that adds CSS classes to generated markdown output.
 *
 * <p>The extension is deliberately narrow: it does not alter parsing, AST shape, or core rendering
 * rules. Instead, it attaches classes at the renderer boundary so callers can style generated HTML
 * and Elemental2 DOM nodes without replacing the built-in renderers.
 *
 * <p>Callers can configure three class sources:
 *
 * <ul>
 *   <li>base classes that are added to every generated element
 *   <li>node-specific classes keyed by markdown node type
 *   <li>tag-specific classes keyed by rendered element tag name
 * </ul>
 *
 * <p>Class values are merged with any pre-existing {@code class} attribute. This preserves renderer
 * output such as {@code language-*} classes on code blocks while still adding the configured
 * styling hooks.
 */
public final class MarkdownClassExtension
    implements HtmlRenderer.HtmlRendererExtension, Elemental2Renderer.Elemental2RendererExtension {

  private final List<String> baseClasses;
  private final List<ClassRule> nodeClassRules;
  private final Map<String, List<String>> tagClassRules;

  /**
   * Capture the configured class rules from the builder.
   *
   * @param builder configuration source
   */
  private MarkdownClassExtension(Builder builder) {
    this.baseClasses = List.copyOf(builder.baseClasses);
    this.nodeClassRules = List.copyOf(builder.nodeClassRules);
    this.tagClassRules = copyTagRules(builder.tagClassRules);
  }

  /**
   * Create a new builder for configuring class injection behavior.
   *
   * @return a fresh builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Install the HTML attribute provider used to merge classes into rendered tags.
   *
   * @param rendererBuilder HTML renderer builder to extend
   */
  @Override
  public void extend(HtmlRenderer.Builder rendererBuilder) {
    rendererBuilder.attributeProviderFactory(
        new AttributeProviderFactory() {
          @Override
          public AttributeProvider create(AttributeProviderContext context) {
            return new HtmlClassAttributeProvider();
          }
        });
  }

  /**
   * Install the Elemental2 attribute provider used to merge classes into rendered DOM elements.
   *
   * @param rendererBuilder DOM renderer builder to extend
   */
  @Override
  public void extend(Elemental2Renderer.Builder rendererBuilder) {
    rendererBuilder.attributeProviderFactory(
        new ElementAttributeProviderFactory() {
          @Override
          public ElementAttributeProvider create(ElementAttributeProviderContext context) {
            return new ElementClassAttributeProvider();
          }
        });
  }

  /**
   * Builder for {@link MarkdownClassExtension}.
   *
   * <p>The builder stores classes as individual tokens and applies them in insertion order during a
   * render pass. Repeated invocations for the same node type or tag append more class tokens to the
   * existing rule.
   */
  public static final class Builder {
    private final List<String> baseClasses = new ArrayList<>();
    private final List<ClassRule> nodeClassRules = new ArrayList<>();
    private final Map<String, List<String>> tagClassRules = new LinkedHashMap<>();

    /**
     * Add one or more classes that should be applied to every generated element.
     *
     * @param classNames CSS class tokens to add
     * @return this builder for chaining
     */
    public Builder classes(String... classNames) {
      baseClasses.addAll(normalizeClassNames(classNames));
      return this;
    }

    /**
     * Add one or more classes that should be applied whenever a node of the supplied type is
     * rendered.
     *
     * <p>Subclass matches are accepted, so a rule registered for a superclass also applies to its
     * concrete descendants.
     *
     * @param nodeType markdown node type to match
     * @param classNames CSS class tokens to add
     * @return this builder for chaining
     */
    public Builder nodeClasses(Class<? extends Node> nodeType, String... classNames) {
      Objects.requireNonNull(nodeType, "nodeType must not be null");
      List<String> normalized = normalizeClassNames(classNames);
      if (!normalized.isEmpty()) {
        nodeClassRules.add(new ClassRule(nodeType, normalized));
      }
      return this;
    }

    /**
     * Add one or more classes that should be applied whenever a specific HTML tag is rendered.
     *
     * @param tagName rendered tag name to match
     * @param classNames CSS class tokens to add
     * @return this builder for chaining
     */
    public Builder tagClasses(String tagName, String... classNames) {
      String normalizedTagName = normalizeTagName(tagName);
      List<String> normalizedClasses = normalizeClassNames(classNames);
      if (!normalizedClasses.isEmpty()) {
        tagClassRules
            .computeIfAbsent(normalizedTagName, ignored -> new ArrayList<>())
            .addAll(normalizedClasses);
      }
      return this;
    }

    /**
     * Build the configured extension.
     *
     * @return immutable extension instance
     */
    public MarkdownClassExtension build() {
      return new MarkdownClassExtension(this);
    }
  }

  /** Rule that associates a markdown node type with one or more CSS class tokens. */
  private static final class ClassRule {
    private final Class<? extends Node> nodeType;
    private final List<String> classNames;

    private ClassRule(Class<? extends Node> nodeType, List<String> classNames) {
      this.nodeType = nodeType;
      this.classNames = List.copyOf(classNames);
    }

    private boolean matches(Node node) {
      Class<?> current = node.getClass();
      while (current != null) {
        if (nodeType.equals(current)) {
          return true;
        }
        current = current.getSuperclass();
      }
      return false;
    }
  }

  /** HTML attribute provider that merges configured classes into the rendered attribute map. */
  private final class HtmlClassAttributeProvider implements AttributeProvider {
    @Override
    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
      mergeClasses(node, tagName, attributes);
    }
  }

  /**
   * Elemental2 attribute provider that merges configured classes into the rendered attribute map.
   */
  private final class ElementClassAttributeProvider implements ElementAttributeProvider {
    @Override
    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
      mergeClasses(node, tagName, attributes);
    }
  }

  /**
   * Merge the configured classes into the supplied attribute map.
   *
   * <p>Existing classes are preserved and appear first. Additional classes are appended in the
   * order they were configured, with duplicates removed while preserving first-seen order.
   *
   * @param node markdown node being rendered
   * @param tagName rendered tag name
   * @param attributes mutable attribute map to update in place
   */
  private void mergeClasses(Node node, String tagName, Map<String, String> attributes) {
    LinkedHashSet<String> classes = new LinkedHashSet<>();
    addClassTokens(classes, attributes.get("class"));
    classes.addAll(baseClasses);
    for (ClassRule rule : nodeClassRules) {
      if (rule.matches(node)) {
        classes.addAll(rule.classNames);
      }
    }
    classes.addAll(tagClassRules.getOrDefault(normalizeTagName(tagName), List.of()));

    if (classes.isEmpty()) {
      attributes.remove("class");
    } else {
      attributes.put("class", String.join(" ", classes));
    }
  }

  /**
   * Normalize a tag name to the canonical lookup form.
   *
   * @param tagName tag name to normalize
   * @return lower-case tag name
   */
  private static String normalizeTagName(String tagName) {
    Objects.requireNonNull(tagName, "tagName must not be null");
    String normalized = tagName.trim();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("tagName must not be empty");
    }
    return normalized.toLowerCase(Locale.ROOT);
  }

  /**
   * Normalize a set of class names into individual CSS tokens.
   *
   * <p>Whitespace inside an argument is treated as a separator so callers can pass either one token
   * per argument or a pre-joined string.
   *
   * @param classNames raw class-name arguments
   * @return normalized class tokens
   */
  private static List<String> normalizeClassNames(String... classNames) {
    Objects.requireNonNull(classNames, "classNames must not be null");
    List<String> normalized = new ArrayList<>();
    for (String className : classNames) {
      Objects.requireNonNull(className, "className must not be null");
      String trimmed = className.trim();
      if (trimmed.isEmpty()) {
        continue;
      }
      for (String token : trimmed.split("\\s+")) {
        if (!token.isEmpty()) {
          normalized.add(token);
        }
      }
    }
    return normalized;
  }

  /**
   * Add classes from a possibly pre-existing {@code class} attribute to the ordered set.
   *
   * @param classes ordered set being built
   * @param value attribute value to split into class tokens
   */
  private static void addClassTokens(Set<String> classes, String value) {
    if (value == null) {
      return;
    }
    for (String token : value.trim().split("\\s+")) {
      if (!token.isEmpty()) {
        classes.add(token);
      }
    }
  }

  /**
   * Copy the tag rule map into a structure owned by the immutable extension.
   *
   * @param source builder-owned tag rule map
   * @return immutable copy of the tag rules
   */
  private static Map<String, List<String>> copyTagRules(Map<String, List<String>> source) {
    Map<String, List<String>> copy = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : source.entrySet()) {
      copy.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    return Collections.unmodifiableMap(copy);
  }
}
