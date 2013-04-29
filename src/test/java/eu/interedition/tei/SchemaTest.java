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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.io.PatternFilenameFilter;
import eu.interedition.tei.util.XML;
import org.junit.Test;

import javax.xml.stream.XMLEventReader;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SchemaTest {

    private static final Logger LOG = Logger.getLogger(SchemaTest.class.getName());

    @Test
    public void readSchema() throws Exception {
        final String teiRootPath = System.getProperty("tei.root");
        if (teiRootPath != null) {
            for (Map.Entry<String, String> dep : SpecificationGraph.create(Schema.read(new File(teiRootPath))).specificationDependencies.entries()) {
                LOG.fine(Joiner.on(" - ").join(dep.getKey(), dep.getValue()));
            }
        }
    }

    @Test
    public void readCustomizations() throws Exception {
        final String dataPath = System.getProperty("tei.data");
        if (dataPath != null) {
            final File data = new File(dataPath);
            Preconditions.checkArgument(data.isDirectory(), data);
            for (File oddFile : data.listFiles(new PatternFilenameFilter(".+\\.odd$"))) {
                LOG.fine(oddFile.toString());
                XMLEventReader xml = XML.inputFactory().createXMLEventReader(new StreamSource(oddFile));
                try {
                    Schema.read(xml);
                } catch (UnsupportedOperationException e) {
                    LOG.log(Level.SEVERE, e.getMessage(), e);
                } finally {
                    xml.close();
                }
            }
        }
    }
}
