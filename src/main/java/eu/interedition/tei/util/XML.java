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

package eu.interedition.tei.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XML {

    private static XMLOutputFactory outputFactory;
    private static XMLInputFactory inputFactory;

    public static String requiredAttributeValue(StartElement element, String attributeName) {
        return Objects.requireNonNull(optionalAttributeValue(element, attributeName));
    }

    public static String optionalAttributeValue(StartElement element, String attributeName) {
        final Attribute attribute = element.getAttributeByName(new QName(attributeName));
        return Optional.ofNullable(attribute).map(Attribute::getValue).filter(s -> !s.isEmpty()).orElse(null);
    }

    public static XMLOutputFactory outputFactory() {
        if (outputFactory == null) {
            outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        }
        return outputFactory;
    }

    public static XMLInputFactory inputFactory() {
        if (inputFactory == null) {
            inputFactory = XMLInputFactory.newFactory();
            inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
            inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
            inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
            inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        }
        return inputFactory;
    }

    public static final ErrorHandler STRICT_ERROR_HANDLER = new ErrorHandler() {
        @Override
        public void warning(SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }
    };

    public static boolean hasName(StartElement element, String ns, String localName) {
        return equals(element.getName(), ns, localName);
    }

    public static boolean hasName(EndElement element, String ns, String localName) {
        return equals(element.getName(), ns, localName);
    }

    public static boolean equals(QName name, String ns, String localName) {
        return localName.equals(name.getLocalPart()) && (ns == null || ns.equals(name.getNamespaceURI()));
    }

    public static URI toURI(String str) {
        return (str == null || str.isEmpty() ? null : URI.create(str));
    }

    public static List<String> toList(String str) {
        return Arrays.asList(Optional.ofNullable(str).orElse("").split("\\s+"));
    }
}
