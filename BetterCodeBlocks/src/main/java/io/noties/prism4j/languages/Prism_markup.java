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
public abstract class Prism_markup {

  @NonNull
  public static Prism4j.Grammar create(@NonNull Prism4j prism4j) {
    final Prism4j.Token entity = token("entity", pattern(compile("&#?[\\da-z]{1,8};", Pattern.CASE_INSENSITIVE)));
    return grammar(
      "markup",
      token("comment", pattern(compile("<!--[\\s\\S]*?-->"))),
      token("prolog", pattern(compile("<\\?[\\s\\S]+?\\?>"))),
      token("doctype", pattern(compile("<!DOCTYPE[\\s\\S]+?>", Pattern.CASE_INSENSITIVE))),
      token("cdata", pattern(compile("<!\\[CDATA\\[[\\s\\S]*?]]>", Pattern.CASE_INSENSITIVE))),
      token(
        "tag",
        pattern(
          compile("<\\/?(?!\\d)[^\\s>\\/=$<%]+(?:\\s+[^\\s>\\/=]+(?:=(?:(\"|')(?:\\\\[\\s\\S]|(?!\\1)[^\\\\])*\\1|[^\\s'\">=]+))?)*\\s*\\/?>", Pattern.CASE_INSENSITIVE),
          false,
          true,
          null,
          grammar(
            "inside",
            token(
              "tag",
              pattern(
                compile("^<\\/?[^\\s>\\/]+", Pattern.CASE_INSENSITIVE),
                false,
                false,
                null,
                grammar(
                  "inside",
                  token("punctuation", pattern(compile("^<\\/?"))),
                  token("namespace", pattern(compile("^[^\\s>\\/:]+:")))
                )
              )
            ),
            token(
              "attr-value",
              pattern(
                compile("=(?:(\"|')(?:\\\\[\\s\\S]|(?!\\1)[^\\\\])*\\1|[^\\s'\">=]+)", Pattern.CASE_INSENSITIVE),
                false,
                false,
                null,
                grammar(
                  "inside",
                  token(
                    "punctuation",
                    pattern(compile("^=")),
                    pattern(compile("(^|[^\\\\])[\"']"), true)
                  ),
                  entity
                )
              )
            ),
            token("punctuation", pattern(compile("\\/?>"))),
            token(
              "attr-name",
              pattern(
                compile("[^\\s>\\/]+"),
                false,
                false,
                null,
                grammar(
                  "inside",
                  token("namespace", pattern(compile("^[^\\s>\\/:]+:")))
                )
              )
            )
          )
        )
      ),
      entity
    );
  }

  private Prism_markup() {
  }
}
