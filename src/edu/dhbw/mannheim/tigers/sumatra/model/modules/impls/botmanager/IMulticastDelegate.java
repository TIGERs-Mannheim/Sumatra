/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.04.2011
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;

/**
 * Interface for multicast commands and management.
 * 
 * @author AndreR
 * 
 */
public interface IMulticastDelegate
{
	public void setGroupedMove(int botId, TigerMotorMoveV2 move);
	public void setGroupedKick(int botId, TigerKickerKickV2 kick);
	public void setGroupedDribble(int botId, TigerDribble dribble);
	public void setIdentity(TigerBot bot);
	public void removeIdentity(TigerBot bot);
	public boolean setMulticast(int botId, boolean enable);
}
