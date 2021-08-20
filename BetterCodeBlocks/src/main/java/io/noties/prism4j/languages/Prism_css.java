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

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static io.noties.prism4j.Prism4j.grammar;
import static io.noties.prism4j.Prism4j.pattern;
import static io.noties.prism4j.Prism4j.token;

import androidx.annotation.NonNull;

import io.noties.prism4j.GrammarUtils;
import io.noties.prism4j.Prism4j;

@SuppressWarnings("unused")
public abstract class Prism_css {

  // todo: really important one..
  // before a language is requested (fro example css)
  // it won't be initialized (so we won't modify markup to highlight css) before it was requested...

  @NonNull
  public static Prism4j.Grammar create(@NonNull Prism4j prism4j) {

    final Prism4j.Grammar grammar = grammar(
      "css",
      token("comment", pattern(compile("\\/\\*[\\s\\S]*?\\*\\/"))),
      token(
        "atrule",
        pattern(
          compile("@[\\w-]+?.*?(?:;|(?=\\s*\\{))", CASE_INSENSITIVE),
          false,
          false,
          null,
          grammar(
            "inside",
            token("rule", pattern(compile("@[\\w-]+")))
          )
        )
      ),
      token(
        "url",
        pattern(compile("url\\((?:([\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1|.*?)\\)", CASE_INSENSITIVE))
      ),
      token("selector", pattern(compile("[^{}\\s][^{};]*?(?=\\s*\\{)"))),
      token(
        "string",
        pattern(compile("(\"|')(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1"), false, true)
      ),
      token(
        "property",
        pattern(compile("[-_a-z\\xA0-\\uFFFF][-\\w\\xA0-\\uFFFF]*(?=\\s*:)", CASE_INSENSITIVE))
      ),
      token("important", pattern(compile("\\B!important\\b", CASE_INSENSITIVE))),
      token("function", pattern(compile("[-a-z0-9]+(?=\\()", CASE_INSENSITIVE))),
      token("punctuation", pattern(compile("[(){};:]")))
    );

    // can we maybe add some helper to specify simplified location?

    // now we need to put the all tokens from grammar inside `atrule` (except the `atrule` of cause)
    final Prism4j.Token atrule = grammar.tokens().get(1);
    final Prism4j.Grammar inside = GrammarUtils.findFirstInsideGrammar(atrule);
    if (inside != null) {
      for (Prism4j.Token token : grammar.tokens()) {
        if (!"atrule".equals(token.name())) {
          inside.tokens().add(token);
        }
      }
    }

    final Prism4j.Grammar markup = prism4j.grammar("markup");
    if (markup != null) {
      GrammarUtils.insertBeforeToken(markup, "tag",
        token(
          "style",
          pattern(
            compile("(<style[\\s\\S]*?>)[\\s\\S]*?(?=<\\/style>)", CASE_INSENSITIVE),
            true,
            true,
            "language-css",
            grammar
          )
        )
      );

      // important thing here is to clone found grammar
      // otherwise we will have stackoverflow (inside tag references style-attr, which
      // references inside tag, etc)
      final Prism4j.Grammar markupTagInside;
      {
        Prism4j.Grammar _temp = null;
        final Prism4j.Token token = GrammarUtils.findToken(markup, "tag");
        if (token != null) {
          _temp = GrammarUtils.findFirstInsideGrammar(token);
          if (_temp != null) {
            _temp = GrammarUtils.clone(_temp);
          }
        }
        markupTagInside = _temp;
      }

      GrammarUtils.insertBeforeToken(markup, "tag/attr-value",
        token(
          "style-attr",
          pattern(
            compile("\\s*style=(\"|')(?:\\\\[\\s\\S]|(?!\\1)[^\\\\])*\\1", CASE_INSENSITIVE),
            false,
            false,
            "language-css",
            grammar(
              "inside",
              token(
                "attr-name",
                pattern(
                  compile("^\\s*style", CASE_INSENSITIVE),
                  false,
                  false,
                  null,
                  markupTagInside
                )
              ),
              token("punctuation", pattern(compile("^\\s*=\\s*['\"]|['\"]\\s*$"))),
              token(
                "attr-value",
                pattern(
                  compile(".+", CASE_INSENSITIVE),
                  false,
                  false,
                  null,
                  grammar
                )
              )

            )
          )
        )
      );
    }

    return grammar;
  }

  private Prism_css() {
  }
}
