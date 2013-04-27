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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.HashMap;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class LocalizedStrings extends HashMap<String, String> {

    public static final String DEFAULT_LANG = "en";

    public void set(String locale, String str) {
        put(locale, str.replaceAll("\\s+", " "));
    }

    public void add(StartElement element, XMLEventReader xml) throws XMLStreamException {
        final QName name = element.getName();
        final Attribute lang = element.getAttributeByName(new QName(XMLConstants.XML_NS_URI, "lang"));
        final StringBuilder sb = new StringBuilder();
        while (xml.hasNext()) {
            final XMLEvent event = xml.nextEvent();
            if (event.isEndElement()) {
                if (name.equals(event.asEndElement().getName())) {
                    set(lang == null ? DEFAULT_LANG : lang.getValue(), sb.toString());
                    return;
                }
            } else if (event.isCharacters()) {
                sb.append(event.asCharacters().getData());
            }
        }
    }

    @Override
    public String toString() {
        return get(DEFAULT_LANG);
    }
}
