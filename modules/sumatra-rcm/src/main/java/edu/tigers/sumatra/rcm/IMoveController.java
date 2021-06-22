/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.rcm;

import edu.tigers.sumatra.botmanager.botskills.AMoveBotSkill;
import edu.tigers.sumatra.proto.BotActionCommandProtos;


public interface IMoveController
{
	AMoveBotSkill control(final BotActionCommandProtos.BotActionCommand command, final ControllerState controllerState);
}
