/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.filevault.maven.packaging.it;

import java.io.File;
import java.io.IOException;

import org.apache.jackrabbit.filevault.maven.packaging.it.util.ProjectBuilderExtension;
import org.apache.jackrabbit.filevault.maven.packaging.it.util.ProjectBuilder;
import org.apache.maven.it.VerificationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProjectBuilderExtension.class)
class SupplementalArtifactsIT {

    @Test
    void testMultipleSupplementalArtifacts(ProjectBuilder projectBuilder) throws VerificationException, IOException {
        projectBuilder.setTestProjectDir("/supplemental-artifacts/two-packages-in-one-module");
        projectBuilder.build();
        // verify contents of main artifacts
        projectBuilder.verifyExpectedFiles();
        // verify contents of supplemental artifact (with classifier "libs")
        projectBuilder.verifyExpectedFiles(new File(projectBuilder.getTestProjectDir(), "expected-files-libs.txt"), 
                ProjectBuilder.verifyPackageZipEntries(new File(projectBuilder.getTestProjectDir(), "target/package-plugin-test-pkg-1.0.0-SNAPSHOT-libs.zip")));
    }
}
