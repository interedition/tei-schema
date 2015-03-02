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
import java.net.URI;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Reference implements Comparable<Reference> {

    final String key;
    final Optional<URI> source;

    public static Reference from(StartElement element) {
        return new Reference(
                XML.requiredAttr(element, "key"),
                XML.attr(element, "source").map(URI::create)
        );
    }

    public Reference(String key) {
        this(key, Optional.empty());
    }

    public Reference(String key, Optional<URI> source) {
        this.key = key;
        this.source = source;
    }


    @Override
    public int hashCode() {
        return Objects.hash(key, source);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Reference) {
            Reference other = (Reference) obj;
            return Objects.equals(key, other.key) && Objects.equals(source, other.source);
        }
        return super.equals(obj);
    }

    @Override
    public int compareTo(Reference o) {
        return COMPARATOR.compare(this, o);
    }

    private static final Comparator<Reference> COMPARATOR = Comparator.comparing((Reference r) -> r.key)
            .thenComparing(Comparator.nullsFirst(Comparator.comparing(r -> r.source.orElse(null))));
}
