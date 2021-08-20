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
public class Prism_git {

  @NonNull
  public static Prism4j.Grammar create(@NonNull Prism4j prism4j) {
    return grammar("git",
      token("comment", pattern(compile("^#.*", MULTILINE))),
      token("deleted", pattern(compile("^[-–].*", MULTILINE))),
      token("inserted", pattern(compile("^\\+.*", MULTILINE))),
      token("string", pattern(compile("(\"|')(?:\\\\.|(?!\\1)[^\\\\\\r\\n])*\\1", MULTILINE))),
      token("command", pattern(
        compile("^.*\\$ git .*$", MULTILINE),
        false,
        false,
        null,
        grammar("inside",
          token("parameter", pattern(compile("\\s--?\\w+", MULTILINE)))
        )
      )),
      token("coord", pattern(compile("^@@.*@@$", MULTILINE))),
      token("commit_sha1", pattern(compile("^commit \\w{40}$", MULTILINE)))
    );
  }
}
