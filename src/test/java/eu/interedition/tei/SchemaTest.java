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

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SchemaTest {

    private static final Logger LOG = Logger.getLogger(SchemaTest.class.getName());

    @Test
    public void readSchema() throws Exception {
        try (InputStream xmlStream = getClass().getResourceAsStream("/tei-all.xml")) {
            SpecificationGraph.create(Schema.read(xmlStream))
                    .specificationDependencies
                    .forEach((id, dependencies) -> LOG.fine(() -> Stream.of(id, dependencies).map(Object::toString).collect(Collectors.joining(" - "))));
        }
    }

    @Test
    public void readCustomizations() throws Exception {
        final String dataPath = System.getProperty("tei.data");
        if (dataPath != null) {
            final File data = new File(dataPath);
            if (!data.isDirectory()) {
                throw new IllegalArgumentException(data.toString());
            }
            for (File oddFile : data.listFiles((dir, name) -> name.endsWith(".odd"))) {
                LOG.fine(oddFile::toString);
                try (InputStream xmlStream = new FileInputStream(oddFile)) {
                    Schema.read(xmlStream);
                } catch (UnsupportedOperationException e) {
                    LOG.log(Level.SEVERE, e, e::getMessage);
                }
            }
        }
    }
}
