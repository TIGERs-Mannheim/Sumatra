/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;


/**
 * This play moves all bots to the maintenance position.
 */
public abstract class AMaintenancePlay extends APlay
{
	private long tDestReached = 0;


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

		Map<BotID, ITrackedBot> otherBots = new IdentityHashMap<>();
		otherBots.putAll(getWorldFrame().getOpponentBots());
		otherBots.putAll(getWorldFrame().getTigerBotsVisible());

		List<ARole> roles = new ArrayList<>(getRoles());
		roles.sort(Comparator.comparing(ARole::getBotID));

		ICircle shape;
		boolean destsReached = true;
		for (ARole aRole : roles)
		{
			MoveRole moveRole = (MoveRole) aRole;
			do
			{
				dest = dest.addNew(direction);
				shape = Circle.createCircle(dest, Geometry.getBotRadius() * 2);

			} while (AiMath.isShapeOccupiedByBots(shape, AiMath.getNonMovingBots(otherBots, 0.2), aRole.getBotID()));

			moveRole.updateDestination(dest);
			moveRole.updateTargetAngle(orientation * Math.PI / 180);

			destsReached = destsReached && (moveRole.isCompleted() || moveRole.isDestinationReached());
		}
		if (destsReached)
		{
			if (tDestReached == 0)
			{
				tDestReached = getWorldFrame().getTimestamp();
			} else if ((getWorldFrame().getTimestamp() - tDestReached) > 1e9)
			{
				for (ARole aRole : roles)
				{
					aRole.setCompleted();
				}
			}
		} else
		{
			tDestReached = 0;
		}
	}
}
