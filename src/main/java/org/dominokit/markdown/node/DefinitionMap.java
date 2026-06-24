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
 * A map that can be used to store and look up reference definitions by a label.
 *
 * @param <D> definition value type
 */
public class DefinitionMap<D> {

  private final Class<D> type;
  private final Map<String, D> definitions = new LinkedHashMap<>();

  public DefinitionMap(Class<D> type) {
    this.type = type;
  }

  public Class<D> getType() {
    return type;
  }

  public void addAll(DefinitionMap<D> other) {
    for (Map.Entry<String, D> entry : other.definitions.entrySet()) {
      definitions.putIfAbsent(entry.getKey(), entry.getValue());
    }
  }

  public D putIfAbsent(String label, D definition) {
    String normalizedLabel = Escaping.normalizeLabelContent(label);
    return definitions.putIfAbsent(normalizedLabel, definition);
  }

  public D get(String label) {
    String normalizedLabel = Escaping.normalizeLabelContent(label);
    return definitions.get(normalizedLabel);
  }

  public Set<String> keySet() {
    return definitions.keySet();
  }

  public Collection<D> values() {
    return definitions.values();
  }
}
