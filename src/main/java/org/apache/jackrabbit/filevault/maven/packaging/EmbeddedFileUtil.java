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
package org.apache.jackrabbit.filevault.maven.packaging;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static java.nio.file.StandardOpenOption.WRITE;

public class EmbeddedFileUtil {

    public static File updateManifest(File file, String manifestPropertiesStr) throws MojoFailureException {
        if(file.isFile() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"))) {

            if(StringUtils.isNotBlank(manifestPropertiesStr) && manifestPropertiesStr.contains("=")) {
                String[] manProps = manifestPropertiesStr.split(";");
                Attributes attributeMap = new Attributes();
                for(String manProp: manProps) {
                    String[] seg = manProp.split("=");
                    attributeMap.put(new Attributes.Name(seg[0]), seg[1]);
                }

                try {
                    String parent = file.getParent();
                    String fileName = file.getName();

                    File destFile = new File(parent + File.separator + "_tmp" + File.separator + fileName);
                    destFile.mkdirs();

                    // copy jar file
                    Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    // update manifest in copied jar. return new file only if successful
                    if(updateManifestEntries(destFile.toPath(), attributeMap))
                        return destFile;
                } catch (IOException e) {
                    throw new MojoFailureException("Processing Embedded File failure", e);
                }
            }
        }

        return file;
    }

    private static boolean updateManifestEntries(Path jarPath, Attributes attributeMap) throws IOException {
        try (FileSystem jarFS = FileSystems.newFileSystem(URI.create("jar:" + jarPath.toUri()), Maps.newHashMap())) {
            Path manifestPath = jarFS.getPath("META-INF", "MANIFEST.MF");
            Manifest manifest = readManifest(manifestPath);
            Attributes mainAttributes = manifest.getMainAttributes();

            // update only matching entries
            for(Object newKey: attributeMap.keySet()) {
                if(mainAttributes.containsKey(newKey)) {
                    mainAttributes.put(newKey, attributeMap.get(newKey));
                }
            }

            if (!attributeMap.isEmpty()) {
                writeManifest(manifestPath, manifest);
                return true;
            } else {
                return false;
            }
        }
    }

    private static Manifest readManifest(Path manifestPath) throws IOException {
        try (InputStream is = Files.newInputStream(manifestPath)) {
            return new Manifest(is);
        }
    }

    private static void writeManifest(Path manifestPath, Manifest manifest) throws IOException {
        try (OutputStream os = Files.newOutputStream(manifestPath, WRITE)) {
            manifest.write(os);
        }
    }

}
