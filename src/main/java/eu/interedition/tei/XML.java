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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XML {

    private static DocumentBuilderFactory documentBuilderFactory;
    private static TransformerFactory transformerFactory;
    private static SAXParserFactory saxParserFactory;
    private static XMLOutputFactory xmlOutputFactory;
    private static XMLInputFactory xmlInputFactory;

    public static SAXParser saxParser() {
        try {
            if (saxParserFactory == null) {
                saxParserFactory = SAXParserFactory.newInstance();
                saxParserFactory.setNamespaceAware(true);
                saxParserFactory.setValidating(false);
            }
            return saxParserFactory.newSAXParser();
        } catch (ParserConfigurationException e) {
            throw Throwables.propagate(e);
        } catch (SAXException e) {
            throw Throwables.propagate(e);
        }
    }

    public static DocumentBuilderFactory documentBuilderFactory() {
        if (documentBuilderFactory == null) {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setValidating(false);
        }
        return documentBuilderFactory;
    }

    public static DocumentBuilder newDocumentBuilder() {
        try {
            return documentBuilderFactory().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }

    public static TransformerFactory transformerFactory() {
        if (transformerFactory == null) {
            transformerFactory = TransformerFactory.newInstance();
        }
        return transformerFactory;
    }

    public static Transformer newTransformer() {
        try {
            return transformerFactory().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }

    public static Iterable<Node> nodes(final NodeList nodeList) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return new AbstractIterator<Node>() {
                    private int nc = -1;

                    @Override
                    protected Node computeNext() {
                        return (++nc < nodeList.getLength() ? nodeList.item(nc) : endOfData());
                    }
                };
            }
        };
    }

    public static Iterable<Element> elements(final NodeList nodeList) {
        return Iterables.filter(nodes(nodeList), Element.class);
    }

    public static String requiredAttributeValue(Element element, String qname) {
        return Preconditions.checkNotNull(Strings.emptyToNull(element.getAttribute(qname)));
    }

    public static String requiredAttributeValue(StartElement element, String attributeName) {
        return Preconditions.checkNotNull(optionalAttributeValue(element, attributeName));
    }

    public static String optionalAttributeValue(StartElement element, String attributeName) {
        final Attribute attribute = element.getAttributeByName(new QName(attributeName));
        return Strings.emptyToNull(attribute == null ? "" : attribute.getValue());
    }

    public static XMLOutputFactory outputFactory() {
        if (xmlOutputFactory == null) {
            xmlOutputFactory = XMLOutputFactory.newInstance();
            xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        }
        return xmlOutputFactory;
    }

    public static XMLInputFactory inputFactory() {
        if (xmlInputFactory == null) {
            xmlInputFactory = XMLInputFactory.newFactory();
            xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
            xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
            xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        }
        return xmlInputFactory;
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
}
