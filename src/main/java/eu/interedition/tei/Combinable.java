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
public interface Combinable {

    EditOperation getEditOperation();

    /**
     * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
     */
    enum EditOperation {
        ADD, CHANGE, REPLACE, DELETE;

        public static Optional<EditOperation> from(StartElement element) {
            return XML.attr(element, "mode").map(mode -> {
                switch (mode) {
                    case "change":
                        return CHANGE;
                    case "add":
                        return ADD;
                    case "delete":
                        return DELETE;
                    case "replace":
                        return REPLACE;
                    default:
                        throw new IllegalArgumentException(mode);
                }
            });
        }
    }
}
