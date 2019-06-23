/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRoleStopState extends AOffensiveRoleState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	
	private AMoveToSkill skill = null;
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	
	/**
	 * @param role
	 */
	public OffensiveRoleStopState(final OffensiveRole role)
	{
		super(role);
	}
	
	
	@Override
	public IVector2 getMoveDest()
	{
		return skill.getMoveCon().getDestination();
	}
	
	
	@Override
	public String getIdentifier()
	{
		return OffensiveStrategy.EOffensiveStrategy.STOP.name();
	}
	
	
	@Override
	public void doExitActions()
	{
		// not needed here
	}
	
	
	@Override
	public void doEntryActions()
	{
		skill = AMoveToSkill.createMoveToSkill();
		skill.getMoveCon().updateLookAtTarget(new DynamicPosition(getWFrame().getBall()));
		setNewSkill(skill);
	}
	
	
	@Override
	public void doUpdate()
	{
		IVector2 moveTarget = calcStopMoveTarget(getWFrame(), getBotID());
		moveTarget = AiMath.adjustMovePositionWhenItsInvalid(getWFrame(), getBotID(), moveTarget);
		skill.getMoveCon()
				.updateDestination(moveTarget);
	}
	
	
	private IVector2 calcStopMoveTarget(final WorldFrame wFrame, final BotID key)
	{
		IVector2 ballPos = wFrame.getBall().getPos();
		IVector2 moveTarget;
		double distanceToBall = Geometry.getBotToBallDistanceStop()
				+ (Geometry.getBotRadius() * 2.0);

		int safteyCounter = 0;
		do
		{
			ICircle positionCircle = Circle.createCircle(ballPos, distanceToBall);
			moveTarget = ballPos.subtractNew(Vector2.fromXY(Geometry.getBotToBallDistanceStop(), 0));
			moveTarget = positionCircle.nearestPointOutside(moveTarget);

			if (safteyCounter > 8)
			{
				log.warn("Offensive Stop State could not find a valid Position.");
				break;
			}

			// check circle around ball
			for (int i = 0; i < 18; i++)
			{
				moveTarget = CircleMath.stepAlongCircle(moveTarget, ballPos, AngleMath.deg2rad(20));
				if (isMoveTargetValid(moveTarget, wFrame, key))
				{
					break;
				}
			}

			// bigger radius for next circle check
			distanceToBall += Geometry.getBotRadius() * 1.2;
			safteyCounter++;
		} while (!isMoveTargetValid(moveTarget, wFrame, key));
		return moveTarget;
	}
	
	
	private boolean isMoveTargetValid(final IVector2 moveTarget, final WorldFrame wFrame, final BotID key)
	{
		double marginPenalty = Geometry.getPenaltyAreaMargin() + 50;
		if (!Geometry.getField().isPointInShape(moveTarget))
		{
			return false;
		}
		if (Geometry.getPenaltyAreaOur().isPointInShape(moveTarget, marginPenalty))
		{
			return false;
		}
		if (Geometry.getPenaltyAreaTheir().isPointInShape(moveTarget, marginPenalty))
		{
			return false;
		}
		
		BotIDMap<ITrackedBot> botMap = new BotIDMap<>(wFrame.getBots());
		botMap.remove(key);
		
		for (BotID id : botMap.keySet())
		{
			if (VectorMath.distancePP(moveTarget, botMap.getWithNull(id).getPos()) < (Geometry.getBotRadius() * 3))
			{
				return false;
			}
		}
		return true;
	}
	
	
}
