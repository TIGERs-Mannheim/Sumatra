/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;
import com.google.common.collect.Sets;

import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.NGeometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.AttackerTouchedOpponentInDefenseArea;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This Rule detects attackers that touch a robot inside its own defense area
 */
public class AttackerTouchedOpponentRobotDetector extends AGameEventDetector
{
	@Configurable(comment = "[mm] The minimal distance that is not considered a contact", defValue = "10.0")
	private static double touchDistance = 10.0;

	@Configurable(comment = "[s] The amount of time before a violation is reported again for the same bot", defValue = "1.5")
	private static double violatorCoolDownTime = 1.5;

	@Configurable(comment = "[mm] Extra margin around the pen area. There is always a bot-radius wide margin.", defValue = "-20.0")
	private static double penAreaMargin = -20.0;


	private Map<Violation, Long> oldViolators = new HashMap<>();


	public AttackerTouchedOpponentRobotDetector()
	{
		super(EGameEventDetectorType.ATTACKER_TOUCHED_OPPONENT_ROBOT, EGameState.RUNNING);
	}


	@Override
	public Optional<IGameEvent> doUpdate()
	{
		IBotIDMap<ITrackedBot> bots = frame.getWorldFrame().getBots();
		long timestamp = frame.getTimestamp();

		Set<BotID> defendersInPenArea = bots.entrySet().stream()
				.filter(e -> NGeometry.getPenaltyArea(e.getKey().getTeamColor())
						.withMargin(Geometry.getBotRadius() + penAreaMargin)
						.isPointInShape(e.getValue().getPos()))
				.map(Map.Entry::getKey).collect(Collectors.toSet());

		Set<Violation> violators = new HashSet<>();

		for (BotID defenderID : defendersInPenArea)
		{
			if (!bots.containsKey(defenderID))
			{
				/*
				 * The defender is omitted by the world predictor in this frame (maybe due to vision problems) or not
				 * located
				 * on the field. In this case it is skipped until it reappears on the field.
				 */
				continue;
			}
			ITrackedBot defender = bots.getWithNull(defenderID);
			violators.addAll(getViolators(bots, defender));
		}

		/*
		 * Update the timestamp of all violators for which a violation has already been generated but which are still
		 * violating the rule
		 */
		Sets.intersection(violators, oldViolators.keySet())
				.forEach(bot -> oldViolators.put(bot, timestamp));

		/*
		 * Remove all old violators which have reached the cool down time
		 */
		oldViolators.entrySet()
				.removeIf(entry -> (timestamp - entry.getValue()) / 1e9 > violatorCoolDownTime);

		// get the first new violator
		Optional<Violation> violation = Sets.difference(violators, oldViolators.keySet()).stream().findFirst();

		if (violation.isPresent())
		{
			ITrackedBot violator = bots.getWithNull(violation.get().getViolator());
			ITrackedBot victim = bots.getWithNull(violation.get().getVictim());
			if (violator == null || victim == null)
			{
				// robots vanished?
				return Optional.empty();
			}
			IVector2 location = calcTwoPointCenter(violator.getPos(), victim.getPos());

			// add current validator to old validator
			oldViolators.put(violation.get(), timestamp);

			return Optional.of(new AttackerTouchedOpponentInDefenseArea(violation.get().getViolator(),
					violation.get().getVictim(), location));

		}
		return Optional.empty();
	}


	private Set<Violation> getViolators(final IBotIDMap<ITrackedBot> bots, final ITrackedBot defender)
	{
		ETeamColor defenderColor = defender.getBotId().getTeamColor();
		ICircle circle = Circle.createCircle(defender.getPos(), touchDistance + (Geometry.getBotRadius() * 2));
		List<ITrackedBot> attackingBots = AutoRefUtil.filterByColor(bots, defenderColor.opposite());

		return attackingBots.stream()
				.filter(bot -> circle.isPointInShape(bot.getPos(), 0))
				.map(bot -> new Violation(bot.getBotId(), defender.getBotId()))
				.collect(Collectors.toSet());
	}

	private class Violation
	{
		private final BotID violator;
		private final BotID victim;


		public Violation(BotID violator, BotID victim)
		{
			this.violator = violator;
			this.victim = victim;
		}


		public BotID getViolator()
		{
			return violator;
		}


		public BotID getVictim()
		{
			return victim;
		}


		@Override
		public int hashCode()
		{
			return violator.hashCode() * victim.hashCode();
		}


		@Override
		public boolean equals(final Object obj)
		{
			if (obj == null || obj.getClass() != Violation.class)
			{
				return false;
			}
			Violation other = (Violation) obj;
			return other.getViolator() == violator && other.getVictim() == victim;
		}
	}


	private IVector2 calcTwoPointCenter(final IVector2 a, final IVector2 b)
	{
		Vector2 ab = b.subtractNew(a);
		return ab.multiply(0.5d).add(a);
	}


	@Override
	public void doReset()
	{
		oldViolators.clear();
	}
}
