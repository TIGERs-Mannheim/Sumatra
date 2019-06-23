/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.10.2010
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValueBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.EMoveToMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.BlockSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipFastSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


// tigers-mannheim.de/git/Sumatra.git


/**
 * <a href="http://www.gockel-09.de/91=92=1a.jpg">
 * Keeper</a> Role for the {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.KeeperPlay}.
 * 
 * @author PhilippP {ph.posovszky@gmail.com}
 */
public class KeeperRole extends ARole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		DEFEND,
		MOVE_TO_PENALTYAREA,
		CHIP_KICK,
		MOVE_CHIP_KICK
	}
	
	private enum EEvent
	{
		OUTSIDE_PENALTYAREA,
		INSIDE_PENALTYAREA,
		CHIP_KICK_DONE,
		CHIP_KICK_CANCELED,
		CHIP_KICK,
		MOVE_CHIP_KICK
	}
	
	@Configurable(comment = "Dist [mm] - Distance to chip ball out of penArea.")
	private static int	chipKickDistance				= 1000;
	
	@Configurable(comment = "Security Dist [mm] - Distance around the penalty area. If the ball is inside this area the keeper tries to kick it away")
	private static int	chipKickDecisionDistance	= 100;
	
	@Configurable(comment = "Speed of the ball [m/s] - If the ball is slower, the bot will try to kick it out of the penalty area.")
	private static float	chipKickDecisionVelocity	= 0.4f;
	
	@Configurable(comment = "Dist [mm] - The distance to the goal line from the initial keeper position")
	private static int	distToGoalLine					= 500;
	
	@Configurable(comment = "Dist [mm] - Longest spline to be computed for the keeper.", speziType = EBotType.class, spezis = { "GRSIM" })
	private static int	maxSplineLength				= 1000;
	
	@Configurable(comment = "Dist [mm] - Distance around the penalty area where the bot is allowed to block.")
	private static int	safetyAroundPenalty			= 400;
	
	private boolean		allowChipkick					= true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public KeeperRole()
	{
		super(ERole.KEEPER);
		setInitialState(new MoveOutsidePenaltyState());
		addTransition(EStateId.DEFEND, EEvent.OUTSIDE_PENALTYAREA, new MoveOutsidePenaltyState());
		addTransition(EStateId.DEFEND, EEvent.MOVE_CHIP_KICK, new MoveChipKickState());
		addTransition(EStateId.MOVE_CHIP_KICK, EEvent.CHIP_KICK, new ChipKickState());
		addTransition(EStateId.MOVE_CHIP_KICK, EEvent.CHIP_KICK_CANCELED, new NormalBlockState());
		addTransition(EStateId.CHIP_KICK, EEvent.CHIP_KICK_DONE, new NormalBlockState());
		addTransition(EStateId.MOVE_TO_PENALTYAREA, EEvent.INSIDE_PENALTYAREA, new NormalBlockState());
		
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Move to the PenaltyArea with Playfinder
	 * 
	 * @author PhilippP
	 */
	private class MoveOutsidePenaltyState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			IMoveToSkill skill = AMoveSkill.createMoveToSkill(EMoveToMode.DO_COMPLETE);
			skill.getMoveCon().setPenaltyAreaAllowed(true);
			skill.getMoveCon().updateDestination(AIConfig.getGeometry().getGoalOur().getGoalCenter()
					.addNew(AVector2.X_AXIS.scaleToNew(distToGoalLine)));
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(getBot().getPos()))
			{
				nextState(EEvent.INSIDE_PENALTYAREA);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			nextState(EEvent.INSIDE_PENALTYAREA);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.MOVE_TO_PENALTYAREA;
		}
		
	}
	
	// --------------------------------------------------------------------------
	private class NormalBlockState implements IRoleState
	{
		private BlockSkill	posSkill;
		
		
		@Override
		public void doEntryActions()
		{
			posSkill = new BlockSkill(distToGoalLine, maxSplineLength);
			setNewSkill(posSkill);
		}
		
		
		@Override
		public void doUpdate()
		{
			// keeper is allowed to drive out of the field behind the goal
			if (((getBot().getPos().x() > (AIConfig.getGeometry().getGoalOur().getGoalCenter().x())) && (!AIConfig
					.getGeometry()
					.getPenaltyAreaOur().isPointInShape(getBot().getPos(), safetyAroundPenalty))))
			{
				nextState(EEvent.OUTSIDE_PENALTYAREA);
			}
			
			if (isBallLyingInPenaltyArea(getAiFrame()) && allowChipkick)
			{
				
				nextState(EEvent.MOVE_CHIP_KICK);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.DEFEND;
		}
		
	}
	
	// --------------------------------------------------------------------------
	
	private class MoveChipKickState implements IRoleState
	{
		private IMoveToSkill	skill	= null;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveSkill.createMoveToSkill(EMoveToMode.DO_COMPLETE);
			skill.getMoveCon().setPenaltyAreaAllowed(true);
			skill.getMoveCon().setBotsObstacle(false);
			skill.getMoveCon().updateLookAtTarget(getWFrame().getBall());
			
			IVector2 ball = getAiFrame().getWorldFrame().ball.getPos();
			IVector2 prepareChipPos = GeoMath.stepAlongLine(ball, AIConfig.getGeometry().getGoalOur().getGoalCenter(),
					140);
			skill.getMoveCon().updateDestination(prepareChipPos);
			
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!isBallLyingInPenaltyArea(getAiFrame()))
			{
				nextState(EEvent.CHIP_KICK_CANCELED);
			}
			
			if (skill.getMoveCon().checkCondition(getWFrame(), getBotID()) == EConditionState.FULFILLED)
			{
				nextState(EEvent.CHIP_KICK);
			}
			TrackedBall ball = getAiFrame().getWorldFrame().ball;
			if (!ball.getVel().equals(AVector2.ZERO_VECTOR, 0.01f))
			{
				IVector2 prepareChipPos = GeoMath.stepAlongLine(ball.getPos(), AIConfig.getGeometry().getGoalOur()
						.getGoalCenter(),
						140);
				skill.getMoveCon().updateDestination(prepareChipPos);
				
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.MOVE_CHIP_KICK;
		}
		
	}
	
	private class ChipKickState implements IRoleState
	{
		
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new ChipFastSkill(new DynamicPosition(bestChipPos())));
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!isBallLyingInPenaltyArea(getAiFrame()))
			{
				nextState(EEvent.CHIP_KICK_DONE);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
			nextState(EEvent.CHIP_KICK_DONE);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.CHIP_KICK;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private IVector2 bestChipPos()
	{
		ValueBot passTarget = getAiFrame().getTacticalField().getBestPassTarget();
		if (passTarget == null)
		{
			return AIConfig.getGeometry().getCenter();
		}
		
		TrackedTigerBot possibleTarget = getWFrame().getTiger(passTarget.getBotID());
		
		// do not chip to the best target if it is the keeper himself or if it is a too high angle
		if ((possibleTarget.getPos().equals(getPos(), 20f))
				|| (Math.abs(possibleTarget.getPos().subtractNew(getPos()).getAngle()) > (Math.PI / 4)))
		{
			return AIConfig.getGeometry().getCenter();
		}
		return possibleTarget.getPos();
	}
	
	
	/**
	 * Checks if the ball is in our penalty Area
	 * 
	 * @param currentFrame
	 * @return true for in PenaltyArea or fals for not
	 */
	private boolean isBallLyingInPenaltyArea(final BaseAiFrame currentFrame)
	{
		TrackedBall ball = currentFrame.getWorldFrame().ball;
		boolean isBallInPenaltyArea = AIConfig.getGeometry().getPenaltyAreaOur()
				.isPointInShape(ball.getPos(), chipKickDecisionDistance);
		boolean isBallNotMoving = (ball.getVel().equals(AVector2.ZERO_VECTOR, chipKickDecisionVelocity));
		return isBallInPenaltyArea && isBallNotMoving;
	}
	
	
	@Override
	public boolean isKeeper()
	{
		return true;
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
	}
	
	
	/**
	 * @return the distToGoalLine
	 */
	public static int getDistToGoalLine()
	{
		return distToGoalLine;
	}
	
	
	/**
	 * @return the allowChipkick
	 */
	public boolean isAllowChipkick()
	{
		return allowChipkick;
	}
	
	
	/**
	 * @param allowChipkick the allowChipkick to set
	 */
	public void setAllowChipkick(final boolean allowChipkick)
	{
		this.allowChipkick = allowChipkick;
	}
	
	
}
