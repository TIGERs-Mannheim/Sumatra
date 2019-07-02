/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.others.cheerings;

import static edu.tigers.sumatra.math.AngleMath.PI_TWO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.tigers.sumatra.ai.pandora.plays.others.CheeringPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.botmanager.botskills.data.ESong;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


public class TigerCheeringPlay implements ICheeringPlay
{
	private static final double SECONDS_PER_FULL_TURN = 6;
	private static final double SECONDS_PER_TURN = 30;
	private static final double[][] PATH = {
			{ 0, 0 },
			{ 0.07, 0.07 },
			{ 0.15, 0.14 },
			{ 0.23, 0.2 },
			{ 0.31, 0.26 },
			{ 0.39, 0.31 },
			{ 0.48, 0.35 },
			{ 0.58, 0.38 },
			{ 0.68, 0.38 },
			{ 0.78, 0.37 },
			{ 0.87, 0.32 },
			{ 0.93, 0.24 },
			{ 0.98, 0.15 },
			{ 1, 0.06 }
	};
	private static final int[] INTERVALS = {
			0, 55, 110, 125, 151, 166, 192, 275, 330, 345, 371, 386, 412, 495, 550, 565, 591, 606, 632, 742
	};
	private static final int INTERVAL_LENGTH = 880;
	private CheeringPlay play = null;
	private double scale;
	private int numberOfLoops = 0;
	private int numberOfActiveLoops = -1;


	@Override
	public void initialize(final CheeringPlay play)
	{
		this.play = play;
		int totalNumberOfPositions = PATH.length * 4 - 2;
		for (int i = 0; i < totalNumberOfPositions; i++)
		{
			wrapPosition(i);
		}
		scale = Geometry.getFieldWidth() / 2 - Geometry.getBotRadius() / 2 - 300;

		if (scale > 2000)
		{
			scale = 2000;
		}
	}


	@Override
	public boolean isDone()
	{
		// 4-Cycles
		return numberOfLoops == 3519;
	}


	@Override
	public List<IVector2> calcPositions()
	{
		Collection roles = play.getRoles();
		List<IVector2> positions = new ArrayList<>();

		for (int botId = 0; botId < roles.size(); botId++)
		{
			positions.add(calculateForBot(botId, 0));
		}
		return positions;
	}


	@Override
	public void doUpdate()
	{
		int intervalPos = numberOfLoops % INTERVAL_LENGTH;
		int intervalIndex = 0;
		while (intervalIndex < INTERVALS.length && INTERVALS[intervalIndex] < intervalPos)
		{
			intervalIndex += 1;
		}

		List<ARole> roles = play.getRoles();
		List<IVector2> positions = calcPositions();

		for (int botId = 0; botId < roles.size(); botId++)
		{
			final MoveRole moveRole = (MoveRole) roles.get(botId);

			if (botId % 2 == 0)
			{
				play.setSong(moveRole.getBotID(), ESong.EYE_OF_THE_TIGER_1);
			} else
			{
				play.setSong(moveRole.getBotID(), ESong.EYE_OF_THE_TIGER_2);
			}

			IVector2 moveTo = positions.get(botId);
			Vector2 moveFrom = calculateForBot(botId, -5);

			Vector2 lookAt = moveTo.subtractNew(moveFrom);
			lookAt.multiply(20);
			lookAt = lookAt.add(moveFrom);
			moveRole.getMoveCon().updateLookAtTarget(lookAt);
			moveRole.getMoveCon().updateDestination(moveTo);
		}

		if (intervalIndex % 2 != 0)
		{
			numberOfActiveLoops++;
		}
		numberOfLoops++;
	}


	@Override
	public ECheeringPlays getType()
	{
		return ECheeringPlays.TIGER;
	}


	private Vector2 calculateForBot(int botId, int timeOffset)
	{
		double globalPos = (((double) (numberOfActiveLoops + timeOffset)) % (100 * SECONDS_PER_FULL_TURN))
				/ (SECONDS_PER_FULL_TURN * 100d);

		int numberOfVirtualBots = play.getRoles().size();
		if (numberOfVirtualBots % 2 == 0)
			numberOfVirtualBots += 1;

		globalPos += botId / (double) numberOfVirtualBots;
		while (globalPos > 1)
		{
			globalPos -= 1;
		}

		return universalModify(calcPos(globalPos));
	}


	private Vector2 universalModify(Vector2 position)
	{
		double turn = (((double) numberOfActiveLoops) % (100 * SECONDS_PER_TURN)) / (SECONDS_PER_TURN * 100d);

		return position.multiply(scale).turn(turn * PI_TWO);
	}


	private Vector2 calcPos(double globalPos)
	{
		int totalNumberOfPositions = PATH.length * 4 - 2;

		globalPos *= totalNumberOfPositions;
		int lowerPointIndex = (int) Math.floor(globalPos);
		int upperPointIndex = (int) Math.ceil(globalPos);
		Vector2 lowerPoint = wrapPosition(lowerPointIndex);
		Vector2 upperPoint = wrapPosition(upperPointIndex);

		double x = lowerPoint.x() + (globalPos % 1) * (upperPoint.x() - lowerPoint.x());
		double y = lowerPoint.y() + (globalPos % 1) * (upperPoint.y() - lowerPoint.y());

		return Vector2.fromXY(x, y);

	}


	/*
	 * 0
	 * 1
	 * 2
	 * 3 2
	 * 4 1
	 * 5 0
	 * 6 1
	 * 7 2
	 * 8 2
	 * 9 1
	 */
	private Vector2 wrapPosition(int index)
	{
		boolean mirrorX = false;
		boolean mirrorY = false;

		if (index > 2 * PATH.length - 1)
		{
			index -= 2 * PATH.length - 1;
			mirrorY = true;
		}

		if (index >= PATH.length)
		{
			index = 2 * PATH.length - index - 1;
			mirrorX = true;
		}


		if (index < 0)
		{
			return Vector2.fromXY(0, 0);
		}

		Vector2 pos = Vector2.fromXY(PATH[index][0], PATH[index][1]);

		if (mirrorY)
		{
			pos.setX(-pos.x());
		}

		if (mirrorX)
		{
			pos.setY(-pos.y());
		}

		return pos;
	}
}

