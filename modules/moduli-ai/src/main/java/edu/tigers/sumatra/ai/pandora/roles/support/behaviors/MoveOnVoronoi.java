/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.ASupportBehavior;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.circle.ICircular;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * This support behavior drive the supporter to a near area where is much space using the voronoi diagram.
 */
public class MoveOnVoronoi extends ASupportBehavior
{
	@Configurable(comment = "Defines whether this behavior is active or not", defValue = "false")
	private static boolean isActive = false;

	static
	{
		ConfigRegistration.registerClass("roles", MoveOnVoronoi.class);
	}

	private MoveToSkill skill;


	public MoveOnVoronoi(final ARole role)
	{
		super(role);
	}


	@Override
	public double calculateViability()
	{
		if (getIsActive())
		{
			return 1;
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
		List<ICircle> circles = getRole().getAiFrame().getTacticalField().getFreeSpots();
		Optional<ICircle> selectedCircle = Optional.empty();

		while (circles != null && !circles.isEmpty())
		{
			Optional<ICircle> circle = findNearestCircle(circles);
			if (circle.isPresent())
			{
				selectedCircle = circle;
				break;
			}
		}

		if (selectedCircle.isPresent())
		{
			double dist = selectedCircle.get().center().distanceTo(getRole().getPos());
			if (dist < Geometry.getBotRadius() * 3 || selectedCircle.get().center().x() < getRole().getPos().x())
			{
				selectedCircle = Optional.of(findNextCircle(circles, selectedCircle.get()).orElse(selectedCircle.get()));

			}
			circles.remove(selectedCircle.get());
		}

		selectedCircle.map(ICircular::center).ifPresent(skill::updateDestination);
	}


	private Optional<ICircle> findNextCircle(List<ICircle> circles, ICircle selectedCircle)
	{
		IVector2 pos = getRole().getPos();
		return circles.stream()
				.filter(c -> c.center().x() > selectedCircle.center().x())
				.filter(c -> c != selectedCircle)
				.min(Comparator.comparingDouble(a -> a.center().distanceTo(pos)));
	}


	private Optional<ICircle> findNearestCircle(List<ICircle> circles)
	{
		IVector2 pos = getRole().getPos();
		return circles.stream().min(Comparator.comparingDouble(a -> a.center().distanceTo(pos)));

	}


	@Override
	public boolean getIsActive()
	{
		return isActive && !getRole().getAiFrame().getTacticalField().getFreeSpots().isEmpty();
	}
}
