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

import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;
import static io.noties.prism4j.Prism4j.grammar;
import static io.noties.prism4j.Prism4j.pattern;
import static io.noties.prism4j.Prism4j.token;

import androidx.annotation.NonNull;

import io.noties.prism4j.Prism4j;

@SuppressWarnings("unused")
public class Prism_makefile {

  @NonNull
  public static Prism4j.Grammar create(@NonNull Prism4j prism4j) {
    return grammar("makefile",
      token("comment", pattern(
        compile("(^|[^\\\\])#(?:\\\\(?:\\r\\n|[\\s\\S])|[^\\\\\\r\\n])*"),
        true
      )),
      token("string", pattern(
        compile("([\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1"),
        false,
        true
      )),
      token("builtin", pattern(compile("\\.[A-Z][^:#=\\s]+(?=\\s*:(?!=))"))),
      token("symbol", pattern(
        compile("^[^:=\\r\\n]+(?=\\s*:(?!=))", MULTILINE),
        false,
        false,
        null,
        grammar("inside",
          token("variable", pattern(compile("\\$+(?:[^(){}:#=\\s]+|(?=[({]))")))
        )
      )),
      token("variable", pattern(compile("\\$+(?:[^(){}:#=\\s]+|\\([@*%<^+?][DF]\\)|(?=[({]))"))),
      token("keyword",
        pattern(compile("-include\\b|\\b(?:define|else|endef|endif|export|ifn?def|ifn?eq|include|override|private|sinclude|undefine|unexport|vpath)\\b")),
        pattern(
          compile("(\\()(?:addsuffix|abspath|and|basename|call|dir|error|eval|file|filter(?:-out)?|findstring|firstword|flavor|foreach|guile|if|info|join|lastword|load|notdir|or|origin|patsubst|realpath|shell|sort|strip|subst|suffix|value|warning|wildcard|word(?:s|list)?)(?=[ \\t])"),
          true
        )
      ),
      token("operator", pattern(compile("(?:::|[?:+!])?=|[|@]"))),
      token("punctuation", pattern(compile("[:;(){}]")))
    );
  }
}
