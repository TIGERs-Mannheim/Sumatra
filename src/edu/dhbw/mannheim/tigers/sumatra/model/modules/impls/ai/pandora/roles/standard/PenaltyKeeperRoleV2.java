/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.05.2014
 * Author(s): dirk
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standard;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.EMoveToMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PositionSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.ShapeLayer;


/**
 * TODO dirk, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author dirk
 */
public class PenaltyKeeperRoleV2 extends ARole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log	= Logger.getLogger(PenaltyKeeperRoleV2.class.getName());
	
	private enum EStateId
	{
		MOVE_TO_GOAL_CENTER,
		BLOCK_SHOOTING_LINE
	}
	
	private enum EEvent
	{
		KEEPER_ON_GOAL_CENTER,
	}
	
	
	// Prepare penalty they -> Keeper to goal center
	// normal start -> keeper left / right on goal line (shooter is allowed to shoot)
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public PenaltyKeeperRoleV2()
	{
		super(ERole.PENALTY_KEEPER_V2);
		setInitialState(new MoveToGoalCenter());
		addTransition(EStateId.MOVE_TO_GOAL_CENTER, EEvent.KEEPER_ON_GOAL_CENTER, new BlockShootingLine());
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		// TODO dirk: Auto-generated method stub
		
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Move to the goal center with Playfinder
	 * 
	 * @author Dirk
	 */
	private class MoveToGoalCenter implements IRoleState
	{
		private IMoveToSkill	skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveSkill.createMoveToSkill(EMoveToMode.DO_COMPLETE);
			skill.getMoveCon().updateDestination(AIConfig.getGeometry().getGoalOur().getGoalCenter());
			setNewSkill(skill);
			skill.getMoveCon().setPenaltyAreaAllowed(true);
		}
		
		
		@Override
		public void doUpdate()
		{
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
			nextState(EEvent.KEEPER_ON_GOAL_CENTER);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.MOVE_TO_GOAL_CENTER;
		}
	}
	
	
	/**
	 * Block the shooting line
	 * 
	 * @author Dirk
	 */
	private class BlockShootingLine implements IRoleState
	{
		private PositionSkill	skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new PositionSkill(AIConfig.getGeometry().getGoalOur().getGoalCenter(), 0);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			TrackedBot enemyBot = getAiFrame().getTacticalField().getEnemyClosestToBall().getBot();
			IVector2 direction = new Vector2(getWFrame().getBall().getPos().subtractNew(enemyBot.getPos()));// enemyBot.getAngle());
			float pufferToGoalPost = AIConfig.getGeometry().getBotRadius() + 50;
			try
			{
				IVector2 goalLineIntersect = GeoMath.intersectionPoint(getWFrame().getBall().getPos(), direction, AIConfig
						.getGeometry().getGoalOur().getGoalCenter(), AVector2.Y_AXIS);
				if (goalLineIntersect.y() < (AIConfig.getGeometry().getGoalOur().getGoalPostRight().y() + pufferToGoalPost))
				{
					goalLineIntersect = AIConfig.getGeometry().getGoalOur().getGoalPostRight()
							.subtractNew(AVector2.Y_AXIS.scaleToNew(-pufferToGoalPost));
				}
				if (goalLineIntersect.y() > (AIConfig.getGeometry().getGoalOur().getGoalPostLeft().y() - pufferToGoalPost))
				{
					goalLineIntersect = AIConfig.getGeometry().getGoalOur().getGoalPostLeft()
							.subtractNew(AVector2.Y_AXIS.scaleToNew(pufferToGoalPost));
				}
				goalLineIntersect = goalLineIntersect.addNew(AVector2.X_AXIS.scaleToNew(AIConfig.getGeometry()
						.getBotRadius() * (3.f / 4.f)));
				ShapeLayer.addDebugShape(new DrawableCircle(new Circle(goalLineIntersect, 20)));
				skill.setDestination(goalLineIntersect);
				skill.setOrientation(getWFrame().getBall().getPos().subtractNew(getPos())
						.getAngle());
			} catch (MathException err)
			{
				log.warn("Math exception: shooting line parallel to goal line?");
				err.printStackTrace();
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
			return EStateId.BLOCK_SHOOTING_LINE;
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
