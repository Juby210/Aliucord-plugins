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

package io.noties.prism4j.languages;

import static java.util.regex.Pattern.compile;
import static io.noties.prism4j.Prism4j.grammar;
import static io.noties.prism4j.Prism4j.pattern;
import static io.noties.prism4j.Prism4j.token;

import androidx.annotation.NonNull;

import java.util.regex.Pattern;

import io.noties.prism4j.Prism4j;

@SuppressWarnings("unused")
public abstract class Prism_clike {

  @NonNull
  public static Prism4j.Grammar create(@NonNull Prism4j prism4j) {
    return grammar(
      "clike",
      token(
        "comment",
        pattern(compile("(^|[^\\\\])\\/\\*[\\s\\S]*?(?:\\*\\/|$)"), true),
        pattern(compile("(^|[^\\\\:])\\/\\/.*"), true, true)
      ),
      token(
        "string",
        pattern(compile("([\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1"), false, true)
      ),
      token(
        "class-name",
        pattern(
          compile("((?:\\b(?:class|interface|extends|implements|trait|instanceof|new)\\s+)|(?:catch\\s+\\())[\\w.\\\\]+"),
          true,
          false,
          null,
          grammar("inside", token("punctuation", pattern(compile("[.\\\\]"))))
        )
      ),
      token(
        "keyword",
        pattern(compile("\\b(?:if|else|while|do|for|return|in|instanceof|function|new|try|throw|catch|finally|null|break|continue)\\b"))
      ),
      token("boolean", pattern(compile("\\b(?:true|false)\\b"))),
      token("function", pattern(compile("[a-z0-9_]+(?=\\()", Pattern.CASE_INSENSITIVE))),
      token(
        "number",
        pattern(compile("\\b0x[\\da-f]+\\b|(?:\\b\\d+\\.?\\d*|\\B\\.\\d+)(?:e[+-]?\\d+)?", Pattern.CASE_INSENSITIVE))
      ),
      token("operator", pattern(compile("--?|\\+\\+?|!=?=?|<=?|>=?|==?=?|&&?|\\|\\|?|\\?|\\*|\\/|~|\\^|%"))),
      token("punctuation", pattern(compile("[{}\\[\\];(),.:]")))
    );
  }

  private Prism_clike() {
  }
}
