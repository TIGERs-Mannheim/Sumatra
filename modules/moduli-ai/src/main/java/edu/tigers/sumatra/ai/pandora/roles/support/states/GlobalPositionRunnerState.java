/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.states;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.support.SupportPosition;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;


/**
 * This state runs through all global positions
 */
public class GlobalPositionRunnerState extends ASupporterState
{
	
	private AMoveToSkill moveToSkill;
	private SupportPosition currentPosition;
	
	@Configurable(comment = "Time to reach global position in seconds")
	private static int timeToReachPosition = 3;
	
	@Configurable(comment = "Marked as reached distance")
	private static double distanceReached = 200;
	
	@Configurable(comment = "Number of possible next global positions")
	private static int maxPossibleNextPositions = 5;
	
	private Random rnd = new Random(0);
	
	static
	{
		ConfigRegistration.registerClass("roles", GlobalPositionRunnerState.class);
	}
	
	
	/**
	 * @param role the parent of the states
	 */
	public GlobalPositionRunnerState(final SupportRole role)
	{
		super(role);
	}
	
	
	@Override
	public void doEntryActions()
	{
		rnd = new Random(getWFrame().getTimestamp());
		moveToSkill = AMoveToSkill.createMoveToSkill();
		setNewSkill(moveToSkill);
	}
	
	
	@Override
	public void doUpdate()
	{
		SupportRole.EEvent globalEvent = parent.getGlobalEvent();
		if (globalEvent != null && globalEvent != SupportRole.EEvent.RUN_THROUGH_GLOBAL_POSITIONS)
		{
			triggerEvent(globalEvent);
			return;
		}
		
		if (isUpdatePositionReasonable())
		{
			VectorDistanceComparator comperator = new VectorDistanceComparator(getPos());
			List<SupportPosition> globalPos = getAiFrame().getTacticalField().getSelectedSupportPositions().stream()
					.filter(s -> !s.isCovered())
					.filter(pos -> pos.getPos().distanceTo(getPos()) > distanceReached)
					.sorted((a, b) -> comperator.compare(a.getPos(), b.getPos()))
					.limit(maxPossibleNextPositions)
					.collect(Collectors.toList());
			
			if (globalPos.isEmpty())
			{
				triggerEvent(SupportRole.EEvent.LOCAL_OPTIMIZATION);
				return;
			}
			
			IVector2 destination = globalPos.get(rnd.nextInt(globalPos.size())).getPos();
			currentPosition = new SupportPosition(destination, getWFrame().getTimestamp());
			
			moveToSkill.getMoveCon().updateDestination(destination);
		}
	}
	
	
	private boolean isUpdatePositionReasonable()
	{
		return currentPosition == null
				|| currentPosition.getPos().distanceTo(getPos()) < distanceReached
				|| currentPosition.getBirth() + timeToReachPosition * 1e9 < getWFrame().getTimestamp();
	}
}
