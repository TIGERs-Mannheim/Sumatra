/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 3, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * Receive a ball and directly kick to a target
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class RedirectBallSkill extends AMoveSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log					= Logger.getLogger(RedirectBallSkill.class.getName());
	private static final float		BALL_MOVE_TOL		= 0.1f;
	private static final float		NEAR_BALL_TOL		= 150f;
	
	private float						positioningTol;
	private float						maxShootVel;
	
	private final IVector2			shootTarget;
	private final IVector2			position;
	private long						lastSplineUpdate	= System.nanoTime();
	private long						wasFirstNearBall	= Long.MAX_VALUE;
	private float						destOrientation	= 0;
	
	private boolean					stay					= false;
	
	private boolean					useChipper			= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param shootTarget
	 * @param position
	 */
	public RedirectBallSkill(IVector2 shootTarget, IVector2 position)
	{
		super(ESkillName.REDIRECT_BALL);
		this.shootTarget = shootTarget;
		this.position = position;
	}
	
	
	/**
	 * @param shootTarget
	 * @param position
	 * @param stay
	 */
	public RedirectBallSkill(IVector2 shootTarget, IVector2 position, boolean stay)
	{
		super(ESkillName.REDIRECT_BALL);
		this.shootTarget = shootTarget;
		this.position = position;
		this.stay = stay;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected boolean isComplete(TrackedTigerBot bot)
	{
		// this will cause too early cancellation!
		// if (bot.hasBallContact())
		// {
		// log.debug("had ball contact");
		// return true;
		// }
		if (((System.nanoTime() - wasFirstNearBall) > TimeUnit.MILLISECONDS.toNanos(500)))
		{
			log.debug("was near ball");
			return true;
		}
		return false;
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		positioningTol = AIConfig.getTolerances(getBot().getBotType()).getPositioning();
		maxShootVel = AIConfig.getSkills(bot.getBotType()).getMaxShootVelocity();
		
		// if (useChipper)
		// {
		// float kickLength = AiMath.determinChipShotTarget(getWorldFrame(), 300, bot.getPos(),
		// chipLandingSpotX);
		// getDevices().chipRoll(cmds, kickLength);
		// } else
		// {
		getDevices().kickMax(cmds);
		// }
		calcSpline(bot);
		return cmds;
	}
	
	
	@Override
	protected List<ACommand> doCalcExitActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		getDevices().disarm(cmds);
		return cmds;
	}
	
	
	@Override
	protected void periodicProcess(TrackedTigerBot bot, List<ACommand> cmds)
	{
		float ballNewDistance = GeoMath.distancePP(getWorldFrame().ball.getPos(), bot.getPos());
		if ((wasFirstNearBall == Long.MAX_VALUE) && (ballNewDistance < NEAR_BALL_TOL))
		{
			wasFirstNearBall = System.nanoTime();
		}
		
		if (((System.nanoTime() - lastSplineUpdate) > TimeUnit.MILLISECONDS.toNanos(300)))
		{
			calcSpline(bot);
		}
	}
	
	
	private void calcSpline(TrackedTigerBot bot)
	{
		final IVector2 dest = getReceivePoint();
		List<IVector2> path = new LinkedList<IVector2>();
		path.add(dest);
		IVector2 kickerPos = AiMath.getBotKickerPos(dest, bot.getAngle());
		float shootAngle = shootTarget.subtractNew(kickerPos).getAngle();
		float ballVel = AIConfig.getRoles().getPassSenderBallEndVel()
				+ AIConfig.getRoles().getIndirectReceiverBallVelCorrection();
		IVector2 ballSpeed = kickerPos.subtractNew(getWorldFrame().ball.getPos()).scaleTo(ballVel);
		destOrientation = GeoMath.approxOrientationBallDamp(maxShootVel, ballSpeed, getBot().getBotType(), getBot()
				.getAngle(), shootAngle);
		
		if (getPositionTraj() != null)
		{
			IVector2 curDest = DistanceUnit.METERS.toMillimeters(getPositionTraj().getPosition(
					getPositionTraj().getTotalTime()));
			float dist = GeoMath.distancePP(dest, curDest);
			if (dist > positioningTol)
			{
				log.debug("dist is " + dist + "; calc new spline; curDest=" + curDest + " dest=" + dest);
			} else if (SumatraMath.isEqual(getRotationTraj().getPosition(getRotationTraj().getTotalTime()),
					destOrientation))
			{
				return;
			}
		}
		
		createSpline(bot, path, destOrientation);
		lastSplineUpdate = System.nanoTime();
	}
	
	
	/**
	 * Corrected destination of the bot
	 * 
	 * @return
	 */
	private IVector2 getReceivePoint()
	{
		if (stay)
		{
			return getBot().getPos();
		}
		// negation hier rein oder raus ??? ?
		if (getWorldFrame().ball.getVel().equals(Vector2.ZERO_VECTOR, BALL_MOVE_TOL))
		{
			return position;
		}
		IVector2 kickerPos = AiMath.getBotKickerPos(position, getBot().getAngle());
		Vector2 leadPoint = GeoMath.leadPointOnLine(kickerPos, new Line(getWorldFrame().ball.getPos(),
				getWorldFrame().ball.getVel()));
		IVector2 dir = new Vector2(destOrientation).scaleTo(-75);
		return leadPoint.addNew(dir);
	}
	
	
	/**
	 * @return the destOrientation
	 */
	public final float getDestOrientation()
	{
		return destOrientation;
	}
	
	
	/**
	 * @return the useChipper
	 */
	public final boolean isUseChipper()
	{
		return useChipper;
	}
	
	
	/**
	 * @return the destination of the redirect Role
	 */
	public final IVector2 getRedirectDestination()
	{
		return position;
	}
	
	
	/**
	 * @param useChipper the useChipper to set
	 */
	public final void setUseChipper(boolean useChipper)
	{
		this.useChipper = useChipper;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
