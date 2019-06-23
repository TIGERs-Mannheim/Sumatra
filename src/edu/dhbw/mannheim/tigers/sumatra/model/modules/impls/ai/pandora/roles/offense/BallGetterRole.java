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

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.GetBallSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.TurnAroundBallSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * BallGetter goes behind the ball and acquires ball possession.
 * Assumes the ball just sits somewhere and no-one possesses the ball.
 * 
 * @author FlorianS, TobiasK
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallGetterRole extends ARole
{
	private static final Logger	log				= Logger.getLogger(BallGetterRole.class.getName());
	private static long				timestamp;
	private static final float		END_SPEED		= 0.2f;
	
	// private static final float POS_EQUAL_TO_BALL_POS_TOL = 0.5f;
	// private static final int ALT_POS_ANGLE_STEP_DIV = 32;
	// private static final float MIN_ANGLE_TOL = (float) Math.PI / 4;
	//
	
	/** should the bot touch the ball or just drive as near as possible to the ball? */
	private final EBallContact		ballContact;
	// private final EBehavior behavior;
	
	private IVector2					secPoint			= null;
	
	private IVector2					viewPoint;
	
	private boolean					wasMoving		= false;
	private boolean					isImpossible	= false;
	
	/**
	 * How near should we come to the ball?
	 */
	public enum EBallContact
	{
		/** keep distance to ball and do not touch it */
		DISTANCE,
		/** slightly touch ball */
		TOUCH,
		/** touch ball and activate dribbler */
		DRIBBLE,
	}
	
	private enum EStateId
	{
		GET_BEFORE,
		GET,
		TURN,
	}
	
	private enum EEvent
	{
		NEXT_TO_BALL,
		BALL_CONTACT,
		LOOKING_AT_TARGET,
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Default constructor, not recommended!
	 */
	public BallGetterRole()
	{
		this(Vector2.ZERO_VECTOR, EBallContact.DISTANCE);
	}
	
	
	/**
	 * 
	 * @param viewPoint point to look at, e.g. goal, receiver
	 * @param ballContact should the bot touch the ball or just drive as near as possible to the ball?
	 */
	public BallGetterRole(final IVector2 viewPoint, final EBallContact ballContact)
	{
		super(ERole.BALL_GETTER);
		
		final IRoleState getBeforeState = new GetBeforeState();
		final IRoleState getState = new GetState();
		@SuppressWarnings("unused")
		final IRoleState turnState = new TurnState();
		
		setInitialState(getBeforeState);
		addTransition(EStateId.GET_BEFORE, EEvent.NEXT_TO_BALL, getState);
		if (ballContact != EBallContact.DISTANCE)
		{
			addEndTransition(EStateId.GET, EEvent.BALL_CONTACT);
			addEndTransition(EStateId.TURN, EEvent.LOOKING_AT_TARGET);
			addEndTransition(EStateId.TURN, EEvent.NEXT_TO_BALL);
		} else
		{
			addEndTransition(EStateId.GET_BEFORE, EEvent.NEXT_TO_BALL);
		}
		setViewPoint(viewPoint);
		this.ballContact = ballContact;
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
				handleNotMovingBall();
			} else
			{
				handleMovingBall();
			}
		}
		
		
		private void handleNotMovingBall()
		{
			wasMoving = false;
			IVector2 dest = destinationForNotMovingBall(viewPoint);
			updateDestination(dest);
			IVector2 dest2ball = getAiFrame().worldFrame.getBall().getPos().subtractNew(dest);
			getMoveCon().setVelAtDestination(dest2ball.scaleToNew(END_SPEED));
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
			boolean moving = !ball.equals(Vector2.ZERO_VECTOR, 0.4f);
			return moving;
		}
		
		
		private IVector2 destinationForNotMovingBall(IVector2 lookAtTarget)
		{
			TrackedBall ball = getAiFrame().worldFrame.getBall();
			IVector2 goal2Ball = ball.getPos().subtractNew(lookAtTarget);
			goal2Ball = goal2Ball.scaleToNew(AIConfig.getGeometry().getBotRadius()
					+ AIConfig.getGeometry().getBallRadius() + 10);
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
				
				if (viewPoint == null)
				{
					destination = GeoMath.stepAlongLine(ballposInNSec,
							AIConfig.getGeometry().getGoalTheir().getGoalCenter(), -AIConfig.getGeometry().getBotRadius() * 3)
							.addNew(ballposInNSec);
					
				} else
				{
					destination = GeoMath
							.stepAlongLine(ballposInNSec, viewPoint, -AIConfig.getGeometry().getBotRadius() * 3);
					
				}
				
				Sisyphus sis = new Sisyphus();
				MovementCon moveCon = new MovementCon();
				moveCon.updateDestination(destination);
				moveCon.setBallObstacle(true);
				moveCon.setPenaltyAreaAllowed(false);
				// if (isSecPointNeeded(destination))
				// {
				// IVector2 firstpoint = ball2BallInNSec.getNormalVector()
				// .scaleToNew(AIConfig.getGeometry().getBotRadius() * 2).addNew(ballposInNSec);
				// IVector2 secondpoint = ball2BallInNSec.getNormalVector()
				// .scaleToNew(-AIConfig.getGeometry().getBotRadius() * 2).addNew(ballposInNSec);
				// List<IVector2> ballSecure1 = new ArrayList<IVector2>();
				// List<IVector2> ballSecure2 = new ArrayList<IVector2>();
				// ballSecure1.add(firstpoint);
				// ballSecure2.add(secondpoint);
				// moveCon.setIntermediateStops(ballSecure1);
				// ISpline splineToTarget = sis.calculateSpline(getBot(), getAiFrame().worldFrame, moveCon);
				// moveCon.setIntermediateStops(ballSecure2);
				// ISpline splineToTarget2 = sis.calculateSpline(getBot(), getAiFrame().worldFrame, moveCon);
				//
				// if ((splineToTarget.getTotalTime() <= i) || (splineToTarget2.getTotalTime() <= i))
				// {
				// if (splineToTarget.getTotalTime() <= splineToTarget2.getTotalTime())
				// {
				// secPoint = firstpoint;
				// }
				// secPoint = secondpoint;
				// break;
				// }
				// } else
				// {
				float splineLength = sis.calculateSpline(getBot(), getAiFrame().worldFrame, moveCon).getTotalTime();
				if (splineLength <= (i / 10))
				{
					break;
				}
				// }
			}
			return destination;
		}
		
		
		@SuppressWarnings("unused")
		private boolean isSecPointNeeded(IVector2 destination)
		{
			IVector2 bot2dest = destination.subtractNew(getBot().getPos());
			IVector2 ball2dest = destination.subtractNew(getAiFrame().worldFrame.getBall().getPos());
			float angle = AngleMath.getShortestRotation(bot2dest.getAngle(), ball2dest.getAngle());
			return (Math.abs(angle) < 0.4) && (ball2dest.getLength2() > 500);
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
				handleNotMovingBall();
			}
			if (isBallMoving() && ((TimeUnit.NANOSECONDS.toMillis(acttime - timestamp) > 500) || !wasMoving))
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
			log.trace("get before skill completed");
			switch (checkMovementCondition())
			{
				case FULFILLED:
					nextState(EEvent.NEXT_TO_BALL);
					break;
				case BLOCKED:
				case PENDING:
					log.warn("MoveTo Skill completed, but movement condition not fullfilled!");
					setNewSkill(new MoveToSkill(getMoveCon()));
					break;
				default:
					log.error("Unexpected movement condition state: " + checkMovementCondition());
					nextState(EEvent.NEXT_TO_BALL);
			}
		}
		
		
		// private IVector2 getLookAtTarget()
		// {
		// final IVector2 lookAtTarget;
		// switch (behavior)
		// {
		// case TURN_AROUND_BALL:
		// lookAtTarget = GeoMath.stepAlongLine(getAiFrame().worldFrame.ball.getPos(), getPos(), -AIConfig
		// .getGeometry().getBotRadius());
		// break;
		// case DRIVE_AROUND_BALL:
		// lookAtTarget = viewPoint;
		// break;
		// default:
		// throw new IllegalStateException();
		// }
		// return lookAtTarget;
		// }
		
		
		// /**
		// * Calc destination while regarding blocked destinations.
		// */
		// private void calcDestination(final IVector2 lookAtTarget)
		// {
		// float distance = AIConfig.getGeneral(getBotType()).getPositioningPreAiming();
		//
		// IVector2 dest;
		// // if the ball is the viewpoint
		// if (getAiFrame().worldFrame.ball.getPos().equals(lookAtTarget, POS_EQUAL_TO_BALL_POS_TOL))
		// {
		// dest = GeoMath.stepAlongLine(getAiFrame().worldFrame.ball.getPos(), getPos(), distance);
		// } else
		// {
		// dest = GeoMath.stepAlongLine(getAiFrame().worldFrame.ball.getPos(), lookAtTarget, -(distance));
		// }
		// updateDestinationFree(EDestFreeMode.FREE_OF_BOTS);
		// updateDestination(dest);
		//
		// // original destination
		// getAiFrame().addDebugShape(new DrawablePoint(getDestination(), Color.blue));
		//
		// int securityCounter = ALT_POS_ANGLE_STEP_DIV * 2;
		// int negPosToggle = 1;
		// boolean blocked = false;
		// while (checkMovementCondition(getAiFrame().worldFrame) == EConditionState.BLOCKED)
		// {
		// blocked = true;
		// updateDestination(GeoMath.stepAlongCircle(getDestination(), getAiFrame().worldFrame.ball.getPos(),
		// ((float) Math.PI / ALT_POS_ANGLE_STEP_DIV) * negPosToggle));
		// getAiFrame().addDebugShape(new DrawablePoint(getDestination(), Color.yellow));
		// securityCounter--;
		// negPosToggle *= -1;
		// if (negPosToggle < 0)
		// {
		// negPosToggle += 2;
		// }
		// if (securityCounter < 0)
		// {
		// // No free position found, however its senseless to try something else...
		// log.warn("No free position found");
		// break;
		// }
		// }
		// if (blocked)
		// {
		// // final destination
		// getAiFrame().addDebugShape(new DrawablePoint(getDestination(), Color.red));
		// }
		// }
		//
	}
	
	private class GetState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new GetBallSkill(ballContact == EBallContact.DRIBBLE));
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
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
			if (!wasMoving)
			{
				log.trace("Skill GET does nothing because the ball was not moving before");
				nextState(EEvent.BALL_CONTACT);
			}
			if (getBot().hasBallContact())
			{
				nextState(EEvent.BALL_CONTACT);
			} else
			{
				float ballDist = GeoMath.distancePP(getAiFrame().worldFrame.ball.getPos(), getPos());
				log.warn("GetballSkill completed without ball contact, restarting GetBallSkill! (Dist to ball: " + ballDist
						+ ")");
				// setNewSkill(new GetBallSkill(ballContact == EBallContact.DRIBBLE));
				nextState(EEvent.BALL_CONTACT);
			}
		}
	}
	
	private class TurnState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new TurnAroundBallSkill(viewPoint));
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!wasMoving)
			{
				log.trace("Skill TURN does nothing because the ball was not moving before");
				nextState(EEvent.NEXT_TO_BALL);
			}
			if (GeoMath.distancePP(getPos(), getAiFrame().worldFrame.ball.getPos()) > (AIConfig.getGeneral(getBotType())
					.getPositioningPreAiming() + 50))
			{
				nextState(EEvent.NEXT_TO_BALL);
			}
			updateDestination(getPos());
			updateLookAtTarget(viewPoint);
		}
		
		
		@Override
		public EStateId getIdentifier()
		{
			return EStateId.TURN;
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			switch (getMoveCon().getAngleCon().checkCondition(getAiFrame().worldFrame, botID))
			{
				case FULFILLED:
					nextState(EEvent.LOOKING_AT_TARGET);
					break;
				default:
					doEntryActions();
			}
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
