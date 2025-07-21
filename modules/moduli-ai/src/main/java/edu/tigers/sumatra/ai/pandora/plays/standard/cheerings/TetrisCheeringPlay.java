/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard.cheerings;

import edu.tigers.sumatra.ai.pandora.plays.standard.CheeringPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.botmanager.botskills.data.ESong;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import org.apache.commons.lang.Validate;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class TetrisCheeringPlay extends ASongPlayingCheeringPlay
{
	private static final double SPACING = 50;
	private static final double JUMP_TIME = 0.6;
	private int stage = 0;
	private int jumpCnt = 0;
	private long tStart = -1;

	private boolean done = false;
	private Set<Integer> turningRobots = Set.of();


	public TetrisCheeringPlay()
	{
		super(
				List.of(ESong.TETRIS),
				List.of(),
				1
		);
	}

	@Override
	public void initialize(final CheeringPlay play)
	{
		super.initialize(play);
		this.stage = 0;
		this.tStart = -1;
		this.turningRobots = Set.of();
		this.done = false;
	}


	@Override
	public int requiredNumRobots()
	{
		return 8;
	}


	@Override
	public boolean isDone()
	{
		return done;
	}


	@Override
	public List<IVector2> calcPositions()
	{
		var roles = getPlay().getPermutedRoles();
		if (roles.size() >= 8)
		{
			return Stream.concat(
					Stream.of(
							calcPos(9, 0),
							calcPos(9, 1),
							calcPos(9, 3),
							calcPos(9, 2),
							calcPos(10, 0),
							calcPos(10, 1),
							calcPos(10, 2),
							calcPos(10, 3)
					),
					IntStream.range(0, roles.size() - 8)
							.mapToObj(i -> Vector2.fromXY(i * (2 * Geometry.getBotRadius() + SPACING),
									-Geometry.getFieldWidth() / 2 + Geometry.getBotRadius()))
							.map(IVector2.class::cast)

			).toList();
		}
		return roles.stream()
				.map(ARole::getBotID)
				.map(BotID::getNumber)
				.map(number -> Vector2.fromXY(number * (2 * Geometry.getBotRadius() + SPACING),
						-Geometry.getFieldWidth() / 2 + Geometry.getBotRadius()))
				.map(IVector2.class::cast)
				.toList();
	}


	@Override
	public void doUpdate()
	{
		super.doUpdate();
		doRotation();
		tryJump();
		switch (stage)
		{
			case 1 -> stageOne();
			case 2 -> stageTwo();
			case 3 -> stageThree();
			case 4 -> stageFour();
			default -> stage = 1;
		}
	}


	@Override
	void handleInterrupt(int loopCount, int interruptCount)
	{
		// Unused
	}


	private void doRotation()
	{
		var roles = getPlay().getPermutedRoles();
		long tNow = getPlay().getWorldFrame().getTimestamp();
		for (int i = 0; i < roles.size(); ++i)
		{
			double rotation;
			if (turningRobots.contains(i))
			{
				double rotationFactor = ((tNow - tStart) * 1e-9) / JUMP_TIME;
				rotation = AngleMath.PI_TWO * SumatraMath.cap(rotationFactor, 0, 1);
			} else
			{
				rotation = 0;
			}
			roles.get(i).updateTargetAngle(rotation);
		}
	}


	private void pos(int robot, int row, int column)
	{
		var pos = calcPos(row, column);
		getPlay().getPermutedRoles().get(robot).updateDestination(pos);
		circle(pos, robot);
	}


	private IVector2 calcPos(int row, int column)
	{
		double distBetween = Geometry.getBotRadius() * 2 + SPACING;
		double xRow = (-4.5 + row) * distBetween;
		double yColumn = (column - 1.5) * distBetween;
		return Vector2.fromXY(xRow, yColumn);
	}


	private void circle(IVector2 pos, int robot)
	{
		circle(pos, Color.GREEN, robot);
	}


	private void circle(IVector2 pos, Color color, int robot)
	{
		getPlay().addShape(new DrawableCircle(pos, 100, color));
		getPlay().addShape(new DrawableAnnotation(pos, String.valueOf(robot), color));
	}


	private void tryJump()
	{
		long tNow = getPlay().getWorldFrame().getTimestamp();
		if (tStart == -1)
		{
			tStart = tNow;
		}
		double jumpDiff = (tNow - tStart) * 1e-9;
		if (jumpDiff >= JUMP_TIME && getPlay().getPermutedRoles().stream()
				.allMatch(r -> r.getDestination().distanceTo(r.getPos()) < 0.5 * Geometry.getBotRadius()))
		{
			jumpCnt += 1;
			tStart = tNow;
		}
	}


	private void stageOne()
	{
		switch (jumpCnt)
		{
			case 0 ->
			{
				pos(0, 9, 0);
				pos(1, 9, 1);
				pos(2, 8, 2);
				pos(3, 9, 2);

				pos(4, 10, 0);
				pos(5, 10, 1);
				pos(6, 10, 2);
				pos(7, 10, 3);
			}
			case 1 ->
			{

				pos(0, 8, 0);
				pos(1, 8, 1);
				pos(2, 7, 2);
				pos(3, 8, 2);
			}
			case 2 ->
			{
				pos(0, 7, 1);
				pos(1, 7, 2);
				pos(2, 6, 3);
				pos(3, 7, 3);
			}
			case 3 ->
			{
				pos(0, 6, 1);
				pos(1, 6, 2);
				pos(2, 5, 3);
				pos(3, 6, 3);
			}
			case 4 ->
			{
				pos(0, 6, 3);
				pos(1, 5, 3);
				pos(2, 4, 2);
				pos(3, 4, 3);
			}
			case 5 ->
			{
				pos(0, 5, 3);
				pos(1, 4, 3);
				pos(2, 3, 2);
				pos(3, 3, 3);
			}
			case 6 ->
			{
				pos(0, 4, 2);
				pos(1, 3, 2);
				pos(2, 2, 1);
				pos(3, 2, 2);
			}
			case 7 ->
			{
				pos(0, 2, 3);
				pos(1, 2, 2);
				pos(2, 3, 1);
				pos(3, 2, 1);
			}
			case 8 ->
			{
				pos(0, 1, 3);
				pos(1, 1, 2);
				pos(2, 2, 1);
				pos(3, 1, 1);
			}
			case 9 ->
			{
				pos(0, 0, 3);
				pos(1, 0, 2);
				pos(2, 1, 1);
				pos(3, 0, 1);
			}
			default ->
			{
				jumpCnt = 0;
				stage = 2;
			}
		}

	}


	private void stageTwo()
	{
		switch (jumpCnt)
		{
			case 0 ->
			{
				pos(4, 10, 1);
				pos(5, 10, 2);
				pos(6, 10, 3);
				pos(7, 9, 3);
			}
			case 1 ->
			{
				pos(4, 10, 2);
				pos(5, 10, 3);
				pos(6, 9, 3);
				pos(7, 8, 3);
			}
			case 2 ->
			{
				pos(4, 10, 3);
				pos(5, 9, 3);
				pos(6, 8, 3);
				pos(7, 7, 3);
			}
			case 3 ->
			{
				pos(4, 9, 3);
				pos(5, 8, 3);
				pos(6, 7, 3);
				pos(7, 6, 3);
			}
			case 4 ->
			{
				pos(4, 8, 2);
				pos(5, 7, 2);
				pos(6, 6, 2);
				pos(7, 5, 2);
			}
			case 5 ->
			{
				pos(4, 7, 1);
				pos(5, 6, 1);
				pos(6, 5, 1);
				pos(7, 4, 1);
			}
			case 6 ->
			{
				pos(4, 6, 0);
				pos(5, 5, 0);
				pos(6, 4, 0);
				pos(7, 3, 0);
			}
			case 7 ->
			{
				pos(4, 5, 0);
				pos(5, 4, 0);
				pos(6, 3, 0);
				pos(7, 2, 0);
			}
			case 8 ->
			{
				pos(4, 4, 0);
				pos(5, 3, 0);
				pos(6, 2, 0);
				pos(7, 1, 0);
			}
			case 9 ->
			{
				pos(4, 3, 0);
				pos(5, 2, 0);
				pos(6, 1, 0);
				pos(7, 0, 0);
			}
			case 12 ->
			{
				clearLastRow(10, 3, List.of(7, 3, 1, 0));
				pos(2, 0, 1);
				pos(4, 2, 0);
				pos(5, 1, 0);
				pos(6, 0, 0);
			}
			default -> clearLastRow(10, 3, List.of(7, 3, 1, 0));
		}
	}


	private void stageThree()
	{
		switch (jumpCnt)
		{
			case 0 ->
			{
				pos(0, 10, 2);
				pos(1, 9, 2);
				pos(3, 9, 1);
				pos(7, 10, 1);
			}
			case 1 ->
			{
				pos(0, 9, 2);
				pos(1, 8, 2);
				pos(3, 8, 1);
				pos(7, 9, 1);
			}
			case 2 ->
			{
				pos(0, 8, 1);
				pos(1, 7, 1);
				pos(3, 7, 0);
				pos(7, 8, 0);
			}
			case 3 ->
			{
				pos(0, 7, 1);
				pos(1, 6, 1);
				pos(3, 6, 0);
				pos(7, 7, 0);
			}
			case 4 ->
			{
				pos(0, 6, 1);
				pos(1, 5, 1);
				pos(3, 5, 0);
				pos(7, 6, 0);
			}
			case 5 ->
			{
				pos(0, 5, 2);
				pos(1, 4, 2);
				pos(3, 4, 1);
				pos(7, 5, 1);
			}
			case 6 ->
			{
				pos(0, 4, 2);
				pos(1, 3, 2);
				pos(3, 3, 1);
				pos(7, 4, 1);
			}
			case 7 ->
			{
				pos(0, 3, 3);
				pos(1, 2, 3);
				pos(3, 2, 2);
				pos(7, 3, 2);
			}
			case 8 ->
			{
				pos(0, 2, 3);
				pos(1, 1, 3);
				pos(3, 1, 2);
				pos(7, 2, 2);
			}
			case 9 ->
			{
				pos(0, 1, 3);
				pos(1, 0, 3);
				pos(3, 0, 2);
				pos(7, 1, 2);
			}
			case 12 ->
			{
				clearLastRow(10, 4, List.of(6, 2, 3, 1));
				pos(0, 0, 3);
				pos(4, 1, 0);
				pos(5, 0, 0);
				pos(7, 0, 2);
			}
			default -> clearLastRow(10, 4, List.of(6, 2, 3, 1));
		}
	}


	private void stageFour()
	{
		switch (jumpCnt)
		{
			case 0 ->
			{
				pos(6, 9, 0);
				pos(2, 10, 0);
				pos(3, 10, 1);
				pos(1, 10, 2);
			}
			case 1 ->
			{
				pos(6, 8, 0);
				pos(2, 9, 0);
				pos(3, 9, 1);
				pos(1, 9, 2);
			}
			case 2 ->
			{
				pos(6, 7, 0);
				pos(2, 8, 0);
				pos(3, 8, 1);
				pos(1, 8, 2);
			}
			case 3 ->
			{
				pos(6, 6, 0);
				pos(2, 7, 0);
				pos(3, 7, 1);
				pos(1, 7, 2);
			}
			case 4 ->
			{
				pos(6, 5, 0);
				pos(2, 6, 0);
				pos(3, 6, 1);
				pos(1, 6, 2);
			}
			case 5 ->
			{
				pos(6, 4, 0);
				pos(2, 5, 0);
				pos(3, 5, 1);
				pos(1, 5, 2);
			}
			case 6 ->
			{
				pos(6, 3, 0);
				pos(2, 4, 0);
				pos(3, 4, 1);
				pos(1, 4, 2);
			}
			case 7 ->
			{
				pos(6, 2, 1);
				pos(2, 3, 1);
				pos(3, 3, 2);
				pos(1, 3, 3);
			}
			case 8 ->
			{
				pos(6, 1, 1);
				pos(2, 2, 1);
				pos(3, 2, 2);
				pos(1, 2, 3);
			}
			case 9 ->
			{
				pos(6, 0, 1);
				pos(2, 1, 1);
				pos(3, 1, 2);
				pos(1, 1, 3);
			}
			case 10 -> turningRobots = Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8);
			default ->
			{
				turningRobots = Set.of();
				done = true;
				stage = 0;
				jumpCnt = 0;
			}
		}
	}


	private void clearLastRow(int jumpCntOffset, int nextStage, List<Integer> bots)
	{
		Validate.isTrue(bots.size() == 4);
		switch (jumpCnt - jumpCntOffset)
		{
			case 0 -> turningRobots = bots.stream().collect(Collectors.toUnmodifiableSet());
			case 1 ->
			{
				turningRobots = Set.of();
				pos(bots.get(0), 0, -2);
				pos(bots.get(1), 0, -1);
				pos(bots.get(2), 0, 4);
				pos(bots.get(3), 0, 5);
			}
			case 2 ->
			{
				pos(bots.get(0), 10, -2);
				pos(bots.get(1), 10, -1);
				pos(bots.get(2), 10, 4);
				pos(bots.get(3), 10, 5);
			}
			case 3 ->
			{
				pos(bots.get(0), 10, 0);
				pos(bots.get(1), 10, 1);
				pos(bots.get(2), 10, 2);
				pos(bots.get(3), 10, 3);
			}
			default ->
			{
				jumpCnt = 0;
				stage = nextStage;
			}
		}
	}


	@Override
	public ECheeringPlays getType()
	{
		return ECheeringPlays.TETRIS;
	}
}
