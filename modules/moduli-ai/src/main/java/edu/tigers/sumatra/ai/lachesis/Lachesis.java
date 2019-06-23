/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.lachesis;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class is used to assigned the needed roles of each play to a robot.
 * (@see {@link Lachesis#assignRoles(AthenaAiFrame)})
 * 
 * @author Gero, Oliver Steinbrecher <OST1988@aol.com>
 */
public class Lachesis
{
	private final IRoleAssigner roleAssigner;
	
	
	/**
	 * Default
	 */
	public Lachesis()
	{
		roleAssigner = new SimpleRoleAssigner();
	}
	
	
	/**
	 * <p>
	 * Takes the roles and assigns them. It also catches a lot of special cases (less roles then bots, less bots then
	 * roles, etc.) for debugging purposes.
	 * </p>
	 * 
	 * @param frame
	 */
	public final void assignRoles(final AthenaAiFrame frame)
	{
		final BotIDMap<ITrackedBot> assignees = new BotIDMap<>(
				frame.getWorldFrame().tigerBotsAvailable);
		roleAssigner.assignRoles(assignees, frame.getPlayStrategy().getActivePlays(), frame);
	}
}
