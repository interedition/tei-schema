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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.PatternFilenameFilter;
import org.kohsuke.rngom.parse.IllegalSchemaException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Schema implements Identified {
    private static final Logger LOG = Logger.getLogger(Schema.class.getName());

    final String ident;
    final String prefix;
    final Set<String> start;
    final String namespace;

    final Map<String, Module> modules;
    final Set<String> moduleReferences;
    final Map<String, Specification> specifications;

    public Schema(String ident, String prefix, Set<String> start, String namespace, Map<String, Module> modules, Set<String> moduleReferences, Map<String, Specification> specifications) {
        this.ident = ident;
        this.prefix = prefix;
        this.start = start;
        this.namespace = namespace;
        this.modules = modules;
        this.moduleReferences = moduleReferences;
        this.specifications = specifications;
    }

    public Schema(StartElement element, Map<String, Module> modules, Set<String> moduleReferences, Map<String, Specification> specifications) {
        this(
                XML.optionalAttributeValue(element, "ident"),
                XML.optionalAttributeValue(element, "prefix"),
                Sets.newHashSet(Arrays.asList(Objects.firstNonNull(XML.optionalAttributeValue(element, "start"), "").trim().split("\\s+"))),
                XML.optionalAttributeValue(element, "ns"),
                modules,
                moduleReferences,
                specifications
        );
    }

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
            for (Specification specification : Specification.read(xml)) {
                final String id = specification.getIdent();
                Preconditions.checkState(
                        specifications.put(id, specification) == null,
                        id + " is not a unique identifier"
                );
            }
        }

        final Map<String, Module> modules = Module.read(guidelines);

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Read {0} specification(s) and {1} module(s) in {2}", new Object[]{
                    specifications.size(), modules.size(), stopwatch.stop()
            });
        }

        return new Schema(
                "all",
                "tei_",
                Sets.newHashSet("TEI", "teiCorpus"),
                "tei",
                modules,
                Collections.<String>emptySet(),
                specifications
        );
    }

    public static Schema read(XMLEventReader xml) throws XMLStreamException, IllegalSchemaException {
        StartElement schemaSpecElement = null;
        Map<String, Specification> specifications = Maps.newHashMap();
        Set<String> moduleReferences = null;
        while (xml.hasNext()) {
            final XMLEvent event = xml.nextEvent();

            if (event.isStartElement()) {
                final StartElement element = event.asStartElement();
                if (XML.hasName(element, Specification.TEI_NS, "moduleRef")) {
                    moduleReferences.add(XML.requiredAttributeValue(element, "key"));
                } else if (Specification.isSpecificationElement(element)) {
                    final Specification specification = Specification.from(event, xml);
                    specifications.put(specification.getIdent(), specification);
                }
            }
        }

        return (schemaSpecElement == null ? null : new Schema(
                schemaSpecElement,
                Collections.<String, Module>emptyMap(),
                moduleReferences,
                specifications
        ));
    }

    @Override
    public String getIdent() {
        return ident;
    }

    public Set<String> getStart() {
        return start;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNamespace() {
        return namespace;
    }

    public Map<String, Module> getModules() {
        return modules;
    }

    public Map<String, Specification> getSpecifications() {
        return specifications;
    }
}
