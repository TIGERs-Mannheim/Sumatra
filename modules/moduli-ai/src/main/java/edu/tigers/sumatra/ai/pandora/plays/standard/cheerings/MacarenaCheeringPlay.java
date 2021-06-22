/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard.cheerings;

import edu.tigers.sumatra.ai.pandora.plays.standard.CheeringPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.ArrayList;
import java.util.List;


public class MacarenaCheeringPlay implements ICheeringPlay
{

	private double distanceBetweenBots = Geometry.getBotRadius() * 3d;
	private int shakingState = 0;
	private boolean direction = true;
	private int numDances = 0;
	private boolean done = false;
	private boolean targetSet = false;
	private CheeringPlay play;
	private double time;


	@Override
	public void initialize(CheeringPlay play)
	{
		this.play = play;
		this.time = play.getWorldFrame().getTimestamp();
		this.shakingState = 0;
	}


	@Override
	public boolean isDone()
	{
		return done;
	}


	@Override
	public List<IVector2> calcPositions()
	{
		List<ARole> roles = play.getRoles();
		List<IVector2> positions = new ArrayList<>(roles.size());

		int numLinesInDanceBlock = (int) Math.sqrt(roles.size());
		int numRowsInDanceBlock = roles.size() / numLinesInDanceBlock;

		double startYValue = -0.5 * (numLinesInDanceBlock - 1) * distanceBetweenBots;
		int additionalBotsInMid = roles.size() % numLinesInDanceBlock;

		int roleCount = 0;
		for (int i = 0; i < numLinesInDanceBlock; i++)
		{
			int numBotsInRow = numRowsInDanceBlock;
			if (i == numLinesInDanceBlock / 2)
				numBotsInRow += additionalBotsInMid;

			List<ARole> botsInRow = new ArrayList<>();
			for (int j = 0; j < numBotsInRow; j++)
				botsInRow.add(roles.get(roleCount++));

			int numBots = botsInRow.size();
			double startX = -0.5 * (numBots - 1) * distanceBetweenBots;

			int j = 0;
			for (int k = 0; k < botsInRow.size(); k++)
			{
				double x = startX + j * distanceBetweenBots;

				IVector2 target = Vector2.fromXY(x, startYValue + i * distanceBetweenBots);

				positions.add(target);
				j++;
			}

		}
		return positions;
	}


	@Override
	public void doUpdate()
	{
		double curTime = play.getWorldFrame().getTimestamp();

		if (curTime - time > 9.92e9 / 17 && !targetSet)
		{
			time = curTime;
			if (shakingState == 0)
			{
				if (numDances == 0)
				{
					turn();
				}

				// (Not done yet) Start Music
				shake();
			} else if (shakingState == 15)
			{
				shakingState = -1;
				numDances++;
				turn();
				if (numDances >= 3)
					done = true;
			} else if (shakingState < 12)
			{
				shake();
			}
			shakingState++;

			targetSet = true;
		}

		checkTargetReached();
	}


	private void checkTargetReached()
	{
		int targetReached = 0;
		for (ARole aRole : play.getRoles())
		{
			if (((MoveRole) aRole).isDestinationReached())
				targetReached++;
		}
		if (targetReached == play.getRoles().size())
			targetSet = false;
	}


	private void shake()
	{
		if (shakingState % 2 == 0)
		{
			direction = !direction;
		}
		moveSide(direction);
	}


	private void turn()
	{
		List<ARole> roles = play.getRoles();

		for (ARole aRole : roles)
		{
			MoveRole role = (MoveRole) aRole;
			role.updateTargetAngle(numDances * AngleMath.PI_HALF);
		}
	}


	private void moveSide(boolean direction)
	{
		int dir = 1;
		if (direction)
			dir = -1;
		for (ARole aRole : play.getRoles())
		{
			MoveRole role = (MoveRole) aRole;
			IVector2 destinationVect = Vector2.fromAngle(role.getBot().getOrientation())
					.multiplyNew(dir * 0.3 * distanceBetweenBots);
			IVector2 destination = role.getPos().addNew(destinationVect);
			role.updateDestination(destination);
		}
	}


	@Override
	public ECheeringPlays getType()
	{
		return ECheeringPlays.MACARENA;
	}
}
