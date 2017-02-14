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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.xml.XMLConstants;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public interface Whitespace {

    static Function<Markup, Markup> compress(Predicate<Markup> container, Function<Markup, Optional<Boolean>> preserveSpace) {
        return new Function<Markup, Markup>() {

            private final Deque<Boolean> containerContext = new ArrayDeque<>();
            private final Deque<Boolean> spacePreservationContext = new ArrayDeque<>();

            private char lastChar = ' ';

            @Override
            public Markup apply(Markup markup) {
                switch (markup.type) {
                    case START_DOCUMENT:
                        containerContext.clear();
                        spacePreservationContext.clear();
                        lastChar = ' ';
                        break;
                    case START:
                        final Markup.Start markupStart = (Markup.Start) markup;

                        containerContext.push(container.test(markupStart));
                        spacePreservationContext.push(preserveSpace.apply(markupStart).orElseGet(() -> Optional.ofNullable(spacePreservationContext.peek()).orElse(false)));
                        break;
                    case END:
                        containerContext.pop();
                        spacePreservationContext.pop();
                        break;
                    case TEXT:
                        final Markup.Text textMarkup = (Markup.Text) markup;
                        final String text = textMarkup.text;
                        final StringBuilder compressed = new StringBuilder();
                        final boolean preserveSpace = Optional.ofNullable(spacePreservationContext.peek()).orElse(false);
                        for (int cc = 0, length = text.length(); cc < length; cc++) {
                            char currentChar = text.charAt(cc);
                            if (!preserveSpace && Character.isWhitespace(currentChar) && (Character.isWhitespace(lastChar) || (!containerContext.isEmpty() && containerContext.peek()))) {
                                continue;
                            }
                            if (currentChar == '\n' || currentChar == '\r') {
                                currentChar = ' ';
                            }
                            compressed.append(lastChar = currentChar);
                        }
                        return new Markup.Text(textMarkup.sourceRange, compressed.toString());
                }

                return markup;
            }
        };
    }

    static Function<Markup, Markup> breakLines(Predicate<Markup> lineBreak) {
        return new Function<Markup, Markup>() {

            private boolean atStartOfText = true;
            private int introduceBreaks = 0;

            @Override
            public Markup apply(Markup markup) {
                if (markup.type == Markup.Type.START_DOCUMENT) {
                    atStartOfText = true;
                    introduceBreaks = 0;
                }

                if (!atStartOfText && lineBreak.test(markup)) {
                    introduceBreaks++;
                }

                if (markup.type == Markup.Type.TEXT) {
                    final Markup.Text textMarkup = (Markup.Text) markup;
                    final String text = textMarkup.text;

                    if (text.trim().length() == 0) {
                        return markup;
                    }

                    if (!atStartOfText && introduceBreaks > 0) {
                        final StringBuilder sb = new StringBuilder(introduceBreaks + text.length());
                        for (; introduceBreaks > 0; introduceBreaks--) {
                            sb.append("\n");
                        }
                        sb.append(text);
                        return new Markup.Text(textMarkup.sourceRange, sb.toString());
                    }

                    atStartOfText = false;
                }

                return markup;
            }
        };
    }

    static Optional<Boolean> isSpacePreserved(Markup markup) {
        switch (markup.type) {
            case START:
                return Optional.ofNullable(markup.asStart().element.attributes.getValue(XMLConstants.XML_NS_URI, "space"))
                        .map("preserve"::equalsIgnoreCase);
        }
        return Optional.empty();

    }
}
