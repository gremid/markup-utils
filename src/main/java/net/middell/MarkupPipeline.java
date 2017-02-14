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

import org.codehaus.stax2.LocationInfo;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;

/**
 * @author <a href="http://gregor.middell.net/">Gregor Middell</a>
 */
public class MarkupPipeline {

    public interface Operator {
        Markup[] process(Markup input);
    }

    private final Operator[] operators;

    public void process(Source source, Consumer<Markup> consumer) throws XMLStreamException {
        final XMLStreamReader2 streamReader = (XMLStreamReader2) XML_INPUT_FACTORY.createXMLStreamReader(source);
        try {
            process(streamReader, consumer);
        } finally {
            streamReader.close();
        }
    }
    
    public void process(XMLStreamReader2 reader, Consumer<Markup> consumer) throws XMLStreamException {
        final Deque<Markup.Element> hierarchy = new LinkedList<>();
        final List<Markup> buf = new ArrayList<>();
        while (reader.hasNext()) {
            final int event = reader.next();
            final LocationInfo locationInfo = reader.getLocationInfo();
            final OffsetRange range = new OffsetRange(locationInfo.getStartingCharOffset(), locationInfo.getEndingCharOffset());
            switch (event) {
                case XMLStreamConstants.START_DOCUMENT:
                    process(new Markup.StartDocument(range), consumer, buf);
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    process(new Markup.EndDocument(range), consumer, buf);
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    final AttributesImpl attributes = new AttributesImpl();
                    for (int ac = 0, al = reader.getAttributeCount(); ac < al; ac++) {
                        final String ln = reader.getAttributeLocalName(ac);
                        attributes.addAttribute(
                                Optional.ofNullable(reader.getAttributeNamespace(ac)).orElse(""),
                                ln,
                                Stream.of(reader.getAttributePrefix(ac), ln)
                                        .filter(s -> s != null && !s.isEmpty())
                                        .collect(Collectors.joining(":")),
                                reader.getAttributeType(ac),
                                reader.getAttributeValue(ac)
                        );
                    }
                    final Markup.Element element = new Markup.Element(reader.getNamespaceURI(), reader.getLocalName(), attributes);
                    hierarchy.push(element);
                    process(new Markup.Start(range, element), consumer, buf);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    process(new Markup.End(range, hierarchy.pop()), consumer, buf);
                    break;
                case XMLStreamConstants.CDATA:
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.SPACE:
                    final String text = reader.getText();
                    process(new Markup.Text(range, text), consumer, buf);
                    break;
            }
        }

    }

    private void process(Markup input, Consumer<Markup> output, List<Markup> buf) {
        buf.clear();
        buf.add(input);
        for (Operator op : operators) {
            final Markup[] opInput = buf.toArray(new Markup[buf.size()]);
            buf.clear();
            for (Markup markup : opInput) {
                for (Markup opOutput : op.process(markup)) {
                    buf.add(opOutput);
                }
            }
        }
        buf.forEach(output::accept);
    }

    private MarkupPipeline(Operator[] operators) {
        this.operators = operators;
    }

    public static class Builder {

        private final List<Operator> operators = new LinkedList<>();

        public Builder filter(Predicate<Markup> filter) {
            return addOperator(input -> filter.test(input) ? new Markup[] { input } : EMPTY_RESULT);
        }

        public Builder map(Function<Markup, Markup> mapping) {
            return addOperator(input -> new Markup[] { mapping.apply(input) });
        }

        public Builder addOperator(Operator op) {
            operators.add(op);
            return this;
        }

        public MarkupPipeline build() {
            return new MarkupPipeline(operators.toArray(new Operator[operators.size()]));
        }
    }

    private static final Markup[] EMPTY_RESULT = new Markup[0];

    private static final XMLInputFactory2 XML_INPUT_FACTORY = (XMLInputFactory2) XMLInputFactory2.newInstance();

    static {
        XML_INPUT_FACTORY.configureForRoundTripping();
    }


}
