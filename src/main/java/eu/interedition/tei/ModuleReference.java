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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ModuleReference extends Reference {
    final Set<Reference> included;
    final Set<Reference> excluded;
    final Optional<String> prefix;

    public ModuleReference(String key, URI source, Set<Reference> included, Set<Reference> excluded, Optional<String> prefix) {
        super(key, Optional.of(source));
        this.included = included;
        this.excluded = excluded;
        this.prefix = prefix;
    }

    public static ModuleReference from(StartElement element) {
        return new ModuleReference(
                XML.requiredAttr(element, "key"),
                XML.attr(element, "source").map(URI::create).orElse(null),
                XML.attr(element, "included").map(ref -> XML.WS_RUN.splitAsStream(ref).map(Reference::new)).orElse(Stream.<Reference>empty()).collect(Collectors.toCollection(TreeSet::new)),
                XML.attr(element, "except").map(ref -> XML.WS_RUN.splitAsStream(ref).map(Reference::new)).orElse(Stream.<Reference>empty()).collect(Collectors.toCollection(TreeSet::new)),
                XML.attr(element, "prefix")
        );
    }
}
