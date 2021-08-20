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
import androidx.annotation.Nullable;

public class PatternImpl implements Prism4j.Pattern {

    private final java.util.regex.Pattern regex;
    private final boolean lookbehind;
    private final boolean greedy;
    private final String alias;
    private final Prism4j.Grammar inside;

    public PatternImpl(
            @NonNull java.util.regex.Pattern regex,
            boolean lookbehind,
            boolean greedy,
            @Nullable String alias,
            @Nullable Prism4j.Grammar inside) {
        this.regex = regex;
        this.lookbehind = lookbehind;
        this.greedy = greedy;
        this.alias = alias;
        this.inside = inside;
    }

    @NonNull
    @Override
    public java.util.regex.Pattern regex() {
        return regex;
    }

    @Override
    public boolean lookbehind() {
        return lookbehind;
    }

    @Override
    public boolean greedy() {
        return greedy;
    }

    @Nullable
    @Override
    public String alias() {
        return alias;
    }

    @Nullable
    @Override
    public Prism4j.Grammar inside() {
        return inside;
    }

    @Override
    public String toString() {
        return ToString.toString(this);
    }
}
