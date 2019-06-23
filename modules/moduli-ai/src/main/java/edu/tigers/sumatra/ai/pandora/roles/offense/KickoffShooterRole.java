/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 31, 2014
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.offense;

import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.data.math.ProbabilityMath;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EMoveMode;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * Simple shooter role, basically to test skill
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class KickoffShooterRole extends ARole
{
	@Configurable(comment = "kick speed for straight indirect kickoff")
	private static double	kickSpeedStraight					= 2.0;
	
	@Configurable(comment = "the lower bound for x for a shot target")
	private static double	lowerBorder							= 0.2;
	
	@Configurable(comment = "the upper bound for x for a shot target")
	private static double	upperBorder							= 0.8;
	
	@Configurable(comment = "the count of bot radias o closeness to foe")
	private static double	botRadiiAwayForStraightKick	= 3;
	
	private boolean			normalStartCalled					= false;
	private EMoveState		state									= EMoveState.LEFT;
	private int					numberOfTurns						= 0;
	
	private IVector2			moveDestination					= null;
	
	private IVector2			shotTarget							= null;
	
	
	/**
	  */
	public KickoffShooterRole()
	{
		super(ERole.KICKOFF_SHOOTER);
		numberOfTurns = 0;
		
		moveDestination = Geometry.getCenter().subtractNew(new Vector2(300, 0));
		
		IRoleState state1 = new PrepareState();
		setInitialState(state1);
		addTransition(EStateId.PREPARE, EEvent.READY, new MoveState());
	}
	
	
	// --------------------------------------------------------------------------
	// --- states ---------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private enum EStateId
	{
		PREPARE,
		MOVE;
	}
	
	private enum EEvent
	{
		READY;
	}
	
	private enum EMoveState
	{
		LEFT,
		RIGHT,
		MIDDLE;
	}
	
	private class PrepareState implements IRoleState
	{
		
		AMoveToSkill skill = null;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().updateTargetAngle(0);
			skill.getMoveCon().updateDestination(new Vector2(-300, 0));
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((getAiFrame().getRefereeMsg() != null)
					&& (getAiFrame().getRefereeMsg().getCommand() == Command.NORMAL_START))
			{
				normalStartCalled = true;
			}
			
			if (normalStartCalled && (GeoMath.distancePP(getPos(), skill.getMoveCon().getDestination()) < 100)
					&& (getBot().getVel().getLength2() < 0.2))
			{
				triggerEvent(EEvent.READY);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.PREPARE;
		}
	}
	
	private class MoveState implements IRoleState
	{
		
		private AMoveToSkill	skill				= null;
		private int				counter			= 0;
		private boolean		kickSkillSet	= false;
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().updateLookAtTarget(getWFrame().getBall());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (GeoMath.distancePP(getPos(), moveDestination) < 50)
			{
				if (state == EMoveState.RIGHT)
				{
					state = EMoveState.LEFT;
				} else
				{
					state = EMoveState.RIGHT;
				}
				counter++;
			}
			
			counter = 1;
			if ((counter > numberOfTurns) && !kickSkillSet)
			{
				kickSkillSet = true;
				shotTarget = findKickOffTarget();
				
				KickSkill kickSkill;
				
				if (shotTarget != null)
				{
					kickSkill = prepareDirectKick(shotTarget);
				} else
				{
					kickSkill = prepareIndirectKick();
				}
				
				setNewSkill(kickSkill);
				
			}
			skill.getMoveCon().updateDestination(moveDestination);
			
			if (skill.isDestinationReached())
			{
				setCompleted();
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.MOVE;
		}
		
		
		private IVector2 findKickOffTarget()
		{
			IVector2 target = getAiFrame().getTacticalField().getBestDirectShootTarget();
			if (target == null)
			{
				target = Geometry.getGoalTheir().getGoalCenter();
			}
			double score = ProbabilityMath.getDirectShootScoreChance(getWFrame(), getPos(), false);
			if (score < 0.5)
			{ // bad chance
				return null;
			}
			// good chance
			return target;
		}
		
		
		private KickSkill prepareDirectKick(final IVector2 target)
		{
			EKickMode mode = EKickMode.MAX;
			EKickerDevice device = EKickerDevice.STRAIGHT;
			
			return new KickSkill(new DynamicPosition(target), mode, device, EMoveMode.CHILL, 8);
		}
		
		
		private KickSkill prepareIndirectKick()
		{
			IVector2 target = findBestSpaceInEnemyHalf();
			EKickerDevice device = getKickerDeviceForTarget(target);
			EKickMode mode = getKickModeForDevice(device);
			double kickSpeed = getKickSpeedForDevice(device);
			
			return new KickSkill(new DynamicPosition(target), mode, device, EMoveMode.CHILL, kickSpeed);
		}
		
		
		private IVector2 findBestSpaceInEnemyHalf()
		{
			List<ValuePoint> bestBallPositions = getAiFrame().getTacticalField().getScoreChancePoints();
			
			bestBallPositions = cropFoundPositions(bestBallPositions);
			
			if (bestBallPositions.isEmpty())
			{
				return Geometry.getGoalTheir().getGoalCenter();
			}
			
			bestBallPositions.sort(ValuePoint.VALUE_HIGH_COMPARATOR);
			
			return bestBallPositions.get(0);
		}
		
		
		private List<ValuePoint> cropFoundPositions(final List<ValuePoint> pointsToCrop)
		{
			List<ValuePoint> croppedPoints = new ArrayList<ValuePoint>();
			
			double lowerBorderInField = Geometry.getGoalTheir().getGoalCenter().get(0) * lowerBorder;
			double upperBorderInField = Geometry.getGoalTheir().getGoalCenter().get(0) * upperBorder;
			
			for (ValuePoint possiblePoint : pointsToCrop)
			{
				if ((possiblePoint.get(0) > lowerBorderInField) && (possiblePoint.get(0) < upperBorderInField))
				{
					croppedPoints.add(possiblePoint);
				}
			}
			
			return croppedPoints;
		}
		
		
		private EKickerDevice getKickerDeviceForTarget(final IVector2 target)
		{
			Line lineToTarget = Line.newLine(target, Geometry.getCenter());
			
			List<BotID> nearestBots = AiMath.getFoeBotsNearestToLineSorted(getAiFrame(), lineToTarget);
			
			BotID nearestBot = null;
			
			if (nearestBots.isEmpty())
			{
				return EKickerDevice.STRAIGHT;
			}
			nearestBot = nearestBots.get(0);
			
			double distanceOfClosestFoeBotToLine = GeoMath.distancePL(getAiFrame().getWorldFrame().getBot(nearestBot)
					.getPos(), lineToTarget);
			
			if (distanceOfClosestFoeBotToLine < (botRadiiAwayForStraightKick * Geometry.getBotRadius()))
			{
				// return EKickerDevice.CHIP;
			}
			return EKickerDevice.STRAIGHT;
		}
		
		
		private EKickMode getKickModeForDevice(final EKickerDevice device)
		{
			switch (device)
			{
				case CHIP:
					return EKickMode.PASS;
				case STRAIGHT:
					return EKickMode.FIXED_SPEED;
				default:
					return null;
			}
		}
		
		
		private double getKickSpeedForDevice(final EKickerDevice device)
		{
			switch (device)
			{
				case CHIP:
					return 0;
				case STRAIGHT:
					return kickSpeedStraight;
				default:
					return 0;
			}
		}
	}
	
	
	/**
	 * @return the moveDestination
	 */
	public IVector2 getMoveDestination()
	{
		return moveDestination;
	}
	
	
	/**
	 * @return The chosen shot target for this shooter
	 */
	public IVector2 getShotTarget()
	{
		return shotTarget;
	}
	
}
