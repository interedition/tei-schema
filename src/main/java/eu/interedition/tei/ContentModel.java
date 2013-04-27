/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of TEI Schema Tools.
 *
 * TEI Schema Tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TEI Schema Tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.tei;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.kohsuke.rngom.digested.DDefine;
import org.kohsuke.rngom.digested.DElementPattern;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DPatternWalker;
import org.kohsuke.rngom.digested.DRefPattern;
import org.kohsuke.rngom.digested.DSchemaBuilderImpl;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.xml.SAXParseable;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import static org.kohsuke.rngom.xml.util.WellKnownNamespaces.RELAX_NG;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ContentModel {
    public static final String CONTENT_MODEL_DEF = "_fragment";

    final DPattern grammar;
    final DPattern root;

    private ContentModel(DPattern grammar, DPattern root) {
        this.grammar = grammar;
        this.root = root;
    }

    public SortedSet<String> getReferences() {
        final SortedSet<String> references = Sets.newTreeSet();
        Objects.firstNonNull(root, grammar).accept(new DPatternWalker() {
            @Override
            public Void onElement(DElementPattern p) {
                for (QName name : p.getName().listNames()) {
                    final String ns = name.getNamespaceURI();
                    if (!Strings.isNullOrEmpty(ns) && !Specification.TEI_NS.equals(ns)) {
                        continue;
                    }
                    references.add(name.getLocalPart());
                }
                return super.onElement(p);
            }

            @Override
            public Void onRef(DRefPattern p) {
                final String refName = p.getName();
                if (!references.contains(refName)) {
                    references.add(refName);
                    final DPattern target = p.getTarget().getPattern();
                    if (target != null) {
                        return target.accept(this);
                    }
                }
                return null;
            }
        });
        return references;
    }

    @Override
    public String toString() {
        return RelaxCompactSerializer.toString(Objects.firstNonNull(root, grammar));
    }

    public static ContentModel parse(XMLEventReader xml, String containerName) throws XMLStreamException, IllegalSchemaException {
        final StringWriter schema = new StringWriter();
        final XMLEventFactory xef = XMLEventFactory.newFactory();

        final XMLEventWriter grammar = XML.outputFactory().createXMLEventWriter(new StreamResult(schema));
        try {
            grammar.add(xef.createStartDocument());
            grammar.add(xef.createStartElement("", RELAX_NG, "grammar"));
            grammar.add(xef.createStartElement("", RELAX_NG, "define",
                    Collections.singleton(xef.createAttribute("name", CONTENT_MODEL_DEF)).iterator(),
                    Collections.emptySet().iterator()));

            while (xml.hasNext()) {
                final XMLEvent event = xml.nextEvent();
                if (event.isEndElement()) {
                    if (XML.hasName(event.asEndElement(), Specification.TEI_NS, containerName)) {
                        break;
                    }
                }
                grammar.add(event);
            }

            grammar.add(xef.createEndElement("", RELAX_NG, "define"));

            grammar.add(xef.createStartElement("", RELAX_NG, "start"));
            grammar.add(xef.createStartElement("", RELAX_NG, "ref",
                    Collections.singleton(xef.createAttribute("name", CONTENT_MODEL_DEF)).iterator(),
                    Collections.emptySet().iterator()));
            grammar.add(xef.createEndElement("", RELAX_NG, "ref"));
            grammar.add(xef.createEndElement("", RELAX_NG, "start"));

            grammar.add(xef.createEndElement("", RELAX_NG, "grammar"));
            grammar.add(xef.createEndDocument());
        } finally {
            grammar.close();
        }

        final DPattern grammarPattern = (DPattern) new SAXParseable(
                new InputSource(new StringReader(schema.toString())),
                XML.STRICT_ERROR_HANDLER
        ).parse(new DSchemaBuilderImpl());

        final List<DPattern> root = Lists.newArrayList();
        grammarPattern.accept(new DPatternWalker() {
            @Override
            public Void onRef(DRefPattern p) {
                final DDefine target = p.getTarget();
                if (target != null && CONTENT_MODEL_DEF.equals(p.getName())) {
                    root.add(target.getPattern());
                }
                return null;
            }
        });

        return new ContentModel(grammarPattern, Iterables.getFirst(root, null));
    }
}
