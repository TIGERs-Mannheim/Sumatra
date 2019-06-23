/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import java.util.HashMap;
import java.util.Map;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.snapshot.SnapObject;
import edu.tigers.sumatra.snapshot.Snapshot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimulationParameters
{
	private final Map<BotID, SimulationObject>	initBots			= new HashMap<>();
	private final SimulationObject					initBall			= new SimulationObject();
	private Referee.SSL_Referee.Command				refereeCommand	= Referee.SSL_Referee.Command.HALT;
	private IVector2										ballPlacementPos;
	
	
	/**
	 * Default
	 */
	public SimulationParameters()
	{
		// empty
	}
	
	
	/**
	 * @param snapshot
	 */
	public SimulationParameters(Snapshot snapshot)
	{
		for (Map.Entry<BotID, SnapObject> entry : snapshot.getBots().entrySet())
		{
			SimulationObject o = new SimulationObject();
			o.setPos(entry.getValue().getPos());
			o.setVel(entry.getValue().getVel());
			initBots.put(entry.getKey(), o);
		}
		initBall.setPos(snapshot.getBall().getPos());
		initBall.setVel(snapshot.getBall().getVel());
	}
	
	/**
	 * @return the initBall
	 */
	public final SimulationObject getInitBall()
	{
		return initBall;
	}
	
	
	/**
	 * @return the initBots
	 */
	public final Map<BotID, SimulationObject> getInitBots()
	{
		return initBots;
	}
	
	
	public Referee.SSL_Referee.Command getRefereeCommand()
	{
		return refereeCommand;
	}
	
	
	public void setRefereeCommand(final Referee.SSL_Referee.Command refereeCommand)
	{
		this.refereeCommand = refereeCommand;
	}
	
	
	public IVector2 getBallPlacementPos()
	{
		return ballPlacementPos;
	}
	
	
	public void setBallPlacementPos(final IVector2 ballPlacementPos)
	{
		this.ballPlacementPos = ballPlacementPos;
	}
}
