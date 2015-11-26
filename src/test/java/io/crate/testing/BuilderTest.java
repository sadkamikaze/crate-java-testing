/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.testing;

import io.crate.action.sql.SQLResponse;
import io.crate.shade.org.elasticsearch.common.settings.ImmutableSettings;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class BuilderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testBuilder() throws Throwable {
        CrateTestServer testServer = CrateTestServer.builder()
                .clusterName("mycluster")
                .fromVersion("0.52.0")
                .workingDir(tempFolder.getRoot().getAbsolutePath())
                .host("127.0.0.1")
                .httpPort(12345)
                .transportPort(55555)
                .settings(ImmutableSettings.builder()
                        .put("stats.enabled", true)
                        .build())
                .build();
        assertThat(testServer.crateHost, is("127.0.0.1"));
        assertThat(testServer.httpPort, is(12345));
        assertThat(testServer.transportPort, is(55555));

        try {
            testServer.before();

            SQLResponse response = testServer.execute("select version['number'] from sys.nodes");
            assertThat(response.rowCount(), is(1L));
            assertThat((String) response.rows()[0][0], is("0.52.0"));

            SQLResponse clusterResponse = testServer.execute("select name, settings['stats']['enabled'] from sys.cluster");
            assertThat((String) clusterResponse.rows()[0][0], is("mycluster"));

            assertThat(String.valueOf(clusterResponse.rows()[0][1]), is("true"));

        } finally {
            testServer.after();
        }
    }

    @Test
    public void testNoURL() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("no crate version, file or download url given");
        CrateTestServer.builder()
                .clusterName("mycluster")
                .workingDir(tempFolder.getRoot().getAbsolutePath())
                .host("127.0.0.1")
                .httpPort(12345)
                .transportPort(55555)
                .settings(ImmutableSettings.builder()
                        .put("stats.enabled", true)
                        .build())
                .build();

    }
}