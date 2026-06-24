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

/**
 * Context object for inline parser construction.
 *
 * <p>The initial Phase 3 block parser uses a plain-text inline parser and does not expose
 * definitions or delimiter processors yet. The interface is kept so Phase 4 can grow the API
 * without changing the public factory shape.
 */
public interface InlineParserContext {}
