/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import edu.tigers.sumatra.ai.metis.m2mmarking.Man2ManMarkerPositionFinder;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.Set;


/**
 * Defender that protects near an opponent (marking the threat).
 */
public class Man2ManMarkerRole extends AOuterDefenseRole
{
	private final Man2ManMarkerPositionFinder man2ManMarkerPositionFinder = new Man2ManMarkerPositionFinder();


	public Man2ManMarkerRole()
	{
		super(ERole.MAN_2_MAN_MARKER);

		setInitialState(new DefendState());
	}


	@Override
	protected Set<BotID> ignoredBots(IVector2 dest)
	{
		return closeOpponentBots(dest);
	}


	@Override
	protected IVector2 findDest()
	{
		return man2ManMarkerPositionFinder.findMan2ManMarkerPosition(getWFrame().getBots(), getBotID(), threat);
	}


}
