/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 18, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.LinearPolicy;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.ECommandType;
import edu.dhbw.mannheim.tigers.sumatra.util.learning.IPolicyController;


/**
 * Base class for any driver
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ABaseDriver implements IPathDriver
{
	private boolean						done					= false;
	private final List<ECommandType>	supportedCommands	= new ArrayList<>(5);
	
	private final IPolicyController	velocityPolicy		= new LinearPolicy();
	private List<IDrawableShape>		shapes				= new ArrayList<>();
	private List<IDrawableShape>		shapesDebug			= new ArrayList<>();
	
	
	protected final void addSupportedCommand(final ECommandType cmd)
	{
		supportedCommands.add(cmd);
	}
	
	
	protected final void removeSupportedCommand(final ECommandType cmd)
	{
		supportedCommands.remove(cmd);
	}
	
	
	protected final void clearSupportedCommands()
	{
		supportedCommands.clear();
	}
	
	
	@Override
	public void setMovingSpeed(final EMovingSpeed speed)
	{
	}
	
	
	/**
	 * @param bot
	 * @param wFrame
	 */
	@Override
	public void update(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
	}
	
	
	@Override
	public void decoratePath(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		shapes.addAll(this.shapes);
	}
	
	
	@Override
	public void decoratePathDebug(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		shapes.addAll(shapesDebug);
	}
	
	
	@Override
	public List<ECommandType> getSupportedCommands()
	{
		return supportedCommands;
	}
	
	
	@Override
	public boolean isDone()
	{
		return done;
	}
	
	
	/**
	 * @param done the done to set
	 */
	public final void setDone(final boolean done)
	{
		this.done = done;
	}
	
	
	/**
	 * @return the velocityPolicy
	 */
	protected IPolicyController getVelocityPolicy()
	{
		return velocityPolicy;
	}
	
	
	/**
	 * @param shapes the shapes to set
	 */
	public final void setShapes(final List<IDrawableShape> shapes)
	{
		this.shapes = shapes;
	}
	
	
	/**
	 * @param shapesDebug the shapesDebug to set
	 */
	public final void setShapesDebug(final List<IDrawableShape> shapesDebug)
	{
		this.shapesDebug = shapesDebug;
	}
	
	
	@Override
	public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public IVector3 getNextVelocity(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public TrajPath getPath()
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public IVector3 getNextLocalVelocity(final TrackedTigerBot bot, final WorldFrame wFrame, final float dt)
	{
		IVector3 vel = getNextVelocity(bot, wFrame);
		Vector2 localVel = AiMath.convertGlobalBotVector2Local(vel.getXYVector(), bot.getAngle());
		
		float velComp = -vel.z() * dt;
		localVel.turn(velComp);
		return new Vector3(localVel.x(), localVel.y(), vel.z());
	}
}
