/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.match;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;


/**
 * A OneBot Play to realize a solo keeper behavior.
 */
public class KeeperPlay extends APlay
{
	public KeeperPlay()
	{
		super(EPlay.KEEPER);
	}


	@Override
	protected ARole onAddRole()
	{
		if (!getRoles().isEmpty())
		{
			throw new IllegalStateException("Keeper Play can not handle more than 1 role!");
		}
		return new KeeperRole();
	}
}
