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
package org.dominokit.markdown.internal;

import java.util.HashMap;
import java.util.Map;
import org.dominokit.markdown.node.DefinitionMap;

public class Definitions {

  private final Map<Class<?>, DefinitionMap<?>> definitionsByType = new HashMap<>();

  public <D> void addDefinitions(DefinitionMap<D> definitionMap) {
    var existingMap = getMap(definitionMap.getType());
    if (existingMap == null) {
      definitionsByType.put(definitionMap.getType(), definitionMap);
    } else {
      existingMap.addAll(definitionMap);
    }
  }

  public <V> V getDefinition(Class<V> type, String label) {
    var definitionMap = getMap(type);
    if (definitionMap == null) {
      return null;
    }
    return definitionMap.get(label);
  }

  private <V> DefinitionMap<V> getMap(Class<V> type) {
    //noinspection unchecked
    return (DefinitionMap<V>) definitionsByType.get(type);
  }
}
