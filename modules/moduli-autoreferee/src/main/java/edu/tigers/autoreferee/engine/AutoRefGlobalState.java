/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine;

import java.util.EnumMap;
import java.util.Map;

import edu.tigers.sumatra.ids.ETeamColor;


public class AutoRefGlobalState
{
	private final Map<ETeamColor, Integer> failedBallPlacements = new EnumMap<>(ETeamColor.class);
	private EBallPlacementStage ballPlacementStage = EBallPlacementStage.UNKNOWN;
	
	
	public Map<ETeamColor, Integer> getFailedBallPlacements()
	{
		return failedBallPlacements;
	}
	
	
	public EBallPlacementStage getBallPlacementStage()
	{
		return ballPlacementStage;
	}
	
	
	public void setBallPlacementStage(final EBallPlacementStage ballPlacementStage)
	{
		this.ballPlacementStage = ballPlacementStage;
	}
	
	public enum EBallPlacementStage
	{
		UNKNOWN,
		IN_PROGRESS,
		FAILED,
		SUCCEEDED,
		CANCELED
	}
}
