package edu.tigers.sumatra.ai.metis.support.behaviors;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ids.BotID;


public interface ISupportBehavior
{
	SupportBehaviorPosition calculatePositionForRobot(BotID botID);

	void updateData(BaseAiFrame baseAiFrame);

	boolean isEnabled();
}
