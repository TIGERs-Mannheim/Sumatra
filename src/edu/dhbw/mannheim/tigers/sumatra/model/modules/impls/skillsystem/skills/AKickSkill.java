/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 5, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * Basics methods for kicking (straight or chip)
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public abstract class AKickSkill extends AMoveSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log						= Logger.getLogger(AKickSkill.class.getName());
	
	private static final float		TOLERANCE				= 30;
	
	private static final float		BALL_SPEED_THRES		= 3f;
	
	private IVector2					lookAtTarget;
	private float						initialKickerLevel	= 0;
	private float						kickerDisChargedTreshold;
	private long						timeStart				= Long.MAX_VALUE;
	private long						timeout					= TimeUnit.MILLISECONDS.toNanos(2000);
	
	/**
	 */
	public enum EEvent
	{
		/**  */
		KICKED,
		/**  */
		RETRY,
		/**  */
		TIMED_OUT,
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param skillName
	 */
	public AKickSkill(ESkillName skillName)
	{
		super(skillName);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		initialKickerLevel = bot.getBot().getKickerLevel();
		kickerDisChargedTreshold = AIConfig.getSkills(bot.getBotType()).getKickerDischargeTreshold();
		timeStart = System.nanoTime();
		return cmds;
	}
	
	
	@Override
	protected boolean isComplete(TrackedTigerBot bot)
	{
		if ((bot.getBotType() != EBotType.GRSIM)
				&& (((bot.getBotType() != EBotType.TIGER_V2) || ((TigerBotV2) bot.getBot()).getLogMovement())))
		{
			float chargeDiff = (initialKickerLevel - bot.getBot().getKickerLevel());
			if ((chargeDiff) > kickerDisChargedTreshold)
			{
				log.debug("Completed due to kicker discharge: " + initialKickerLevel + " - "
						+ bot.getBot().getKickerLevel() + " < " + chargeDiff);
				notifyEvent(EEvent.KICKED);
				return true;
			}
		} else if (getWorldFrame().ball.getVel().getLength2() > BALL_SPEED_THRES)
		{
			log.debug("Completed due to ball speed");
			notifyEvent(EEvent.KICKED);
			return true;
		}
		long diff = (System.nanoTime() - timeStart);
		if (diff > timeout)
		{
			log.debug("timed out.");
			notifyEvent(EEvent.TIMED_OUT);
			return true;
		}
		return false;
	}
	
	
	@Override
	protected List<ACommand> doCalcExitActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		getDevices().allOff(cmds);
		stopMove(cmds);
		return cmds;
	}
	
	
	protected void calcSpline(TrackedTigerBot bot)
	{
		if (lookAtTarget == null)
		{
			lookAtTarget = bot.getPos().addNew(
					new Vector2(bot.getAngle()).multiply(AIConfig.getGeometry().getBotRadius() * 8));
			log.trace("lookAtTarget: " + lookAtTarget);
		}
		
		List<IVector2> nodes1 = new LinkedList<IVector2>();
		List<IVector2> nodes2 = new LinkedList<IVector2>();
		
		nodes1.add(DistanceUnit.MILLIMETERS.toMeters(bot.getPos()));
		
		float dist = GeoMath.distancePP(getWorldFrame().ball.getPos(), bot.getPos());
		log.trace("dist: " + dist);
		float positioningPre = AIConfig.getGeneral(bot.getBotType()).getPositioningPreAiming();
		if (dist > positioningPre)
		{
			nodes1.add(DistanceUnit.MILLIMETERS.toMeters(GeoMath.stepAlongLine(getWorldFrame().ball.getPos(),
					lookAtTarget, -positioningPre)));
		} else
		{
			nodes1.add(DistanceUnit.MILLIMETERS.toMeters(bot.getPos()));
		}
		nodes2.add(nodes1.get(nodes1.size() - 1));
		float positioningPost = AIConfig.getGeneral(bot.getBotType()).getPositioningPostAiming();
		if (dist > positioningPost)
		{
			nodes2.add(DistanceUnit.MILLIMETERS.toMeters(GeoMath.stepAlongLine(getWorldFrame().ball.getPos(),
					lookAtTarget, -positioningPost)));
		}
		nodes2.add(DistanceUnit.MILLIMETERS.toMeters(GeoMath.stepAlongLine(getWorldFrame().ball.getPos(), lookAtTarget,
				-AIConfig.getGeometry().getBotRadius() + TOLERANCE)));
		nodes2.add(DistanceUnit.MILLIMETERS.toMeters(GeoMath.stepAlongLine(getWorldFrame().ball.getPos(), lookAtTarget,
				AIConfig.getGeometry().getBotRadius() + TOLERANCE)));
		log.trace("nodes: " + nodes1 + " " + nodes2);
		
		SplineTrajectoryGenerator gen = createDefaultGenerator(bot);
		gen.setReducePathScore(0.2f);
		gen.setPositionTrajParams(2, 2);
		
		float finalW = lookAtTarget.subtractNew(getWorldFrame().ball.getPos()).getAngle();
		IVector2 middleSpeed = Vector2.ZERO_VECTOR;
		SplinePair3D pair1 = gen.create(nodes1, bot.getVel(), middleSpeed, bot.getAngle(), finalW, bot.getaVel(), 0f);
		SplinePair3D pair2 = gen.create(nodes2, middleSpeed, Vector2.ZERO_VECTOR, finalW, finalW, 0f, 0f);
		pair1.append(pair2);
		
		setNewTrajectory(pair1, System.nanoTime());
		List<IVector2> allNodes = new LinkedList<IVector2>();
		allNodes.add(getBot().getPos());
		allNodes.add(getBot().getPos());
		visualizePath(getBot().getId(), allNodes, pair1);
	}
	
	
	/**
	 * 
	 * Resets the time out counter. Can be used to implement a retry.
	 * Not usable with grSim, because of side effects.
	 * 
	 * @see KickAutoSkill
	 * 
	 */
	protected void resetTimeOut(TrackedTigerBot bot)
	{
		if (bot.getBotType() != EBotType.GRSIM)
		{
			timeStart = System.nanoTime();
		} else
		{
			log.info("ResetTimeOut diabled, because of side effects by using grSim.");
		}
	}
	
	
	/**
	 * @return the lookAtTarget
	 */
	public final IVector2 getLookAtTarget()
	{
		return lookAtTarget;
	}
	
	
	/**
	 * @param lookAtTarget the lookAtTarget to set
	 */
	public final void setLookAtTarget(IVector2 lookAtTarget)
	{
		this.lookAtTarget = lookAtTarget;
	}
	
	
	/**
	 * @return the timeout
	 */
	public final long getTimeout()
	{
		return timeout;
	}
	
	
	/**
	 * @param timeout the timeout to set
	 */
	public final void setTimeout(long timeout)
	{
		this.timeout = timeout;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
