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

package eu.interedition.tei.rng;

import org.kohsuke.rngom.digested.*;
import org.kohsuke.rngom.nc.NameClass;
import org.kohsuke.rngom.nc.NameClassVisitor;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NamespaceCollector extends DPatternWalker implements NameClassVisitor<Object> {

    protected final SortedSet<String> namespaces = new TreeSet<>();

    public static NamespaceCollector on(DPattern pattern) {
        final NamespaceCollector namespaceCollector = new NamespaceCollector();
        pattern.accept(namespaceCollector);
        return namespaceCollector;
    }

    public Map<String, String> toMapping() {
        return toMapping(Collections.<String, String>emptyMap());
    }

    public Map<String, String> toMapping(Map<String, String> initial) {
        final Map<String,String> mapping = new HashMap<>(initial);
        int i = 0;
        for (String ns : namespaces) {
            if (!mapping.containsKey(ns)) {
                mapping.put(ns, "ns_" + i++);
            }
        }
        return mapping;
    }

    @Override
    public Void onGrammar(DGrammarPattern p) {
        for (DDefine define : p) {
            final DPattern pattern = define.getPattern();
            if (pattern != null) {
                pattern.accept(this);
            }
        }

        return super.onGrammar(p);
    }

    @Override
    protected Void onXmlToken(DXmlTokenPattern p) {
        p.getName().accept(this);
        return super.onXmlToken(p);
    }

    @Override
    public Void onData(DDataPattern p) {
        final DPattern except = p.getExcept();
        if (except != null) {
            except.accept(this);
        }
        return super.onData(p);
    }

    @Override
    public Void onRef(DRefPattern p) {
        return null;
    }

    @Override
    public Object visitChoice(NameClass nc1, NameClass nc2) {
        nc1.accept(this);
        nc2.accept(this);
        return null;
    }

    @Override
    public Object visitNsName(String ns) {
        namespaces.add(ns);
        return null;
    }

    @Override
    public Object visitNsNameExcept(String ns, NameClass nc) {
        visitNsName(ns);
        nc.accept(this);
        return null;
    }

    @Override
    public Object visitAnyName() {
        return null;
    }

    @Override
    public Object visitAnyNameExcept(NameClass nc) {
        nc.accept(this);
        return null;
    }

    @Override
    public Object visitName(QName name) {
        final String ns = name.getNamespaceURI();
        if (ns != null && !ns.isEmpty()) {
            namespaces.add(ns);
        }
        return null;
    }

    @Override
    public Object visitNull() {
        return null;
    }
}
