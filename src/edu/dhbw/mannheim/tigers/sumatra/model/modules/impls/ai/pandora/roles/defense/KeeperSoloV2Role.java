/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.10.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipAutoSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndBlockV2Skill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndBlockV2Skill.EBlockModus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * <a href="http://www.gockel-09.de/91=92=1a.jpg">
 * Keeper</a> Role for the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.KeeperSoloPlay}.
 * 
 * 
 * @author PhilippP {ph.posovszky@gmail.com}
 * 
 */
public class KeeperSoloV2Role extends ADefenseRole
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
		OUSTIDE_PENALTYAREA,
		INSIDE_PENALTYAREA,
		INTERSECTION,
		INTERSECT_DONE,
		CHIP_KICK_DONE,
		CHIP_KICK_CANCELD,
		CHIP_KICK,
		MOVE_CHIP_KICK
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public KeeperSoloV2Role()
	{
		super(ERole.KEEPER_SOLO_V2, true, true);
		setInitialState(new MoveOutsidePenaltyState());
		// If outside Penaltyarea use MoveState to move to penatlyArea
		addTransition(EStateId.DEFEND, EEvent.OUSTIDE_PENALTYAREA, new MoveOutsidePenaltyState());
		addTransition(EStateId.DEFEND, EEvent.MOVE_CHIP_KICK, new MoveChipKickState());
		addTransition(EStateId.MOVE_CHIP_KICK, EEvent.CHIP_KICK, new ChipKickState());
		addTransition(EStateId.MOVE_CHIP_KICK, EEvent.CHIP_KICK_CANCELD, new NormalBlockState());
		addTransition(EStateId.CHIP_KICK, EEvent.CHIP_KICK_DONE, new NormalBlockState());
		// If reach penalty area, switch to state
		addTransition(EStateId.MOVE_TO_PENALTYAREA, EEvent.INSIDE_PENALTYAREA, new NormalBlockState());
		
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * Move to the PenaltyArea with Playfinder
	 * 
	 * @author PhilippP
	 * 
	 */
	private class MoveOutsidePenaltyState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveToSkill(getMoveCon()));
			updateDestination(new Vector2((-AIConfig.getGeometry().getFieldLength() / 2) + 100, 0));
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
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
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
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveAndBlockV2Skill(EBlockModus.KEEPER_INTERSEC));
		}
		
		
		@Override
		public void doUpdate()
		{
			TrackedBall ball = getAiFrame().worldFrame.ball;
			
			if (!AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(getBot().getPos()))
			{
				nextState(EEvent.OUSTIDE_PENALTYAREA);
			}
			
			// TODO abfragen ob der ball auf der linie liegt dann soll er auch ein chipkick machen bzw ihn rausdopsen
			if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(ball.getPos(), 100)
					&& (ball.getVel().equals(Vector2.ZERO_VECTOR, 0.4f)))
			{
				nextState(EEvent.MOVE_CHIP_KICK);
			}
			
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
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
		
		@Override
		public void doEntryActions()
		{
			IVector2 pos = getAiFrame().worldFrame.ball.getPos();
			pos = new Vector2(pos.x() - 100, pos.y());
			
			updateDestination(pos);
			updateLookAtTarget(new Vector2(0, 0));
			MovementCon moveCon = getMoveCon();
			moveCon.setBotsObstacle(true);
			setNewSkill(new MoveToSkill(moveCon));
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(getAiFrame().worldFrame.ball.getPos(), 100))
			{
				nextState(EEvent.CHIP_KICK_CANCELD);
			}
			// TODO config rausziehen
			
			if (checkMoveCondition())
			{
				nextState(EEvent.CHIP_KICK);
			} else
			{
				IVector2 pos = getAiFrame().worldFrame.ball.getPos();
				pos = new Vector2(pos.x() - 100, pos.y());
				updateDestination(pos);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
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
			// TODO in config bringen
			setNewSkill(new ChipAutoSkill(new Vector2(0, 0), 1));
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!isBallInPenaltyArea(getAiFrame()))
			{
				nextState(EEvent.CHIP_KICK_DONE);
			}
			updateLookAtTarget(getAiFrame().worldFrame.ball.getPos());
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
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
	
	/**
	 * Checks if the ball is in our penalty Area
	 * 
	 * @param currentFrame
	 * @return true for in PenaltyArea or fals for not
	 */
	private boolean isBallInPenaltyArea(AIInfoFrame currentFrame)
	{
		IVector2 ballPosition = currentFrame.worldFrame.ball.getPos();
		return AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(ballPosition);
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
	
	
	@Override
	protected void updateMoveCon(AIInfoFrame aiFrame)
	{
		// nothing to do
		
	}
	
}
