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

import javax.xml.stream.events.StartElement;

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

        public static EditOperation from(StartElement element) {
            final String mode = XML.optionalAttributeValue(element, "mode");
            if (mode == null) {
                return null;
            }
            if ("change".equals(mode)) {
                return CHANGE;
            } else if ("add".equals(mode)) {
                return ADD;
            } else if ("delete".equals(mode)) {
                return DELETE;
            } else if ("replace".equals(mode)) {
                return REPLACE;
            }
            throw new IllegalArgumentException(mode);
        }
    }
}
