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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JPanel;

import org.opencv.core.Mat;

public class VideoPanel extends JPanel {
	private static final long	serialVersionUID	= 258324488913829106L;
													
	private BufferedImage		img;
								
	public VideoPanel() {
		super();
	}
	
	/**
	 * Draws VideoPanel according to JPanel, but if a image was previously supplied, draws the image as well.
	 * 
	 * @param g
	 *            Graphics
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (img != null)
			g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
		else
			g.drawString("Check video input!", 320 - 50, 240);
	}
	
	/**
	 * Convenience function. Paints a matrix onto the JPanel.
	 * 
	 * @param m
	 *            Matrix
	 */
	public void paintComponent(Mat m) {
		if (getGraphics() != null) {
			setImage(toBufferedImage(m));
			paintComponent(getGraphics());
		}
	}
	
	public void setImage(BufferedImage img) {
		this.img = img;
	}
	
	/**
	 * Converts an OpenCV Matrix into an BufferedImage. Used for drawing the image onto the GUI.
	 * 
	 * @param m
	 *            OpenCV Matrix object
	 * @return Native Java BufferedImage
	 */
	private BufferedImage toBufferedImage(Mat m) {
		if (!m.empty()) {
			int type = BufferedImage.TYPE_BYTE_GRAY;
			if (m.channels() > 1) {
				type = BufferedImage.TYPE_3BYTE_BGR;
			}
			int bufferSize = m.channels() * m.cols() * m.rows();
			byte[] b = new byte[bufferSize];
			m.get(0, 0, b); // get all the pixels
			BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
			final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			System.arraycopy(b, 0, targetPixels, 0, b.length);
			return image;
		}
		
		return null;
	}
}
