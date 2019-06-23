/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.01.2012
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PullBackSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * This role can be a successor of a BallGetterRole.
 * It will try to conquer the ball in case, the opponent also tries to get it.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallConquerRole extends ARole
{
	private static final float	SMALL_CIRCLE_R	= 100;
	
	/** point, bot should look at */
	private IVector2				lookAtAfter		= AIConfig.getGeometry().getGoalTheir().getGoalCenter();
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Default constructor
	 */
	public BallConquerRole()
	{
		this(Vector2.ZERO_VECTOR);
	}
	
	
	/**
	 * 
	 * @param lookAtAfter point to look at after ball was conquered (could also be a pass receiver)
	 */
	public BallConquerRole(final IVector2 lookAtAfter)
	{
		super(ERole.BALL_CONQUERER);
		setInitialState(new GetState());
		addTransition(EStateId.GET, EEvent.GOT_BALL, new PullState());
		addEndTransition(EStateId.PULL, EEvent.PULLED);
		setLookAtAfter(lookAtAfter);
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		GET,
		PULL
	}
	
	private enum EEvent
	{
		GOT_BALL,
		PULLED
	}
	
	private class GetState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveToSkill(getMoveCon()));
		}
		
		
		@Override
		public void doUpdate()
		{
			final float positioningDistancePost = AIConfig.getGeneral(getBotType()).getPositioningPostAiming();
			IVector2 position = GeoMath.stepAlongLine(getAiFrame().worldFrame.ball.getPos(), getPos(),
					positioningDistancePost);
			updateDestination(position);
			updateLookAtTarget(getAiFrame().worldFrame.ball.getPos());
			getAiFrame().addDebugShape(new DrawableCircle(new Circle(getDestination(), SMALL_CIRCLE_R), Color.red));
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
			nextState(EEvent.GOT_BALL);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.GET;
		}
	}
	
	private class PullState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			final float y;
			if (getPos().y() < lookAtAfter.y())
			{
				// TODO NicolaiO verify
				y = -AIConfig.getGeometry().getBotRadius() * 3;
			} else
			{
				y = -AIConfig.getGeometry().getBotRadius() * 3;
			}
			Vector2 destination = GeoMath.stepAlongLine(getPos(), getAiFrame().worldFrame.ball.getPos(), -AIConfig
					.getGeometry().getBotRadius() * 2);
			destination.add(new Vector2(0, y));
			setNewSkill(new PullBackSkill(destination, lookAtAfter));
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
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			nextState(EEvent.PULLED);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.PULL;
		}
	}
	
	
	/**
	 * @param lookAtAfter
	 */
	public final void setLookAtAfter(IVector2 lookAtAfter)
	{
		if (lookAtAfter.equals(GeoMath.INIT_VECTOR))
		{
			throw new IllegalArgumentException("You can not set the lookAtAfter to INIT_VECTOR!");
		}
		this.lookAtAfter = lookAtAfter;
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.STRAIGHT_KICKER);
		features.add(EFeature.DRIBBLER);
	}
}
