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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AttributeList extends ArrayList<AttributeNode> implements AttributeNode {

    final boolean alternative;

    public AttributeList(StartElement element) {
        this(XML.attr(element, "org").filter("choice"::equals).isPresent());
    }

    public AttributeList(boolean alternative) {
        this.alternative = alternative;
    }

    public static AttributeList parse(StartElement attListElement, XMLEventReader xml) throws XMLStreamException, IllegalSchemaException {
        final AttributeList attributeList = new AttributeList(attListElement);
        while (xml.hasNext()) {
            final XMLEvent event = xml.nextEvent();
            if (event.isStartElement()) {
                final StartElement element = event.asStartElement();
                if (XML.hasName(element, Namespaceable.DEFAULT_NS_STR, "attDef")) {
                    attributeList.add(AttributeDefinition.parse(element, xml));
                } else if (XML.hasName(element, Namespaceable.DEFAULT_NS_STR, "attList")) {
                    attributeList.add(AttributeList.parse(element, xml));
                } else if (XML.hasName(element, Namespaceable.DEFAULT_NS_STR, "attRef")) {
                    attributeList.add(new AttributeReference(element));
                }
            } else if (event.isEndElement()) {
                final EndElement element = event.asEndElement();
                if (XML.hasName(element, Namespaceable.DEFAULT_NS_STR, "attList")) {
                    break;
                }
            }
        }
        return attributeList;
    }

}
