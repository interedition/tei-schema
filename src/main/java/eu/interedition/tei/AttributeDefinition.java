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

import org.kohsuke.rngom.parse.IllegalSchemaException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AttributeDefinition implements AttributeNode, Comparable<Identified>, Identified, Combinable {
    final String ident;
    final String namespace;
    final String usage;
    final ContentModel dataType;
    final Values values;
    final LocalizedStrings defaultValues;
    final Combinable.EditOperation editOperation;

    public AttributeDefinition(StartElement element, ContentModel dataType, Values values, LocalizedStrings defaultValues) {
        this.ident = XML.requiredAttributeValue(element, "ident");
        this.usage = XML.optionalAttributeValue(element, "usage");
        this.namespace = XML.optionalAttributeValue(element, "ns");
        this.editOperation = Combinable.EditOperation.from(element);
        this.dataType = dataType;
        this.values = values;
        this.defaultValues = defaultValues;
    }

    public String getIdent() {
        return ident;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getUsage() {
        return usage;
    }

    public ContentModel getDataType() {
        return dataType;
    }

    public LocalizedStrings getDefaultValues() {
        return defaultValues;
    }

    @Override
    public Combinable.EditOperation getEditOperation() {
        return editOperation;
    }

    @Override
    public int compareTo(Identified o) {
        return ident.compareTo(o.getIdent());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Identified) {
            return ident.equals(((Identified) obj).getIdent());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return ident.hashCode();
    }

    public static AttributeDefinition parse(StartElement attDefElement, XMLEventReader xml) throws XMLStreamException, IllegalSchemaException {
        final LocalizedStrings defaultValues = new LocalizedStrings();
        Values values = null;
        ContentModel dataType = null;
        while (xml.hasNext()) {
            final XMLEvent event = xml.nextEvent();
            if (event.isStartElement()) {
                final StartElement element = event.asStartElement();
                if (XML.hasName(element, Specification.TEI_NS, "datatype")) {
                    dataType = ContentModel.parse(xml, "datatype");
                } else if (XML.hasName(element, Specification.TEI_NS, "defaultVal")) {
                    defaultValues.add(element, xml);
                } else if (XML.hasName(element, Specification.TEI_NS, "valList")) {
                    values = Values.parse(element, xml);
                }
            } else if (event.isEndElement()) {
                if (XML.hasName(event.asEndElement(), Specification.TEI_NS, "attDef")) {
                    break;
                }
            }
        }
        return new AttributeDefinition(attDefElement, dataType, values, defaultValues);
    }
}
