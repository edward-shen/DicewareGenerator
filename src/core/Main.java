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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class Main {
	
	/*
	 * An inherent issue that is presented in this code is that the data manipulation is GUI-based, rather than variable-based. To clarify, various functions within this code takes the value of the
	 * Strings located in the JComponents rather than using a private variable and then calling functions to update the GUI. This is horrendous coding, and in a future update, this will be fixed. I
	 * apologize for such terrible work, as I was under a time constraint and was not thinking properly. As of writing this note, I still am, and thus cannot fix it before the presentation date. TODO:
	 * Change code to be variable-based rather than GUI-based.
	 */
	
	private JFrame				jf						= new JFrame("DiceWare Password Generator");
	private GUI					jp						= new GUI();
														
	private JLabel				passwordText			= new JLabel("Your password is:");
	private JLabel				passwordData			= new JLabel(" ");
	private JLabel				passwordStrText			= new JLabel("Approximate bits of entropy: ");
	private JLabel				passwordStr				= new JLabel("0 bits");
	private JLabel				dictionaryText			= new JLabel("Current Dictionary Hash: ");
	private JLabel				dictionaryHash			= new JLabel(" ");
	private JCheckBox			useSpecialChars			= new JCheckBox("Use Special Characters?");
	private JFormattedTextField	minSpChars				= new JFormattedTextField(NumberFormat.INTEGER_FIELD);
	private JCheckBox			useNumbers				= new JCheckBox("Use Numbers?                   ");
	private JFormattedTextField	minNum					= new JFormattedTextField(NumberFormat.INTEGER_FIELD);
	private JLabel				minSizeText				= new JLabel("Minimum phrase length:          ");
	private JFormattedTextField	minSize					= new JFormattedTextField(NumberFormat.INTEGER_FIELD);
	private JLabel				curPhraseSizeText		= new JLabel("Current Phrase Size: ");
	private JLabel				curPhraseSize			= new JLabel("0");
	private JLabel				wordsLeftText			= new JLabel("Rolls left: ");
	private JLabel				wordsLeft				= new JLabel("0");
	private JButton				capturePassphraseWord	= new JButton("Capture Passphrase word");
	private JButton				generateDictionary		= new JButton("Generate new Dictionary");
	private JButton				connectVC				= new JButton("Connect to webcam");
	private JButton				captureRawImg			= new JButton("Capture Raw Image");
	private JButton				captureDetImg			= new JButton("Capture Detected Image");
	private JLabel				copyright				= new JLabel("\u00a9 2016 Edward Shen. OpenCV's libraries and code were used under the BSD 3-Clause License.");
														
	private PasswordGenerator	pg						= new PasswordGenerator();
	private VideoPanel			livefeed				= new VideoPanel();
	private Classifier			cl						= new Classifier();
	private Thread				livestreamThread;
								
	private ArrayList<String>	passphrase;
	private int					curNumSp				= 0;
	private int					curNumNum				= 0;
														
	/**
	 * Opens the video stream and generates the UI.
	 */
	public Main() {
		
		cl.openVC(0);
		
		generateUI();
		
	}
	
	/**
	 * Creates all UI aspects and their respective settings and calls the functions to attach them and to add listeners.
	 */
	private void generateUI() {
		
		// Init JFrame
		jf.setSize(1000, 600);
		jf.setMinimumSize(new Dimension(400, 300));
		jf.setLocationRelativeTo(null);
		jf.setResizable(false);
		jf.setIconImage(new ImageIcon("res/icon.png").getImage());
		jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));
		
		livefeed.setPreferredSize(new Dimension(640, 480));
		minNum.setValue(1);
		minNum.setColumns(10);
		minNum.addPropertyChangeListener("value", null);
		minSpChars.setValue(1);
		minSpChars.setColumns(10);
		minSpChars.addPropertyChangeListener("value", null);
		minSize.setValue(1);
		minSize.setColumns(10);
		minSize.addPropertyChangeListener("value", null);
		
		passwordData.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
		dictionaryHash.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
		
		updatePhraseSize();
		
		addComponents();
		addListeners();
		
		useNumbers.setSelected(true);
		useSpecialChars.setSelected(true);
		minSize.setText("5");
		capturePassphraseWord.setEnabled(false);
		
		copyright.setFont(copyright.getFont().deriveFont(8f));
		
		jf.add(jp);
		jf.pack();
		jf.setVisible(true);
		
		// Starts a new thread to safely update the Livestream
		livestreamThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					livefeed.paintComponent(cl.getNextImage());
				}
				
			}
		});
		
		livestreamThread.setDaemon(true); // So that it should (read: not working right now; TODO: Debug) exit properly
		livestreamThread.start();
		
	}
	
	/**
	 * Attaches components to the main JPanel
	 */
	private void addComponents() {
		// Whitespace sorted by group.
		jp.add(livefeed);
		
		jp.add(passwordText);
		jp.add(passwordData);
		
		jp.makeAndAddGroup(passwordStrText, passwordStr);
		
		jp.add(dictionaryText);
		jp.add(dictionaryHash);
		
		jp.makeAndAddGroup(useSpecialChars, minSpChars);
		
		jp.makeAndAddGroup(useNumbers, minNum);
		
		jp.makeAndAddGroup(minSizeText, minSize);
		
		jp.makeAndAddGroup(curPhraseSizeText, curPhraseSize);
		jp.makeAndAddGroup(wordsLeftText, wordsLeft);
		jp.makeAndAddGroup(capturePassphraseWord, generateDictionary);
		
		jp.makeAndAddGroup(connectVC, captureRawImg, captureDetImg);
		
		jp.add(copyright);
		
	}
	
	/**
	 * Adds listeners to buttons, textfields, checkboxes, etc.
	 */
	private void addListeners() {
		
		useNumbers.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				minNum.setEnabled(useNumbers.isSelected());
				updatePhraseSize();
			}
		});
		
		useSpecialChars.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				minSpChars.setEnabled(useSpecialChars.isSelected());
				updatePhraseSize();
				
			}
		});
		
		minSize.addCaretListener(new CaretListener() {
			
			@Override
			public void caretUpdate(CaretEvent arg0) {
				updatePhraseSize();
				
			}
			
		});
		
		capturePassphraseWord.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int curVal = Integer.valueOf(String.valueOf(wordsLeft.getText())) - 1;
				disableSettings();
				wordsLeft.setText(String.valueOf(curVal));
				setPassphraseWord();
				// Our reset function. It honestly should be placed within a separate function, but there hasn't been another use for it, so it's been placed here.
				if (curVal == 0) {
					enableSettings();
					setPassphrase();
					updatePhraseSize();
					curNumNum = 0;
					curNumSp = 0;
					passphrase = null;
				}
				
			}
			
		});
		
		generateDictionary.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				generateDictionary.setEnabled(false);
				pg.generateDict(true, true); // Generate dictionaries regardless of selection.
				dictionaryHash.setText(pg.getDictHash());
				generateDictionary.setEnabled(true);
				capturePassphraseWord.setEnabled(true);
			}
			
		});
		
		connectVC.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!cl.isVCConnected()) {
					cl.closeVC();
					cl.openVC(0);
				}
			}
		});
		
		captureRawImg.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cl.captureRawImg();
			}
		});
		
		captureDetImg.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cl.captureDetImg();
				
			}
		});
		
	}
	
	/**
	 * Enables every user option field that was previously disabled by {@code disabledSettings()}.
	 * <p>
	 * Note that options that were previously disabled before being disabled (i.e. if the textfield for using numbers were disabled) will continue to be disabled after calling this function
	 */
	private void enableSettings() {
		useSpecialChars.setEnabled(true);
		if (useSpecialChars.isSelected())
			minSpChars.setEnabled(true);
		useNumbers.setEnabled(true);
		if (useNumbers.isSelected())
			minNum.setEnabled(true);
		minSizeText.setEnabled(true);
		minSize.setEnabled(true);
		
	}
	
	/**
	 * Disables all user option fields, except buttons.
	 */
	private void disableSettings() {
		useSpecialChars.setEnabled(false);
		minSpChars.setEnabled(false);
		useNumbers.setEnabled(false);
		minNum.setEnabled(false);
		minSizeText.setEnabled(false);
		minSize.setEnabled(false);
	}
	
	/**
	 * Gets the values within the textboxes of the user options fields, and calculate the minimum number of words to be generated.
	 */
	private void updatePhraseSize() {
		int minNumVal = 0;
		int minSpVal = 0;
		int minSizeVal = 0;
		try {
			minSizeVal = Integer.valueOf(minSize.getText().replaceAll(",", ""));
			minSpVal = Integer.valueOf(minSpChars.getText().replaceAll(",", ""));
			minNumVal = Integer.valueOf(minNum.getText().replaceAll(",", ""));
		} catch (NumberFormatException e) {
			// Throws an error when the box contains no text, but shouldn't be an issue.
		}
		
		/*
		 * If the minimum phrase length is greater than the number of number "words" and special character "words", set it equal to the minimum phrase length. Otherwise, set the value equal to the
		 * combined value of the number of number "words" and special character "words".
		 * 
		 * Examples: User wants at least 4 words, with 1 being a special "word" and no number "words". The below function returns 4, as 4 > 1 + 0.
		 * 
		 * User wants at least 2 words, with 3 being special "words" and 2 being number "words". The below function returns 5, as 2 < 2 + 3.
		 */
		String val;
		if (useNumbers.isSelected() && useSpecialChars.isSelected()) {
			val = String.valueOf((minSizeVal > (minNumVal + minSpVal)) ? minSizeVal : minNumVal + minSpVal);
			
		} else if (useNumbers.isSelected() && !useSpecialChars.isSelected()) {
			val = String.valueOf((minSizeVal > (minNumVal + minSpVal)) ? minSizeVal : minNumVal);
			
		} else if (!useNumbers.isSelected() && useSpecialChars.isSelected()) {
			val = String.valueOf((minSizeVal > (minNumVal + minSpVal)) ? minSizeVal : minSpVal);
		} else {
			val = String.valueOf(minSizeVal);
		}
		
		curPhraseSize.setText(val);
		wordsLeft.setText(val);
	}
	
	/**
	 * Parses the ArrayList {@code passphrase} and updates the responding JLabel to display the password.
	 */
	private void setPassphrase() {
		String password = "";
		
		for (int i = 0; i < passphrase.size(); i++) {
			password += passphrase.get(i) + "    "; // Since JComponents do not support \t, 4 spaces were used instead.
		}
		
		password = password.trim();
		
		passwordData.setText(password);
		passwordStr.setText(pg.getPassStrength(password));
		
	}
	
	/**
	 * Stores the detected and converted dice data.
	 * <p>
	 * Initializes the ArrayList {@code passphrase} to store our passwords, captures the current image data, parses it to a line number, and sends its off to {@code PasswordGenerator} class to be
	 * converted into a String. Then stores the data.
	 */
	private void setPassphraseWord() {
		
		// You know, this probably should have been done with a primitive array. Oops.
		// TODO: convert to primitive array?
		
		SecureRandom rnd = new SecureRandom();
		String word;
		
		// If this is our first word in our phrase, generate a new passphrase
		if (passphrase == null) {
			passphrase = new ArrayList<>();
			for (int i = 0; i < Integer.valueOf(curPhraseSize.getText()); i++) {
				passphrase.add("-1"); // Fill our arraylist with -1s to get empty slots used later.
			}
		}
		
		Map<Integer, Integer> data = cl.getData(); // Get our data from the image
		
		int rawValue = 0;
		int value = 0;
		
		try {
			// rawValue is used for debugging only, and is used to verify that our data is correct.
			rawValue = data.get(0) * 10000 + data.get(1) * 1000 + data.get(2) * 100 + data.get(3) * 10 + data.get(4);
			// Converts our 1-6 digit range into 0-5 to be used for base conversion. Yes, it works.
			value = (data.get(0) - 1) * 10000 + (data.get(1) - 1) * 1000 + (data.get(2) - 1) * 100 + (data.get(3) - 1) * 10 + data.get(4) - 1;
		} catch (NoSuchElementException e) {
			System.out.println(data.values());
		}
		
		try {
			value = Integer.valueOf(Integer.toString(Integer.parseInt(Integer.toString(value), 6), 10)); // Actual conversion from base 6 to base 10
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.err.println("Dice Code was " + rawValue + "!");
		}
		
		// Generates the special characters first, and moves on to numbers and finally words once the quota has been reached for the respective word type.
		if (useSpecialChars.isSelected() && curNumSp < Integer.valueOf(minSpChars.getText())) {
			word = pg.getWord(DICTIONARY.DICT_SPECIAL, value);
			curNumSp++;
		} else if (useNumbers.isSelected() && curNumNum < Integer.valueOf(minNum.getText())) {
			word = pg.getWord(DICTIONARY.DICT_NUMBERS, value);
			curNumNum++;
		} else
			word = pg.getWord(DICTIONARY.DICT_NORMAL, value);
			
		// Find a random empty (value = "-1") position in the array
		int pos;
		do {
			pos = rnd.nextInt(Integer.valueOf(curPhraseSize.getText()));
		} while (!passphrase.get(pos).equals("-1"));
		
		System.out.println("Got word " + word + "\tat pos " + pos + " with Dice Code " + rawValue + "!"); // Debug
		passphrase.set(pos, word); // Sets our word to the passphrase
		
	}
	
}
