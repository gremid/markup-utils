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

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import static net.middell.MarkupPredicates.element;
import static net.middell.MarkupPredicates.localName;
import static net.middell.MarkupPredicates.start;
import static org.junit.Assert.assertTrue;

/**
 * Unit test skeleton.
 */
public class MarkupTest {

    @Test
    public void process() throws Exception {
        final MarkupPipeline pipeline = new MarkupPipeline.Builder()
                .filter(MarkupFilter.contextual(
                        start(element(localName("TEI", "del", "choice"))),
                        start(element(localName("text", "reg", "corr", "expan")))
                ))
                .filter(MarkupFilter.contextual(
                        start(element(localName("note"))),
                        ms -> false
                ))
                .map(Whitespace.compress(
                        start(element(localName("div", "subst", "choice"))),
                        Whitespace::isSpacePreserved
                ))
                .map(Whitespace.breakLines(
                        start(element(localName("head", "div", "p")))
                ))
                .build();

        for (String sample : new String[]{ "/dostoyevsky.xml", "/homer-iliad-tei.xml"}) {
            try (InputStream stream = MarkupTest.class.getResourceAsStream(sample)) {
                final StringBuilder str = new StringBuilder();
                pipeline.process(new StreamSource(stream), markup -> {
                    if (markup.type == Markup.Type.TEXT) {
                        str.append(markup.asText().text);
                    }
                });
                System.out.println(str);
            }
        }
    }


}
