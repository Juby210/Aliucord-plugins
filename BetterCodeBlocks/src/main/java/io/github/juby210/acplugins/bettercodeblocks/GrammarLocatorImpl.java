/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.bettercodeblocks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.*;

import io.noties.prism4j.GrammarLocator;
import io.noties.prism4j.Prism4j;

import io.noties.prism4j.languages.*;

public final class GrammarLocatorImpl implements GrammarLocator {
    private final Map<String, Prism4j.Grammar> cache = new HashMap<>(3);

    @Nullable
    @Override
    public Prism4j.Grammar grammar(@NonNull Prism4j prism4j, @NonNull String language) {
        final String name = realLanguageName(language);
        if (cache.containsKey(name)) return cache.get(name);

        Prism4j.Grammar grammar = obtainGrammar(prism4j, name);
        if (grammar == null) {
            cache.put(name, null);
        } else {
            cache.put(name, grammar);
            triggerModify(prism4j, name);
        }

        return grammar;
    }

    // added cs -> csharp, py -> python, kt -> kotlin
    @NonNull
    public static String realLanguageName(@NonNull String name) {
        switch (name) {
            case "js":
                return "javascript";
            case "xml":
            case "html":
            case "mathml":
            case "svg":
                return "markup";
            case "cs":
            case "dotnet":
                return "csharp";
            case "jsonp":
                return "json";
            case "py":
                return "python";
            case "kt":
                return "kotlin";
        }
        return name;
    }

    @Nullable
    protected Prism4j.Grammar obtainGrammar(@NonNull Prism4j prism4j, @NonNull String name) {
        switch (name) {
            case "brainfuck":
                return Prism_brainfuck.create(prism4j);
            case "c":
                return Prism_c.create(prism4j);
            case "clike":
                return Prism_clike.create(prism4j);
            case "clojure":
                return Prism_clojure.create(prism4j);
            case "cpp":
                return Prism_cpp.create(prism4j);
            case "csharp":
                return Prism_csharp.create(prism4j);
            case "css":
                return Prism_css.create(prism4j);
            case "css-extras":
                return Prism_css_extras.create(prism4j);
            case "dart":
                return Prism_dart.create(prism4j);
            case "git":
                return Prism_git.create(prism4j);
            case "go":
                return Prism_go.create(prism4j);
            case "groovy":
                return Prism_groovy.create(prism4j);
            case "java":
                return Prism_java.create(prism4j);
            case "javascript":
                return Prism_javascript.create(prism4j);
            case "json":
                return Prism_json.create(prism4j);
            case "kotlin":
                return Prism_kotlin.create(prism4j);
            case "latex":
                return Prism_latex.create(prism4j);
            case "makefile":
                return Prism_makefile.create(prism4j);
            case "markdown":
                return Prism_markdown.create(prism4j);
            case "markup":
                return Prism_markup.create(prism4j);
            case "python":
                return Prism_python.create(prism4j);
            case "scala":
                return Prism_scala.create(prism4j);
            case "sql":
                return Prism_sql.create(prism4j);
            case "swift":
                return Prism_swift.create(prism4j);
            case "yaml":
                return Prism_yaml.create(prism4j);
        }
        return null;
    }

    protected void triggerModify(@NonNull Prism4j prism4j, @NonNull String name) {
        switch (name) {
            case "markup":
                prism4j.grammar("css");
                prism4j.grammar("javascript");
                break;
            case "css":
                prism4j.grammar("css-extras");
                break;
        }
    }
}
