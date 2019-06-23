/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import java.util.ArrayList;

import org.junit.Test;
import org.mockito.Mockito;

import edu.tigers.sumatra.ai.data.IPlayStrategy;
import edu.tigers.sumatra.ai.data.KickoffStrategy;
import edu.tigers.sumatra.ai.data.PlayStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class KickoffActionsCalcTest
{
	@Test
	public void testEmptyFieldShouldNotThrowAnyExpections()
	{
		KickoffActionsCalc kickoffActionsCalc = new KickoffActionsCalc();
		
		
		KickoffStrategy kickoffStrategy = new KickoffStrategy();
		TacticalField newTacticalField = Mockito.mock(TacticalField.class);
		Mockito.when(newTacticalField.getKickoffStrategy()).thenReturn(kickoffStrategy);
		
		WorldFrame worldFrame = Mockito.mock(WorldFrame.class);
		IBotIDMap<ITrackedBot> trackedBots = new BotIDMap<>();
		Mockito.when(worldFrame.getBots()).thenReturn(trackedBots);
		
		IPlayStrategy playStrategy = Mockito.mock(PlayStrategy.class);
		Mockito.when(playStrategy.getActiveRoles(ERole.KICKOFF_SHOOTER)).thenReturn(new ArrayList<>());
		
		AIInfoFrame aiInfoFrame = Mockito.mock(AIInfoFrame.class);
		Mockito.when(aiInfoFrame.getPlayStrategy()).thenReturn(playStrategy);
		
		GameState prepareKickoffWe = GameState.Builder.create()
				.withState(EGameState.PREPARE_KICKOFF)
				.forTeam(ETeamColor.YELLOW)
				.withOurTeam(ETeamColor.YELLOW)
				.build();
		
		BaseAiFrame baseAiFrame = Mockito.mock(BaseAiFrame.class);
		Mockito.when(baseAiFrame.getGamestate()).thenReturn(prepareKickoffWe);
		Mockito.when(baseAiFrame.getWorldFrame()).thenReturn(worldFrame);
		Mockito.when(baseAiFrame.getPrevFrame()).thenReturn(aiInfoFrame);
		
		kickoffActionsCalc.doCalc(newTacticalField, baseAiFrame);
	}
}