/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.impl;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.line.v2.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 */
public class BallHoldInPenAreaDetector extends APreparingGameEventDetector
{
	
	private static final int PRIORITY = 1;
	private static final long BALL_IN_PENAREA_TIMEOUT_MS = 15_000;
	
	private ETeamColor teamColor;
	private long entryTime;
	private boolean ballOutSidePenAreas;
	private Map<ETeamColor, IPenaltyArea> penAreas;
	
	
	/**
	 * returns instance of this detector
	 */
	public BallHoldInPenAreaDetector()
	{
		super(EGameEventDetectorType.BALL_HOLD_IN_PENAREA, EGameState.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		ballOutSidePenAreas = true;
		penAreas = new EnumMap<>(ETeamColor.class);
		penAreas.put(ETeamColor.YELLOW, NGeometry.getPenaltyArea(ETeamColor.YELLOW));
		penAreas.put(ETeamColor.BLUE, NGeometry.getPenaltyArea(ETeamColor.BLUE));
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate(final IAutoRefFrame frame)
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
				FollowUpAction followUp = new FollowUpAction(FollowUpAction.EActionType.INDIRECT_FREE, teamColor.opposite(),
						getNewBallPos(frame));
				GameEvent violation = new GameEvent(EGameEvent.BALL_HOLDING, frame.getTimestamp(), teamColor, followUp);
				return Optional.of(violation);
			}
		} else
		{
			ballOutSidePenAreas = true;
		}
		return Optional.empty();
	}
	
	
	private IVector2 getNewBallPos(final IAutoRefFrame frame)
	{
		IVector2 newBallPos = penAreas.get(teamColor).nearestPointOutside(frame.getWorldFrame().getBall().getPos());
		newBallPos = LineMath.stepAlongLine(newBallPos, frame.getWorldFrame().getBall().getPos(),
				-(RuleConstraints.getStopRadius() + RuleConstraints.getBotToPenaltyAreaMarginStandard()));
		return NGeometry.getField().nearestPointInside(newBallPos, -Geometry.getBotRadius());
	}
}
