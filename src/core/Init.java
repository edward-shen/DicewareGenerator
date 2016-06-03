/* DicewareGenerator provides a method to generate cryptographically secure passwords via dice.
 * Copyright (C) 2016 Edward Shen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.opencv.core.Core;

public class Init {
	// Dictionary from http://simson.net/ref/2005/csci_e-170/web2.txt (GNU dictionary)
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // required by OpenCV
		
		// Delete any files that were left over from the previous run, to ensure that new data is created.
		try {
			Files.deleteIfExists(Paths.get("res/output.txt"));
			Files.deleteIfExists(Paths.get("res/spCharDict.txt"));
			Files.deleteIfExists(Paths.get("res/numDict.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Start the program
		new Main();
	}
}
