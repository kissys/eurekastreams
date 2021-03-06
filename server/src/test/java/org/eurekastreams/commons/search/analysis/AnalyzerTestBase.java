/*
 * Copyright (c) 2009-2010 Lockheed Martin Corporation
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
package org.eurekastreams.commons.search.analysis;

import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.junit.Assert;

/**
 * Base class for testing Analyzers.
 */
public class AnalyzerTestBase
{
    /**
     * Assert the input Analyzer parses the input String as expected.
     * 
     * @param analyzer
     *            the analyzer to test
     * @param input
     *            the string to tokenize
     * @param expectedImages
     *            the expected results of the analyzing
     * @throws Exception
     *             on error
     */
    protected void assertAnalyzesTo(final Analyzer analyzer, final String input, final String[] expectedImages)
            throws Exception
    {
        TokenStream ts = analyzer.tokenStream("dummy", new StringReader(input));
        final Token reusableToken = new Token();

        Token nextToken;

        // while ((nextToken = ts.next(reusableToken)) != null)
        // {
        // System.out.println(nextToken.term());
        // }
        //
        // return;

        for (int i = 0; i < expectedImages.length; i++)
        {
            nextToken = ts.next(reusableToken);
            // System.out.println(nextToken.term());
            Assert.assertNotNull(reusableToken);
            Assert.assertEquals(expectedImages[i], nextToken.term());
        }
        Assert.assertNull(ts.next(reusableToken));
        ts.close();
    }
}
