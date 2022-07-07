/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.Comparator;
import java.util.List;


/**
 * This play moves all bots to the maintenance position.
 */
public abstract class AMaintenancePlay extends APlay
{
	private long tDestReached = 0;

	private final PointChecker pointChecker = new PointChecker().checkBallDistances().checkPointFreeOfBots();

	protected AMaintenancePlay(EPlay play)
	{
		super(play);
	}


	@Override
	protected ARole onRemoveRole()
	{
		return getLastRole();
	}


	@Override
	protected ARole onAddRole()
	{
		MoveRole role = new MoveRole();
		role.getMoveCon().setPenaltyAreaOurObstacle(false);
		role.getMoveCon().setGoalPostsObstacle(true);
		return role;
	}


	protected void drawDestinations(IVector2 startPos, IVector2 direction, double orientation)
	{
		for (int i = 0; i < BotID.BOT_ID_MAX; i++)
		{
			IVector2 pos = startPos.addNew(direction.multiplyNew(i));
			getShapes(EAiShapesLayer.AI_MAINTENANCE).add(
					new DrawableBotShape(pos, orientation * Math.PI / 180, Geometry.getBotRadius(),
							Geometry.getOpponentCenter2DribblerDist())
			);
		}
	}


	/**
	 * Compute bot actions based on a line defined by start pos and direction vector
	 *
	 * @param startPos
	 * @param direction
	 * @param orientation
	 */
	protected void calculateBotActions(IVector2 startPos, IVector2 direction, double orientation)
	{
		IVector2 dest = startPos.subtractNew(direction);

		List<MoveRole> roles = findRoles(MoveRole.class);
		roles.sort(Comparator.comparing(ARole::getBotID));

		boolean destsReached = true;
		for (MoveRole role : roles)
		{
			do
			{
				dest = dest.addNew(direction);
			} while (!pointChecker.allMatch(getAiFrame().getBaseAiFrame(), dest, role.getBotID()));

			role.updateDestination(dest);
			role.updateTargetAngle(orientation * Math.PI / 180);

			destsReached = destsReached && (role.isCompleted() || role.isDestinationReached());
		}
		if (destsReached)
		{
			if (tDestReached == 0)
			{
				tDestReached = getWorldFrame().getTimestamp();
			} else if ((getWorldFrame().getTimestamp() - tDestReached) > 1e9)
			{
				getRoles().forEach(ARole::setCompleted);
			}
		} else
		{
			tDestReached = 0;
		}
	}
}
