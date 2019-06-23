/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.08.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;


/**
 * <Insert some random, sounding one-liner that explains the existence of this thin interface>
 * 
 * @author Gero
 */
public interface IRefereeMsgConsumer
{
	/**
	 * 
	 * @param msg
	 */
	void onNewRefereeMsg(RefereeMsg msg);
	
	
	/**
	 * @return
	 */
	ETeamColor getTeamColor();
}
