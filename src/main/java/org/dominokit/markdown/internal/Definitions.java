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
package org.dominokit.markdown.internal;

import java.util.HashMap;
import java.util.Map;
import org.dominokit.markdown.node.DefinitionMap;

/**
 * Registry of definition maps keyed by their value type.
 *
 * <p>The parser collects reference definitions in a single place, but the lookup API is typed by
 * definition class so extensions can add their own definition kinds without interfering with the
 * built-in link-reference storage.
 */
public class Definitions {

  private final Map<Class<?>, DefinitionMap<?>> definitionsByType = new HashMap<>();

  /**
   * Add all definitions from the provided map.
   *
   * <p>If a map for the same type already exists, the new entries are merged without replacing
   * earlier ones.
   *
   * @param definitionMap map of definitions to merge into the registry
   */
  public <D> void addDefinitions(DefinitionMap<D> definitionMap) {
    var existingMap = getMap(definitionMap.getType());
    if (existingMap == null) {
      definitionsByType.put(definitionMap.getType(), definitionMap);
    } else {
      existingMap.addAll(definitionMap);
    }
  }

  /**
   * Look up a definition by type and label.
   *
   * @param type the definition value type
   * @param label the label to normalize and search for
   * @param <V> the definition type
   * @return the matching definition, or {@code null}
   */
  public <V> V getDefinition(Class<V> type, String label) {
    var definitionMap = getMap(type);
    if (definitionMap == null) {
      return null;
    }
    return definitionMap.get(label);
  }

  /**
   * Retrieve the stored map for a given type.
   *
   * <p>The cast is safe because each entry is inserted under the same type key used for lookup.
   *
   * @param type definition value type
   * @param <V> value type
   * @return the stored map for the type, or {@code null}
   */
  private <V> DefinitionMap<V> getMap(Class<V> type) {
    //noinspection unchecked
    return (DefinitionMap<V>) definitionsByType.get(type);
  }
}
