/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 9, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver;

import java.awt.Color;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableText;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.ECommandType;
import edu.dhbw.mannheim.tigers.sumatra.util.PIDController;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TrajPathDriver extends PositionDriver
{
	@SuppressWarnings("unused")
	private static final Logger		log							= Logger.getLogger(TrajPathDriver.class.getName());
	
	private TrajPath						path							= null;
	
	@Configurable
	private static float					pidXYp						= 0.003f;
	@Configurable
	private static float					pidXYd						= 0.0f;
	@Configurable
	private static float					pidXYi						= 0.0f;
	@Configurable
	private static float					pidWp							= 2f;
	@Configurable
	private static float					pidWd							= 0.0f;
	@Configurable
	private static float					pidWi							= 0.0f;
	
	@Configurable
	private static float					delay							= 0.07f;
	
	@Configurable
	private static ECommandMode		commandType					= ECommandMode.TRAJ_POS;
	
	@Configurable
	private static float					switchToNearTol			= 500;
	
	@Configurable
	private static float					switchToFarTol				= 700;
	
	
	@Configurable(comment = "time [s]")
	private static float					positionMoveLookAhead	= 0.3f;
	
	@Configurable(comment = "move mode for sending positions")
	private static EPositionMoveMode	positionMoveMode			= EPositionMoveMode.POS_ON_TANGENT;
	
	
	private enum EPositionMoveMode
	{
		POS_ON_SPLINE,
		POS_ON_TANGENT
	}
	
	
	private transient PIDController	pidX	= new PIDController(pidXYp, pidXYi, pidXYd);
	private transient PIDController	pidY	= new PIDController(pidXYp, pidXYi, pidXYd);
	private transient PIDController	pidW	= new PIDController(pidWp, pidWi, pidWd, true);
	
	
	private enum ECommandMode
	{
		TRAJ_ONLY,
		POS_ONLY,
		VEL_ONLY,
		TRAJ_POS,
		VEL_POS,
	}
	
	
	private EState	state	= EState.FAR;
	
	private enum EState
	{
		FAR,
		NEAR
	}
	
	
	/**
	 * 
	 */
	public TrajPathDriver()
	{
		setCommandType();
	}
	
	
	private void setCommandType()
	{
		switch (commandType)
		{
			case POS_ONLY:
				clearSupportedCommands();
				addSupportedCommand(ECommandType.POS);
				break;
			case TRAJ_ONLY:
				clearSupportedCommands();
				addSupportedCommand(ECommandType.TRAJ_PATH);
				break;
			case TRAJ_POS:
				clearSupportedCommands();
				switch (state)
				{
					case FAR:
						addSupportedCommand(ECommandType.TRAJ_PATH);
						break;
					case NEAR:
						addSupportedCommand(ECommandType.POS);
						break;
				}
				break;
			case VEL_ONLY:
				clearSupportedCommands();
				addSupportedCommand(ECommandType.VEL);
				break;
			case VEL_POS:
				clearSupportedCommands();
				switch (state)
				{
					case FAR:
						addSupportedCommand(ECommandType.VEL);
						break;
					case NEAR:
						addSupportedCommand(ECommandType.POS);
						break;
				}
		}
	}
	
	
	@Override
	public void update(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		if (path == null)
		{
			return;
		}
		float dist2Dest = GeoMath.distancePP(bot.getPos(), path.getFinalDestination());
		
		EState curState = state;
		if (dist2Dest < switchToNearTol)
		{
			state = EState.NEAR;
		} else if (dist2Dest > switchToFarTol)
		{
			state = EState.FAR;
		}
		if (curState != state)
		{
			setCommandType();
		}
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.TRAJ_PATH;
	}
	
	
	@Override
	public TrajPath getPath()
	{
		return path;
	}
	
	
	/**
	 * @param path the path to set
	 */
	public final void setPath(final TrajPath path)
	{
		this.path = path;
	}
	
	
	@Override
	public void decoratePath(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		super.decoratePath(bot, shapes);
		if ((path != null) && (path.getRemainingTime() > 0))
		{
			TrajPath pathCopy = new TrajPath(path);
			pathCopy.updateCurrentTime();
			shapes.add(pathCopy);
		}
	}
	
	
	@Override
	public boolean isDone()
	{
		return (path != null) && (path.getRemainingTime() <= 0);
	}
	
	
	@Override
	public IVector3 getNextVelocity(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		TrajPath curPath = path;
		
		if (curPath == null)
		{
			return AVector3.ZERO_VECTOR;
		}
		
		float t = curPath.getVeryCurrentTime();
		IVector2 curDest = curPath.getPosition(t);
		IVector2 curVel = curPath.getVelocity(t);
		float orient = curPath.getOrientation(t);
		float aVel = curPath.getaVel(t);
		
		IVector2 pastDest = curPath.getPosition(t - delay);
		float pastOrient = curPath.getOrientation(t - delay);
		
		pidX.setSetpoint(curDest.x());
		pidY.setSetpoint(curDest.y());
		pidW.setSetpoint(orient);
		pidX.update((bot.getPos().x()) + (curDest.x() - pastDest.x()));
		pidY.update((bot.getPos().y()) + (curDest.y() - pastDest.y()));
		pidW.update(bot.getAngle() + (orient - pastOrient));
		
		
		IVector2 error = new Vector2(pidX.getResult(), pidY.getResult());
		IVector2 outVel = curVel.addNew(error);
		
		float errorW = pidW.getResult();
		float outVelW = aVel + errorW;
		return new Vector3(outVel, outVelW);
	}
	
	
	@Override
	public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
	{
		TrajPath curPath = path;
		
		if (curPath == null)
		{
			return super.getNextDestination(bot, wFrame);
		}
		
		float ct = curPath.getVeryCurrentTime();
		IVector2 curDest;
		float orient;
		switch (positionMoveMode)
		{
			case POS_ON_SPLINE:
			{
				float t = Math.min(ct + positionMoveLookAhead, curPath.getTotalTime());
				curDest = (curPath.getPosition(t));
				orient = curPath.getOrientation(t);
				break;
			}
			case POS_ON_TANGENT:
			{
				float t = Math.min(ct, curPath.getTotalTime());
				IVector2 pos = curPath.getPosition(t);
				IVector2 dest = pos.addNew(curPath.getVelocity(t).multiply(positionMoveLookAhead * 1000));
				curDest = dest;
				orient = curPath.getOrientation(t);
				break;
			}
			default:
				throw new IllegalArgumentException();
		}
		
		return new Vector3(curDest, orient);
	}
	
	
	@Override
	public void decoratePathDebug(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
	{
		shapes.add(new DrawableText(bot.getPos(), state.name(), Color.red));
	}
	
	
}
