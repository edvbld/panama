/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/*
 * @test
 * @bug 8224013
 * @modules jdk.jextract
 * @build JextractToolRunner
 * @run testng/othervm -Duser.language=en TestSrcDump
 */
public class TestSrcDump extends JextractToolRunner {
    @Test
    public void testNoPkg() {
        Path src = getOutputFilePath("gensrc");
        if (Files.exists(src)) {
            if (Files.isDirectory(src)) {
                deleteDir(src);
            } else {
                deleteFile(src);
            }
        }
        run("--src-dump-dir", src.toString(),
            getInputFilePath("simple.h").toString()).checkSuccess();
        try {
            assertTrue(Files.isRegularFile(src.resolve(staticForwarderName("simple.h") + ".java")));
        } finally {
            deleteDir(src);
        }
    }

    @Test
    public void testTargetDir() throws IOException {
        Path src = getOutputFilePath("gensrc");
        Path realTarget = getOutputFilePath("realGenSrc");
        Files.createDirectory(realTarget);
        Files.createSymbolicLink(src, realTarget);
        run("--src-dump-dir", src.toString(),
                getInputFilePath("simple.h").toString()).checkSuccess();
        try {
            assertTrue(Files.isRegularFile(src.resolve(staticForwarderName("simple.h") + ".java")));
        } finally {
            deleteFile(src);
            deleteDir(realTarget);
        }
    }
}
