/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 29, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.metis.testutils;

import org.mockito.Mockito;

import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.data.ITrackedBot;
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
		SimpleWorldFrame simpleWorldFrame = new SimpleWorldFrame(new BotIDMap<ITrackedBot>(), null, idToSet, 0);
		WorldFrame worldFrame = new WorldFrame(simpleWorldFrame, ETeamColor.BLUE, true);
		
		BaseAiFrame baseAiFrame = Mockito.mock(BaseAiFrame.class);
		Mockito.when(baseAiFrame.getWorldFrame()).thenReturn(worldFrame);
		
		return baseAiFrame;
	}
}
