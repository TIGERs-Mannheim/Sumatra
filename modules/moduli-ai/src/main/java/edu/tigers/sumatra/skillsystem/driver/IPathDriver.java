/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import java.util.List;
import java.util.Set;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.skillsystem.MovementCon;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ShapeMap;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * A path driver gives you your next destination, given a path or spline
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IPathDriver
{
	/**
	 * Get the next point+orientation on the path, given our current position and velocity
	 * 
	 * @param bot
	 * @param wFrame
	 * @return [mm,mm,rad]
	 */
	IVector3 getNextDestination(ITrackedBot bot, WorldFrame wFrame);
	
	
	/**
	 * Get next/current velocity (x,y,w) for the robot
	 * 
	 * @param bot
	 * @param wFrame
	 * @return [m/s,m/s,rad/s]
	 */
	IVector3 getNextVelocity(ITrackedBot bot, WorldFrame wFrame);
	
	
	/**
	 * @param bot
	 * @param wFrame
	 * @param dt
	 * @return
	 */
	IVector3 getNextLocalVelocity(final ITrackedBot bot, final WorldFrame wFrame, final double dt);
	
	
	/**
	 * @param bot
	 * @param aBot TODO
	 * @param wFrame
	 */
	void update(final ITrackedBot bot, ABot aBot, final WorldFrame wFrame);
	
	
	/**
	 * @return
	 */
	ShapeMap getShapes();
	
	
	/**
	 * @return
	 */
	Set<EBotSkill> getSupportedCommands();
	
	
	/**
	 * @return
	 */
	boolean isDone();
	
	
	/**
	 * @return
	 */
	EPathDriver getType();
	
	
	/**
	 * @param layer
	 * @param shapes
	 */
	void setShapes(final EShapesLayer layer, final List<IDrawableShape> shapes);
	
	
	/**
	 * @return
	 */
	MovementCon getMoveCon();
	
	
	/**
	 * @param moveCon
	 */
	void setMoveCon(MovementCon moveCon);
}
