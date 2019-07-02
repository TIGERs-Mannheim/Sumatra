/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.geometry.NGeometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.KeeperHeldBall;


/**
 * Detect if a ball is hold inside the penalty by a keeper
 */
public class BallHoldInDefenseAreaDetector extends AGameEventDetector
{
	private static final long BALL_IN_PENAREA_TIMEOUT_MS = 15_000;
	
	private ETeamColor teamColor;
	private long entryTime;
	private boolean ballOutSidePenAreas;
	private Map<ETeamColor, IPenaltyArea> penAreas;
	
	
	/**
	 * returns instance of this detector
	 */
	public BallHoldInDefenseAreaDetector()
	{
		super(EGameEventDetectorType.BALL_HOLD_IN_DEFENSE_AREA, EGameState.RUNNING);
	}
	
	
	@Override
	protected void doPrepare()
	{
		ballOutSidePenAreas = true;
		penAreas = new EnumMap<>(ETeamColor.class);
		penAreas.put(ETeamColor.YELLOW, NGeometry.getPenaltyArea(ETeamColor.YELLOW));
		penAreas.put(ETeamColor.BLUE, NGeometry.getPenaltyArea(ETeamColor.BLUE));
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		if (ballOutSidePenAreas)
		{
			for (Map.Entry<ETeamColor, IPenaltyArea> entry : penAreas.entrySet())
			{
				if (entry.getValue().isPointInShape(frame.getWorldFrame().getBall().getPos()))
				{
					teamColor = entry.getKey();
					entryTime = frame.getTimestamp();
					ballOutSidePenAreas = false;
					return Optional.empty();
				}
			}
		}
		if (!ballOutSidePenAreas && penAreas.get(teamColor).isPointInShape(frame.getWorldFrame().getBall().getPos()))
		{
			if ((frame.getTimestamp() - entryTime) > TimeUnit.MILLISECONDS.toNanos(BALL_IN_PENAREA_TIMEOUT_MS))
			{
				ballOutSidePenAreas = true;
				return Optional.of(new KeeperHeldBall(teamColor, frame.getWorldFrame().getBall().getPos(),
						(frame.getTimestamp() - entryTime) / 1e9));
			}
		} else
		{
			ballOutSidePenAreas = true;
		}
		return Optional.empty();
	}
}
