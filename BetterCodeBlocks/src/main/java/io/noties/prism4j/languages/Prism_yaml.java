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
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.compile;
import static io.noties.prism4j.Prism4j.grammar;
import static io.noties.prism4j.Prism4j.pattern;
import static io.noties.prism4j.Prism4j.token;

import androidx.annotation.NonNull;

import io.noties.prism4j.Prism4j;

@SuppressWarnings("unused")
public class Prism_yaml {

  @NonNull
  public static Prism4j.Grammar create(@NonNull Prism4j prism4j) {
    return grammar("yaml",
      token("scalar", pattern(
        compile("([\\-:]\\s*(?:![^\\s]+)?[ \\t]*[|>])[ \\t]*(?:((?:\\r?\\n|\\r)[ \\t]+)[^\\r\\n]+(?:\\2[^\\r\\n]+)*)"),
        true,
        false,
        "string"
      )),
      token("comment", pattern(compile("#.*"))),
      token("key", pattern(
        compile("(\\s*(?:^|[:\\-,\\[{\\r\\n?])[ \\t]*(?:![^\\s]+)?[ \\t]*)[^\\r\\n{\\[\\]},#\\s]+?(?=\\s*:\\s)"),
        true,
        false,
        "atrule"
      )),
      token("directive", pattern(
        compile("(^[ \\t]*)%.+", MULTILINE),
        true,
        false,
        "important"
      )),
      token("datetime", pattern(
        compile("([:\\-,\\[{]\\s*(?:![^\\s]+)?[ \\t]*)(?:\\d{4}-\\d\\d?-\\d\\d?(?:[tT]|[ \\t]+)\\d\\d?:\\d{2}:\\d{2}(?:\\.\\d*)?[ \\t]*(?:Z|[-+]\\d\\d?(?::\\d{2})?)?|\\d{4}-\\d{2}-\\d{2}|\\d\\d?:\\d{2}(?::\\d{2}(?:\\.\\d*)?)?)(?=[ \\t]*(?:$|,|]|\\}))", MULTILINE),
        true,
        false,
        "number"
      )),
      token("boolean", pattern(
        compile("([:\\-,\\[{]\\s*(?:![^\\s]+)?[ \\t]*)(?:true|false)[ \\t]*(?=$|,|]|\\})", MULTILINE | CASE_INSENSITIVE),
        true,
        false,
        "important"
      )),
      token("null", pattern(
        compile("([:\\-,\\[{]\\s*(?:![^\\s]+)?[ \\t]*)(?:null|~)[ \\t]*(?=$|,|]|\\})", MULTILINE | CASE_INSENSITIVE),
        true,
        false,
        "important"
      )),
      token("string", pattern(
        compile("([:\\-,\\[{]\\s*(?:![^\\s]+)?[ \\t]*)(\"|')(?:(?!\\2)[^\\\\\\r\\n]|\\\\.)*\\2(?=[ \\t]*(?:$|,|]|\\}))", MULTILINE),
        true,
        true
      )),
      token("number", pattern(
        compile("([:\\-,\\[{]\\s*(?:![^\\s]+)?[ \\t]*)[+-]?(?:0x[\\da-f]+|0o[0-7]+|(?:\\d+\\.?\\d*|\\.?\\d+)(?:e[+-]?\\d+)?|\\.inf|\\.nan)[ \\t]*(?=$|,|]|\\})", MULTILINE | CASE_INSENSITIVE),
        true
      )),
      token("tag", pattern(compile("![^\\s]+"))),
      token("important", pattern(compile("[&*][\\w]+"))),
      token("punctuation", pattern(compile("---|[:\\[\\]{}\\-,|>?]|\\.\\.\\.")))
    );
  }
}
