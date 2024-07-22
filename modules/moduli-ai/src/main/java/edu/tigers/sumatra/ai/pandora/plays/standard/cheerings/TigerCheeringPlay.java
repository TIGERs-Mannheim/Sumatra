/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.standard.cheerings;

import edu.tigers.sumatra.ai.pandora.plays.standard.CheeringPlay;
import edu.tigers.sumatra.botmanager.botskills.data.ESong;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.ERotationDirection;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.ArrayList;
import java.util.List;

import static edu.tigers.sumatra.math.AngleMath.PI_TWO;


public class TigerCheeringPlay extends ASongPlayingCheeringPlay
{
	private static final double SECONDS_PER_FULL_TURN = 7;
	private static final double SECONDS_PER_TURN = 30;
	private static final double SECONDS_FOR_FINAL_ROTATION = 1;
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
	private double scale;
	private boolean destinationsMoving = false;
	private boolean turn = false;
	private double timeTurnStart = -1;
	private double lastTimeSinceStart = -1;

	private double accumulatedTimeMoving = 0;


	public TigerCheeringPlay()
	{
		super(List.of(ESong.EYE_OF_THE_TIGER_LEAD, ESong.EYE_OF_THE_TIGER_FOLLOW),
				List.of(
						0.000,
						0.300,
						1.110,
						1.410,
						1.526,
						1.826,
						1.942,
						2.242,
						3.330,
						3.630,
						3.756,
						4.046,
						4.162,
						4.462,
						5.550,
						5.850,
						5.966,
						6.266,
						6.382,
						6.907
				),
				2);
	}


	@Override
	public void initialize(final CheeringPlay play)
	{
		super.initialize(play);
		destinationsMoving = false;
		turn = false;
		accumulatedTimeMoving = 0;
		lastTimeSinceStart = -1;
		scale = Geometry.getFieldWidth() / 2 - Geometry.getBotRadius() / 2 - 300;
		if (scale > 2000)
		{
			scale = 2000;
		}
	}


	@Override
	public List<IVector2> calcPositions()
	{
		var positions = new ArrayList<IVector2>();

		for (int botIndex = 0; botIndex < getPlay().getPermutedRoles().size(); botIndex++)
		{
			positions.add(calculateForBot(botIndex, 0));
		}
		return positions;
	}


	@Override
	public void doUpdate()
	{
		super.doUpdate();
		if (lastTimeSinceStart == -1)
		{
			lastTimeSinceStart = timeSinceStart();
		}
		if (destinationsMoving)
		{
			accumulatedTimeMoving += timeSinceStart() - lastTimeSinceStart;
		}
		lastTimeSinceStart = timeSinceStart();

		var roles = getPlay().getPermutedRoles();

		for (int botIndex = 0; botIndex < roles.size(); ++botIndex)
		{
			var role = roles.get(botIndex);
			var moveTo = calculateForBot(botIndex, 0);
			var moveFrom = calculateForBot(botIndex, -0.05);

			var lookAt = moveFrom.subtractNew(moveTo).multiply(20).add(moveTo);

			role.updateLookAtTarget(lookAt);
			role.updateDestination(moveTo);

			if (turn)
			{
				var time = timeSinceStartOfLoop() - (timeTurnStart + 0.4);
				var rotation = SumatraMath.cap(1, 0, time / SECONDS_FOR_FINAL_ROTATION) * PI_TWO;
				var dir = botIndex % 2 == 0 ? ERotationDirection.CLOCKWISE : ERotationDirection.COUNTER_CLOCKWISE;
				role.updateTargetAngle(AngleMath.rotateAngle(lookAt.getAngle(), rotation, dir));
			}

		}
	}


	@Override
	void handleInterrupt(int loopCount, int interruptCount)
	{
		turn = interruptCount == 19;
		timeTurnStart = timeSinceStartOfLoop();
		destinationsMoving = interruptCount % 2 == 0;
	}


	@Override
	public ECheeringPlays getType()
	{
		return ECheeringPlays.TIGER;
	}


	private IVector2 calculateForBot(int botIndex, double timeOffset)
	{
		double globalPos =
				((accumulatedTimeMoving - timeOffset) % (SECONDS_PER_FULL_TURN + 1)) / (SECONDS_PER_FULL_TURN + 1);

		int numberOfVirtualBots = getPlay().getPermutedRoles().size();
		if (numberOfVirtualBots % 2 == 0)
			numberOfVirtualBots += 1;

		globalPos += botIndex / (double) numberOfVirtualBots;
		while (globalPos > 1)
		{
			globalPos -= 1;
		}

		return universalModify(calcPos(globalPos));
	}


	private IVector2 universalModify(Vector2 position)
	{
		double rotation = ((accumulatedTimeMoving) % SECONDS_PER_TURN) / SECONDS_PER_TURN;

		return position.multiply(scale).turn(rotation * PI_TWO);
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

