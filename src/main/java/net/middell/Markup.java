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

import org.xml.sax.Attributes;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public abstract class Markup {

    public enum Type {
        START_DOCUMENT, END_DOCUMENT, START, END, TEXT;


        @Override
        public String toString() {
            switch (this) {
                case START_DOCUMENT:
                    return "{";
                case END_DOCUMENT:
                    return "}";
                case START:
                    return "<";
                case END:
                    return ">";
                case TEXT:
                    return ".";
            }
            throw new IllegalStateException();
        }
    }

    public final Type type;
    public final OffsetRange sourceRange;

    protected Markup(Type type, OffsetRange sourceRange) {
        this.type = type;
        this.sourceRange = sourceRange;
    }

    public Start asStart() {
        return (Start) this;
    }

    public End asEnd() {
        return (End) this;
    }

    public Text asText() {
        return (Text) this;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", sourceRange, type);
    }

    public static class Element {
        public final String nsUri;
        public final String localName;
        public final Attributes attributes;

        public Element(String nsUri, String localName, Attributes attributes) {
            this.nsUri = nsUri;
            this.localName = localName;
            this.attributes = attributes;
        }

        @Override
        public String toString() {
            return String.format("{%s}%s[%s]", nsUri, localName, toString(attributes));
        }

        protected static String toString(Attributes attributes) {
            final StringBuilder builder = new StringBuilder();
            for (int ac = 0, al = attributes.getLength(); ac < al; ac++) {
                builder.append(String.format("@%s='%s'", attributes.getQName(ac), attributes.getValue(ac)));
                if (builder.length() > 0) {
                    builder.append(", ");
                }
            }
            return builder.toString();
        }
    }

    public static class StartDocument extends Markup {
        protected StartDocument(OffsetRange sourceRange) {
            super(Type.START_DOCUMENT, sourceRange);
        }
    }

    public static class EndDocument extends Markup {
        protected EndDocument(OffsetRange sourceRange) {
            super(Type.END_DOCUMENT, sourceRange);
        }
    }

    public static class Start extends Markup {
        public final Element element;

        public Start(OffsetRange sourceRange, Element element) {
            super(Type.START, sourceRange);
            this.element = element;
        }

        @Override
        public String toString() {
            return super.toString() + " " + element.toString();
        }
    }

    public static class End extends Markup {
        public final Element element;

        public End(OffsetRange sourceRange, Element element) {
            super(Type.END, sourceRange);
            this.element = element;
        }

        @Override
        public String toString() {
            return super.toString() + " " + element.toString();
        }
    }

    public static class Text extends Markup {
        public final String text;

        public Text(OffsetRange sourceRange, String text) {
            super(Type.TEXT, sourceRange);
            this.text = text;
        }

        @Override
        public String toString() {
            return super.toString() + " " + escapeText(text);
        }
    }

    public static String escapeText(String text) {
        return "'" + text.replaceAll("'", "\\'").replaceAll("[\r\n]", "\u00b6") + "'";
    }
}
