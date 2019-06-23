/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.cam;

import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.cam.data.ACamObject;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Filter out unwanted objects from camera, for example balls from another field half
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CamObjectFilter
{
	@Configurable(comment = "Perform filtering of cam objects")
	private static boolean	filterEnabled		= false;
	
	@Configurable(comment = "P1 of rectangle defining a range where objects are ignored")
	private static IVector2	exclusionRectP1	= Vector2.ZERO_VECTOR;
	@Configurable(comment = "P2 of rectangle defining a range where objects are ignored")
	private static IVector2	exclusionRectP2	= Vector2.ZERO_VECTOR;
	
	@Configurable(comment = "Specified cameras will be filtered")
	private static Integer[]	excludedCameras	= {};
	
	static
	{
		ConfigRegistration.registerClass("cam", CamObjectFilter.class);
	}
	
	
	/**
	 * @param camDetectionFrame detections from a single camera
	 * @return detection frame with filtered objects
	 */
	public CamDetectionFrame filter(CamDetectionFrame camDetectionFrame)
	{
		if (!filterEnabled)
		{
			return camDetectionFrame;
		}
		List<CamRobot> newBotY = filterCamObjects(camDetectionFrame.getRobotsYellow());
		List<CamRobot> newBotB = filterCamObjects(camDetectionFrame.getRobotsBlue());
		List<CamBall> newBalls = filterCamObjects(camDetectionFrame.getBalls());
		
		return new CamDetectionFrame(camDetectionFrame, newBalls, newBotY, newBotB);
	}
	
	
	private boolean isWithinExclusionRectangle(final IVector2 p)
	{
		Rectangle rect = Rectangle.fromPoints(exclusionRectP1, exclusionRectP2);
		return rect.isPointInShape(p, -0.00001f);
	}
	
	
	private <T extends ACamObject> List<T> filterCamObjects(final List<T> incoming)
	{
		List<T> newObjects = new ArrayList<>();
		for (T newBall : incoming)
		{
			if (isFilteredCamera(newBall.getCameraId())
					|| isWithinExclusionRectangle(newBall.getPos().getXYVector()))
			{
				continue;
			}
			newObjects.add(newBall);
		}
		return newObjects;
	}
	
	
	private boolean isFilteredCamera(int camId)
	{
		for (int i : excludedCameras)
		{
			if (i == camId)
			{
				return true;
			}
		}
		return false;
	}
}
