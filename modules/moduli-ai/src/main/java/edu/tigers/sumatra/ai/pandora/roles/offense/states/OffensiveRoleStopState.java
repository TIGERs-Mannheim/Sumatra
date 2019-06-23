/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRoleStopState extends AOffensiveRoleState implements IRoleState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	
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
	
	private AMoveToSkill skill = null;
	
	
	@Override
	public void doExitActions()
	{
		
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
		skill.getMoveCon()
				.updateDestination(moveTarget);
	}
	
	
	private IVector2 calcStopMoveTarget(final WorldFrame wFrame, final BotID key)
	{
		IVector2 moveTarget = null;
		IVector2 ballPos = wFrame.getBall().getPos();
		double distanceToBall = Geometry.getBotToBallDistanceStop()
				+ (Geometry.getBotRadius() * 2.0);
		Circle positionCircle = new Circle(ballPos, distanceToBall);
		
		moveTarget = ballPos.subtractNew(new Vector2(Geometry.getBotToBallDistanceStop(), 0));
		moveTarget = positionCircle.nearestPointOutside(moveTarget);
		
		int i = 0;
		while (!isMoveTargetValid(moveTarget, wFrame, key))
		{
			moveTarget = GeoMath.stepAlongCircle(moveTarget, ballPos, AngleMath.DEG_TO_RAD * 20);
			if (i > 18)
			{
				break;
			}
			i++;
		}
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
		BotIDMap<ITrackedBot> botMap = new BotIDMap<>();
		for (BotID id : wFrame.getTigerBotsAvailable().keySet())
		{
			botMap.put(id, wFrame.getTigerBotsVisible().get(id));
		}
		for (BotID id : wFrame.getFoeBots().keySet())
		{
			botMap.put(id, wFrame.getFoeBot(id));
		}
		botMap.remove(key);
		
		for (BotID id : botMap.keySet())
		{
			if (GeoMath.distancePP(moveTarget, botMap.get(id).getPos()) < (Geometry.getBotRadius() * 3))
			{
				return false;
			}
		}
		return true;
	}
	
	
	@Override
	public Enum<?> getIdentifier()
	{
		return EOffensiveStrategy.STOP;
	}
}
