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

import java.util.List;

import io.noties.prism4j.GrammarUtils;
import io.noties.prism4j.Prism4j;


@SuppressWarnings("unused")
public class Prism_swift {

  @NonNull
  public static Prism4j.Grammar create(@NonNull Prism4j prism4j) {

    final Prism4j.Grammar swift = GrammarUtils.extend(
      GrammarUtils.require(prism4j, "clike"),
      "swift",
      token("string", pattern(
        compile("(\"|')(\\\\(?:\\((?:[^()]|\\([^)]+\\))+\\)|\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1"),
        false,
        true,
        null,
        grammar("inside", token("interpolation", pattern(
          compile("\\\\\\((?:[^()]|\\([^)]+\\))+\\)"),
          false,
          false,
          null,
          grammar("inside", token("delimiter", pattern(
            compile("^\\\\\\(|\\)$"),
            false,
            false,
            "variable"
          )))
        )))
      )),
      token("keyword", pattern(
        compile("\\b(?:as|associativity|break|case|catch|class|continue|convenience|default|defer|deinit|didSet|do|dynamic(?:Type)?|else|enum|extension|fallthrough|final|for|func|get|guard|if|import|in|infix|init|inout|internal|is|lazy|left|let|mutating|new|none|nonmutating|operator|optional|override|postfix|precedence|prefix|private|protocol|public|repeat|required|rethrows|return|right|safe|self|Self|set|static|struct|subscript|super|switch|throws?|try|Type|typealias|unowned|unsafe|var|weak|where|while|willSet|__(?:COLUMN__|FILE__|FUNCTION__|LINE__))\\b")
      )),
      token("number", pattern(
        compile("\\b(?:[\\d_]+(?:\\.[\\de_]+)?|0x[a-f0-9_]+(?:\\.[a-f0-9p_]+)?|0b[01_]+|0o[0-7_]+)\\b", CASE_INSENSITIVE)
      ))
    );

    final List<Prism4j.Token> tokens = swift.tokens();

    tokens.add(token("constant", pattern(compile("\\b(?:nil|[A-Z_]{2,}|k[A-Z][A-Za-z_]+)\\b"))));
    tokens.add(token("atrule", pattern(compile("@\\b(?:IB(?:Outlet|Designable|Action|Inspectable)|class_protocol|exported|noreturn|NS(?:Copying|Managed)|objc|UIApplicationMain|auto_closure)\\b"))));
    tokens.add(token("builtin", pattern(compile("\\b(?:[A-Z]\\S+|abs|advance|alignof(?:Value)?|assert|contains|count(?:Elements)?|debugPrint(?:ln)?|distance|drop(?:First|Last)|dump|enumerate|equal|filter|find|first|getVaList|indices|isEmpty|join|last|lexicographicalCompare|map|max(?:Element)?|min(?:Element)?|numericCast|overlaps|partition|print(?:ln)?|reduce|reflect|reverse|sizeof(?:Value)?|sort(?:ed)?|split|startsWith|stride(?:of(?:Value)?)?|suffix|swap|toDebugString|toString|transcode|underestimateCount|unsafeBitCast|with(?:ExtendedLifetime|Unsafe(?:MutablePointers?|Pointers?)|VaList))\\b"))));

    final Prism4j.Token interpolationToken = GrammarUtils.findToken(swift, "string/interpolation");
    final Prism4j.Grammar interpolationGrammar = interpolationToken != null
      ? GrammarUtils.findFirstInsideGrammar(interpolationToken)
      : null;
    if (interpolationGrammar != null) {
      interpolationGrammar.tokens().addAll(swift.tokens());
    }

    return swift;
  }
}
