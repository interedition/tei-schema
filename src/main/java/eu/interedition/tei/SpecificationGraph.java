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

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SpecificationGraph {

    final Schema schema;
    final SortedMap<String, SortedSet<String>> moduleMembers = new TreeMap<>();
    final SortedMap<String, SortedSet<String>> classMembers = new TreeMap<>();
    final SortedMap<String, SortedSet<String>> specificationDependencies = new TreeMap<>();
    final SortedMap<String, SortedSet<String>> moduleDependencies = new TreeMap<>();

    public static SpecificationGraph create(Schema schema) {
        final SpecificationGraph graph = new SpecificationGraph(schema);

        for (Specification specification : schema.specifications.values()) {
            final String module = specification.getModule();
            if (module != null) {
                graph.moduleMembers.computeIfAbsent(module, m -> new TreeSet<>()).add(specification.getIdent());
            }
            final String id = specification.getIdent();
            for (String classMembership : specification.classes.keySet()) {
                graph.classMembers.computeIfAbsent(classMembership, m -> new TreeSet<>()).add(id);
            }
        }

        for (Specification spec : schema.specifications.values()) {
            final String id = spec.getIdent();
            if (Specification.Type.CLASS.equals(spec.getType())) {
                for (String member : graph.classMembers.get(id)) {
                    final int order = id.compareTo(member);
                    if (order < 0) {
                        graph.specificationDependencies.computeIfAbsent(id, k -> new TreeSet<>()).add(member);
                    } else if (order > 0) {
                        graph.specificationDependencies.computeIfAbsent(id, k -> new TreeSet<>()).add(id);
                    }
                }
            }
            if (spec.getContent() != null) {
                for (String ref : spec.getContent().getReferences()) {
                    if (schema.specifications.containsKey(ref)) {
                        final int order = id.compareTo(ref);
                        if (order < 0) {
                            graph.specificationDependencies.computeIfAbsent(id, k -> new TreeSet<>()).add(ref);
                        } else if (order > 0) {
                            graph.specificationDependencies.computeIfAbsent(ref, k -> new TreeSet<>()).add(id);
                        }
                    }
                }
            }
        }

        graph.specificationDependencies.forEach((id, dependencies) -> {
            final String moduleId1 = schema.specifications.get(id).getModule();
            if (moduleId1 == null) {
                return;
            }
            dependencies.forEach(dependency -> {
                final String moduleId2 = schema.specifications.get(dependency).getModule();
                if (moduleId2 == null) {
                    return;
                }
                final int order = moduleId1.compareTo(moduleId2);
                if (order < 0) {
                    graph.moduleDependencies.computeIfAbsent(moduleId1, k -> new TreeSet<>()).add(moduleId2);
                } else if (order > 0) {
                    graph.moduleDependencies.computeIfAbsent(moduleId2, k -> new TreeSet<>()).add(moduleId1);
                }
            });
        });

        return graph;
    }

    private SpecificationGraph(Schema schema) {
        this.schema = schema;
    }
}
