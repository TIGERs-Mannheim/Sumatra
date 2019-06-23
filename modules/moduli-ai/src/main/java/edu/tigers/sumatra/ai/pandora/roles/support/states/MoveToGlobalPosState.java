/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.states;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;


/**
 * This state moves to the global supporter position for this support role set in ASupportState
 */
public class MoveToGlobalPosState extends ASupporterState
{
	
	private static final Logger log = Logger.getLogger(MoveToGlobalPosState.class.getName());
	private AMoveToSkill moveToSkill;
	
	@Configurable(comment = "switch to local optimization if distance to global pos is smaller than this value")
	private static double distanceReachedGlobalPosition = 1000;
	
	static
	{
		ConfigRegistration.registerClass("roles", MoveToGlobalPosState.class);
	}
	
	
	/**
	 * @param role the parent of the states
	 */
	public MoveToGlobalPosState(final SupportRole role)
	{
		super(role);
	}
	
	
	@Override
	public void doEntryActions()
	{
		moveToSkill = AMoveToSkill.createMoveToSkill();
		setNewSkill(moveToSkill);
	}
	
	
	@Override
	public void doUpdate()
	{
		SupportRole.EEvent globalEvent = parent.getGlobalEvent();
		if (globalEvent != null && globalEvent != SupportRole.EEvent.MOVE)
		{
			triggerEvent(globalEvent);
			return;
		}
		
		IVector2 globalPos = getGlobalPosition();
		if (globalPos == null || isNearGlobalPosition(globalPos, getPos()))
		{
			triggerEvent(SupportRole.EEvent.LOCAL_OPTIMIZATION);
			return;
		}
		boolean isFastPosNecessary = getPos().distanceTo(globalPos) > Geometry.getFieldLength() / 2;
		moveToSkill.getMoveCon().setFastPosMode(isFastPosNecessary);
		if (VectorMath.distancePP(getGlobalPosition(), getWFrame().getBall().getPos()) < 100)
		{
			globalPos = getPos();
		} else if (Geometry.getPenaltyAreaOur().isPointInShape(globalPos))
		{
			log.warn("support pos inside penArea: " + globalPos);
			globalPos = getPos();
		}
		moveToSkill.getMoveCon().updateDestination(globalPos);
	}
	
	
	static boolean isNearGlobalPosition(IVector2 globalPosition, IVector2 currentPosition)
	{
		return globalPosition.distanceTo(currentPosition) < distanceReachedGlobalPosition;
	}
	
}
