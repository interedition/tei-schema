/*
 * Copyright (c) 2015 The Interedition Development Group.
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
 * along with the project. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.tei;

import eu.interedition.tei.util.XML;
import org.kohsuke.rngom.parse.IllegalSchemaException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Schema implements Identified, Namespaceable {

    final String ident;
    final URI namespace;
    final String prefix;
    final Set<String> start;

    final Set<ModuleReference> modules;
    final Set<Reference> elements;
    final Set<Reference> macros;
    final Set<Reference> classes;
    final Map<String, Specification> specifications;

    public static Schema read(InputStream xmlStream) throws XMLStreamException, IllegalSchemaException {
        Optional<String> ident = Optional.empty();
        Optional<String> prefix = Optional.empty();
        Optional<URI> ns = Optional.empty();
        Set<String> start = Collections.emptySet();
        
        final Set<ModuleReference> modules = new TreeSet<>();
        final Set<Reference> elements = new TreeSet<>();
        final Set<Reference> macros = new TreeSet<>();
        final Set<Reference> classes = new TreeSet<>();
        final Set<Specification> specifications = new HashSet<>();

        final XMLEventReader xml = XML.inputFactory().createXMLEventReader(xmlStream);
        try {
            while (xml.hasNext()) {
                final XMLEvent event = xml.nextEvent();
                if (event.isStartElement()) {
                    final StartElement element = event.asStartElement();
                    final QName elementName = element.getName();
                    if (DEFAULT_NS_STR.equals(elementName.getNamespaceURI())) {
                        switch (elementName.getLocalPart()) {
                            case "schemaSpec":
                                ident = XML.attr(element, "ident");
                                prefix = XML.attr(element, "prefix");
                                start = XML.attr(element, "start").map(XML.WS_RUN::splitAsStream).orElse(Stream.<String>empty()).collect(Collectors.<String>toSet());
                                ns = XML.attr(element, "ns").map(URI::create);
                                break;
                            case "moduleRef":
                                if (XML.attr(element, "url").isPresent()) {
                                    throw new UnsupportedOperationException("moduleRef@url");
                                }
                                modules.add(ModuleReference.from(element));
                                break;
                            case "macroRef":
                                macros.add(Reference.from(element));
                                break;
                            case "classRef":
                                classes.add(Reference.from(element));
                                break;
                            case "elementSpec":
                            case "classSpec":
                            case "macroSpec":
                                System.out.println(XML.requiredAttr(element, "ident"));
                                specifications.add(Specification.from(element, xml));
                                break;
                        }
                    }
                }
            }
        } finally {
            xml.close();
        }

        return new Schema(
                ident.orElse(""), prefix.orElse(""), start, ns,
                modules, elements, macros, classes,
                specifications.stream().collect(Collectors.toMap(Specification::getIdent, Function.identity()))
        );
    }

    @Override
    public String getIdent() {
        return ident;
    }

    public Optional<String> getModule() {
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

    private Schema(String ident, String prefix, Set<String> start, Optional<URI> namespace,
                   Set<ModuleReference> modules,
                   Set<Reference> elements,
                   Set<Reference> macros,
                   Set<Reference> classes,
                   Map<String, Specification> specifications) {
        this.ident = ident;
        this.namespace = namespace.orElse(DEFAULT_NS);
        this.prefix = prefix;
        this.start = start;
        this.modules = modules;
        this.elements = elements;
        this.macros = macros;
        this.classes = classes;
        this.specifications = specifications;
    }
}
