/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.07.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.multiteammessage;

import org.apache.commons.configuration.SubnodeConfiguration;

import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.MultiTeamMessage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AMultiTeamMessage;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IMultiTeamMessageConsumer;


/**
 * @author JulianT
 */
public class MultiTeamMessageHandler extends AMultiTeamMessage
{
	private final MultiTeamMessageReceiver	receiver;
	
	
	/**
	 * @param subnodeConfig
	 */
	public MultiTeamMessageHandler(final SubnodeConfiguration subnodeConfig)
	{
		receiver = new MultiTeamMessageReceiver(this);
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		resetCountDownLatch();
	}
	
	
	@Override
	public void deinitModule()
	{
		receiver.cleanup();
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		receiver.start();
	}
	
	
	@Override
	public void stopModule()
	{
		receiver.cleanup();
	}
	
	
	protected void notifyConsumer(final MultiTeamMessage message)
	{
		for (IMultiTeamMessageConsumer consumer : getConsumers())
		{
			consumer.onNewMultiTeamMessage(message);
		}
	}
	
	
	protected void onNewMultiTeamMessage(final TeamPlan teamPlan)
	{
		final MultiTeamMessage message = new MultiTeamMessage(teamPlan);
		notifyConsumer(message);
		notifyNewMultiTeamMessage(message);
	}
	
	
	/**
	 * @throws InterruptedException
	 */
	public void waitOnSignal() throws InterruptedException
	{
		getStartSignal().await();
	}
}
