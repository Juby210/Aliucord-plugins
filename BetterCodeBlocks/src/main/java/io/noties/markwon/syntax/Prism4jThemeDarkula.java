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

package io.noties.markwon.syntax;

import android.text.SpannableStringBuilder;
import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.noties.markwon.core.spans.EmphasisSpan;
import io.noties.markwon.core.spans.StrongEmphasisSpan;

public class Prism4jThemeDarkula extends Prism4jThemeBase {
    @NonNull
    @Override
    protected ColorHashMap init() {
        return new ColorHashMap()
            .add(0xFF808080, "comment", "prolog", "cdata")
            .add(0xFFcc7832, "delimiter", "boolean", "keyword", "selector", "important", "atrule")
            .add(0xFFa9b7c6, "operator", "punctuation", "attr-name")
            .add(0xFFe8bf6a, "tag", "doctype", "builtin")
            .add(0xFF6897bb, "entity", "number", "symbol")
            .add(0xFF9876aa, "property", "constant", "variable")
            .add(0xFF6a8759, "string", "char")
            .add(0xFFbbb438, "annotation")
            .add(0xFFa5c261, "attr-value")
            .add(0xFF287bde, "url")
            .add(0xFFffc66d, "function")
            .add(0xFF364135, "regex")
            .add(0xFF294436, "inserted")
            .add(0xFF484a4a, "deleted");
    }

    @Override
    protected void applyColor(@NonNull String language, @NonNull String type, @Nullable String alias, int color, @NonNull SpannableStringBuilder builder, int start, int end) {
        super.applyColor(language, type, alias, color, builder, start, end);

        if (isOfType("important", type, alias)
            || isOfType("bold", type, alias)) {
            builder.setSpan(new StrongEmphasisSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (isOfType("italic", type, alias)) {
            builder.setSpan(new EmphasisSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
