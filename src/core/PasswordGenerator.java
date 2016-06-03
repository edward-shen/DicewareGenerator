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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.BitSet;
import java.util.stream.Stream;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class PasswordGenerator {
	private final int		SPECIAL_DICT_SIZE	= 10000;
	private final int		numDice				= 5;
	private final int		cardinality			= (int) Math.pow(6, numDice);
	private final String	wordDict			= "res/wordDict.txt";
	private final String	spCharDict			= "res/spCharDict.txt";
	private final String	numDict				= "res/numDict.txt";
	private final String	outputFile			= "res/output.txt";
	private SecureRandom	rnd					= new SecureRandom();
	private int				dictSize			= getDictSize(wordDict);
	private char[]			specialChars		= "~!@#$%^&*()_+-={}:\"<>?[];',./".toCharArray();
	private char[]			numberChars			= "0123456789".toCharArray();
	private PrintWriter		writer				= null;
												
	/**
	 * Generates dictionaries (word lists). By default, it only generates a word list.
	 * <p>
	 * If the parameters are set to true, generate a dictionary containing either numbers or special characters only. Note that all functions called to it should have both parameters as true, as the
	 * code will fail if any dictionary does not exist. The parameters are provided in case of niche cases. (None to be found so far)
	 * <p>
	 * 
	 * @param allowSpecial
	 * @param allowNumbers
	 */
	public void generateDict(boolean allowSpecial, boolean allowNumbers) {
		System.out.println("Generating dictionaries...");
		
		try {
			writer = new PrintWriter(outputFile, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		BitSet wordList = new BitSet(dictSize); // Used to confirm that a word isn't in the dictionary twice.
		
		while (wordList.cardinality() < cardinality) { // while dice dictionary needs to be populated
			// Get a random word
			int next = rnd.nextInt(dictSize);
			while (wordList.get(next)) // and until a unique word appears...
				next = rnd.nextInt(dictSize); // try, try again.
				
			try (Stream<String> lines = Files.lines(Paths.get(wordDict))) {
				// Write to file and note the word as used
				writer.println(lines.skip(next).findFirst().get());
				wordList.set(next, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Creates the other dictionaries if true.
		if (allowSpecial)
			createSpecialDict(specialChars, spCharDict);
			
		if (allowNumbers)
			createSpecialDict(numberChars, numDict);
			
		if (writer != null)
			writer.close();
			
		System.out.println("Done."); // Generation of dictionaries finished.
	}
	
	/**
	 * Helper function. Generates a special dictionary.
	 * <p>
	 * Generates a sequence of characters by picking 6 random ones from the {@code chars} param to be used as a word. Dictionary size is defined by the final variable {@code SPECIAL_DICT_SIZE}.
	 * <p>
	 * 
	 * @param chars
	 *            character array of valid characters to be used in each word
	 * @param loc
	 *            output location of dictionary.
	 */
	private void createSpecialDict(char[] chars, String loc) {
		try {
			writer = new PrintWriter(loc, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < SPECIAL_DICT_SIZE; i++) {
			String toFile = "";
			for (int y = 0; y < 6; y++)
				toFile += chars[rnd.nextInt(chars.length)];
			writer.println(toFile);
		}
		
	}
	
	/**
	 * Returns the number of lines (size) of a dictionary.
	 * <p>
	 * We can reduce CPU usage by short-circuiting known dictionary sizes.
	 * <p>
	 * 
	 * @param file
	 *            location of dictionary
	 * @return the number of lines in a specified file.
	 */
	private int getDictSize(String file) {
		
		if (file.equals(outputFile))
			return cardinality;
		if (file.equals(spCharDict) || file.equals(numDict))
			return SPECIAL_DICT_SIZE;
			
		try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i)
					if (c[i] == '\n')
						++count;
						
			}
			return (count == 0 && !empty) ? 1 : count;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * Generates a dictionary ID by hashing (via MD5) our dictionary
	 * <p>
	 * 
	 * @return Dictionary ID
	 */
	public String getDictHash() {
		try {
			// The word dictionary is always generated, so we use that to computer our "ID"
			return new HexBinaryAdapter().marshal(MessageDigest.getInstance("MD5").digest(Files.readAllBytes(Paths.get(outputFile))));
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * Computes the approximate bits of entropy of a String.
	 * <p>
	 * 
	 * @param password
	 * @return Rounded number of bits of entropy, wrapped in a string.
	 */
	public String getPassStrength(String password) {
		int poolSize = 1;
		if (password.matches(".*[a-z]"))
			poolSize += 26;
		if (password.matches(".*[A-Z]"))
			poolSize += 26;
		if (password.matches(".*[0-9]"))
			poolSize += 10;
		if (password.matches(".*[~`\\-+=_|}{\":L?><][';/.,]"))
			poolSize += 20;
		if (password.matches(".*[!@#$%^&*()]"))
			poolSize += 10;
			
		// FIXME: the method used to calculate this may be incorrect. Verify and fix.
		long str = Math.round(Math.log(poolSize) / Math.log(2)) * password.length();
		
		return String.valueOf(str);
		
	}
	
	/**
	 * Retrieves a word from the dictionaries.
	 * <p>
	 * If the size of a dictionary is equal to our cardinality, then we merely get the (n - 1)th line, where n is equal to the parameter value {@code value}. If the size of a dictionary is greater
	 * than our cardinality, get a crytographically secure random number to offset the lookup, so that the entire dictionary may be used.
	 * <p>
	 * 
	 * @param dict
	 * @param value
	 * @return the word found.
	 */
	public String getWord(DICTIONARY dict, int value) {
		int randOffset;
		
		// Ensure that our dictionaries exist.
		verifyDicts();
		
		if (dict.ordinal() == 0) {
			try (Stream<String> lines = Files.lines(Paths.get(outputFile))) {
				return lines.skip(value).findFirst().get();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if (dict.ordinal() == 1) {
			randOffset = rnd.nextInt(SPECIAL_DICT_SIZE - 7776);
			try (Stream<String> lines = Files.lines(Paths.get(spCharDict))) {
				return lines.skip(value + randOffset).findFirst().get();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if (dict.ordinal() == 2) {
			randOffset = rnd.nextInt(SPECIAL_DICT_SIZE - 7776);
			try (Stream<String> lines = Files.lines(Paths.get(numDict))) {
				return lines.skip(value + randOffset).findFirst().get();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		// This should never happen.
		return null;
	}
	
	/**
	 * Helper function. Generates all the dictionaries if they haven't been generated yet.
	 */
	private void verifyDicts() {
		if (Files.notExists(Paths.get(outputFile))) {
			generateDict(true, true);
		}
		
	}
}
