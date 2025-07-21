/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard.cheerings;

import edu.tigers.sumatra.ai.pandora.plays.standard.CheeringPlay;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.botmanager.botskills.data.ELedColor;
import edu.tigers.sumatra.botmanager.botskills.data.ESong;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.ArrayList;
import java.util.List;


public class ImperialCheeringPlay extends ASongPlayingCheeringPlay
{

	private static final double DISTANCE = -2.5 * Geometry.getBotRadius();
	private static final Vector2 STEP = Vector2.fromXY(-0.5 * Geometry.getBotRadius(), 0);
	private List<MoveRole> marching;


	public ImperialCheeringPlay()
	{
		super(
				List.of(ESong.IMPERIAL_MARCH),
				getTimings(),
				1
		);

	}


	@Override
	public void initialize(CheeringPlay play)
	{
		super.initialize(play);
		marching = new ArrayList<>();
	}


	private static List<Double> getTimings()
	{
		var timings = new ArrayList<Double>();
		for (double i = 0; i < ESong.IMPERIAL_MARCH.getDuration(); i = i + 0.6)
		{
			timings.add(i);
		}
		return timings;
	}


	@Override
	public List<IVector2> calcPositions()
	{
		var roles = getPlay().getPermutedRoles();
		var positions = new ArrayList<IVector2>(roles.size());
		boolean isEven = roles.size() % 2 == 0;

		addMarching(roles.getFirst());

		roles.stream().map(MoveRole::getBotID).forEach(botID -> getPlay().setEyeColor(botID, ELedColor.RED));

		roles.forEach(r -> r.updateTargetAngle(AngleMath.PI));

		positions.add(Vector2.fromXY(200, 0));

		for (int i = 0; i < roles.size() - 1; i++)
		{
			double bonus = isEven && i % 2 == 1 ? 0.5 * DISTANCE : 0;
			positions.add(
					Vector2.fromXY(bonus + DISTANCE * (int) (i / 2.0), 6 * Geometry.getBotRadius() * Math.pow(-1, i % 2)));
		}

		return positions;
	}


	@Override
	public ECheeringPlays getType()
	{
		return ECheeringPlays.IMPERIAL_MARCH;
	}


	@Override
	void handleInterrupt(int loopCount, int interruptCount)
	{
		marching.forEach(r -> r.updateDestination(r.getDestination().addNew(STEP)));

		List<MoveRole> additions;

		if (marching.size() == 1)
		{
			additions = getPlay().getPermutedRoles().stream()
					.filter(r -> !marching.contains(r))
					.filter(r -> r.getPos().x() > marching.getLast().getPos().x() + 2 * Geometry.getBotRadius())
					.toList();
		} else
		{
			additions = getPlay().getPermutedRoles().stream()
					.filter(r -> !marching.contains(r))
					.filter(
							r -> r.getPos().x() > marching.get(marching.size() - 2).getPos().x() + 2 * Geometry.getBotRadius())
					.toList();
		}
		additions.forEach(this::addMarching);
		additions.forEach(this::shiftPos);
	}


	private void addMarching(MoveRole role)
	{
		marching.add(role);
	}


	private void shiftPos(MoveRole role)
	{
		int sign = role.getPos().y() > 0 ? -1 : 1;
		role.updateDestination(role.getPos().addNew(Vector2.fromY(sign * 3 * Geometry.getBotRadius())));
	}
}
