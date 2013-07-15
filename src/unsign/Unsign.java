/*******************************************************************************
  This file is licensed under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an "AS
  IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  express or implied.  See the License for the specific language
  governing permissions and limitations under the License.
 *******************************************************************************/

package unsign;

import static unsign.Utils.close;
import static unsign.Utils.delete;
import static unsign.Utils.transferFile;

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

            // renameTo often fails so fall back to file transfer.
            if (!input.renameTo(renamedInput)) {
                try {
                    transferFile(input, renamedInput);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Unable to rename " + input);
                }
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
            e.printStackTrace();
            throw new RuntimeException("Unable to sign zip " + input.getAbsolutePath());
        } finally {
            close(inputZip);
            close(outputZip);
        }

        delete(renamedInput);
    }

    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            System.out.println("Usage: java -jar unsign.jar input.zip");
            System.exit(0);
        }

        final File input = new File(args[0]);

        unsign(input);
    }
}