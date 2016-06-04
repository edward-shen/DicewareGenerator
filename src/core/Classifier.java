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

import java.util.HashMap;
import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class Classifier {
	private CascadeClassifier		diceCascade	= new CascadeClassifier("res/newMethod/diceCascade.xml");
	private CascadeClassifier		pipCascade	= new CascadeClassifier("res/newMethod/pipCascade6.xml");
	private VideoCapture			vc			= new VideoCapture();
	private Mat						image;
									
	private Map<Integer, Integer>	data		= new HashMap<>();
												
	/**
	 * Uses the classifier to detect and highlight ROIs (Region of Interest) from an existing image.
	 * <p>
	 * This has not been updated to detect the pips of a dice face. Thus, for now, this function should be avoided.
	 * 
	 * @param loc
	 *            Location of file
	 */
	@Deprecated
	public void detImg(String loc) {
		
		Mat image = Imgcodecs.imread(loc);
		
		MatOfRect diceDetections = new MatOfRect();
		diceCascade.detectMultiScale(image, diceDetections);
		
		System.out.println(String.format("Detected %s dice", diceDetections.toArray().length));
		
		// Draw a bounding box around each face.
		for (Rect rect : diceDetections.toArray()) {
			Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
		}
		
		// Save the visualized detection.
		String filename = "dice1Detection.png";
		System.out.println(String.format("Writing %s", filename));
		Imgcodecs.imwrite(filename, image);
		
	}
	
	/**
	 * Opens a video feed for detection.
	 * 
	 * @param i
	 *            Video device id to be used. ID is rather arbitrary, but if only one video device is presented, use 0.
	 */
	public void openVC(int i) {
		vc.open(i);
	}
	
	/**
	 * Closes the video feed specified by {@code openCV(int i)} function.\
	 * 
	 * @see openVC
	 */
	public void closeVC() {
		vc.release();
	}
	
	/**
	 * Checks if the video feed is properly connected.
	 * @return boolean
	 */
	public boolean isVCConnected() {
		return vc.isOpened();
	}
	
	/**
	 * Captures the current image (without any detection graphics) and saves it to a folder. Generally used for creating samples.
	 */
	public void captureRawImg() {
		Mat image = new Mat();
		vc.read(image);
		Imgcodecs.imwrite("rawImg/imageraw" + System.currentTimeMillis() + ".png", image);
	}
	
	/**
	 * Captures the current image (with the detection markup) and saves it to a folder. Generally used for sharing progress, and showing off to others c;
	 */
	public void captureDetImg() {
		Imgcodecs.imwrite("detImg/" + System.currentTimeMillis() + ".png", getNextImage());
		
	}
	
	/**
	 * Reads the livestream, then detects the desired features. Then stores valid data. Note that if invalid data is supplied, then it copies the previous value.
	 * 
	 * @return an OpenCV matrix of our image.
	 */
	public Mat getNextImage() {
		image = new Mat();
		vc.read(image); // Sets the matrix to the current livestream frame.
		
		MatOfRect diceDetections = new MatOfRect(); // Essentially an array of locations where our dice features were detected. (Stupid wrappers)
		
		// Note that detectMultiScale has thrown an unknown exception before (literally, unknown). This is to prevent crashing.
		try {
			diceCascade.detectMultiScale(image, diceDetections, 1.1, 4, 0, new Size(20, 20), new Size(38, 38));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Debug, used for console output
		String curDetect = "";
		
		// Iterates for every Dice ROI
		for (int i = 0; i < diceDetections.toArray().length; i++) {
			
			Rect diceRect = diceDetections.toArray()[i];
			
			// Draws rectangles around our detected ROI
			Point startingPoint = new Point(diceRect.x, diceRect.y);
			Point endingPoint = new Point(diceRect.x + diceRect.width, diceRect.y + diceRect.height);
			Imgproc.rectangle(image, startingPoint, endingPoint, new Scalar(255, 255, 0));
			
			MatOfRect pipDetections = new MatOfRect();
			
			try {
				/*
				 * Now this is interesting. We essentially create a sub-array of the image, with our dice ROI as the image. Then we perform the detection on the image. This gives us the relative
				 * positions of our pip ROIs to the dice ROI. Later on, we can draw the circles around the pip ROI, with the centers' positions adjusted by adding the dice ROI positions, so that it
				 * renders properly. This is an amazing trick, as it not only eliminates false positives in non-dice ROIs, but it reduces how many pixels the classifier has to analyze to only at most
				 * 38 x 38 pixels (because of the size restraints provided while detecting dice ROIs). This means we can set the precision to an insane level, without performance loss.
				 */
				pipCascade.detectMultiScale(image.submat(diceRect), pipDetections, 1.01, 4, 0, new Size(2, 2), new Size(10, 10));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Gets the number of detected pips and draws a cricle around the ROI
			int numPips = 0;
			for (int y = 0; y < pipDetections.toArray().length; y++) {
				Rect pipRect = pipDetections.toArray()[y]; // Provides the relative position of the pips to the dice ROI
				/*
				 * Finds the absolute center of a pip. diceRect.x and diceRect.y provides the top-left position of the dice ROI. pipRect.x and pipRect.y provides the top-left position of the pip ROI.
				 * Normally, to find a center of an object with size (w, h) with the top-left point (x, y), we divide the width and height by two, and then add on the x pos to the width and y pos to
				 * the height. Now, since pipDetections only provide relative positioning to the dice ROI, we also need to add the dice position to find our absolute center position (aka relative to
				 * the entire image).
				 */
				Point center = new Point(diceRect.x + pipRect.x + pipRect.width / 2, diceRect.y + pipRect.y + pipRect.height / 2);
				Imgproc.ellipse(image, center, new Size(pipRect.width / 2, pipRect.height / 2), 0, 0, 360, new Scalar(255, 0, 255), 1, 0, 0);
				
				numPips++;
			}
			
			// Disgusting or Elegant? Discuss.
			// If there's more than 6 pips, then ignore the data and add the last valid value, or zero, if there isn't an previous valid value.
			numPips = ((numPips <= 6) ? numPips : ((data.get(i) != null) ? data.get(i) : 0));
			
			/*
			 * Basically, if there's 5 or less dice, we store the data. if we somehow find more than 5 dice, any detections we do will be worthless anyways, because there's no way to tell the actually
			 * dice from the garbage. Thus, if that happens, we just ignore the data.
			 */
			if (diceDetections.toArray().length < 6)
				data.put(i, numPips);
				
			curDetect += "d: " + i + " n: " + numPips + "\t"; // formats our console output
			
		}
		
		// System.out.println(curDetect); // Output to console of the data for debugging purposes.
		
		return image;
	}
	
	/**
	 * Gets the current dice data.
	 * 
	 * @return dice data
	 */
	public Map<Integer, Integer> getData() {
		return data;
	}
}
