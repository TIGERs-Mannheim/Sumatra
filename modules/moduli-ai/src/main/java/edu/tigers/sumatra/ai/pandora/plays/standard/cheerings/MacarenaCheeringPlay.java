/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard.cheerings;

import edu.tigers.sumatra.ai.pandora.plays.standard.CheeringPlay;
import edu.tigers.sumatra.botmanager.botskills.data.ESong;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class MacarenaCheeringPlay extends ASongPlayingCheeringPlay
{

	private static final double DISTANCE_BETWEEN_BOTS = Geometry.getBotRadius() * 3d;
	private static final double MOVE_OFFSET = Geometry.getBotRadius();
	private Map<BotID, IVector2> centerPositions;
	private boolean shake = false;


	public MacarenaCheeringPlay()
	{
		super(List.of(ESong.MACARENA),
				List.of(
						0.0, //  0 | Move 1 - 4 interrupts
						0.6,
						1.2,
						1.8,
						2.4, //  4 | Move 2 - 4 interrupts
						3.0,
						3.6,
						4.2,
						4.8, //  8 | Move 3 - 4 interrupts
						5.4,
						6.0,
						6.6,
						7.2, // 12 | Shake Start
						9.0, // 13 | Shake End
						9.2  // 14 | Turn
				), 4);
	}


	@Override
	public void initialize(CheeringPlay play)
	{
		super.initialize(play);
		centerPositions = null;
	}


	@Override
	void handleInterrupt(int loopCount, int interruptCount)
	{
		if (interruptCount < 12)
		{
			var offset = Vector2.fromAngleLength(getRotation(loopCount), getOffsetDistance(interruptCount));
			for (var role : getPlay().getPermutedRoles())
			{
				role.updateDestination(centerPositions.get(role.getBotID()).addNew(offset));
			}
		} else if (interruptCount == 12)
		{
			shake = true;
		} else if (interruptCount == 13)
		{
			shake = false;
			for (var role : getPlay().getPermutedRoles())
			{
				role.updateDestination(centerPositions.get(role.getBotID()));
			}
		} else if (interruptCount == 14)
		{
			var lookingAngle = getRotation(loopCount + 1);
			for (var role : getPlay().getPermutedRoles())
			{
				role.updateTargetAngle(lookingAngle);
			}
		}
	}


	private double getRotation(int loopCount)
	{
		return AngleMath.normalizeAngle((loopCount % 4) * AngleMath.PI_HALF);
	}


	private double getOffsetDistance(int interruptCount)
	{
		if (interruptCount >= 12)
		{
			return 0;
		}
		return switch (interruptCount % 4)
		{
			case 0, 2 -> MOVE_OFFSET;
			case 1 -> 2 * MOVE_OFFSET;
			default -> 0;
		};
	}


	@Override
	public List<IVector2> calcPositions()
	{
		var roles = getPlay().getPermutedRoles();
		var positions = new ArrayList<IVector2>(roles.size());

		roles.forEach(r -> r.updateTargetAngle(0));
		int numLinesInDanceBlock = (int) Math.sqrt(roles.size());
		int numRowsInDanceBlock = roles.size() / numLinesInDanceBlock;

		double startYValue = -0.5 * (numLinesInDanceBlock - 1) * DISTANCE_BETWEEN_BOTS;
		int additionalBotsInMid = roles.size() % numLinesInDanceBlock;

		int roleCount = 0;
		for (int i = 0; i < numLinesInDanceBlock; i++)
		{
			int numBotsInRow = numRowsInDanceBlock;
			if (i == numLinesInDanceBlock / 2)
				numBotsInRow += additionalBotsInMid;

			var botsInRow = new ArrayList<>();
			for (int j = 0; j < numBotsInRow; j++)
				botsInRow.add(roles.get(roleCount++));

			int numBots = botsInRow.size();
			double startX = -0.5 * (numBots - 1) * DISTANCE_BETWEEN_BOTS;

			int j = 0;
			for (int k = 0; k < botsInRow.size(); k++)
			{
				double x = startX + j * DISTANCE_BETWEEN_BOTS;

				IVector2 target = Vector2.fromXY(x, startYValue + i * DISTANCE_BETWEEN_BOTS);

				positions.add(target);
				j++;
			}

		}
		return positions;
	}


	@Override
	public void doUpdate()
	{
		if (centerPositions == null)
		{
			var roles = getPlay().getPermutedRoles();
			var positions = calcPositions();
			centerPositions = IntStream.range(0, roles.size()).boxed()
					.collect(Collectors.toMap(i -> roles.get(i).getBotID(), positions::get));
		}
		super.doUpdate();

		if (shake)
		{
			// Orthogonal to looking distance
			var offset = Vector2.fromAngleLength(getRotation(getLoopCounter() + 1), 0.3 * MOVE_OFFSET);
			boolean flipDirection = Math.round((timeSinceStartOfLoop() - 7.2) * 10) % 2 == 0;
			for (var role : getPlay().getPermutedRoles())
			{
				if (flipDirection)
				{
					role.updateDestination(centerPositions.get(role.getBotID()).subtractNew(offset));
				} else
				{
					role.updateDestination(centerPositions.get(role.getBotID()).addNew(offset));
				}
			}
		}
	}


	@Override
	public ECheeringPlays getType()
	{
		return ECheeringPlays.MACARENA;
	}
}
