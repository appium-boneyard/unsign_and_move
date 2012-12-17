/*
Copyright (c) 2012, matt@bootstraponline.com
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

- Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

- Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

- Neither the name of Bootstrap Online nor the names of its contributors may be
used to endorse or promote products derived from this software without specific
prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package unsign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * <pre>
 * Removes the META-INF/ folder from the given ZIP file.
 * Works on APK, JAR, and other ZIP files.
 * </pre>
 * 
 * @author matt@bootstraponline.com
 * @license http://opensource.org/licenses/BSD-3-Clause
 */
public class Unsign {

    private static void unsign(final File input) {
        final byte[] buffer = new byte[4096];

        ZipInputStream inputZip = null;
        ZipOutputStream outputZip = null;
        final File output = input;

        final File renamedInput = new File(input.getParentFile(),
                new Date().getTime() + ".tmp");

        try {

            if (!input.renameTo(renamedInput)) {
                throw new RuntimeException("Unable to rename " + input);
            }

            inputZip = new ZipInputStream(new FileInputStream(renamedInput));
            outputZip = new ZipOutputStream(new FileOutputStream(output));
            ZipEntry entry = null;

            while ((entry = inputZip.getNextEntry()) != null) {

                final String name = entry.getName();

                // Delete files contained in META-INF/ and the META-INF folder.
                if (name.startsWith("META-INF/")) {
                    System.out.println("Removing: " + name);
                    continue;
                }

                // Write entry to output zip.
                outputZip.putNextEntry(new ZipEntry(name));

                int bytesRead = -1;
                while ((bytesRead = inputZip.read(buffer)) != -1) {
                    outputZip.write(buffer, 0, bytesRead);
                }
            }
        } catch (final Exception e) {
            System.out.println("Unable to sign zip " + input.getAbsolutePath());
            e.printStackTrace();
        } finally {
            if (inputZip != null) {
                try {
                    inputZip.close();
                } catch (final IOException e) {
                }
            }
            if (outputZip != null) {
                try {
                    outputZip.close();
                } catch (final IOException e) {
                }
            }
        }

        // Remove input.
        if (!renamedInput.delete()) {
            renamedInput.deleteOnExit();
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            System.out.println("Usage: java -jar unsign.jar input.zip");
        }

        final File input = new File(args[0]);

        unsign(input);
    }
}