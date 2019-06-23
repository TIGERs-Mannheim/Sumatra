/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.remote.impl;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.remote.ICommandResult;
import edu.tigers.autoreferee.remote.IRefboxRemote;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author AndreR <andre@ryll.cc>
 */
public class LocalRefboxRemote implements IRefboxRemote
{
	private static final Logger log = Logger.getLogger(LocalRefboxRemote.class.getName());
	
	
	@Override
	public ICommandResult sendCommand(final RefboxRemoteCommand command)
	{
		RemoteControlProtobufBuilder pbBuilder = new RemoteControlProtobufBuilder();
		SSL_RefereeRemoteControlRequest request = pbBuilder.buildRequest(command);
		
		try
		{
			AReferee refBox = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			refBox.handleControlRequest(request);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find RefBox module.", e);
			CommandResultImpl result = new CommandResultImpl();
			result.setFailed();
			return result;
		}
		
		CommandResultImpl result = new CommandResultImpl();
		result.setSuccessful();
		return result;
	}
	
	
	@Override
	public void stop()
	{
		// nothing to stop
	}
}
