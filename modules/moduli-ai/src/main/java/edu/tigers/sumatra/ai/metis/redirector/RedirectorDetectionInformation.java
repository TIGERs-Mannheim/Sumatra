/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.redirector;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.Optional;


/**
 * @author MarkG
 */
public class RedirectorDetectionInformation
{
	
	private BotID enemyReceiver;
	private BotID friendlyReceiver;
	private IVector2 enemyReceiverPos;
	private IVector2 friendlyReceiverPos;
	private boolean isEnemyReceiving = false;
	private boolean isFriendlyBotReceiving = false;
	private double timeToImpactToEnemy = 0;
	private double timeToImpactToFriendlyBot = 0;
	private boolean isEnemyReceivingBeforeMe = false;
	private boolean friendlyStillApproaching = false;
	private ERecommendedReceiverAction recommendedAction = ERecommendedReceiverAction.NONE;
	private double certainty = 0;
	
	
	public Optional<BotID> getEnemyReceiver()
	{
		return Optional.ofNullable(enemyReceiver);
	}
	
	
	public void setEnemyReceiver(final BotID enemyReceiver)
	{
		this.enemyReceiver = enemyReceiver;
	}
	
	
	public Optional<BotID> getFriendlyReceiver()
	{
		return Optional.ofNullable(friendlyReceiver);
	}
	
	
	public void setFriendlyReceiver(final BotID friendlyReceiver)
	{
		this.friendlyReceiver = friendlyReceiver;
	}
	
	
	public IVector2 getEnemyReceiverPos()
	{
		return enemyReceiverPos;
	}
	
	
	public void setEnemyReceiverPos(final IVector2 enemyReceiverPos)
	{
		this.enemyReceiverPos = enemyReceiverPos;
	}
	
	
	public IVector2 getFriendlyReceiverPos()
	{
		return friendlyReceiverPos;
	}
	
	
	public void setFriendlyReceiverPos(final IVector2 friendlyReceiverPos)
	{
		this.friendlyReceiverPos = friendlyReceiverPos;
	}
	
	
	public boolean isEnemyReceiving()
	{
		return isEnemyReceiving;
	}
	
	
	public void setEnemyReceiving(final boolean enemyReceiving)
	{
		isEnemyReceiving = enemyReceiving;
	}
	
	
	public boolean isFriendlyBotReceiving()
	{
		return isFriendlyBotReceiving;
	}
	
	
	public void setFriendlyBotReceiving(final boolean friendlyBotReceiving)
	{
		isFriendlyBotReceiving = friendlyBotReceiving;
	}
	
	
	public double getTimeToImpactToFriendlyBot()
	{
		return timeToImpactToFriendlyBot;
	}
	
	
	public void setTimeToImpactToFriendlyBot(final double timeToImpactToFriendlyBot)
	{
		this.timeToImpactToFriendlyBot = timeToImpactToFriendlyBot;
	}
	
	
	public double getTimeToImpactToEnemy()
	{
		return timeToImpactToEnemy;
	}
	
	
	public void setTimeToImpactToEnemy(final double timeToImpactToEnemy)
	{
		this.timeToImpactToEnemy = timeToImpactToEnemy;
	}
	
	
	public boolean isEnemyReceivingBeforeMe()
	{
		return isEnemyReceivingBeforeMe;
	}
	
	
	public void setEnemyReceivingBeforeMe(final boolean enemyReceivingBeforeMe)
	{
		isEnemyReceivingBeforeMe = enemyReceivingBeforeMe;
	}
	
	
	public boolean isFriendlyStillApproaching()
	{
		return friendlyStillApproaching;
	}
	
	
	public void setFriendlyStillApproaching(final boolean friendlyStillApproaching)
	{
		this.friendlyStillApproaching = friendlyStillApproaching;
	}
	
	
	public ERecommendedReceiverAction getRecommendedAction()
	{
		return recommendedAction;
	}
	
	
	public void setRecommendedAction(final ERecommendedReceiverAction recommendedAction)
	{
		this.recommendedAction = recommendedAction;
	}
	
	
	public double getCertainty()
	{
		return certainty;
	}
	
	
	public void setCertainty(final double certainty)
	{
		this.certainty = certainty;
	}
}
