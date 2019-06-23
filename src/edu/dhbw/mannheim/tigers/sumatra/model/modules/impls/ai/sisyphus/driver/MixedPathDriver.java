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
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.ECommandType;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This PathDriver will wrap two drivers. It will use the far one unless the bot is near the destination. Then it uses
 * the close one
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MixedPathDriver extends ABaseDriver
{
	private final IPathDriver	pathDriverFar;
	private final IPathDriver	pathDriverClose;
	private IPathDriver			currentDriver;
	private final IVector2		destination;
	
	@Configurable(comment = "Dist [mm] - this is the threshold distance to destination, when the close path driver will be activated")
	private static float			changeToClosePathDriverThreshold	= 100;
	
	
	/**
	 * @param pathDriverFar
	 * @param pathDriverClose
	 * @param destination
	 */
	public MixedPathDriver(final IPathDriver pathDriverFar, final IPathDriver pathDriverClose, final IVector2 destination)
	{
		this.pathDriverFar = pathDriverFar;
		this.pathDriverClose = pathDriverClose;
		currentDriver = pathDriverFar;
		this.destination = destination;
	}
	
	
	private IPathDriver getCurrentPathDriver(final TrackedTigerBot bot)
	{
		IVector2 currentPos = bot.getPos();
		float dist = GeoMath.distancePP(currentPos, destination);
		
		if (dist < (changeToClosePathDriverThreshold))
		{
			currentDriver = pathDriverClose;
			return pathDriverClose;
		}
		currentDriver = pathDriverFar;
		return pathDriverFar;
	}
	
	
	@Override
	public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		return getCurrentPathDriver(bot).getNextDestination(bot, wFrame);
	}
	
	
	@Override
	public IVector3 getNextVelocity(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		return getCurrentPathDriver(bot).getNextVelocity(bot, wFrame);
	}
	
	
	@Override
	public void setMovingSpeed(final EMovingSpeed speed)
	{
		pathDriverClose.setMovingSpeed(speed);
		pathDriverFar.setMovingSpeed(speed);
	}
	
	
	@Override
	public void decoratePath(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		getCurrentPathDriver(bot).decoratePath(bot, shapes);
	}
	
	
	@Override
	public void decoratePathDebug(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		getCurrentPathDriver(bot).decoratePathDebug(bot, shapes);
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.MIXED;
	}
	
	
	@Override
	public List<ECommandType> getSupportedCommands()
	{
		return currentDriver.getSupportedCommands();
	}
	
	
	@Override
	public boolean isDone()
	{
		return currentDriver.isDone();
	}
}
