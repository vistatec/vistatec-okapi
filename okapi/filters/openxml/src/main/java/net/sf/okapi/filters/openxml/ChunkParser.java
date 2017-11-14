/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.filters.openxml;

import net.sf.okapi.common.IdGenerator;

abstract class ChunkParser<T> implements Parser<T> {
    protected StartElementContext startElementContext;
    protected IdGenerator nestedBlockIdGenerator;
    protected StyleDefinitions styleDefinitions;

    ChunkParser(StartElementContext startElementContext, IdGenerator nestedBlockIdGenerator, StyleDefinitions styleDefinitions) {
        this.startElementContext = startElementContext;
        this.nestedBlockIdGenerator = nestedBlockIdGenerator;
        this.styleDefinitions = styleDefinitions;
    }
}