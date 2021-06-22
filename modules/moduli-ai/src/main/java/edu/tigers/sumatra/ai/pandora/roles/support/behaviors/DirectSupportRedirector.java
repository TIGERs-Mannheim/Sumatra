/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.ASupportBehavior;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class DirectSupportRedirector extends ASupportBehavior
{
	@Configurable(comment = "Flag if this behaviour is active", defValue = "true")
	private static boolean isActive = true;

	@Configurable(comment = "[mm]", defValue = "5000.")
	private static double maxDistance = 5000;

	@Configurable(comment = "[mm]", defValue = "5000.")
	private static double maxBallTravelDist = 5000;

	private IVector2 destination;
	static
	{
		ConfigRegistration.registerClass("roles", DirectSupportRedirector.class);
	}

	private MoveToSkill skill;


	public DirectSupportRedirector(final ARole role)
	{
		super(role);
	}


	@Override
	public double calculateViability()
	{
		Optional<IVector2> offensive = getRole().getAiFrame().getTacticalField().getOffensiveStrategy().getAttackerBot()
				.map(b -> getRole().getWFrame().getBot(b).getPos());

		IVector2 goalTheir = Geometry.getGoalTheir().getCenter();
		if (offensive.isPresent())
		{
			List<IVector2> angleRangePosition = getRole().getAiFrame().getTacticalField().getSupportiveGoalPositions()
					.stream()
					.filter(p -> p.distanceTo(goalTheir) + p.distanceTo(offensive.get()) < maxBallTravelDist)
					.collect(Collectors.toList());


			Comparator<IVector2> com = Comparator
					.comparingDouble(
							p -> goalTheir.subtractNew(p).angleTo(offensive.get().subtractNew(p)).orElse(Math.PI));

			Optional<IVector2> pos = angleRangePosition.stream()
					.filter(p -> p.distanceTo(getRole().getPos()) < maxDistance)
					.max(com);

			if (pos.isPresent() && goalTheir.subtractNew(pos.get()).angleTo(offensive.get().subtractNew(pos.get()))
					.orElse(Math.PI) < OffensiveConstants.getMaximumReasonableRedirectAngle())
			{
				Optional<ARole> selectedSupporter = getRole().getAiFrame().getPlayStrategy().getActiveRoles(EPlay.SUPPORT)
						.stream().min(Comparator.comparingDouble(b -> b.getPos().distanceTo(pos.get())));
				if (selectedSupporter.isPresent() && selectedSupporter.get().getBotID() == getRole().getBotID())
				{
					destination = pos.get();
					return 1;
				}
			}
		}
		return 0;
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
		skill.updateDestination(destination);

		List<IDrawableShape> shapes = getRole().getAiFrame().getShapeMap()
				.get(EAiShapesLayer.SUPPORT_ACTIVE_ROLES);
		shapes.add(
				new DrawableArrow(destination, Geometry.getGoalTheir().getCenter().subtractNew(destination), Color.RED));
		Optional<IVector2> offensive = getRole().getAiFrame().getTacticalField().getOffensiveStrategy().getAttackerBot()
				.map(b -> getRole().getWFrame().getBot(b).getPos());

		offensive.ifPresent(o -> shapes.add(new DrawableArrow(o, destination.subtractNew(o), Color.RED)));
	}


	@Override
	public boolean getIsActive()
	{
		return isActive;
	}
}
