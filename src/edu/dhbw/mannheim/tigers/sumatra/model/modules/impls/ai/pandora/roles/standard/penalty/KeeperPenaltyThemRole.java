/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.02.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standard.penalty;


import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.ILine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * The Penalty Keeper.<br>
 * Elfmeterkiller!
 * 
 * @author Malte
 */
public class KeeperPenaltyThemRole extends ARole
{
	/** distance between bot center and goal line to be subtracted */
	private static final int	BOT_GOALLINE_OFFSET	= 30;
	/**
	 * Use the POSITION_OVERRIDE to set the keepers acceleration. Because a small spline couldn't be so fast, as we
	 * needed. The distance will be the fieldWidth!
	 * @TODO Calibrate the POSITION_OVERRIDE to the bot velocity.
	 */
	private static final float	POSITION_OVERRIDE		= AIConfig.getGeometry().getFieldWidth();
	private final ILine			positionLine;
	private final IVector2		positionCenter			= new Vector2((AIConfig.getGeometry().getGoalOur().getGoalCenter()
																			.x() + AIConfig.getGeometry().getBotRadius())
																			- BOT_GOALLINE_OFFSET, 0);
	
	private final float			yBoundarieMinus;
	private final float			yBoundariePlus;
	
	private TrackedBot			shooter					= null;
	
	private enum EStateId
	{
		WAIT,
		KEEPER,
	}
	
	private enum EEvent
	{
		READY,
		DONE
	}
	
	private final IRoleState	waitState	= new WaitingState();
	private final IRoleState	keeperState	= new KeeperState();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public KeeperPenaltyThemRole()
	{
		super(ERole.PENALTY_THEM_KEEPER, true);
		
		setInitialState(waitState);
		addTransition(EStateId.WAIT, EEvent.READY, keeperState);
		addEndTransition(EStateId.WAIT, EEvent.DONE);
		
		positionLine = new Line(positionCenter, AVector2.Y_AXIS);
		yBoundariePlus = (AIConfig.getGeometry().getGoalSize() / 2) - AIConfig.getGeometry().getBotRadius();
		yBoundarieMinus = (-AIConfig.getGeometry().getGoalSize() / 2) + AIConfig.getGeometry().getBotRadius();
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private class WaitingState implements IRoleState
	{
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveAndStaySkill(getMoveCon()));
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			updateDestination(positionCenter);
			updateLookAtTarget(AIConfig.getGeometry().getCenter());
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.WAIT;
		}
		
	}
	
	private class KeeperState implements IRoleState
	{
		// private MoveAndBlockV2Skill skill;
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			// setNewSkill(new MoveToSkill(getMoveCon()));
		}
		
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveAndStaySkill(getMoveCon()));
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			Vector2 destination = new Vector2(positionCenter);
			if (shooter != null)
			{
				try
				{
					destination = GeoMath.intersectionPoint(positionLine,
							new Line(shooter.getPos(), new Vector2(shooter.getAngle())));
				} catch (final MathException err)
				{
					// the shooter is looking to one of the sides of the field. Strange, but do not care because he can't
					// shoot a goal this way
				}
			}
			
			if (destination.y() > yBoundariePlus)
			{
				destination.setY(yBoundariePlus + POSITION_OVERRIDE);
			} else if (destination.y() < yBoundarieMinus)
			{
				destination.setY(yBoundarieMinus - POSITION_OVERRIDE);
			}
			
			
			updateDestination(destination);
			updateLookAtTarget(getAiFrame().worldFrame.ball.getPos());
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.KEEPER;
		}
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public boolean isKeeper()
	{
		return true;
	}
	
	
	/**
	 * @param ready
	 */
	public void setReady(boolean ready)
	{
		nextState(EEvent.READY);
	}
	
	
	/**
	 * @param shooter
	 */
	public void setShooter(TrackedBot shooter)
	{
		this.shooter = shooter;
	}
	
	
	/**
	 * @return
	 */
	public TrackedBot getShooter()
	{
		return shooter;
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
