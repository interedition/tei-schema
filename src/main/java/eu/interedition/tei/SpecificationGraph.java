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

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SpecificationGraph {

    final Schema schema;
    final Multimap<String, String> moduleMembers = TreeMultimap.create();
    final Multimap<String, String> classMembers = TreeMultimap.create();
    final Multimap<String, String> specificationDependencies = TreeMultimap.create();
    final Multimap<String, String> moduleDependencies = TreeMultimap.create();

    public static SpecificationGraph create(Schema schema) {
        final SpecificationGraph graph = new SpecificationGraph(schema);

        for (Specification specification : schema.specifications.values()) {
            final String module = specification.getModule();
            if (module != null) {
                graph.moduleMembers.put(module, specification.getIdent());
            }
            final String id = specification.getIdent();
            for (String classMembership : specification.classes.keySet()) {
                graph.classMembers.put(classMembership, id);
            }
        }

        for (Specification spec : schema.specifications.values()) {
            final String id = spec.getIdent();
            if (Specification.Type.CLASS.equals(spec.getType())) {
                for (String member : graph.classMembers.get(id)) {
                    final int order = id.compareTo(member);
                    if (order < 0) {
                        graph.specificationDependencies.put(id, member);
                    } else if (order > 0) {
                        graph.specificationDependencies.put(member, id);
                    }
                }
            }
            if (spec.getContent() != null) {
                for (String ref : spec.getContent().getReferences()) {
                    if (schema.specifications.containsKey(ref)) {
                        final int order = id.compareTo(ref);
                        if (order < 0) {
                            graph.specificationDependencies.put(id, ref);
                        } else if (order > 0) {
                            graph.specificationDependencies.put(ref, id);
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, String> dependency : graph.specificationDependencies.entries()) {
            final String moduleId1 = schema.specifications.get(dependency.getKey()).getModule();
            final String moduleId2 = schema.specifications.get(dependency.getValue()).getModule();
            if (moduleId1 == null || moduleId2 == null) {
                continue;
            }
            final int order = moduleId1.compareTo(moduleId2);
            if (order < 0) {
                graph.moduleDependencies.put(moduleId1, moduleId2);
            } else if (order > 0) {
                graph.moduleDependencies.put(moduleId2, moduleId1);
            }
        }

        return graph;
    }

    private SpecificationGraph(Schema schema) {
        this.schema = schema;
    }
}
