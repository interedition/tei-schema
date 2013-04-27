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
import com.google.common.collect.Ordering;

import java.util.Comparator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface Identified {

    String getIdent();

    final Function<Identified,String> TO_ID = new Function<Identified, String>() {
        @Override
        public String apply(Identified input) {
            return input.getIdent();
        }
    };

    final Ordering<Identified> ORDERING = Ordering.from(new Comparator<Identified>() {
        @Override
        public int compare(Identified o1, Identified o2) {
            return o1.getIdent().compareTo(o2.getIdent());
        }
    });

}
