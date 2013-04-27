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

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import eu.interedition.tei.util.XML;

import javax.xml.stream.events.StartElement;
import java.net.URI;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ModuleReference extends Reference {
    final Set<Reference> included;
    final Set<Reference> excluded;
    final String prefix;
    final URI url;

    public ModuleReference(String key, URI source, Set<Reference> included, Set<Reference> excluded, String prefix, URI url) {
        super(key, source);
        this.included = included;
        this.excluded = excluded;
        this.prefix = prefix;
        this.url = url;
    }

    public static ModuleReference from(StartElement element) {
        final URI url = XML.toURI(XML.optionalAttributeValue(element, "url"));
        final String urlStr = (url == null ? null : url.toString());
        return new ModuleReference(
                Objects.firstNonNull(XML.optionalAttributeValue(element, "key"), urlStr),
                XML.toURI(XML.optionalAttributeValue(element, "source")),
                Sets.newTreeSet(Iterables.transform(Sets.newHashSet(XML.toList(XML.optionalAttributeValue(element, "included"))), Reference.FROM_STRING)),
                Sets.newTreeSet(Iterables.transform(Sets.newHashSet(XML.toList(XML.optionalAttributeValue(element, "except"))), Reference.FROM_STRING)),
                XML.optionalAttributeValue(element, "prefix"),
                url
        );
    }
}
