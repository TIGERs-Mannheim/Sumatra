package edu.tigers.sumatra.ai.pandora.roles.defense;

import java.util.function.Function;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Helper class to find a destination that keeps a robot away from the ball during ball placement.
 */
public class KeepDistanceToBall
{
	private static final Logger log = Logger.getLogger(KeepDistanceToBall.class.getName());

	private AthenaAiFrame aiFrame;
	private BotID botID;
	private IVector2 destination;

	private final PointChecker pointChecker = new PointChecker()
			.checkBallDistances()
			.checkInsideField()
			.checkCustom(this::isPointFreeOfBots)
			.checkCustom(this::checkOpponentCanGetBallInPenArea);


	public KeepDistanceToBall addCheck(Function<IVector2, Boolean> function)
	{
		pointChecker.checkCustom(function);
		return this;
	}


	public void update(AthenaAiFrame aiFrame, BotID botID, IVector2 destination)
	{
		this.aiFrame = aiFrame;
		this.botID = botID;
		this.destination = destination;
	}


	public IVector2 freeDestination()
	{
		if (pointChecker.allMatch(getAiFrame(), destination))
		{
			return destination;
		} else
		{
			return findNextFreeDest();
		}
	}


	private IVector2 findNextFreeDest()
	{
		IVector2 dest = getPos();
		double step = 100;
		double size = 0;
		int i = 0;
		while (!pointChecker.allMatch(getAiFrame(), dest))
		{
			if (i % 8 == 0)
			{
				size += step;
			}

			IVector2 dir = Vector2.fromAngle(i * AngleMath.PI_QUART);
			dest = getPos().addNew((dir).scaleToNew(size));

			if (step > Geometry.getFieldLength() * 2)
			{
				log.warn("Could not find next free dest at " + getPos());
				return getPos();
			}

			i++;
		}
		return dest;
	}


	private boolean isPointFreeOfBots(final IVector2 point)
	{
		if (point.equals(destination))
		{
			// destination may be occupied by companion, but that's fine
			return true;
		}
		double distance = Geometry.getBotRadius() * 2 + 10;
		return getWFrame().getBots().values().stream()
				.filter(bot -> bot.getBotId() != getBotID())
				.noneMatch(bot -> bot.getPos().distanceTo(point) < distance);
	}


	private boolean checkOpponentCanGetBallInPenArea(final IVector2 point)
	{
		// allow opponents to pass the defense when the ball is inside the penArea
		if (!Geometry.getPenaltyAreaOur().isPointInShapeOrBehind(getWFrame().getBall().getPos()))
		{
			return true;
		}
		double distance = RuleConstraints.getStopRadius();
		return getWFrame().getBots().values().stream()
				.filter(bot -> bot.getBotId() != getBotID())
				.filter(bot -> bot.getBotId().getTeamColor() != getBotID().getTeamColor())
				.noneMatch(bot -> bot.getPos().distanceTo(point) < distance);
	}


	private AthenaAiFrame getAiFrame()
	{
		return aiFrame;
	}


	private WorldFrame getWFrame()
	{
		return aiFrame.getWorldFrame();
	}


	private BotID getBotID()
	{
		return botID;
	}


	private IVector2 getPos()
	{
		return getWFrame().getBot(getBotID()).getPos();
	}
}
