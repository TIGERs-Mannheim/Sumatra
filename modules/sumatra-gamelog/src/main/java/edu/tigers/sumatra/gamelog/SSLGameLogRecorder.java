/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gamelog;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.MessagesRobocupSslWrapper.SSL_WrapperPacket;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.ICamFrameObserver;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;


/**
 * Record SSL Vision and Referee messages.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class SSLGameLogRecorder implements ICamFrameObserver, IRefereeObserver
{
	private static final Logger log = Logger.getLogger(SSLGameLogRecorder.class.getName());
	
	private SSLGameLogWriter writer = new SSLGameLogWriter();
	
	
	/**
	 * Start recording.
	 */
	public void start()
	{
		// only start recording if both required modules are found
		if (!SumatraModel.getInstance().isModuleLoaded(ACam.class)
				|| !SumatraModel.getInstance().isModuleLoaded(AReferee.class))
		{
			return;
		}
		
		try
		{
			if (SumatraModel.getInstance().isModuleLoaded(ACam.class))
			{
				ACam cam = SumatraModel.getInstance().getModule(ACam.class);
				cam.addObserver(this);
			}
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find cam module", e);
		}
		
		try
		{
			AReferee referee = SumatraModel.getInstance().getModule(AReferee.class);
			referee.addObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find referee module", e);
		}
		
		writer.open();
	}
	
	
	/**
	 * Stop recording.
	 */
	public void stop()
	{
		if (!writer.isOpen())
		{
			return;
		}
		
		try
		{
			AReferee referee = SumatraModel.getInstance().getModule(AReferee.class);
			referee.removeObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find referee module", e);
		}
		
		try
		{
			if (SumatraModel.getInstance().isModuleLoaded(ACam.class))
			{
				ACam cam = SumatraModel.getInstance().getModule(ACam.class);
				cam.removeObserver(this);
			}
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find cam module", e);
		}
		
		writer.close();
	}
	
	
	@Override
	public void onNewVisionPacket(final SSL_WrapperPacket packet)
	{
		writer.write(packet, EMessageType.SSL_VISION_2014);
	}
	
	
	@Override
	public void onNewRefereeMsg(final SSL_Referee refMsg)
	{
		writer.write(refMsg, EMessageType.SSL_REFBOX_2013);
	}
}
