/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.03.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * This skills asks sysiphus to create a path, makes a spline out of it and then follows the spline.
 * 
 * @author AndreR
 * 
 */
public class MoveToSkill extends AMoveSkill
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log		= Logger.getLogger(MoveToSkill.class.getName());
	private MovementCon				moveCon;
	
	private boolean					dribble	= false;
	
	private EKickKind					kickKind	= EKickKind.NONE;
	
	private enum EKickKind
	{
		STRAIGHT,
		CHIP,
		NONE;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Move to a target with an orientation as specified in the moveCon.
	 * 
	 * @param moveCon Movement condition.
	 */
	public MoveToSkill(MovementCon moveCon)
	{
		this(ESkillName.MOVE_TO, moveCon);
	}
	
	
	protected MoveToSkill(ESkillName skillName, MovementCon moveCon)
	{
		super(skillName);
		this.moveCon = moveCon;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected boolean isComplete(TrackedTigerBot bot)
	{
		if (checkIsComplete(bot))
		{
			log.trace("completed due to move and rotate conditions fulfilled");
			return true;
		}
		return false;
	}
	
	
	private boolean checkIsComplete(TrackedTigerBot bot)
	{
		// Check conditions
		boolean moveComplete = true;
		boolean rotateComplete = true;
		
		if (moveCon.getDestCon().isActive())
		{
			moveComplete = moveCon.checkCondition(getWorldFrame(), bot.getId()) == EConditionState.FULFILLED;
		}
		
		if (moveCon.getAngleCon().isActive())
		{
			rotateComplete = moveCon.getAngleCon().checkCondition(getWorldFrame(), bot.getId()) == EConditionState.FULFILLED;
		}
		if (moveComplete && rotateComplete)
		{
			return true;
		}
		
		return false;
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		generateNewTrajectory(bot);
		
		return cmds;
	}
	
	
	@Override
	protected void periodicProcess(TrackedTigerBot bot, List<ACommand> cmds)
	{
		// float chargeDiff = (initialKickerLevel - bot.getBot().getKickerLevel());
		// if ((chargeDiff) > kickerDisChargedTreshold)
		// if(bot.getBot().)
		
		if (getWorldFrame().ball.getPos().equals(bot.getPos(), 500))
		{
			if (!dribble)
			{
				getDevices().dribble(cmds, true);
				dribble = true;
			}
			if (getMoveCon().isShoot())
			{
				// check if target is block or not use chip if blocked
				float tripleBallRadius = 3 * AIConfig.getGeometry().getBallRadius();
				boolean useStraight = GeoMath.p2pVisibility(getWorldFrame(), getBot().getPos(), getMoveCon()
						.getLookAtTarget(), tripleBallRadius, new ArrayList<BotID>());
				
				if ((kickKind != EKickKind.STRAIGHT) && useStraight)
				{
					if (getMoveCon().isPass())
					{
						final float kickLength = GeoMath.distancePP(getBot().getPos(), getMoveCon().getLookAtTarget());
						float ballEndVelocity = AIConfig.getRoles().getPassSenderBallEndVel();
						getDevices().kick(cmds, kickLength, ballEndVelocity);
					} else
					{
						getDevices().kickMax(cmds);
					}
				} else if ((kickKind != EKickKind.CHIP) && !useStraight)
				{
					float kickLength = GeoMath.distancePP(getMoveCon().getLookAtTarget(), getMoveCon().getDestCon()
							.getDestination());
					getDevices().chipStop(cmds, kickLength, 1);
				}
			}
		} else
		{
			if (dribble)
			{
				getDevices().dribble(cmds, false);
				getDevices().disarm(cmds);
				dribble = false;
			}
		}
		generateNewTrajectory(bot);
	}
	
	
	private void generateNewTrajectory(TrackedTigerBot bot)
	{
		// Process path planning
		Path path = getSisyphus().calcPath(getWorldFrame(), bot.getId(), getTrajectoryTime(), moveCon);
		
		if (path == null)
		{
			log.error("No path received from calcPath!");
			return;
		}
		
		if (path.isChanged() || (getPositionTraj() == null) || (super.isComplete(bot) && !checkIsComplete(bot)))
		{
			onNewSpline();
			setNewTrajectory(path.getHermiteSpline(), path.getTimestamp());
		}
		
		
	}
	
	
	/**
	 */
	protected void onNewSpline()
	{
		log.trace("New spline created");
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the moveCon
	 */
	public MovementCon getMoveCon()
	{
		return moveCon;
	}
	
	
	@Override
	protected List<ACommand> doCalcExitActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		getSisyphus().stopPathPlanning(bot.getId());
		getDevices().disarm(cmds);
		getDevices().dribble(cmds, false);
		if (moveCon.getVelAtDestination().equals(Vector2.ZERO_VECTOR, 0.1f))
		{
			stopMove(cmds);
		}
		return super.doCalcExitActions(bot, cmds);
	}
}
