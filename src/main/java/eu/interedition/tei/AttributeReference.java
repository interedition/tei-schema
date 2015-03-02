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

import javax.xml.stream.events.StartElement;
import java.util.Optional;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AttributeReference implements AttributeNode, Identified, Comparable<Identified> {

    final String ident;

    public AttributeReference(StartElement element) {
        this.ident = XML.requiredAttr(element, "name");
    }

    @Override
    public String getIdent() {
        return ident;
    }

    public Optional<String> getModule() {
        return Optional.empty();
    }

    @Override
    public int compareTo(Identified o) {
        return ident.compareTo(o.getIdent());
    }

    @Override
    public int hashCode() {
        return ident.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Identified) {
            return ident.equals(((Identified) obj).getIdent());
        }
        return super.equals(obj);
    }
}
