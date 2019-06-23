/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.ECommandType;


/**
 * A path driver gives you your next destination, given a path or spline
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IPathDriver
{
	/**
	 * @return
	 */
	TrajPath getPath();
	
	
	/**
	 * Get the next point+orientation on the path, given our current position and velocity
	 * 
	 * @param bot
	 * @param wFrame
	 * @return [mm,mm,rad]
	 */
	IVector3 getNextDestination(TrackedTigerBot bot, WorldFrame wFrame);
	
	
	/**
	 * Get next/current velocity (x,y,w) for the robot
	 * 
	 * @param bot
	 * @param wFrame
	 * @return [m/s,m/s,rad/s]
	 */
	IVector3 getNextVelocity(TrackedTigerBot bot, WorldFrame wFrame);
	
	
	/**
	 * @param bot
	 * @param wFrame
	 * @param dt
	 * @return
	 */
	IVector3 getNextLocalVelocity(final TrackedTigerBot bot, final WorldFrame wFrame, final float dt);
	
	
	/**
	 * @param bot
	 * @param wFrame
	 */
	void update(final TrackedTigerBot bot, final WorldFrame wFrame);
	
	
	/**
	 * The moving speed should be considered, if not done on a higher level
	 * 
	 * @param speed
	 */
	void setMovingSpeed(EMovingSpeed speed);
	
	
	/**
	 * Add shapes to the list, if you like
	 * 
	 * @param bot
	 * @param shapes
	 */
	void decoratePath(TrackedTigerBot bot, final List<IDrawableShape> shapes);
	
	
	/**
	 * Add shapes to the list, if you like
	 * 
	 * @param bot
	 * @param shapes
	 */
	void decoratePathDebug(TrackedTigerBot bot, final List<IDrawableShape> shapes);
	
	
	/**
	 * @return
	 */
	List<ECommandType> getSupportedCommands();
	
	
	/**
	 * @return
	 */
	boolean isDone();
	
	
	/**
	 * @return
	 */
	EPathDriver getType();
}
