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
import java.util.ArrayList;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Values extends ArrayList<Values.Item> implements Combinable {
    final Type type;
    final Combinable.EditOperation editOperation;

    public Values(Type type, Combinable.EditOperation editOperation) {
        this.type = type;
        this.editOperation = editOperation;
    }

    public Type getType() {
        return type;
    }

    @Override
    public EditOperation getEditOperation() {
        return editOperation;
    }

    public static Values parse(StartElement valList, XMLEventReader xml) throws XMLStreamException, IllegalSchemaException {
        final Values values = new Values(
                XML.attr(valList, "type").map(Type::from).orElse(Type.OPEN),
                Combinable.EditOperation.from(valList).orElse(EditOperation.ADD)
        );

        final QName startName = valList.getName();
        while (xml.hasNext()) {
            final XMLEvent event = xml.nextEvent();
            if (event.isStartElement()) {
                final StartElement element = event.asStartElement();
                if (XML.hasName(element, Namespaceable.DEFAULT_NS_STR, "valItem")) {
                    values.add(Item.from(element, xml));
                }
            } else if (event.isEndElement()) {
                if (event.asEndElement().getName().equals(startName)) {
                    break;
                }
            }
        }
        return values;
    }

    public static class Item implements Combinable {
        final LocalizedStrings descriptions = new LocalizedStrings();
        final LocalizedStrings definitions = new LocalizedStrings();
        final LocalizedStrings altIdents = new LocalizedStrings();
        final String ident;
        final EditOperation editOperation;


        public Item(String ident, EditOperation editOperation) {
            this.ident = ident;
            this.editOperation = editOperation;
        }

        public static Item from(StartElement itemElement, XMLEventReader xml) throws XMLStreamException {
            final Item item = new Item(
                    XML.requiredAttr(itemElement, "ident"),
                    EditOperation.from(itemElement).orElse(EditOperation.ADD)
            );
            final QName startName = itemElement.getName();
            while (xml.hasNext()) {
                final XMLEvent event = xml.nextEvent();
                if (event.isStartElement()) {
                    final StartElement element = event.asStartElement();
                    if (XML.hasName(element, Namespaceable.DEFAULT_NS_STR, "desc")) {
                        item.descriptions.add(element, xml);
                    } else if (XML.hasName(element, Namespaceable.DEFAULT_NS_STR, "gloss")) {
                        item.definitions.add(element, xml);
                    } else if (XML.hasName(element, Namespaceable.DEFAULT_NS_STR, "altIdent")) {
                        item.altIdents.add(element, xml);
                    }
                } else if (event.isEndElement()) {
                    if (event.asEndElement().getName().equals(startName)) {
                        break;
                    }
                }
            }
            return item;
        }

        @Override
        public EditOperation getEditOperation() {
            return editOperation;
        }
    }

    public static enum Type {
        CLOSED, SEMI, OPEN;

        static Type from(String type) {
            if (type == null) {
                return null;
            } else if ("closed".equals(type)) {
                return CLOSED;
            } else if ("semi".equals(type)) {
                return SEMI;
            } else if ("open".equals(type)) {
                return OPEN;
            }
            throw new IllegalArgumentException(type);
        }
    }
}
