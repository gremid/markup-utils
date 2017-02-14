/*
 * Copyright © 2017 Gregor Middell (http://gregor.middell.net/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.middell;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public interface MarkupFilter {

    static Predicate<Markup> contextual(Predicate<Markup> exclude, Predicate<Markup> include) {
        return contextual(exclude, include, ms -> ms.type == Markup.Type.START, ms -> ms.type == Markup.Type.END);
    }

    static Predicate<Markup> contextual(Predicate<Markup> exclude, Predicate<Markup> include, Predicate<Markup> contextStart, Predicate<Markup> contextEnd) {
        return new Predicate<Markup>() {

            private final Deque<Boolean> filterContext = new LinkedList<>();

            @Override
            public boolean test(Markup markup) {
                if (contextStart.test(markup)) {
                    final boolean parentIncluded = (filterContext.isEmpty() ? true : filterContext.peek());
                    filterContext.push(parentIncluded ? !exclude.test(markup) : include.test(markup));
                }

                final boolean accept = (filterContext.isEmpty() || filterContext.peek());

                if (contextEnd.test(markup) && !filterContext.isEmpty()) {
                    filterContext.pop();
                }

                return accept;
            }
        };
    }
}
