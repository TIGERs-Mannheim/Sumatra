/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 18, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.skillsystem.MovementCon;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ShapeMap;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Base class for any driver
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ABaseDriver implements IPathDriver
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(ABaseDriver.class.getName());
	
	private boolean					done					= false;
	private final Set<EBotSkill>	supportedCommands	= new HashSet<>();
	private MovementCon				moveCon				= null;
	
	private final ShapeMap			shapes				= new ShapeMap();
	
	private boolean					firstUpdate			= false;
	private double						dt						= 0;
	private long						tLast					= 0;
	
	
	/**
	 * @param cmd
	 */
	public final void addSupportedCommand(final EBotSkill cmd)
	{
		supportedCommands.add(cmd);
	}
	
	
	/**
	 * @param cmd
	 */
	public final void removeSupportedCommand(final EBotSkill cmd)
	{
		supportedCommands.remove(cmd);
	}
	
	
	/**
	 * 
	 */
	public final void clearSupportedCommands()
	{
		supportedCommands.clear();
	}
	
	
	@Override
	public Set<EBotSkill> getSupportedCommands()
	{
		return supportedCommands;
	}
	
	
	/**
	 * @param done the done to set
	 */
	protected final void setDone(final boolean done)
	{
		this.done = done;
	}
	
	
	@Override
	public ShapeMap getShapes()
	{
		return shapes;
	}
	
	
	@Override
	public void setShapes(final EShapesLayer layer, final List<IDrawableShape> shapes)
	{
		this.shapes.put(layer, shapes);
	}
	
	
	/**
	 * @param bot
	 * @param wFrame
	 */
	@Override
	public void update(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
		if (!firstUpdate)
		{
			onFirstUpdate(bot, aBot, wFrame);
			firstUpdate = true;
			tLast = wFrame.getTimestamp();
			dt = 0.001;
		} else
		{
			dt = (wFrame.getTimestamp() - tLast) / 1e9;
			tLast = wFrame.getTimestamp();
			if (dt == 0)
			{
				log.warn("Zero dt?!");
				dt = 0.001;
			}
		}
	}
	
	
	protected void onFirstUpdate(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
		ConfigRegistration.applySpezis(this, "skills", aBot.getType().name());
	}
	
	
	@Override
	public boolean isDone()
	{
		return done;
	}
	
	
	@Override
	public IVector3 getNextDestination(final ITrackedBot bot, final WorldFrame wFrame)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public IVector3 getNextVelocity(final ITrackedBot bot, final WorldFrame wFrame)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public IVector3 getNextLocalVelocity(final ITrackedBot bot, final WorldFrame wFrame, final double dt)
	{
		IVector3 vel = getNextVelocity(bot, wFrame);
		Vector2 localVel = GeoMath.convertGlobalBotVector2Local(vel.getXYVector(), bot.getAngle());
		
		double velComp = -vel.z() * dt;
		localVel.turn(velComp);
		return new Vector3(localVel.x(), localVel.y(), vel.z());
	}
	
	
	/**
	 * @return the dt
	 */
	public double getDt()
	{
		return dt;
	}
	
	
	/**
	 * @return the moveCon
	 */
	@Override
	public MovementCon getMoveCon()
	{
		return moveCon;
	}
	
	
	/**
	 * @param moveCon the moveCon to set
	 */
	@Override
	public final void setMoveCon(final MovementCon moveCon)
	{
		assert this.moveCon == null;
		this.moveCon = moveCon;
	}
}
