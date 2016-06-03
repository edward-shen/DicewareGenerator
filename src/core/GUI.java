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

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class GUI extends JPanel {
	
	private static final long serialVersionUID = -5004957882401545015L;
	
	/**
	 * Adds a list of components to a single JPanel, and adds them to the superclass.
	 * 
	 * @param components
	 */
	public void makeAndAddGroup(Component... components) {
		JPanel output = new JPanel();
		for (Component c : components) {
			output.add(c);
		}
		
		add(output);
	}
	
	/**
	 * Adds and automatically centers the component.
	 */
	@Override
	public Component add(Component c) {
		((JComponent) c).setAlignmentX(Component.CENTER_ALIGNMENT);
		super.add(c);
		
		return c;
	}
}
