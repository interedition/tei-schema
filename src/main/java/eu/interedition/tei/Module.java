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

import eu.interedition.tei.util.LocalizedStrings;
import eu.interedition.tei.util.XML;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Module implements Identified {

    final LocalizedStrings descriptions = new LocalizedStrings();
    final String ident;

    public Module(String ident) {
        this.ident = ident;
    }

    @Override
    public String getIdent() {
        return ident;
    }

    public String getModule() {
        return null;
    }

    public LocalizedStrings getDescriptions() {
        return descriptions;
    }

    @Override
    public String toString() {
        return "Module[" + getIdent() + "]";
    }

    public static Map<String, Module> read(File guidelines) throws XMLStreamException {
        final Map<String, Module> modules = new HashMap<>();

        for (final File xmlFile : guidelines.listFiles((dir, name) -> name.endsWith(".xml"))) {
            final XMLEventReader xml = XML.inputFactory().createXMLEventReader(new StreamSource(xmlFile));
            try {
                Module module = null;
                while (xml.hasNext()) {
                    final XMLEvent event = xml.nextEvent();
                    if (event.isStartElement()) {
                        final StartElement element = event.asStartElement();
                        if (XML.hasName(element, Namespaceable.DEFAULT_NS_STR, "moduleSpec")) {
                            final String id = XML.requiredAttributeValue(element, "ident");
                            if (modules.put(id, module = new Module(id)) == null) {
                                throw new IllegalStateException(id + " is not a unique identifier");
                            }
                        } else if (module != null && XML.hasName(element, Namespaceable.DEFAULT_NS_STR, "desc")) {
                            module.getDescriptions().add(element, xml);
                        }
                    } else if (event.isEndElement()) {
                        final EndElement element = event.asEndElement();
                        if (XML.hasName(element, Namespaceable.DEFAULT_NS_STR, "moduleSpec")) {
                            module = null;
                        }
                    }
                }
            } finally {
                xml.close();
            }
        }
        return modules;
    }
}
