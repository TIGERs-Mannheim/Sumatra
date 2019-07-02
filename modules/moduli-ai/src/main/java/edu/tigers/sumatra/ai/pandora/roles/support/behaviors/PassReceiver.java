/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.support.SupportPosition;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.ASupportBehavior;
import static edu.tigers.sumatra.math.SumatraMath.max;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;


/**
 * This behavior aims to receive a pass
 */
public class PassReceiver extends ASupportBehavior
{
	
	@Configurable(comment = "Defines whether this behavior is active of not", defValue = "false")
	private static boolean isActive = false;
	
	@Configurable(comment = "Minimum rating for a point to be considered", defValue = "0.5")
	private static double positionThreshold = 0.5;
	
	private Logger log = Logger.getLogger(PassReceiver.class.getName());
	
	static
	{
		ConfigRegistration.registerClass("roles", PassReceiver.class);
	}
	
	
	public PassReceiver(final ARole role)
	{
		super(role);
	}
	
	
	@Override
	public double calculateViability()
	{
		if (!isActive)
		{
			return 0;
		}
		
		// Check if there is an unassigned position and find the shortest one
		SupportPosition nearestPosition;
		IVector2 botPos = getRole().getBot().getPos();
		
		List<SupportPosition> supportPositionList = getRole().getAiFrame().getTacticalField()
				.getSelectedSupportPositions();
		supportPositionList = supportPositionList.stream().filter(
				x -> x.getAssignedBot() == null)
				.sorted((x, y) -> new VectorDistanceComparator(botPos).compare(x.getPos(), y.getPos()))
				.filter(x -> max(x.getPassScore(), x.getShootScore()) >= positionThreshold).collect(Collectors.toList());
		
		if (supportPositionList.isEmpty())
		{
			return 0; // no matching position
		} else
		{
			nearestPosition = supportPositionList.get(0);
		}
		
		nearestPosition.assignBot(getRole().getBotID());
		return max(nearestPosition.getShootScore(), nearestPosition.getPassScore());
	}
	
	
	@Override
	public void doEntryActions()
	{
		getRole().setNewSkill(AMoveToSkill.createMoveToSkill());
	}
	
	
	@Override
	public void doUpdate()
	{
		List<SupportPosition> positions = getRole().getAiFrame().getTacticalField().getSelectedSupportPositions();
		IVector2 dest = null;
		for (SupportPosition p : positions)
		{
			if (p.getAssignedBot() == getRole().getBotID())
			{
				dest = p.getPos();
				break;
			}
		}
		
		// We should now have the position where the bot was assigned to, if not something went wrong.
		
		if (dest == null)
		{
			log.warn("Bot " + getRole().getBotID() + " is assigned as PassReciver but without a target");
			return;
		}
		
		// update destination
		getRole().getCurrentSkill().getMoveCon().updateDestination(dest);
	}

	@Override
	public boolean getIsActive()
	{
		return PassReceiver.isActive();
	}
	
	public static boolean isActive()
	{
		return isActive;
	}
}
