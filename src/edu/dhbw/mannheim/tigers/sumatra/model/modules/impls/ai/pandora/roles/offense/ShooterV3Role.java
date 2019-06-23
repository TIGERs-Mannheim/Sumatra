/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s):
 * FlorianS
 * TobiasK
 * DanielW
 * ChristianK
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * BallGetter goes behind the ball and acquires ball possession.
 * Assumes the ball just sits somewhere and no-one possesses the ball.
 * 
 * @author FlorianS, TobiasK
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ShooterV3Role extends ARole
{
	
	private static final Logger	log							= Logger.getLogger(ShooterV3Role.class.getName());
	private static long				timestamp;
	private IVector2					secPoint						= null;
	
	private IVector2					viewPoint;
	
	private boolean					wasMoving					= false;
	private boolean					isImpossible				= false;
	
	private float						velocityAtShoot			= 0.0f;
	private float						behindBallMoving			= AIConfig.getGeometry().getBotRadius() * 1;
	private float						behindBallStatic			= 0;
	private float						calcNewSplineMovingMs	= 100;
	private float						destEqualsPosTol			= 60;
	
	
	private enum EStateId
	{
		GET_BEFORE,
		GET,
	}
	
	private enum EEvent
	{
		NEXT_TO_BALL,
		DONE
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Default constructor, not recommended!
	 */
	public ShooterV3Role()
	{
		this(Vector2.ZERO_VECTOR);
	}
	
	
	/**
	 * 
	 * @param viewPoint point to look at, e.g. goal, receiver
	 */
	public ShooterV3Role(final IVector2 viewPoint)
	{
		super(ERole.SHOOTERV3);
		
		final IRoleState getBeforeState = new GetBeforeState();
		
		setInitialState(getBeforeState);
		addTransition(EStateId.GET_BEFORE, EEvent.NEXT_TO_BALL, new GetState());
		addEndTransition(EStateId.GET, EEvent.DONE);
		setViewPoint(viewPoint);
		getMoveCon().setShoot(true);
	}
	
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class GetBeforeState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveToSkill(getMoveCon()));
			
			if (!isBallMoving())
			{
				handleNonMovingBall();
			} else
			{
				handleMovingBall();
			}
		}
		
		
		private void handleNonMovingBall()
		{
			wasMoving = false;
			IVector2 dest = destinationForNotMovingBall(viewPoint);
			updateDestination(dest);
			IVector2 dest2ball = getAiFrame().worldFrame.getBall().getPos().subtractNew(dest);
			getMoveCon().setVelAtDestination(dest2ball.scaleToNew(velocityAtShoot));
			// getMoveCon().setBallObstacle(false);
			getMoveCon().setIntermediateStops(new ArrayList<IVector2>());
			updateLookAtTarget(viewPoint);
		}
		
		
		private void handleMovingBall()
		{
			wasMoving = true;
			IVector2 destination = destinationForMovingBall();
			if (!AIConfig.getGeometry().getField().isPointInShape(destination)
					|| AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(destination))
			{
				isImpossible = true;
				// handleNotMovingBall();
			} else
			{
				updateDestination(destination);
				updateLookAtTarget(getAiFrame().worldFrame.getBall().getPos());
				timestamp = System.nanoTime();
				List<IVector2> ballSecure = new ArrayList<IVector2>();
				if (secPoint != null)
				{
					ballSecure.add(secPoint);
				}
				getMoveCon().setIntermediateStops(ballSecure);
			}
		}
		
		
		private boolean isBallMoving()
		{
			IVector2 ball = getAiFrame().worldFrame.getBall().getVel();
			boolean moving = !ball.equals(Vector2.ZERO_VECTOR, 0.1f);
			return moving;
		}
		
		
		private IVector2 destinationForNotMovingBall(IVector2 lookAtTarget)
		{
			TrackedBall ball = getAiFrame().worldFrame.getBall();
			IVector2 goal2Ball = ball.getPos().subtractNew(lookAtTarget);
			goal2Ball = goal2Ball.scaleToNew(AIConfig.getGeometry().getBotRadius()
					+ AIConfig.getGeometry().getBallRadius() + behindBallStatic);
			IVector2 destination = ball.getPos().addNew(goal2Ball);
			return destination;
		}
		
		
		/**
		 * This method will calculate the best point to stop a moving ball.
		 */
		private IVector2 destinationForMovingBall()
		{
			IVector2 destination = null;
			
			// get ball position in n sec
			for (int i = 1; i < 100; i++)
			{
				IVector2 ballposInNSec = getAiFrame().worldFrame.getWorldFramePrediction().getBall().getPosAt(0.1f * i);
				
				destination = GeoMath.stepAlongLine(ballposInNSec, viewPoint, -behindBallMoving);
				
				Sisyphus sis = new Sisyphus();
				MovementCon moveCon = new MovementCon();
				moveCon.updateDestination(destination);
				moveCon.setBallObstacle(true);
				moveCon.setPenaltyAreaAllowed(false);
				moveCon.setShoot(true);
				float splineLength = sis.calculateSpline(getBot(), getAiFrame().worldFrame, moveCon).getTotalTime();
				if (splineLength <= (i / 10))
				{
					break;
				}
			}
			return destination;
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			long acttime = System.nanoTime();
			if (!isBallMoving() && wasMoving)
			{
				handleNonMovingBall();
			}
			if (isBallMoving()
					&& ((TimeUnit.NANOSECONDS.toMillis(acttime - timestamp) > calcNewSplineMovingMs) || !wasMoving))
			{
				handleMovingBall();
			}
			
			
			if (secPoint != null)
			{
				if (getBot().getPos().equals(secPoint, 180))
				{
					List<IVector2> remove = new ArrayList<IVector2>();
					getMoveCon().setIntermediateStops(remove);
				}
			}
			
			if (getDestination().equals(getPos(), destEqualsPosTol))
			{
				nextState(EEvent.NEXT_TO_BALL);
			}
		}
		
		
		@Override
		public EStateId getIdentifier()
		{
			return EStateId.GET_BEFORE;
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			nextState(EEvent.NEXT_TO_BALL);
		}
	}
	
	
	private class GetState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			getMoveCon().setBallObstacle(false);
			IVector2 vel = viewPoint.subtractNew(getAiFrame().worldFrame.ball.getPos()).scaleTo(velocityAtShoot);
			getMoveCon().setVelAtDestination(vel);
			getMoveCon().setShoot(true);
			updateDestination(GeoMath.stepAlongLine(getAiFrame().worldFrame.ball.getPos(), viewPoint, 10));
			setNewSkill(new MoveToSkill(getMoveCon()));
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			updateLookAtTarget(viewPoint);
			if (getAiFrame().worldFrame.ball.getVel().getLength2() > 0.3)
			{
				log.debug("Completed due to moving ball");
				nextState(EEvent.DONE);
			}
		}
		
		
		@Override
		public EStateId getIdentifier()
		{
			return EStateId.GET;
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			nextState(EEvent.DONE);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param newViewPoint
	 */
	public final void setViewPoint(IVector2 newViewPoint)
	{
		if (newViewPoint.equals(GeoMath.INIT_VECTOR))
		{
			throw new IllegalArgumentException("You can not set the viewPoint to INIT_VECTOR!");
		}
		viewPoint = newViewPoint;
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.STRAIGHT_KICKER);
		features.add(EFeature.DRIBBLER);
	}
	
	
	/**
	 * @return the isImpossible
	 */
	public boolean isImpossible()
	{
		return isImpossible;
	}
}
