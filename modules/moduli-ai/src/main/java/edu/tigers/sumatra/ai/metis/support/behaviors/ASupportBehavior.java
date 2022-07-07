/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.AccessLevel;
import lombok.Getter;


public abstract class ASupportBehavior implements ISupportBehavior
{
	@Getter(AccessLevel.PROTECTED)
	private BaseAiFrame aiFrame;


	@Override
	public void updateData(BaseAiFrame baseAiFrame)
	{
		this.aiFrame = baseAiFrame;
	}


	protected WorldFrame getWFrame()
	{
		return aiFrame.getWorldFrame();
	}
}
