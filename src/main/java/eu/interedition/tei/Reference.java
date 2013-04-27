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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import eu.interedition.tei.util.XML;

import javax.xml.stream.events.StartElement;
import java.net.URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Reference implements Comparable<Reference> {

    final String key;
    final URI source;

    public static Reference from(StartElement element) {
        return new Reference(
                XML.requiredAttributeValue(element, "key"),
                XML.toURI(XML.optionalAttributeValue(element, "source"))
        );
    }

    public Reference(String key, URI source) {
        this.key = key;
        this.source = source;
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(key, source);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Reference) {
            Reference other = (Reference) obj;
            return Objects.equal(key, other.key) && Objects.equal(source, other.source);
        }
        return super.equals(obj);
    }

    @Override
    public int compareTo(Reference o) {
        return ComparisonChain.start().compare(key, o.key).compare(source, o.source, Ordering.natural().nullsFirst()).result();
    }

    public static final Function<String, Reference> FROM_STRING = new Function<String, Reference>() {
        @Override
        public Reference apply(String input) {
            return new Reference(input, null);
        }
    };
}
