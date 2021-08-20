/*
  Copyright 2019 Dimitry Ivanov (legal@noties.io)
  Modifications: Copyright 2021 Juby210

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package io.noties.prism4j;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class ArrayUtils {

    @SafeVarargs
    @NonNull
    static <T> List<T> toList(T... args) {
        final int length = args != null
                ? args.length
                : 0;
        final List<T> list = new ArrayList<>(length);
        if (length > 0) {
            Collections.addAll(list, args);
        }
        return list;
    }

    private ArrayUtils() {
    }
}
