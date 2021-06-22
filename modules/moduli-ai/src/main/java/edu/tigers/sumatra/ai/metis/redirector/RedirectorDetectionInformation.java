/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.redirector;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Data;


/**
 * @author MarkG
 */
@Data
public class RedirectorDetectionInformation
{
	private BotID opponentReceiver;
	private BotID friendlyReceiver;
	private IVector2 opponentReceiverPos;
	private IVector2 friendlyReceiverPos;
	private boolean opponentReceiving;
	private boolean friendlyBotReceiving;
	private double timeToImpactToOpponent;
	private double timeToImpactToFriendlyBot;
	private boolean friendlyStillApproaching;
	private ERecommendedReceiverAction recommendedAction = ERecommendedReceiverAction.NONE;
	private double certainty;
}
