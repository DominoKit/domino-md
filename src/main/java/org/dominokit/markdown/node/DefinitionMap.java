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
package org.dominokit.markdown.node;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.dominokit.markdown.internal.util.Escaping;

/**
 * A label-indexed map for reference definitions.
 *
 * <p>Labels are normalized before storage and lookup, which gives the parser CommonMark-compatible
 * case folding and whitespace collapsing semantics.
 *
 * @param <D> definition value type
 */
public class DefinitionMap<D> {

  private final Class<D> type;
  private final Map<String, D> definitions = new LinkedHashMap<>();

  /**
   * Create a definition map for the supplied value type.
   *
   * @param type the definition value type stored by this map
   */
  public DefinitionMap(Class<D> type) {
    this.type = type;
  }

  /**
   * @return the definition type stored by this map
   */
  public Class<D> getType() {
    return type;
  }

  /**
   * Merge definitions from another map without overwriting existing labels.
   *
   * <p>This preserves the first definition seen for a given normalized label, matching the parser's
   * preference for earlier definitions.
   */
  public void addAll(DefinitionMap<D> other) {
    for (Map.Entry<String, D> entry : other.definitions.entrySet()) {
      definitions.putIfAbsent(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Store a definition only if the normalized label has not already been seen.
   *
   * @param label the label to normalize and use as the key
   * @param definition the definition to store
   * @return the previous definition associated with the label, or {@code null}
   */
  public D putIfAbsent(String label, D definition) {
    String normalizedLabel = Escaping.normalizeLabelContent(label);
    return definitions.putIfAbsent(normalizedLabel, definition);
  }

  /**
   * Look up a definition by label.
   *
   * @param label the label to normalize before lookup
   * @return the matching definition, or {@code null}
   */
  public D get(String label) {
    String normalizedLabel = Escaping.normalizeLabelContent(label);
    return definitions.get(normalizedLabel);
  }

  /**
   * @return the normalized labels currently stored in this map
   */
  public Set<String> keySet() {
    return definitions.keySet();
  }

  /**
   * @return the definitions currently stored in this map
   */
  public Collection<D> values() {
    return definitions.values();
  }
}
