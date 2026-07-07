package edu.tigers.sumatra.wp.util;

import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.ids.BotID;

import java.util.Collections;
import java.util.Set;


/**
 * The default robot info provider does not provide any real robot info. It can be used as a dummy.
 */
public class DefaultRobotInfoProvider implements IRobotInfoProvider
{
	private long lastWFTimestamp = 0;


	@Override
	public Set<BotID> getConnectedBotIds()
	{
		return Collections.emptySet();
	}


	@Override
	public RobotInfo getRobotInfo(final BotID botID)
	{
		return RobotInfo.stub(botID, lastWFTimestamp);
	}


	@Override
	public void setLastWFTimestamp(final long lastWFTimestamp)
	{
		this.lastWFTimestamp = lastWFTimestamp;
	}
}
