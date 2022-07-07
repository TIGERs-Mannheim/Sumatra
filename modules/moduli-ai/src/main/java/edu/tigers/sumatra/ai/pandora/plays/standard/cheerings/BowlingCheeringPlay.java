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
		List<IVector2> positions = new ArrayList<>();
		List<ARole> roles = play.getRoles();

		positions.add(Geometry.getGoalOur().getCenter().addNew(Vector2.fromXY(Geometry.getFieldLength() * 0.35, 0)));

		BowlPositions bowls = new BowlPositions();
		roles.stream().map(role -> bowls.next()).forEach(positions::add);
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
		List<IVector2> positions = calcPositions();
		List<ARole> roles = play.getRoles();
		for (int botId = 0; botId < roles.size(); botId++)
		{
			((MoveRole) roles.get(botId)).updateDestination(positions.get(botId));
		}

		for (ARole r : roles)
		{
			if (!((MoveRole) r).isDestinationReached())
			{
				return;
			} else
			{
				((MoveRole) r).getMoveCon().setOurBotsObstacle(false);
			}
		}

		currentState = BowlingState.ROLL;
	}


	private void doRoll()
	{
		ARole ballbot = play.getRoles().get(0);
		((MoveRole) ballbot).updateDestination(Vector2.fromXY(Geometry.getFieldLength() * 0.33, 0));

		for (ARole r : play.getRoles())
		{
			if (!r.equals(ballbot) && !movedFromMid.contains(r) &&
					r.getBot().getPos().subtractNew(ballbot.getBot().getPosByTime(0.5)).x() < 1 * Geometry.getBotRadius())
			{
				double x = r.getBot().getPos().x();
				double y = r.getBot().getPos().y();
				movedFromMid.add(r);

				if (y > 0)
				{
					((MoveRole) r).updateDestination(Vector2.fromXY(x, y + 500));
				} else if (y < 0)
				{
					((MoveRole) r).updateDestination(Vector2.fromXY(x, y - 500));
				} else if (movedFromMid.isEmpty())
				{
					((MoveRole) r).updateDestination(Vector2.fromXY(x, y + 500 - 2 * Geometry.getBotRadius()));
				} else
				{
					((MoveRole) r).updateDestination(Vector2.fromXY(x, y - 500 + 2 * Geometry.getBotRadius()));
				}
			}
		}

		for (ARole r : play.getRoles())
		{
			if (!((MoveRole) r).isDestinationReached())
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
