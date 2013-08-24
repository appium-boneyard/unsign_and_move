/**
 * This file is licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 **/
package unsign;

import static unsign.Utils.close;
import static unsign.Utils.delete;
import static unsign.Utils.transferFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.management.RuntimeErrorException;

public class MoveManifest {

	private static final byte[] BUFFER = new byte[1024 * 1024];

	/**
	 * copy input to output stream.
	 * 
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public static void copyStream(InputStream input, OutputStream output)
			throws IOException {
		int bytesRead;
		while ((bytesRead = input.read(BUFFER)) != -1) {
			output.write(BUFFER, 0, bytesRead);
		}
	}

	/**
	 * move a new manifest file into the given apk zip file.
	 * 
	 * @param apkPath
	 *            original apk file path (will be replaced)
	 * @param manifestPath
	 *            full path to android manifest file
	 */
	public static void moveManifest(final String apkPath,
			final String manifestPath) {

		ZipInputStream inputZip = null;
		ZipOutputStream outputZip = null;
		File origApk = null;
		File tmpApk = null;
		File manifestFile = null;

		try {
			manifestFile = new File(manifestPath);
			String manifestBasename = manifestFile.getName();
			if (!manifestFile.isFile()) {
				throw new RuntimeException("manifest file does not exist: "
						+ manifestFile.getAbsolutePath());

			}

			origApk = new File(apkPath);
			tmpApk = new File(origApk.getParentFile(), new Date().getTime()
					+ ".tmp");

			// renameTo often fails so fall back to file transfer.
			if (!origApk.renameTo(tmpApk)) {
				try {
					transferFile(origApk, tmpApk);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("Unable to rename " + origApk);
				}
			}

			outputZip = new ZipOutputStream(new FileOutputStream(origApk));
			inputZip = new ZipInputStream(new FileInputStream(tmpApk));
			ZipEntry entry = null;
			while ((entry = inputZip.getNextEntry()) != null) {
				String name = entry.getName();
				if (name.indexOf(manifestBasename) < 0) {
					outputZip.putNextEntry(new ZipEntry(name));
					if (!entry.isDirectory()) {
						copyStream(inputZip, outputZip);
					}
					outputZip.closeEntry();
				}
			}
			outputZip.putNextEntry(new ZipEntry(manifestBasename));
			copyStream(new FileInputStream(manifestFile), outputZip);
			outputZip.closeEntry();
		} catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to move new manifest "
					+ manifestFile.getAbsolutePath() + " into zip "
					+ origApk.getAbsolutePath());
		} finally {
			close(inputZip);
			close(outputZip);
		}

		delete(tmpApk);

	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err
					.println("usage: java -jar move_manifest.jar apk-file manifest-file");
			System.exit(1);
		}
		moveManifest(args[0], args[1]);
	}
}