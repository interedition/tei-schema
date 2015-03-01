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
import org.kohsuke.rngom.xml.util.WellKnownNamespaces;

import javax.xml.namespace.QName;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RelaxCompactSerializer implements DPatternVisitor<Void>, NameClassVisitor<Void> {

    @SuppressWarnings("SpellCheckingInspection")
    private final Set<String> KEYWORDS = new HashSet<>(Arrays.asList("attribute", "default", "datatypes", "div", "element", "empty",
            "external", "grammar", "include", "inherit", "list", "mixed", "namespace", "notAllowed", "parent", "start",
            "string", "text", "token"));

    private final PrintWriter target;
    private Map<String, String> namespaces = new HashMap<>();

    public RelaxCompactSerializer(PrintWriter target) {
        this.target = target;
    }

    public RelaxCompactSerializer(Writer target) {
        this(new PrintWriter(target));
    }

    public static String toString(DPattern p) {
        final StringWriter writer = new StringWriter();
        p.accept(new RelaxCompactSerializer(writer));
        return writer.toString();
    }

    @Override
    public Void onGrammar(DGrammarPattern p) {
        mapNamespaces(p);

        target.println("grammar {");
        for (DDefine define : p) {
            final DPattern pattern = define.getPattern();
            if (pattern != null) {
                identifier(define.getName());
                target.print(" = ");
                pattern.accept(this);
                target.println();
            }
        }

        final DPattern start = p.getStart();
        if (start != null) {
            target.print("start = ");
            start.accept(this);
            target.println();
        }

        target.println("}");

        return null;
    }

    protected void mapNamespaces(DGrammarPattern p) {
        namespaces = NamespaceCollector.on(p).toMapping();
        if (!namespaces.isEmpty()) {
            for (Map.Entry<String, String> mapping : namespaces.entrySet()) {
                target.print("namespace ");
                target.print(mapping.getValue());
                target.print(" = ");
                literal(mapping.getKey());
                target.println();
            }
        }
    }

    @Override
    public Void onGroup(DGroupPattern p) {
        concat(p, ", ");
        return null;
    }

    @Override
    public Void onInterleave(DInterleavePattern p) {
        concat(p, " & ");
        return null;
    }

    @Override
    public Void onChoice(DChoicePattern p) {
        concat(p, " | ");
        return null;
    }

    @Override
    public Void onOneOrMore(DOneOrMorePattern p) {
        p.getChild().accept(this);
        target.print("+");
        return null;
    }

    @Override
    public Void onOptional(DOptionalPattern p) {
        p.getChild().accept(this);
        target.print("?");
        return null;
    }

    @Override
    public Void onZeroOrMore(DZeroOrMorePattern p) {
        p.getChild().accept(this);
        target.print("*");
        return null;
    }

    @Override
    public Void onList(DListPattern p) {
        target.print("list { ");
        p.getChild().accept(this);
        target.print(" }");
        return null;
    }

    @Override
    public Void onMixed(DMixedPattern p) {
        target.print("mixed { ");
        p.getChild().accept(this);
        target.print(" }");
        return null;
    }

    @Override
    public Void onRef(DRefPattern p) {
        identifier(p.getName());
        return null;
    }

    @Override
    public Void onEmpty(DEmptyPattern p) {
        target.print("empty");
        return null;
    }

    @Override
    public Void onText(DTextPattern p) {
        target.print("text");
        return null;
    }

    @Override
    public Void onNotAllowed(DNotAllowedPattern p) {
        target.print("notAllowed");
        return null;
    }

    @Override
    public Void onElement(DElementPattern p) {
        target.print("element ");
        p.getName().accept(this);
        target.print(" { ");
        p.getChild().accept(this);
        target.print(" }");
        return null;
    }

    @Override
    public Void onAttribute(DAttributePattern p) {
        target.print("attribute ");
        p.getName().accept(this);
        target.print(" { ");
        p.getChild().accept(this);
        target.print(" }");
        return null;
    }

    @Override
    public Void onData(DDataPattern p) {
        dataType(p.getDatatypeLibrary(), p.getType());

        final List<DDataPattern.Param> params = p.getParams();
        if (!params.isEmpty()) {
            target.print(" { ");
            for (DDataPattern.Param param : params) {
                target.print(param.getName());
                target.print("=");
                literal(param.getValue());
                target.print(" ");
            }

            target.print("}");
        }

        final DPattern except = p.getExcept();
        if (except != null) {
            target.print(" - ");
            except.accept(this);
        }

        return null;
    }

    private void dataType(String library, String type) {
        if (type != null) {
            if (library.isEmpty() || WellKnownNamespaces.XML_SCHEMA_DATATYPES.equals(library)) {
                target.print("xsd:");
            } else {
               throw new UnsupportedOperationException(library);
            }
            target.print(type);
        }
    }

    @Override
    public Void onValue(DValuePattern p) {
        dataType(p.getDatatypeLibrary(), p.getType());
        target.print(" ");
        literal(p.getValue());
        return null;
    }

    @Override
    public Void visitChoice(NameClass nc1, NameClass nc2) {
        target.print("(");
        nc1.accept(this);
        target.print(" | ");
        nc2.accept(this);
        target.print(")");
        return null;
    }

    @Override
    public Void visitNsName(String ns) {
        namespace(ns);
        target.print(":*");
        return null;
    }

    @Override
    public Void visitNsNameExcept(String ns, NameClass nc) {
        visitNsName(ns);
        target.print("-");
        nc.accept(this);
        return null;
    }

    @Override
    public Void visitAnyName() {
        target.print("*");
        return null;
    }

    @Override
    public Void visitAnyNameExcept(NameClass nc) {
        visitAnyName();
        target.print("-");
        nc.accept(this);
        return null;
    }

    @Override
    public Void visitName(QName name) {
        final String ns = name.getNamespaceURI();
        if (ns != null && !ns.isEmpty()) {
            namespace(ns);
            target.print(":");
        }
        target.print(name.getLocalPart());
        return null;
    }

    @Override
    public Void visitNull() {
        return null;
    }

    protected void namespace(String ns) {
        final String prefix = namespaces.get(ns);
        target.print(prefix == null ? "ns" : prefix);
    }

    protected void identifier(String identifier) {
        if (KEYWORDS.contains(identifier)) {
            target.print("\\");
        }
        target.print(identifier);
    }

    protected void literal(String value) {
        final boolean singleQuotes = value.indexOf('\'') >= 0;
        final boolean doubleQuotes = value.indexOf('\"') >= 0;

        if (singleQuotes && doubleQuotes) {
            throw new UnsupportedOperationException("FIXME Literal: " + value);
        }

        final String quote = (singleQuotes ? "\"" : "'");
        final String delimiter = IntStream.range(0, value.contains("\n") ? 3 : 1).mapToObj(i -> quote).collect(Collectors.joining());
        target.print(delimiter);
        target.print(value);
        target.print(delimiter);
    }


    protected void concat(Iterable<DPattern> patterns, String op) {
        final long numPatterns = StreamSupport.stream(patterns.spliterator(), false).count();
        if (numPatterns > 1) {
            target.print("(");
        }
        for (Iterator<DPattern> it = patterns.iterator(); it.hasNext(); ) {
            it.next().accept(this);
            if (it.hasNext()) {
                target.print(op);
            }
        }
        if (numPatterns > 1) {
            target.print(")");
        }
    }
}
