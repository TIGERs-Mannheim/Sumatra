/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.cheerings.ECheeringPlays;
import edu.tigers.sumatra.ai.pandora.plays.standard.cheerings.ICheeringPlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.cheerings.TigerCheeringPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.botmanager.botskills.data.ESong;
import edu.tigers.sumatra.botmanager.botskills.data.MultimediaControl;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.ArrayList;
import java.util.List;


public class CheeringPlay extends APlay
{
	private List<ICheeringPlay> plays = new ArrayList<>();
	private ICheeringPlay activePlay;
	private boolean onPosition = false;


	public CheeringPlay()
	{
		super(EPlay.CHEERING);
		plays.add(new TigerCheeringPlay());
	}


	public void setSong(BotID botID, ESong song)
	{
		MultimediaControl multimediaControl = getAiFrame().getBaseAiFrame().getMultimediaControl().get(botID);
		if (multimediaControl == null)
		{
			return;
		}

		multimediaControl.setSong(song);
	}


	private void resetBots()
	{
		for (ARole role : getRoles())
		{
			((MoveRole) role).getMoveCon().setBotsObstacle(true);
			((MoveRole) role).getMoveCon().setBallObstacle(false);
			((MoveRole) role).getMoveCon().setPenaltyAreaOurObstacle(false);
			((MoveRole) role).getMoveCon().setPenaltyAreaTheirObstacle(false);
			setSong(role.getBotID(), ESong.NONE);
		}
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		if (getRoles().size() < 3)
		{
			return;
		}

		if (activePlay == null || activePlay.isDone())
		{
			resetBots();

			if (plays.isEmpty())
			{
				plays = ECheeringPlays.getShuffledList();
			}
			activePlay = plays.remove(0);

			onPosition = false;
			activePlay.initialize(this);
			List<IVector2> destinations = reorderDestinationsToRoles(activePlay.calcPositions());
			List<ARole> roles = getRoles();
			for (int i = 0; i < destinations.size(); i++)
			{
				((MoveRole) roles.get(i)).updateDestination(destinations.get(i));
			}
		}

		if (onPosition)
		{
			activePlay.doUpdate();
		} else
		{
			List<ARole> roles = getRoles();
			onPosition = true;
			for (ARole aRole : roles)
			{
				MoveRole role = (MoveRole) aRole;
				if (!role.isDestinationReached())
				{
					onPosition = false;
				}
			}
		}
	}


	@Override
	protected ARole onRemoveRole()
	{
		return getLastRole();
	}


	@Override
	protected ARole onAddRole()
	{
		return new MoveRole();
	}
}
