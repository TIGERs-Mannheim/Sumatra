/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.others.cheerings;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.pandora.plays.others.CheeringPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


public class LaolaCheeringPlay implements ICheeringPlay
{
	private double distanceBetweenBots = Geometry.getBotRadius() * 4d;
	
	private int numRolesLastTime;
	private boolean done = false;
	private CheeringPlay play;
	private int racePathState = 0;
	private IVector2[] path;
	private LaolaState cheeringPlayState;
	
	
	@Override
	public void initialize(CheeringPlay play)
	{
		this.cheeringPlayState = LaolaState.INIT;
		this.play = play;
		this.numRolesLastTime = play.getRoles().size();
		
		double totalDistance = (play.getRoles().size() - 2) * distanceBetweenBots;
		double startXValue = -0.5d * totalDistance;
		double fieldLength = Geometry.getFieldLength();
		double fieldWidth = Geometry.getFieldWidth();
		
		path = new IVector2[] {
				Vector2.fromXY(0.25d * fieldLength, -0.15 * fieldWidth),
				Vector2.fromXY(0.3 * fieldLength, 0.2 * fieldWidth),
				Vector2.fromXY(-0.25d * fieldLength, 0.12 * fieldWidth),
				Vector2.fromXY(startXValue - 8d * Geometry.getBotRadius(), -4d * Geometry.getBotRadius()) };
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
		
		double totalDistance = (play.getRoles().size() - 2) * distanceBetweenBots;
		double startXValue = -0.5d * totalDistance;
		
		positions.add(path[racePathState]);
		
		int distanceStep = 0;
		for (int botId = 1; botId < roles.size(); botId++)
		{
			positions.add(Vector2.fromXY(startXValue + distanceStep * distanceBetweenBots, 0));
			distanceStep++;
		}
		return positions;
	}
	
	
	@Override
	public void doUpdate()
	{
		if (play.getRoles().size() <= 3)
		{
			done = true;
			return;
		}
		
		if (numRolesLastTime != play.getRoles().size())
		{
			cheeringPlayState = LaolaState.INIT;
			numRolesLastTime = play.getRoles().size();
		}
		
		switch (cheeringPlayState)
		{
			case INIT:
			case RACE_PATH:
				moveToInitPosition();
				break;
			case LAOLA:
			default:
				moveOnLaolaState();
				break;
		}
		
	}
	
	
	private void moveToInitPosition()
	{
		List<ARole> roles = play.getRoles();
		List<IVector2> positions = calcPositions();
		int targetReached = 0;
		
		for (int botId = 0; botId < roles.size(); botId++)
		{
			MoveRole role = (MoveRole) roles.get(botId);
			role.getMoveCon().updateDestination(positions.get(botId));
			
			if (botId != 0)
			{
				role.getMoveCon().updateTargetAngle(-AngleMath.PI_HALF);
			}
			if (role.isDestinationReached())
				targetReached++;
		}
		
		if (cheeringPlayState == LaolaState.INIT && targetReached == roles.size())
		{
			cheeringPlayState = LaolaState.RACE_PATH;
			racePathState = 0;
			return;
		}
		
		MoveRole specialRole = (MoveRole) roles.get(0);
		boolean posReached = specialRole.getMoveCon().getDestination().isCloseTo(specialRole.getPos(), 500d);
		
		if (posReached && racePathState == path.length - 1)
		{
			cheeringPlayState = LaolaState.LAOLA;
		} else if (posReached)
		{
			racePathState++;
		}
	}
	
	
	private void moveOnLaolaState()
	{
		double totalDistance = (play.getRoles().size() - 2) * distanceBetweenBots;
		double endXValue = 0.5d * totalDistance;
		List<ARole> roles = play.getRoles();
		
		MoveRole specialRole = (MoveRole) roles.get(0);
		
		if (specialRole.getPos().x() < endXValue + 8d * Geometry.getBotRadius())
			specialRole.getMoveCon().updateDestination(specialRole.getPos().addNew(Vector2.fromXY(500d, 0d)));
		
		if (specialRole.isDestinationReached())
			done = true;
		
		for (int botId = 1; botId < roles.size(); botId++)
		{
			MoveRole role = (MoveRole) roles.get(botId);
			double x = role.getMoveCon().getDestination().x();
			double frequency = AngleMath.PI_TWO / (28d * Geometry.getBotRadius());
			double phi = frequency * (specialRole.getPos().x() - x);
			double amplitude = 8d * Geometry.getBotRadius();
			double y = amplitude * Math.cos(Math.min(AngleMath.PI_HALF, Math.max(-AngleMath.PI_HALF, phi)));
			
			
			IVector2 target = Vector2.fromXY(x, y);
			
			role.getMoveCon().updateDestination(target);
			role.getMoveCon().updateTargetAngle(-AngleMath.PI_HALF);
		}
	}
	
	
	@Override
	public ECheeringPlays getType()
	{
		return ECheeringPlays.LAOLA;
	}
	
	private enum LaolaState
	{
		INIT,
		RACE_PATH,
		LAOLA
	}
	
}
