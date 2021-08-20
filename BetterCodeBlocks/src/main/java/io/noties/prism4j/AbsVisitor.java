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

package io.noties.prism4j;

import androidx.annotation.NonNull;

import java.util.List;

public abstract class AbsVisitor implements Prism4j.Visitor {

    @Override
    public void visit(@NonNull List<? extends Prism4j.Node> nodes) {
        for (Prism4j.Node node : nodes) {
            if (node.isSyntax()) {
                visitSyntax((Prism4j.Syntax) node);
            } else {
                visitText((Prism4j.Text) node);
            }
        }
    }

    protected abstract void visitText(@NonNull Prism4j.Text text);

    // do not forget to call visit(syntax.children()) inside
    protected abstract void visitSyntax(@NonNull Prism4j.Syntax syntax);
}
