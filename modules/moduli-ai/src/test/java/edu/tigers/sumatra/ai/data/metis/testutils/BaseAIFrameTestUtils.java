/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.data.metis.testutils;

import org.mockito.Mockito;

import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class BaseAIFrameTestUtils
{
	/**
	 * This class should be used to mock a base ai frame for a given worldframe id
	 * 
	 * @param idToSet The id of the used worldframe
	 * @return A mocked BaseAiFrame that has capabilities to get worldFrame ids.
	 */
	public static BaseAiFrame mockBaseAiFrameForWorldFrameID(final long idToSet)
	{
		SimpleWorldFrame simpleWorldFrame = new SimpleWorldFrame(new BotIDMap<>(), null, null, idToSet, 0);
		WorldFrame worldFrame = new WorldFrame(simpleWorldFrame, EAiTeam.BLUE_PRIMARY, true);
		
		BaseAiFrame baseAiFrame = Mockito.mock(BaseAiFrame.class);
		Mockito.when(baseAiFrame.getWorldFrame()).thenReturn(worldFrame);
		
		return baseAiFrame;
	}
}
