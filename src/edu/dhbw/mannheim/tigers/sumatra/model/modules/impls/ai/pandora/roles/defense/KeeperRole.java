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
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.data.AdvancedPassTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.BlockSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.BlockSkillTrajV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EMoveMode;
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
		CHIP_FAST
	}
	
	private enum EEvent
	{
		OUTSIDE_PENALTYAREA,
		INSIDE_PENALTYAREA,
		CHIP_KICK_DONE,
		MOVE_CHIP_KICK
	}
	
	@Configurable(comment = "Security Dist [mm] - Distance around the penalty area. If the ball is inside this area the keeper tries to kick it away")
	private static int chipKickDecisionDistance = 50;
	
	@Configurable(comment = "Speed of the ball [m/s] - If the ball is slower, the bot will try to kick it out of the penalty area.")
	private static float chipKickDecisionVelocity = 0.4f;
	
	@Configurable(comment = "Dist [mm] - The distance to the goal line from the initial keeper position")
	private static float maxRelativeDistToGoalLine = 0.8f;
	
	@Configurable(comment = "Dist [mm] - Longest spline to be computed for the keeper.", spezis = {
			"", "GRSIM" })
	private static int maxSplineLength = 1000;
	
	@Configurable(comment = "Dist [mm] - Distance around the penalty area where the bot is allowed to block.")
	private static int safetyAroundPenalty = 400;
	
	@Configurable(comment = "Dist [mm] - Distance around the penalty area, inside the bot will pass controlled, outside the bot will just shoot")
	private static int safetyAroundPenaltyKickType = -200;
	
	private boolean allowChipkick = true;
	
	
	@Configurable
	private static boolean trajSkill = true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public KeeperRole()
	{
		super(ERole.KEEPER);
		setInitialState(new MoveOutsidePenaltyState());
		IRoleState blockState = new NormalBlockState();
		addTransition(EStateId.DEFEND, EEvent.OUTSIDE_PENALTYAREA, new MoveOutsidePenaltyState());
		addTransition(EStateId.DEFEND, EEvent.MOVE_CHIP_KICK, new ChipFastState());
		addTransition(EStateId.CHIP_FAST, EEvent.CHIP_KICK_DONE, blockState);
		addTransition(EStateId.MOVE_TO_PENALTYAREA, EEvent.INSIDE_PENALTYAREA, blockState);
		
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
			IMoveToSkill skill = AMoveSkill.createMoveToSkill();
			skill.setDoComplete(true);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().updateDestination(AIConfig.getGeometry().getGoalOur().getGoalCenter()
				.addNew(AVector2.X_AXIS.scaleToNew(maxRelativeDistToGoalLine)));
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(getBot().getPos()))
			{
			triggerEvent(EEvent.INSIDE_PENALTYAREA);
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
			triggerEvent(EEvent.INSIDE_PENALTYAREA);
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
		private ASkill posSkill;
		
		
		@Override
		public void doEntryActions()
		{
			float dist2GoalLine = (maxRelativeDistToGoalLine
				* (AIConfig.getGeometry().getPenaltyAreaOur().getRadiusOfPenaltyArea() - (AIConfig.getGeometry()
						.getBotRadius() * 2)))
				+ AIConfig.getGeometry().getBotRadius();
			if (trajSkill)
			{
			posSkill = new BlockSkillTrajV2(dist2GoalLine);
			} else
			{
			posSkill = new BlockSkill(dist2GoalLine, maxSplineLength);
			}
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
			triggerEvent(EEvent.OUTSIDE_PENALTYAREA);
			}
			if (isBallLyingInPenaltyArea(getAiFrame()) && allowChipkick)
			{
			triggerEvent(EEvent.MOVE_CHIP_KICK);
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
	
	private class ChipFastState implements IRoleState
	{
		
		
		@Override
		public void doEntryActions()
		{
			IVector2 ballPos = getWFrame().getBall().getPos();
			IVector2 goalCenter = AIConfig.getGeometry().getGoalOur().getGoalCenter();
			
			// in general a very dangerous situation: ball is close to penalty area, if our offensive or defensive would
			// try to get
			// it, it s a penalty shot for the enemy! (He needs to get his diameter of 180 behind the ball) So the
			// keeper should cover the shooting line ALL THE TIME. So it is too risky to chip to a reasonable direction.
			// Just drive straight to the ball and chip straight
			IVector2 chippingPosition = GeoMath.stepAlongLine(goalCenter, ballPos, 5000);
			EMoveMode moveMode = EMoveMode.NORMAL;
			
			if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(ballPos, safetyAroundPenaltyKickType))
			{
			// not dangerous, the ball is inside the penalty area, if an enemy tries to get it, we get a freekick
			// so, regard this situation like a freekick
			chippingPosition = new Vector2(AIConfig.getGeometry().getCenter());
			for (AdvancedPassTarget bot : getAiFrame().getTacticalField().getAdvancedPassTargetsRanked())
			{
				if (bot.x > (-AIConfig.getGeometry().getFieldLength() / 3f))
				{
					chippingPosition = new Vector2(bot.x(), bot.y());
					break;
				}
			}
			
			// chippingPosition = GeoMath.stepAlongLine(ballPos, chippingPosition, 10000); // TODO Testen!
			moveMode = EMoveMode.CHILL;
			}
			ChipSkill skill = new ChipSkill(new DynamicPosition(chippingPosition), moveMode);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!isBallLyingInPenaltyArea(getAiFrame()))
			{
			triggerEvent(EEvent.CHIP_KICK_DONE);
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
			triggerEvent(EEvent.CHIP_KICK_DONE);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.CHIP_FAST;
		}
	}
	
	
	/**
	 * Checks if the ball is in our penalty Area
	 * 
	 * @param currentFrame
	 * @return true for in PenaltyArea or fals for not
	 */
	private boolean isBallLyingInPenaltyArea(final BaseAiFrame currentFrame)
	{
		TrackedBall ball = currentFrame.getWorldFrame().getBall();
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
