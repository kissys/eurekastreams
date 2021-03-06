/*
 * Copyright (c) 2009 Lockheed Martin Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eurekastreams.server.service.actions.strategies;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * Document creator, used by AddThemeAction.
 */
public class DocumentCreator
{
    /**
     * Logger.
     */
    private Log log = LogFactory.getLog(DocumentCreator.class);

    /**
     * Fetcher for the XML theme definition.
     */
    private ResourceFetcher xmlFetcher = null;

    /**
     * Constructor.
     * 
     * @param inXMLFetcher
     *            a Strategy that will provide the source XML
     */
    public DocumentCreator(final ResourceFetcher inXMLFetcher)
    {
        xmlFetcher = inXMLFetcher;
    }

    /**
     * Given a file name, returns an xml document.
     * 
     * @param inFileName
     *            the name of the file
     * @return an xml document.
     * @throws Exception
     *             Thrown when cannot parse the xml file.
     */
    public Document execute(final String inFileName) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        log.info("Fetching and parsing input stream from " + inFileName);
        return db.parse(xmlFetcher.getInputStream(inFileName));
    }
}
