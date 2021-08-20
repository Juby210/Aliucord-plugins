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
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.noties.prism4j.Prism4j;

public class Prism4jSyntaxHighlight {
    @NonNull
    public static Prism4jSyntaxHighlight create(
            @NonNull Prism4j prism4j,
            @NonNull Prism4jTheme theme) {
        return new Prism4jSyntaxHighlight(prism4j, theme, null);
    }

    private final Prism4j prism4j;
    private final Prism4jTheme theme;
    private final String fallback;

    protected Prism4jSyntaxHighlight(
            @NonNull Prism4j prism4j,
            @NonNull Prism4jTheme theme,
            @Nullable String fallback) {
        this.prism4j = prism4j;
        this.theme = theme;
        this.fallback = fallback;
    }

    @NonNull
    public CharSequence highlight(@Nullable String info, @NonNull String code) {

        // @since 4.2.2
        // although not null, but still is empty
        if (code.isEmpty()) {
            return code;
        }

        // if info is null, do not highlight -> LICENCE footer very commonly wrapped inside code
        // block without syntax name specified (so, do not highlight)
        return info == null
                ? highlightNoLanguageInfo(code)
                : highlightWithLanguageInfo(info, code);
    }

    @NonNull
    protected CharSequence highlightNoLanguageInfo(@NonNull String code) {
        return code;
    }

    @NonNull
    protected CharSequence highlightWithLanguageInfo(@NonNull String info, @NonNull String code) {

        final CharSequence out;

        final String language;
        final Prism4j.Grammar grammar;
        {
            String _language = info;
            Prism4j.Grammar _grammar = prism4j.grammar(info);
            if (_grammar == null && !TextUtils.isEmpty(fallback)) {
                _language = fallback;
                _grammar = prism4j.grammar(fallback);
            }
            language = _language;
            grammar = _grammar;
        }

        if (grammar != null) {
            out = highlight(language, grammar, code);
        } else {
            out = code;
        }

        return out;
    }

    @NonNull
    protected CharSequence highlight(@NonNull String language, @NonNull Prism4j.Grammar grammar, @NonNull String code) {
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        final Prism4jSyntaxVisitor visitor = new Prism4jSyntaxVisitor(language, theme, builder);
        visitor.visit(prism4j.tokenize(code, grammar));
        return builder;
    }
}
