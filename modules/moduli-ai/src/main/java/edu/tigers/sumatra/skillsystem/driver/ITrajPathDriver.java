/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 21, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITrajPathDriver extends IPathDriver
{
	
	/**
	 * @param path the path to set
	 * @param finalDestination
	 * @param targetAngle
	 */
	void setPath(final TrajectoryWithTime<IVector2> path, IVector2 finalDestination, double targetAngle);
	
	
	/**
	 * @return the path
	 */
	TrajectoryWithTime<IVector2> getPath();
	
}
