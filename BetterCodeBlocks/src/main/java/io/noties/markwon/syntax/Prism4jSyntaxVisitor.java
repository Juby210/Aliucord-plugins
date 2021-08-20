/*
  Copyright 2019 Dimitry Ivanov (legal@noties.io)

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

import androidx.annotation.NonNull;

import io.noties.prism4j.AbsVisitor;
import io.noties.prism4j.Prism4j;

class Prism4jSyntaxVisitor extends AbsVisitor {

    private final String language;
    private final Prism4jTheme theme;
    private final SpannableStringBuilder builder;

    Prism4jSyntaxVisitor(
            @NonNull String language,
            @NonNull Prism4jTheme theme,
            @NonNull SpannableStringBuilder builder) {
        this.language = language;
        this.theme = theme;
        this.builder = builder;
    }

    @Override
    protected void visitText(@NonNull Prism4j.Text text) {
        builder.append(text.literal());
    }

    @Override
    protected void visitSyntax(@NonNull Prism4j.Syntax syntax) {

        final int start = builder.length();
        visit(syntax.children());
        final int end = builder.length();

        if (end != start) {
            theme.apply(language, syntax, builder, start, end);
        }
    }
}
