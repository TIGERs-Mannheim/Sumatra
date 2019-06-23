/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDPObserver;


/**
 *
 */
public interface IGrSimBotObserver extends IBotObserver, ITransceiverUDPObserver
{
	/**
	 * 
	 * @param ip
	 */
	void onIpChanged(String ip);
	
	
	/**
	 * 
	 * @param port
	 */
	void onPortChanged(int port);
	
	
	/**
	 * 
	 * @param features
	 */
	void onBotFeaturesChanged(final Map<EFeature, EFeatureState> features);
}
