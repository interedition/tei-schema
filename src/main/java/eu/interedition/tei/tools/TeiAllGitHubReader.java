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

package eu.interedition.tei.tools;

import eu.interedition.tei.Namespaceable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import javax.json.Json;
import javax.json.JsonObject;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TeiAllGitHubReader {

    private final String ghBasicAuth;
    private final DocumentBuilderFactory documentBuilderFactory;

    public TeiAllGitHubReader() {
        final String ghUser = Optional.ofNullable(System.getenv("GITHUB_USER")).orElseThrow(IllegalArgumentException::new);
        final String ghPassword = Optional.ofNullable(System.getenv("GITHUB_PASSWORD")).orElseThrow(IllegalArgumentException::new);
        
        this.ghBasicAuth = "Basic " + Base64.getEncoder().encodeToString((ghUser + ":" + ghPassword).getBytes(StandardCharsets.UTF_8));

        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.documentBuilderFactory.setValidating(false);
        this.documentBuilderFactory.setNamespaceAware(true);
        this.documentBuilderFactory.setExpandEntityReferences(true);
        this.documentBuilderFactory.setIgnoringComments(true);
        this.documentBuilderFactory.setXIncludeAware(true);
    }

    public static void main(String... args) {
        try {
            final ArrayDeque<String> argDeque = Arrays.stream(args).collect(Collectors.toCollection(ArrayDeque::new));
            final String source = Optional.ofNullable(argDeque.pop()).orElseThrow(IllegalArgumentException::new);
            final File outputFile = Optional.ofNullable(argDeque.pop()).map(File::new).orElseThrow(IllegalArgumentException::new);

            try (PrintWriter outputWriter = new PrintWriter(Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8))) {
                final Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(new DOMSource(new TeiAllGitHubReader().specification(source)), new StreamResult(outputWriter));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public Document specification(String source) throws ParserConfigurationException {
        final Document specification = documentBuilderFactory.newDocumentBuilder().newDocument();
        final Element schemaSpec = (Element) specification.appendChild(specification.createElementNS(Namespaceable.DEFAULT_NS_STR, "schemaSpec"));
        schemaSpec.setAttribute("ident", "tei_all");
        schemaSpec.setAttribute("start", "TEI teiCorpus");
        schemaSpec.setAttribute("prefix", "TEI_");
        schemaSpec.setAttribute("source", source);
        
        specificationDocuments()
                .toSortedList((d1, d2) -> d1.getDocumentElement().getAttribute("ident").compareTo(d2.getDocumentElement().getAttribute("ident")))
                .toBlocking()
                .single()
                .forEach(part -> schemaSpec.appendChild(specification.importNode(part.getDocumentElement(), true)));
        
        return specification;
    }
    
    public Observable<Document> specificationDocuments() {
        return specificationParts().flatMap(url -> Observable.<Document>create(subscriber -> {
            try (InputStream xmlStream = url.openStream()) {
                subscriber.onNext(documentBuilderFactory.newDocumentBuilder().parse(xmlStream));
                subscriber.onCompleted();
            } catch (IOException | SAXException | ParserConfigurationException e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io()));
    }
    
    public Observable<URL> specificationParts() {
        return Observable.create((Subscriber<? super URL> subscriber) -> {
            try {
                final URL root = new URL(String.format("https://api.github.com/repos/%s/contents/P5/Source/Specs?ref=%s",
                        System.getProperty("github.repo", "hcayless/TEI-Guidelines"),
                        System.getProperty("github.repo.ref", "master")
                ));
                
                final Queue<URL> directories = new LinkedList<>(Collections.singleton(root));
                while (!subscriber.isUnsubscribed() && !directories.isEmpty()) {
                    Json.createReader(gitHub(directories.remove()).getInputStream()).readArray().forEach(ghEntry -> {
                        try {
                            final JsonObject ghEntryObject = (JsonObject) ghEntry;
                            final URL entryUrl = new URL(ghEntryObject.getString("url"));
                            switch (ghEntryObject.getString("type", "")) {
                                case "dir":
                                    directories.add(entryUrl);
                                    break;
                                case "file":
                                    if (entryUrl.getPath().endsWith(".xml")) {
                                        subscriber.onNext(new URL(ghEntryObject.getString("download_url")));
                                    }
                            }
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Throwable t) {
                            throw new RuntimeException(t);
                        }
                    });
                }
                subscriber.onCompleted();
            } catch (Throwable t) {
                subscriber.onError(t);
            }
        });
    }
    
    private URLConnection gitHub(URL url) throws IOException {
        final URLConnection connection = url.openConnection();
        connection.setRequestProperty("Authorization", ghBasicAuth);
        return connection;
    }
}
