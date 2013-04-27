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
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.kohsuke.rngom.parse.IllegalSchemaException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import java.util.Map;
import java.util.Set;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Specification implements Identified, Combinable {

    public static final String TEI_NS = "http://www.tei-c.org/ns/1.0";

    final String ident;
    final String module;
    final Type type;
    final String specType;
    final Combinable.EditOperation editOperation;

    final LocalizedStrings descriptions;
    final EditOperation classesEditOperation;
    final LocalizedStrings altIdents;

    final ContentModel content;
    final AttributeList attributes;
    final Map<String, EditOperation> classes;

    public static Specification from(XMLEvent event, XMLEventReader xml) throws XMLStreamException, IllegalSchemaException {
        final StartElement specElement = event.asStartElement();

        final LocalizedStrings descriptions = new LocalizedStrings();
        final LocalizedStrings altIdents = new LocalizedStrings();
        final Map<String, EditOperation> classes = Maps.newHashMap();
        EditOperation classesEditOperation = null;
        AttributeList attributeList = null;
        ContentModel contentModel = null;

        while (xml.hasNext()) {
            event = xml.nextEvent();
            if (event.isStartElement()) {
                final StartElement element = event.asStartElement();
                if (XML.hasName(element, TEI_NS, "classes")) {
                    classesEditOperation = Combinable.EditOperation.from(element);
                } else if (XML.hasName(element, TEI_NS, "memberOf")) {
                    classes.put(XML.requiredAttributeValue(element, "key"), Combinable.EditOperation.from(element));
                } else if (XML.hasName(element, TEI_NS, "content")) {
                    contentModel = ContentModel.parse(xml, "content");
                } else if (XML.hasName(element, TEI_NS, "attList")) {
                    attributeList = AttributeList.parse(element, xml);
                } else if (XML.hasName(element, TEI_NS, "altIdent")) {
                    altIdents.add(element, xml);
                } else if (XML.hasName(element, TEI_NS, "desc")) {
                    descriptions.add(element, xml);
                }
            } else if (event.isEndElement()) {
                final EndElement element = event.asEndElement();
                if (isSpecificationElement(element)) {
                    break;
                }
            }
        }
        return new Specification(
                specElement,
                descriptions,
                altIdents,
                classesEditOperation,
                classes,
                Objects.firstNonNull(attributeList, new AttributeList(false)),
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
        this.ident = XML.requiredAttributeValue(specElement, "ident");
        this.module = XML.requiredAttributeValue(specElement, "module");
        this.type = Type.from(specElement.getName().getLocalPart());
        this.specType = XML.optionalAttributeValue(specElement, "type");
        this.editOperation = Combinable.EditOperation.from(specElement);
    }
    public String getModule() {
        return module;
    }

    @Override
    public String getIdent() {
        return ident;
    }

    public Type getType() {
        return type;
    }

    public String getSpecType() {
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
        return Objects.toStringHelper(this).addValue(ident).toString();
    }

    public static Set<Specification> read(XMLEventReader xml) throws XMLStreamException, TransformerException, IllegalSchemaException {
        final Set<Specification> specifications = Sets.newHashSet();
        while (xml.hasNext()) {
            final XMLEvent event = xml.nextEvent();
            if (event.isStartElement()) {
                if (isSpecificationElement(event.asStartElement())) {
                    specifications.add(Specification.from(event, xml));
                }
            }
        }
        return specifications;
    }

    static boolean isSpecificationElement(StartElement element) {
        return isSpecificationElement(element.getName());
    }

    static boolean isSpecificationElement(EndElement element) {
        return isSpecificationElement(element.getName());
    }

    static boolean isSpecificationElement(QName name) {
        if (TEI_NS.equals(name.getNamespaceURI())) {
            final String localName = name.getLocalPart();
            return ("elementSpec".equals(localName) ||  "classSpec".equals(localName) || "macroSpec".equals(localName));
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

        public Predicate<Specification> predicate() {
            return new Predicate<Specification>() {
                @Override
                public boolean apply(Specification input) {
                    return Type.this.equals(input.getType());
                }
            };
        }
    }
}
