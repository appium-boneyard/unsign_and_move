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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * Inspired by commons-io-2.4. No code is used from Apache commons.
 **/
public abstract class Utils {

    /** Closes the stream if it's not null. **/
    public static void close(final InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
            }
        }
    }

    /** Closes the stream if it's not null. **/
    public static void close(final OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
            }
        }
    }

    /** Closes the channel if it's not null. **/
    public static void close(final FileChannel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Invokes file.delete() and if that fails, file.deleteOnExit(). Immediately
     * returns if file is null.
     **/
    public static void delete(final File file) {
        if (file == null) {
            return;
        }

        if (!file.delete()) {
            file.deleteOnExit();
        }
    }

    /**
     * Transfers input to output and deletes input. Output is deleted if it
     * exists.
     **/
    public static void transferFile(final File input, final File output)
            throws IOException {
        if (input == null) {
            throw new NullPointerException("Input must not be null.");
        }

        if (output == null) {
            throw new NullPointerException("Output must not be null.");
        }

        if (input.isDirectory()) {
            throw new IOException("Input must not be a directory. "
                    + input.getAbsolutePath());
        }

        if (output.isDirectory()) {
            throw new IOException("Output must not be a directory. "
                    + output.getAbsolutePath());
        }

        if (input.getCanonicalFile().equals(output.getCanonicalFile())) {
            throw new IOException("Input must be different from output.");
        }

        if (output.exists()) {
            if (!output.delete()) {
                throw new IOException("Unable to delete output file. "
                        + output.getAbsolutePath());
            }
        }

        // Make parent folders if required.
        final File outputParent = output.getParentFile();
        if (outputParent != null) {
            outputParent.mkdirs();
            // mkdirs will return false if the directories already exist
            // after mkdirs, the outputParent must be an existing directory.
            if (!outputParent.isDirectory() || !outputParent.exists()) {
                throw new IOException("Output parent is not an existing directory. "
                        + output.getAbsolutePath());
            }

            if (!outputParent.canWrite()) {
                throw new IOException("Can't write to output parent. "
                        + output.getAbsolutePath());
            }
        }

        FileChannel inputChannel = null;
        FileInputStream inputStream = null;

        FileChannel outputChannel = null;
        FileOutputStream outputStream = null;

        try {
            inputStream = new FileInputStream(input);
            inputChannel = inputStream.getChannel();

            outputStream = new FileOutputStream(output);
            outputChannel = outputStream.getChannel();

            // Faster than channel.size()
            // http://stackoverflow.com/questions/116574/java-get-file-size-efficiently
            final long inputSize = input.length();
            long outputSize = 0;

            while (outputSize < inputSize) {
                outputSize += outputChannel.transferFrom(inputChannel,
                        outputSize, inputSize - outputSize);
            }

        } finally {
            close(inputChannel);
            close(inputStream);

            close(outputChannel);
            close(outputStream);
        }

        if (output.length() != input.length()) {
            throw new IOException(
                    "Input file failed to transfer to output file. output.length() != input.length() input: "
                            + input.getAbsolutePath()
                            + " output: "
                            + output.getAbsolutePath());
        }

        delete(input);
    }
}