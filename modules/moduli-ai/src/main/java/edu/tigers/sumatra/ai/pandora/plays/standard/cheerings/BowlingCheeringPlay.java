/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard.cheerings;

import edu.tigers.sumatra.ai.pandora.plays.standard.CheeringPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.ArrayList;
import java.util.List;


public class BowlingCheeringPlay implements ICheeringPlay
{
	private CheeringPlay play = null;
	private ArrayList<ARole> movedFromMid;
	private BowlingState currentState = BowlingState.PRE;
	private boolean done = false;


	@Override
	public void initialize(final CheeringPlay play)
	{
		currentState = BowlingState.PRE;
		movedFromMid = new ArrayList<>();
		this.play = play;
	}


	@Override
	public boolean isDone()
	{
		return done;
	}


	@Override
	public List<IVector2> calcPositions()
	{
		var roles = play.getPermutedRoles();
		var positions = new ArrayList<IVector2>();

		positions.add(Geometry.getGoalOur().getCenter().addNew(Vector2.fromXY(Geometry.getFieldLength() * 0.35, 0)));

		var bowls = new BowlPositions();
		roles.stream().map(role -> bowls.next()).limit((long) roles.size() - 1).forEach(positions::add);
		return positions;
	}


	@Override
	public void doUpdate()
	{
		if (currentState == BowlingState.PRE)
		{
			doPre();
		} else if (currentState == BowlingState.ROLL)
		{
			doRoll();
		}
	}


	private void doPre()
	{
		var roles = play.getPermutedRoles();
		var positions = calcPositions();
		for (int botId = 0; botId < roles.size(); botId++)
		{
			roles.get(botId).updateDestination(positions.get(botId));
		}

		for (MoveRole r : roles)
		{
			if (!r.isDestinationReached())
			{
				return;
			} else
			{
				r.getMoveCon().setOurBotsObstacle(false);
			}
		}

		currentState = BowlingState.ROLL;
	}


	private void doRoll()
	{
		var ballBot = play.getPermutedRoles().get(0);
		ballBot.updateDestination(Vector2.fromXY(Geometry.getFieldLength() * 0.33, 0));

		for (var r : play.getPermutedRoles())
		{
			if (!r.equals(ballBot) && !movedFromMid.contains(r) &&
					r.getBot().getPos().subtractNew(ballBot.getBot().getPosByTime(0.5)).x() < 1 * Geometry.getBotRadius())
			{
				double x = r.getBot().getPos().x();
				double y = r.getBot().getPos().y();
				movedFromMid.add(r);

				if (y > 0)
				{
					r.updateDestination(Vector2.fromXY(x, y + 500));
				} else if (y < 0)
				{
					r.updateDestination(Vector2.fromXY(x, y - 500));
				} else
				{
					r.updateDestination(Vector2.fromXY(x, y - 500 + 2 * Geometry.getBotRadius()));
				}
			}
		}

		for (var r : play.getPermutedRoles())
		{
			if (!r.isDestinationReached())
			{
				return;
			}
		}
		done = true;
	}


	@Override
	public ECheeringPlays getType()
	{
		return ECheeringPlays.BOWL;
	}


	enum BowlingState
	{
		PRE,
		ROLL
	}

	private static class BowlPositions
	{

		private int row = 0;
		private int column = 0;


		public IVector2 next()
		{
			IVector2 headpos = Vector2.fromXY(Geometry.getFieldLength() * 0.15, 0);
			IVector2 pos = Vector2.fromXY(
					headpos.x() + Geometry.getBotRadius() * 3 * row,
					headpos.y() - Geometry.getBotRadius() * 3 * (column - (row + 1) / 2.0)
			);

			column++;
			if (column > row)
			{
				row++;
				column = 0;
			}

			return pos;
		}
	}
}
