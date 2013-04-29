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
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.PatternFilenameFilter;
import eu.interedition.tei.util.XML;
import org.kohsuke.rngom.parse.IllegalSchemaException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Schema implements Identified, Namespaceable {
    private static final Logger LOG = Logger.getLogger(Schema.class.getName());

    final String ident;
    final URI namespace;
    final String prefix;
    final Set<String> start;

    final Set<ModuleReference> modules;
    final Set<Reference> elements;
    final Set<Reference> macros;
    final Set<Reference> classes;
    final Map<String, Specification> specifications;

    public static Schema read(File sourceRoot) throws XMLStreamException, TransformerException, IllegalSchemaException {
        final File specs = new File(sourceRoot, "Specs");
        final File guidelines = new File(sourceRoot, "Guidelines/en");

        Preconditions.checkArgument(specs.isDirectory(), specs + " is not a directory");
        Preconditions.checkArgument(guidelines.isDirectory(), guidelines + " is not a directory");

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Reading TEI schema from {0}", sourceRoot);
        }

        final Stopwatch stopwatch = new Stopwatch().start();

        final Map<String, Specification> specifications = Maps.newHashMap();
        final XMLInputFactory xmlInputFactory = XML.inputFactory();

        for (final File specFile : specs.listFiles(new PatternFilenameFilter(".+\\.xml$"))) {
            final XMLEventReader xml = xmlInputFactory.createXMLEventReader(new StreamSource(specFile));
            try {
                for (Specification specification : Specification.read(xml)) {
                    final String id = specification.getIdent();
                    Preconditions.checkState(
                            specifications.put(id, specification) == null,
                            id + " is not a unique identifier"
                    );
                }
            } finally {
                xml.close();
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Read {0} specification(s) from {1} in {2}", new Object[]{
                    specifications.size(),
                    sourceRoot,
                    stopwatch.stop()
            });
        }

        return new Schema(
                "all",
                "tei_",
                Sets.newHashSet("TEI", "teiCorpus"),
                Namespaceable.DEFAULT_NS,
                Collections.<ModuleReference>emptySet(),
                Collections.<Reference>emptySet(),
                Collections.<Reference>emptySet(),
                Collections.<Reference>emptySet(),
                specifications
        );
    }

    public static Schema read(XMLEventReader xml) throws XMLStreamException, IllegalSchemaException {
        Schema schema = null;

        final Map<String, Specification> specifications = Maps.newHashMap();
        final Set<ModuleReference> modules = Sets.newTreeSet();
        final Set<Reference> elements = Sets.newTreeSet();
        final Set<Reference> macros = Sets.newTreeSet();
        final Set<Reference> classes = Sets.newTreeSet();
        while (xml.hasNext()) {
            final XMLEvent event = xml.nextEvent();

            if (event.isStartElement()) {
                final StartElement element = event.asStartElement();
                if (XML.hasName(element, DEFAULT_NS_STR, "schemaSpec")) {
                    Preconditions.checkState(schema == null, "Multiple <schemaSpec/> elements");
                    schema = new Schema(element, modules, elements, macros, classes, specifications);
                } else if (XML.hasName(element, DEFAULT_NS_STR, "moduleRef")) {
                    if (!Strings.isNullOrEmpty(XML.optionalAttributeValue(element, "url"))) {
                        throw new UnsupportedOperationException("moduleRef@url");
                    }
                    modules.add(ModuleReference.from(element));
                } else if (XML.hasName(element, DEFAULT_NS_STR, "elementRef")) {
                    elements.add(Reference.from(element));
                } else if (XML.hasName(element, DEFAULT_NS_STR, "macroRef")) {
                    macros.add(Reference.from(element));
                } else if (XML.hasName(element, DEFAULT_NS_STR, "classRef")) {
                    classes.add(Reference.from(element));
                } else if (Specification.isSpecificationElement(element)) {
                    final Specification specification = Specification.from(event, xml);
                    specifications.put(specification.getIdent(), specification);
                }
            }
        }

        return schema;
    }

    @Override
    public String getIdent() {
        return ident;
    }

    public String getModule() {
        return null;
    }

    public Set<String> getStart() {
        return start;
    }

    public String getPrefix() {
        return prefix;
    }

    public URI getNamespace() {
        return namespace;
    }

    public Map<String, Specification> getSpecifications() {
        return specifications;
    }

    private Schema(String ident, String prefix, Set<String> start, URI namespace,
                   Set<ModuleReference> modules,
                   Set<Reference> elements,
                   Set<Reference> macros,
                   Set<Reference> classes,
                   Map<String, Specification> specifications) {
        this.ident = ident;
        this.namespace = Objects.firstNonNull(namespace, DEFAULT_NS);
        this.prefix = prefix;
        this.start = start;
        this.modules = modules;
        this.elements = elements;
        this.macros = macros;
        this.classes = classes;
        this.specifications = specifications;
    }

    private Schema(StartElement element,
                   Set<ModuleReference> modules,
                   Set<Reference> elements,
                   Set<Reference> macros,
                   Set<Reference> classes,
                   Map<String, Specification> specifications) {
        this(
                XML.optionalAttributeValue(element, "ident"),
                XML.optionalAttributeValue(element, "prefix"),
                Sets.newHashSet(XML.toList(XML.optionalAttributeValue(element, "start"))),
                XML.toURI(XML.optionalAttributeValue(element, "ns")),
                modules,
                elements,
                macros,
                classes,
                specifications
        );
    }

}
