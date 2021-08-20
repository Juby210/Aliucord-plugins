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
import static io.noties.prism4j.Prism4j.pattern;
import static io.noties.prism4j.Prism4j.token;

import androidx.annotation.NonNull;

import io.noties.prism4j.GrammarUtils;
import io.noties.prism4j.Prism4j;

@SuppressWarnings("unused")
public class Prism_scala {

  @NonNull
  public static Prism4j.Grammar create(@NonNull Prism4j prism4j) {
    final Prism4j.Grammar scala = GrammarUtils.extend(
      GrammarUtils.require(prism4j, "java"),
      "scala",
      new GrammarUtils.TokenFilter() {
        @Override
        public boolean test(@NonNull Prism4j.Token token) {
          final String name = token.name();
          return !"class-name".equals(name) && !"function".equals(name);
        }
      },
      token("keyword", pattern(
        compile("<-|=>|\\b(?:abstract|case|catch|class|def|do|else|extends|final|finally|for|forSome|if|implicit|import|lazy|match|new|null|object|override|package|private|protected|return|sealed|self|super|this|throw|trait|try|type|val|var|while|with|yield)\\b")
      )),
      token("string",
        pattern(compile("\"\"\"[\\s\\S]*?\"\"\""), false, true),
        pattern(compile("(\"|')(?:\\\\.|(?!\\1)[^\\\\\\r\\n])*\\1"), false, true)
      ),
      token("number", pattern(
        compile("\\b0x[\\da-f]*\\.?[\\da-f]+|(?:\\b\\d+\\.?\\d*|\\B\\.\\d+)(?:e\\d+)?[dfl]?", CASE_INSENSITIVE)
      ))
    );

    scala.tokens().add(
      token("symbol", pattern(compile("'[^\\d\\s\\\\]\\w*")))
    );

    GrammarUtils.insertBeforeToken(scala, "number",
      token("builtin", pattern(compile("\\b(?:String|Int|Long|Short|Byte|Boolean|Double|Float|Char|Any|AnyRef|AnyVal|Unit|Nothing)\\b")))
    );

    return scala;
  }
}
