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

import eu.interedition.tei.util.LocalizedStrings;
import eu.interedition.tei.util.XML;
import org.kohsuke.rngom.parse.IllegalSchemaException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Specification implements Identified, Namespaceable, Combinable {

    final String ident;
    final Optional<String> module;
    final URI namespace;
    final Type type;
    final Optional<String> specType;
    final Combinable.EditOperation editOperation;

    final LocalizedStrings descriptions;
    final EditOperation classesEditOperation;
    final LocalizedStrings altIdents;

    final ContentModel content;
    final AttributeList attributes;
    final Map<String, EditOperation> classes;

    public static Specification from(StartElement specElement, XMLEventReader xml) throws XMLStreamException, IllegalSchemaException {
        final LocalizedStrings descriptions = new LocalizedStrings();
        final LocalizedStrings altIdents = new LocalizedStrings();
        final Map<String, EditOperation> classes = new HashMap<>();
        Optional<EditOperation> classesEditOperation = Optional.empty();
        AttributeList attributeList = null;
        ContentModel contentModel = null;

        final QName startName = specElement.getName();
        while (xml.hasNext()) {
            final XMLEvent event = xml.nextEvent();
            if (event.isStartElement()) {
                final StartElement element = event.asStartElement();
                final QName elementName = element.getName();
                if (DEFAULT_NS_STR.equals(elementName.getNamespaceURI())) {
                    switch (elementName.getLocalPart())  {
                        case "classes":
                            classesEditOperation = Combinable.EditOperation.from(element);
                            break;
                        case "memberOf":
                            classes.put(XML.requiredAttr(element, "key"), Combinable.EditOperation.from(element).orElse(EditOperation.ADD));
                            break;
                        case "content":
                            contentModel = ContentModel.parse(xml, "content");
                            break;
                        case "attList":
                            attributeList = AttributeList.parse(element, xml);
                            break;
                        case "altIdent":
                            altIdents.add(element, xml);
                            break;
                        case "desc":
                            descriptions.add(element, xml);
                            break;
                    }
                }
            } else if (event.isEndElement()) {
                if (event.asEndElement().getName().equals(startName)) {
                    break;
                }
            }
        }
        return new Specification(
                specElement,
                descriptions,
                altIdents,
                classesEditOperation.orElse(EditOperation.ADD),
                classes,
                Optional.ofNullable(attributeList).orElse(new AttributeList(false)),
                contentModel
        );
    }

    Specification(StartElement specElement, LocalizedStrings descriptions, LocalizedStrings altIdents, EditOperation classesEditOperation, Map<String, EditOperation> classes, AttributeList attributes, ContentModel content) {
        this.descriptions = descriptions;
        this.altIdents = altIdents;
        this.classesEditOperation = classesEditOperation;
        this.classes = classes;
        this.attributes = attributes;
        this.content = content;
        this.ident = XML.requiredAttr(specElement, "ident");
        this.module = XML.attr(specElement, "module");
        this.namespace = XML.attr(specElement, "ns").map(URI::create).orElse(DEFAULT_NS);
        this.type = Type.from(specElement.getName().getLocalPart());
        this.specType = XML.attr(specElement, "type");
        this.editOperation = Combinable.EditOperation.from(specElement).orElse(EditOperation.ADD);
    }

    @Override
    public String getIdent() {
        return ident;
    }

    public Optional<String> getModule() {
        return module;
    }

    public URI getNamespace() {
        return namespace;
    }

    public Type getType() {
        return type;
    }

    public Optional<String> getSpecType() {
        return specType;
    }

    public LocalizedStrings getDescriptions() {
        return descriptions;
    }

    @Override
    public Combinable.EditOperation getEditOperation() {
        return editOperation;
    }

    public ContentModel getContent() {
        return content;
    }

    public AttributeList getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Specification) {
            return ident.equals(((Specification) obj).ident);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return ident.hashCode();
    }

    @Override
    public String toString() {
        return "Specification[" + ident + "]";
    }

    public static Set<Specification> read(XMLEventReader xml) throws XMLStreamException, TransformerException, IllegalSchemaException {
        final Set<Specification> specifications = new HashSet<>();
        while (xml.hasNext()) {
            final XMLEvent event = xml.nextEvent();
            if (event.isStartElement()) {
                final StartElement startElement = event.asStartElement();
                if (isSpecificationElement(startElement)) {
                    specifications.add(Specification.from(startElement, xml));
                }
            }
        }
        return specifications;
    }

    static boolean isSpecificationElement(StartElement element) {
        return isSpecificationElement(element.getName());
    }

    static boolean isSpecificationElement(QName name) {
        if (DEFAULT_NS_STR.equals(name.getNamespaceURI())) {
            final String localName = name.getLocalPart();
            return ("elementSpec".equals(localName) || "classSpec".equals(localName) || "macroSpec".equals(localName));
        }
        return false;
    }

    public static enum Type {
        ELEMENT, CLASS, MACRO;

        public static Type from(String localName) {
            if ("elementSpec".equals(localName)) {
                return ELEMENT;
            } else if ("classSpec".equals(localName)) {
                return CLASS;
            } else if ("macroSpec".equals(localName)) {
                return MACRO;
            } else {
                throw new IllegalArgumentException(localName);
            }
        }
    }
}
