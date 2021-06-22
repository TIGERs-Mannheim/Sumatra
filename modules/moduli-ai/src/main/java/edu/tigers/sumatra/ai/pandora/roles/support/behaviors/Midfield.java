/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPassFactory;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.ASupportBehavior;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;

import java.awt.Color;
import java.util.Comparator;
import java.util.Optional;


public class Midfield extends ASupportBehavior
{
	@Configurable(comment = "Defines if this behavior is enabled", defValue = "true")
	private static boolean isActive = true;

	@Configurable(comment = "How many possible targets should be considered by each bot", defValue = "4")
	private static int nClosestPoints = 3;

	@Configurable(comment = "min distance to consider a possible target", defValue = "600.0")
	private static double minDistanceToPoint = 600.0;

	@Configurable(comment = "Min value for pass score", defValue = "0.25")
	private static double passScoreThreshold = 0.25;

	@Configurable(comment = "Ball decision offset", defValue = "-1000.0")
	private static double ballDecisionOffset = -1000.0;

	@Configurable(comment = "Minimum required offensive positions", defValue = "1")
	private static int minRequiredOffensivePositions = 1;

	@Configurable(comment = "Offense Zone", defValue = "3000.0")
	private static double offenseZone = 3000.0;

	static
	{
		ConfigRegistration.registerClass("roles", Midfield.class);
	}

	private final PassFactory passFactory = new PassFactory();
	private final RatedPassFactory ratedPassFactory = new RatedPassFactory();
	private IVector2 destination = null;
	private MoveToSkill skill;


	public Midfield(ARole role)
	{
		super(role);
	}


	@Override
	public double calculateViability()
	{
		if (!isReasonable() || !isActive)
		{
			return 0;
		}

		Optional<IVector2> pos = getRole().getAiFrame().getTacticalField().getSupporterMidfieldPositions()
				.stream()
				.sorted(Comparator.comparingDouble(point -> point.distanceToSqr(getRole().getPos())))
				.limit(nClosestPoints)
				.filter(point -> point.distanceTo(getRole().getPos()) > minDistanceToPoint)
				.findFirst();

		if (pos.isPresent())
		{
			getRole().getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_MIDFIELD)
					.add(new DrawableCircle(Circle.createCircle(pos.get(), Geometry.getBotRadius() * 1.2), Color.ORANGE));
			getRole().getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_MIDFIELD)
					.add(new DrawableLine(Line.fromPoints(getRole().getPos(), pos.get()), Color.ORANGE));
			destination = pos.get();

			passFactory.update(getRole().getWFrame());
			ratedPassFactory.update(getRole().getWFrame().getOpponentBots().values());

			var pass = passFactory
					.straight(getRole().getBall().getPos(), destination, BotID.noBot(), getRole().getBotID());
			var rating = ratedPassFactory.rateMaxCombined(pass, EPassRating.PASSABILITY, EPassRating.INTERCEPTION);

			if (rating < passScoreThreshold)
			{
				return 0;
			}
			return rating;
		}
		return 0;
	}


	private boolean isReasonable()
	{
		boolean ballOnOurHalf = getRole().getWFrame().getBall().getPos().x() < ballDecisionOffset;
		boolean isOnOpponentHalf = getRole().getPos().x() > 0;
		boolean enoughOffensives = getRole().getWFrame().getTigerBotsAvailable().values().stream()
				.filter(bot -> bot.getPos().x() >= offenseZone).count() > minRequiredOffensivePositions;

		getRole().getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_MIDFIELD)
				.add(new DrawableLine(Line.fromPoints(
						Vector2.fromXY(ballDecisionOffset, -0.5 * Geometry.getFieldWidth()),
						Vector2.fromXY(ballDecisionOffset, 0.5 * Geometry.getFieldWidth())), Color.ORANGE));

		getRole().getAiFrame().getShapeMap().get(EAiShapesLayer.SUPPORT_MIDFIELD)
				.add(new DrawableLine(Line.fromPoints(
						Vector2.fromXY(offenseZone, -0.5 * Geometry.getFieldWidth()),
						Vector2.fromXY(offenseZone, 0.5 * Geometry.getFieldWidth())), Color.RED));

		return enoughOffensives && isOnOpponentHalf && ballOnOurHalf;
	}


	@Override
	public boolean getIsActive()
	{
		return isActive;
	}


	@Override
	public void doEntryActions()
	{
		skill = MoveToSkill.createMoveToSkill();
		getRole().setNewSkill(skill);
	}


	@Override
	public void doUpdate()
	{
		if (destination != null)
		{
			skill.updateDestination(destination);
		}
	}
}
