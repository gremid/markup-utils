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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public interface MarkupPredicates {

    static Predicate<Markup> start(Predicate<Markup> start) {
        return (markup -> markup.type == Markup.Type.START ? start.test(markup) : false);
    }

    static Predicate<Markup> element(Predicate<Markup.Element> element) {
        return markup -> {
            switch (markup.type) {
                case START:
                    return element.test(((Markup.Start) markup).element);
                case END:
                    return element.test(((Markup.End) markup).element);
                case TEXT:
                    return false;
            }
            throw new IllegalArgumentException(markup.toString());
        };
    }

    static Predicate<Markup.Element> localName(String... localNames) {
        final Set<String> localNameSet = new HashSet<>(Arrays.asList(localNames));
        return element -> localNameSet.contains(element.localName);
    }
}
