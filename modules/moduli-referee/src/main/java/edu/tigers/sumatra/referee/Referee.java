/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;
import edu.tigers.sumatra.referee.source.DirectRefereeMsgForwarder;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.referee.source.IRefereeSourceObserver;
import edu.tigers.sumatra.referee.source.NetworkRefereeReceiver;
import edu.tigers.sumatra.referee.source.refbox.RefBox;


/**
 * Implementation of {@link AReferee} which can use various referee message sources.
 *
 * @author AndreR <andre@ryll.cc>
 */
public class Referee extends AReferee implements IConfigObserver, IRefereeSourceObserver
{
	private static final String CONFIG_CATEGORY = "user";
	
	@Configurable(spezis = { "SUMATRA", "", }, defValueSpezis = { "INTERNAL_REFBOX", "NETWORK" })
	private static ERefereeMessageSource activeSource = ERefereeMessageSource.NETWORK;
	
	private final List<ARefereeMessageSource> msgSources = new ArrayList<>();
	
	private ARefereeMessageSource source;
	
	static
	{
		ConfigRegistration.registerClass(CONFIG_CATEGORY, Referee.class);
	}
	
	
	/**
	 * @param subconfig
	 */
	@SuppressWarnings("unused")
	public Referee(final SubnodeConfiguration subconfig)
	{
		msgSources.add(new NetworkRefereeReceiver());
		msgSources.add(new RefBox());
		msgSources.add(new DirectRefereeMsgForwarder());
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		ConfigRegistration.applySpezi(CONFIG_CATEGORY, SumatraModel.getInstance().getEnvironment());
		ConfigRegistration.registerConfigurableCallback(CONFIG_CATEGORY, this);
	}
	
	
	@Override
	public void afterApply(final IConfigClient configClient)
	{
		changeSource(activeSource);
	}
	
	
	private void changeSource(final ERefereeMessageSource newSource)
	{
		if (source != null)
		{
			source.stop();
			source.removeObserver(this);
		}
		
		source = msgSources.stream()
				.filter(s -> s.getType() == newSource)
				.findAny()
				.orElse(source);
		
		source.addObserver(this);
		source.start();
		
		notifyRefereeMsgSourceChanged(source);
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		source = msgSources.stream()
				.filter(s -> s.getType() == activeSource)
				.findAny()
				.orElse(msgSources.get(0));
		
		source.addObserver(this);
		source.start();
		
		notifyRefereeMsgSourceChanged(source);
	}
	
	
	@Override
	public void stopModule()
	{
		source.stop();
		source.removeObserver(this);
	}
	
	
	@Override
	public void deinitModule()
	{
		// nothing to do
	}
	
	
	@Override
	public void onNewRefereeMessage(final SSL_Referee msg)
	{
		TeamConfig.setKeeperIdYellow(msg.getYellow().getGoalie());
		TeamConfig.setKeeperIdBlue(msg.getBlue().getGoalie());
		notifyNewRefereeMsg(msg);
	}
	
	
	@Override
	public void handleControlRequest(final SSL_RefereeRemoteControlRequest request)
	{
		if (source != null)
		{
			source.handleControlRequest(request);
		}
	}
	
	
	@Override
	public ARefereeMessageSource getActiveSource()
	{
		return source;
	}
	
	
	@Override
	public ARefereeMessageSource getSource(final ERefereeMessageSource type)
	{
		return msgSources.stream()
				.filter(s -> s.getType() == type)
				.findAny()
				.orElse(null);
	}
	
	
	@Override
	public void setActiveSource(final ERefereeMessageSource type)
	{
		changeSource(type);
	}
}
