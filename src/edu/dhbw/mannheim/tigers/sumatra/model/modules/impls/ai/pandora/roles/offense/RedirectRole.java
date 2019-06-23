/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.07.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.interfaces.IPointChecker;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.DestinationFreeCondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.visible.VisibleCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.helper.ShooterMemory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.RedirectBallSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * 
 * 
 * @author DanielW
 * 
 */
public class RedirectRole extends AReceiverRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log								= Logger.getLogger(RedirectRole.class.getName());
	private final Vector2f			goal								= AIConfig.getGeometry().getGoalTheir().getGoalCenter();
	private final Vector2			initPosition;
	private final Vector2			curDest;
	
	private boolean					senderCompleted				= false;
	private Vector2					shootTarget						= new Vector2(goal);
	
	private VisibleCon				targetVisibleCon				= new VisibleCon();
	private VisibleCon				receiverVisibleCon			= new VisibleCon();
	
	private static final float		BEST_TARGET_EQUAL_TOL		= 30f;
	private static final float		BALL_SPEED_THRES				= 0.5f;
	private ShooterMemory			mem;
	private boolean					passerUsesChipper				= false;
	
	private boolean					overridePosition				= false;
	private boolean					lookImmediatelyAtGoal		= true;
	private boolean					stayInPosition					= false;
	
	private IPointChecker			pointChecker					= new PointChecker();
	private int							noDestinationFoundCtr		= 0;
	
	private long						timeLastChangedDestination	= 0;
	
	private boolean					useChipper						= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public RedirectRole()
	{
		this(Vector2.ZERO_VECTOR, true, true);
	}
	
	
	/**
	 * @param position
	 */
	public RedirectRole(IVector2 position)
	{
		this(position, true, true);
	}
	
	
	/**
	 * @param initPosition
	 * @param ready
	 */
	public RedirectRole(IVector2 initPosition, boolean ready)
	{
		this(initPosition, ready, true);
	}
	
	
	/**
	 * @param initPosition
	 * @param ready
	 * @param lookAtTargetImmediately Whether the bot immediately looks at the target upon play start.
	 */
	public RedirectRole(IVector2 initPosition, boolean ready, boolean lookAtTargetImmediately)
	{
		super(ERole.REDIRECTER);
		this.initPosition = new Vector2(initPosition);
		curDest = new Vector2(initPosition);
		addCondition(receiverVisibleCon);
		addCondition(targetVisibleCon);
		
		if (ready)
		{
			setInitialState(new ReceiveState());
		} else
		{
			setInitialState(new WaitState());
			addTransition(EStateId.WAIT, EEvent.READY, new ReceiveState());
		}
		lookImmediatelyAtGoal = lookAtTargetImmediately;
		addEndTransition(EStateId.RECEIVE, EEvent.RECEIVED);
	}
	
	
	/**
	 * @param initPosition
	 * @param ready
	 * @param lookAtTargetImmediately Whether the bot immediately looks at the target upon play start.
	 * @param stayInPosition
	 */
	public RedirectRole(IVector2 initPosition, boolean ready, boolean lookAtTargetImmediately, boolean stayInPosition)
	{
		super(ERole.REDIRECTER);
		this.initPosition = new Vector2(initPosition);
		curDest = new Vector2(initPosition);
		addCondition(receiverVisibleCon);
		addCondition(targetVisibleCon);
		
		this.stayInPosition = stayInPosition;
		
		if (ready)
		{
			setInitialState(new ReceiveState());
		} else
		{
			setInitialState(new WaitState());
			addTransition(EStateId.WAIT, EEvent.READY, new ReceiveState());
		}
		lookImmediatelyAtGoal = lookAtTargetImmediately;
		addEndTransition(EStateId.RECEIVE, EEvent.RECEIVED);
	}
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		WAIT,
		RECEIVE,
	}
	
	private enum EEvent
	{
		READY,
		RECEIVED
	}
	
	private class WaitState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			mem = new ShooterMemory(getAiFrame(), getBotID());
			setNewSkill(new MoveAndStaySkill(getMoveCon()));
			updateDestination(curDest);
			getMoveCon().setShoot(true);
			if (lookImmediatelyAtGoal)
			{
				updateLookAtTarget(goal);
			} else
			{
				// TODO: do something about this state.
				// float angle = getAiFrame().worldFrame.tigerBotsAvailable.get(getBotID()).getAngle();
				// getInitTargetPosByAngle(angle);
			}
		}
		
		
		@Override
		public void doUpdate()
		{
			checkDestination(getPos());
			// checkDestination(initPosition);
			if (!overridePosition)
			{
				setInitPosition(getDestination());
			}
			if (lookImmediatelyAtGoal)
			{
				updateLookAtTarget(goal);
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
			return EStateId.WAIT;
		}
		
		
	}
	
	private class ReceiveState implements IRoleState
	{
		private RedirectBallSkill	skill;
		
		
		@Override
		public void doEntryActions()
		{
			if (mem == null)
			{
				mem = new ShooterMemory(getAiFrame(), getBotID());
			}
			shootTarget.set(mem.getBestPoint());
			updateDestination(curDest);
			updateLookAtTarget(shootTarget);
			skill = new RedirectBallSkill(shootTarget, curDest, stayInPosition);
			skill.setUseChipper(useChipper);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			updateTargetAngle(skill.getDestOrientation());
			checkDestination(getBot().getPos());
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
			nextState(EEvent.RECEIVED);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.RECEIVE;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private boolean checkAngle(IVector2 initPos)
	{
		IVector2 senderPos = getAiFrame().worldFrame.ball.getPos();
		
		// No (active) AngleCon, shouldn't really happen.
		if ((getMoveCon() == null) || ((getMoveCon().getAngleCon() != null) && !getMoveCon().getAngleCon().isActive()))
		{
			return false;
		}
		IVector2 kickerPos = AiMath.getBotKickerPos(initPos, getTargetAngle());
		IVector2 shootDir = shootTarget.subtractNew(kickerPos);
		float shortestRotation = AngleMath.getShortestRotation(shootDir.getAngle(), senderPos.subtractNew(kickerPos)
				.getAngle());
		
		if (Math.abs(shortestRotation) > AIConfig.getRoles().getIndirectReceiverMaxAngle())
		{
			return false;
		}
		return true;
	}
	
	
	private void checkDestination(IVector2 currentDestination)
	{
		if ((System.nanoTime() - timeLastChangedDestination) < TimeUnit.MILLISECONDS.toNanos(100))
		{
			return;
		}
		timeLastChangedDestination = System.nanoTime();
		
		if (getAiFrame().worldFrame.ball.getVel().getLength2() > BALL_SPEED_THRES)
		{
			// ball is moving, stop working on positioning!
			return;
		}
		
		mem.update(getAiFrame());
		IVector2 bestTarget = mem.getBestPoint();
		targetVisibleCon.updateEnd(shootTarget);
		if (passerUsesChipper)
		{
			float distance = GeoMath.distancePP(getAiFrame().worldFrame.ball.getPos(), currentDestination);
			receiverVisibleCon.updateEnd(GeoMath.stepAlongLine(getAiFrame().worldFrame.ball.getPos(), currentDestination,
					distance * AIConfig.getRoles().getChipPassDistFactor()));
		} else
		{
			receiverVisibleCon.updateEnd(getAiFrame().worldFrame.ball.getPos());
		}
		
		if (!pointChecker.checkPoint(currentDestination))
		{
			float radius = AIConfig.getGeometry().getBotRadius();
			IVector2 start = currentDestination.addNew(new Vector2(getBot().getAngle()).multiply(-1).scaleTo(radius));
			targetVisibleCon.setRaySize(AIConfig.getGeometry().getBotRadius());
			receiverVisibleCon.setRaySize(AIConfig.getGeometry().getBotRadius());
			int rounds = (int) (AIConfig.getGeometry().getFieldLength() / AIConfig.getGeometry().getBotRadius());
			IVector2 bestPoint = AiMath.findBestPoint(currentDestination, start, pointChecker, rounds);
			if (bestPoint == null)
			{
				targetVisibleCon.setRaySize(AIConfig.getGeometry().getBallRadius());
				receiverVisibleCon.setRaySize(AIConfig.getGeometry().getBallRadius());
				bestPoint = AiMath.findBestPoint(currentDestination, start, pointChecker, rounds);
				if (bestPoint == null)
				{
					noDestinationFoundCtr++;
					if (noDestinationFoundCtr > 100)
					{
						// log.warn("There is no damn point on the field from where we can do an indirect shot?! Probably something is wrong...");
						setCompleted();
					}
					return;
				}
			}
			noDestinationFoundCtr = 0;
			curDest.set(bestPoint);
			IVector2 kickerPos = AiMath.getBotKickerPos(curDest, getTargetAngle());
			targetVisibleCon.updateStart(kickerPos);
			receiverVisibleCon.updateStart(kickerPos);
		} else
		{
			curDest.set(currentDestination);
		}
		
		if (!bestTarget.equals(shootTarget, BEST_TARGET_EQUAL_TOL))
		{
			log.debug("Found better shoot target: " + bestTarget + " (before: " + shootTarget + ")");
			shootTarget.set(bestTarget);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.STRAIGHT_KICKER);
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame aiFrame)
	{
		IVector2 kickerPos = AiMath.getBotKickerPos(curDest, getTargetAngle());
		getAiFrame().addDebugShape(new DrawableLine(Line.newLine(kickerPos, shootTarget), Color.blue, true));
		if (!receiverVisibleCon.getStart().equals(receiverVisibleCon.getEnd()))
		{
			getAiFrame().addDebugShape(
					new DrawableLine(Line.newLine(receiverVisibleCon.getStart(), receiverVisibleCon.getEnd()), Color.blue,
							true));
		}
		if ((getBot().getAngle() < AngleMath.PI_HALF) || (getBot().getAngle() > (AngleMath.PI_TWO - AngleMath.PI_HALF)))
		{
			Goal goalTheir = AIConfig.getGeometry().getGoalTheir();
			try
			{
				IVector2 intP = GeoMath.intersectionPoint(new Line(getPos(), new Vector2(getBot().getAngle())),
						Line.newLine(goalTheir.getGoalCenter(), goalTheir.getGoalPostLeft()));
				if (Math.abs(intP.y()) < (AIConfig.getGeometry().getFieldWidth() / 2))
				{
					getAiFrame().addDebugShape(new DrawableLine(Line.newLine(getPos(), intP), Color.red, true));
				}
			} catch (MathException err)
			{
				// ignore this
			}
		}
	}
	
	
	@Override
	public void setReady()
	{
		if (!senderCompleted)
		{
			senderCompleted = true;
			nextState(EEvent.READY);
		}
	}
	
	
	@Override
	public boolean isReady()
	{
		return getCurrentState() == EStateId.RECEIVE;
	}
	
	
	@Override
	public final void setPassUsesChipper(boolean passUsesChipper)
	{
		passerUsesChipper = passUsesChipper;
	}
	
	
	@Override
	public final void setInitPosition(IVector2 pos)
	{
		initPosition.set(pos);
	}
	
	
	/**
	 * @return the overridePosition
	 */
	public final boolean isOverridePosition()
	{
		return overridePosition;
	}
	
	
	/**
	 * @param overridePosition the overridePosition to set
	 */
	public final void setOverridePosition(boolean overridePosition)
	{
		this.overridePosition = overridePosition;
	}
	
	
	private class PointChecker implements IPointChecker
	{
		private final DestinationFreeCondition	destFreeCon	= new DestinationFreeCondition();
		
		
		public PointChecker()
		{
			float tol = AIConfig.getGeometry().getBotRadius() * 4;
			destFreeCon.setDestFreeTol(tol);
			destFreeCon.setConsiderFoeBots(true);
		}
		
		
		@Override
		public boolean checkPoint(IVector2 point)
		{
			if ((point.x() < 0) || (point.x() > ((AIConfig.getGeometry().getFieldLength() / 2) - 200)))
			{
				return false;
			}
			
			if (!AIConfig.getGeometry().getField().isPointInShape(point))
			{
				return false;
			}
			
			if (AIConfig.getGeometry().getPenaltyAreaTheir().isPointInShape(point))
			{
				return false;
			}
			
			IVector2 senderPos = getAiFrame().worldFrame.ball.getPos();
			float yBall = senderPos.y();
			float yRec = point.y();
			if (((yBall > 0) && (yRec > 0)) || ((yBall < 0) && (yRec < 0)))
			{
				return false;
			}
			
			if (!checkAngle(point))
			{
				return false;
			}
			
			if (GeoMath.distancePP(receiverVisibleCon.getStart(), receiverVisibleCon.getEnd()) < 500)
			{
				return false;
			}
			
			// TODO Redirector schiesst ohne dierekte Schussbahn
			IVector2 kickerPos = AiMath.getBotKickerPos(point, getTargetAngle());
			targetVisibleCon.updateStart(kickerPos);
			if (!targetVisibleCon.checkCondition(getAiFrame().worldFrame, getBotID()).isOk())
			{
				return false;
			}
			
			receiverVisibleCon.updateStart(kickerPos);
			if (!receiverVisibleCon.checkCondition(getAiFrame().worldFrame, getBotID()).isOk())
			{
				return false;
			}
			
			if (AIConfig.getGeometry().getPenaltyAreaTheir().isPointInShape(point))
			{
				return false;
			}
			destFreeCon.updateDestination(point);
			if (!destFreeCon.checkCondition(getAiFrame().worldFrame, getBotID()).isOk())
			{
				return false;
			}
			
			return true;
		}
	}
	
	
	/**
	 * This method will return the Redirect Destination
	 * @return will return the Redirect Destination
	 */
	public IVector2 getRedirectDestination()
	{
		return curDest;
	}
}
