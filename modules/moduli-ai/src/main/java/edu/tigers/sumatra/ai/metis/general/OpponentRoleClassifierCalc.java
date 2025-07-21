/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.data.TimeLimitedBuffer;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import smile.math.distance.DynamicTimeWarping;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Classifiers opponent robots into typical behavior roles
 */
@RequiredArgsConstructor
public class OpponentRoleClassifierCalc extends ACalculator
{
	@Configurable(comment = "[s] time to keep past man2man marker active", defValue = "0.4")
	private static double timeToKeepPastMan2ManMarkerActive = 0.4;

	private final Map<BotID, Long> man2manMarkerRobotStartTimes = new HashMap<>();

	private final Map<BotID, TimeLimitedBuffer<IVector2>> botVelocities = new HashMap<>();

	@Getter
	private List<BotID> opponentMan2ManMarkers;

	private final Supplier<Set<BotID>> potentialOffensiveBots;


	@Override
	protected void doCalc()
	{
		getWFrame().getBots().values()
				.forEach(e ->
				{
					if (!botVelocities.containsKey(e.getBotId()))
					{
						botVelocities.put(e.getBotId(), new TimeLimitedBuffer<>());
						botVelocities.get(e.getBotId()).setMaxElements(50);
						botVelocities.get(e.getBotId()).setMaxDuration(2.0);
					}
					botVelocities.get(e.getBotId()).add(getWFrame().getTimestamp(), e.getVel());
				});

		opponentMan2ManMarkers = calcOpponentMan2ManMarkers();

		// visualize man2manMarkers
		opponentMan2ManMarkers.forEach(
				e -> {
					var dRectTheir = new DrawableRectangle(
							Rectangle.fromCenter(getWFrame().getBot(e).getPos(), Geometry.getBotRadius() * 2.2,
									Geometry.getBotRadius() * 2.2))
							.setColor(new Color(5, 242, 255, 150))
							.setFill(true);
					getShapes(EAiShapesLayer.AI_OPPONENT_CLASSIFIER).add(dRectTheir);
				}
		);
	}


	private List<BotID> calcOpponentMan2ManMarkers()
	{
		for (var supporter : potentialOffensiveBots.get())
		{
			var closestOpponent = getWFrame().getOpponentBots().values().stream()
					.min(Comparator.comparingDouble(e -> e.getPos().distanceTo(getWFrame().getBot(supporter).getPos())));
			if (closestOpponent.isPresent() &&
					botVelocities.containsKey(closestOpponent.get().getBotId()) &&
					botVelocities.containsKey(supporter) &&
					botVelocities.get(closestOpponent.get().getBotId()).getElements().size() ==
							botVelocities.get(supporter).getElements().size())
			{
				var opponentXVelocities = botVelocities.get(closestOpponent.get().getBotId()).getElements().stream()
						.mapToDouble(IVector::x).toArray();
				var ourXVelocities = botVelocities.get(supporter).getElements().stream().mapToDouble(IVector::x)
						.toArray();
				double velDistX = DynamicTimeWarping.d(opponentXVelocities, ourXVelocities, 30);

				var opponentYVelocities = botVelocities.get(closestOpponent.get().getBotId()).getElements().stream()
						.mapToDouble(IVector::y).toArray();
				var ourYVelocities = botVelocities.get(supporter).getElements().stream().mapToDouble(IVector::y)
						.toArray();
				double velDistY = DynamicTimeWarping.d(opponentYVelocities, ourYVelocities, 30);

				var dRectTheir = new DrawableRectangle(
						Rectangle.fromCenter(closestOpponent.get().getPos(), Geometry.getBotRadius() * 2.2,
								Geometry.getBotRadius() * 2.2))
						.setColor(Color.RED);

				var dRectOur = new DrawableRectangle(
						Rectangle.fromCenter(getWFrame().getBot(supporter).getPos(), Geometry.getBotRadius() * 2.2,
								Geometry.getBotRadius() * 2.2))
						.setColor(Color.GREEN.darker());


				if (velDistX < 20 && velDistY < 20)
				{
					man2manMarkerRobotStartTimes.put(closestOpponent.get().getBotId(), getWFrame().getTimestamp());
				}

				getShapes(EAiShapesLayer.AI_OPPONENT_CLASSIFIER).add(dRectOur);
				getShapes(EAiShapesLayer.AI_OPPONENT_CLASSIFIER).add(dRectTheir);

				var dArrow = new DrawableArrow(closestOpponent.get().getPos(),
						getWFrame().getBot(supporter).getPos().subtractNew(closestOpponent.get()
								.getPos())).setColor(Color.ORANGE);
				getShapes(EAiShapesLayer.AI_OPPONENT_CLASSIFIER).add(dArrow);

				var dAnno = new DrawableAnnotation(
						closestOpponent.get().getPos().addNew(Vector2.fromY(Geometry.getBotRadius() * 1.5)),
						String.format("x: %.2f, y: %.2f", velDistX, velDistY)
				);
				dAnno.setColor(Color.RED);

				getShapes(EAiShapesLayer.AI_OPPONENT_CLASSIFIER).add(dAnno);
			}
		}

		return man2manMarkerRobotStartTimes.entrySet()
				.stream()
				.filter(e -> getWFrame().getBots().containsKey(e.getKey()))
				.filter(e -> getWFrame().getTimestamp() - e.getValue() < timeToKeepPastMan2ManMarkerActive * 1e9)
				.map(Map.Entry::getKey)
				.distinct()
				.toList();
	}
}
