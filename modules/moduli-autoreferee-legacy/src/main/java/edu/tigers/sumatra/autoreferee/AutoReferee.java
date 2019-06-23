/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 14, 2014
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.sumatra.autoreferee;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.data.PlayStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.autoreferee.cases.BallSpeedingCase;
import edu.tigers.sumatra.autoreferee.cases.BotSpeedStopCase;
import edu.tigers.sumatra.autoreferee.cases.IRefereeCase;
import edu.tigers.sumatra.autoreferee.cases.MultipleDefendersCase;
import edu.tigers.sumatra.autoreferee.cases.OutOfBoundsCase;
import edu.tigers.sumatra.autoreferee.cases.TooNearToBallCase;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * AutoReferee module
 * 
 * @author Lukas Magel
 */
public class AutoReferee extends AModule implements IRefereeObserver, IWorldFrameObserver
{
	/**  */
	public static final String		MODULE_ID				= "autoreferee_legacy";
	private static final String	TEAM_AT_FAULT_KEY		= "TEAM_AT_FAULT";
																		
	private List<IRefereeCase>		refereeCases			= null;
	private final Logger				log						= Logger.getLogger(AutoReferee.class);
	private Properties				refMsgs					= null;
	private String						refMsgsPath				= "";
	private SSL_Referee				latestRefereeMsg		= null;
	private Command					lastRefereeCmd			= null;
	private final RefereeMetis		refMetis					= new RefereeMetis();
	private AIInfoFrame				lastAif					= null;
	private AutoRefereeActions		autoRefereeActions;
											
	private boolean					processRefereeCases	= false;
																		
																		
	/**
	 * @param subconfig
	 */
	public AutoReferee(final SubnodeConfiguration subconfig)
	{
		refMsgs = new Properties();
		refMsgsPath = "RefereeMsgStrings.properties";
		
		refereeCases = new ArrayList<IRefereeCase>();
		refereeCases.add(new OutOfBoundsCase());
		refereeCases.add(new BallSpeedingCase());
		refereeCases.add(new MultipleDefendersCase(ETeamColor.BLUE));
		refereeCases.add(new MultipleDefendersCase(ETeamColor.YELLOW));
		refereeCases.add(new BotSpeedStopCase());
		refereeCases.add(new TooNearToBallCase());
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		InputStream inStream = null;
		try
		{
			URL msgsURL = AutoReferee.class.getResource(refMsgsPath);
			inStream = msgsURL.openStream();
			refMsgs.load(inStream);
		} catch (IOException err)
		{
			log.error(err.getMessage(), err);
			throw new InitModuleException(err.getMessage(), err);
		} finally
		{
			if (inStream != null)
			{
				try
				{
					inStream.close();
				} catch (IOException err)
				{
					log.warn(err.getMessage(), err);
				}
			}
		}
		
	}
	
	
	@Override
	public void deinitModule()
	{
	
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		for (IRefereeCase refCase : refereeCases)
		{
			refCase.reset();
		}
		
		try
		{
			AWorldPredictor predictor = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.addWorldFrameConsumer(this);
			
			AReferee referee = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			referee.addObserver(this);
			autoRefereeActions = new AutoRefereeActions(referee);
		} catch (ModuleNotFoundException err)
		{
			throw new StartModuleException(err.getMessage(), err);
		}
	}
	
	
	@Override
	public void stopModule()
	{
		try
		{
			AWorldPredictor predictor = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.removeWorldFrameConsumer(this);
			
			AReferee referee = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			referee.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find a module", err);
		}
		autoRefereeActions = null;
	}
	
	
	/**
	 * @param wFrame
	 * @param refMsg
	 * @return
	 */
	public List<RefereeCaseMsg> process(final WorldFrameWrapper wFrame, final RefereeMsg refMsg)
	{
		List<RefereeCaseMsg> caseMsgs = new ArrayList<>();
		
		BaseAiFrame baif = new BaseAiFrame(wFrame, refMsg != null, lastAif,
				ETeamColor.YELLOW);
		if (lastAif == null)
		{
			lastAif = generateAIInfoFrame(baif);
			return caseMsgs;
		}
		MetisAiFrame maif = refMetis.process(baif);
		AIInfoFrame newAif = new AIInfoFrame(new AthenaAiFrame(maif, new PlayStrategy(new PlayStrategy.Builder())));
		lastAif.cleanUp();
		lastAif = newAif;
		
		if (processRefereeCases)
		{
			for (IRefereeCase refCase : refereeCases)
			{
				caseMsgs.addAll(refCase.process(maif));
			}
		}
		
		autoRefereeActions.process(maif);
		
		return caseMsgs;
		
	}
	
	
	private AIInfoFrame generateAIInfoFrame(final BaseAiFrame baseAiFrame)
	{
		return new AIInfoFrame(new AthenaAiFrame(new MetisAiFrame(baseAiFrame, new TacticalField()), new PlayStrategy(
				new PlayStrategy.Builder())));
	}
	
	
	private String buildLogString(final RefereeCaseMsg msg)
	{
		StringBuilder stringMsg = new StringBuilder();
		stringMsg.append(refMsgs.get(msg.getMsgType().toString()));
		stringMsg.append(System.lineSeparator());
		stringMsg.append(refMsgs.get(TEAM_AT_FAULT_KEY));
		stringMsg.append(msg.getTeamAtFault().toString());
		return stringMsg.toString();
	}
	
	
	@Override
	public void onNewRefereeMsg(final SSL_Referee msg)
	{
		latestRefereeMsg = msg;
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		// TODO actually, you should do the processing somewhere else, to avoid latencies in worldframe spreading
		RefereeMsg refMsg = null;
		if ((latestRefereeMsg != null) && !latestRefereeMsg.getCommand().equals(lastRefereeCmd))
		{
			refMsg = new RefereeMsg(wFrameWrapper.getSimpleWorldFrame().getTimestamp(), latestRefereeMsg);
			lastRefereeCmd = latestRefereeMsg.getCommand();
		}
		List<RefereeCaseMsg> caseMsgs = process(wFrameWrapper, refMsg);
		
		for (RefereeCaseMsg msg : caseMsgs)
		{
			log.warn(buildLogString(msg));
		}
	}
	
	
	@Override
	public void onClearWorldFrame()
	{
	}
	
	
	/**
	 * @return the autoRefereeActions
	 */
	public final AutoRefereeActions getAutoRefereeActions()
	{
		return autoRefereeActions;
	}
	
	
	/**
	 * @return the processRefereeCases
	 */
	public final boolean isProcessRefereeCases()
	{
		return processRefereeCases;
	}
	
	
	/**
	 * @param processRefereeCases the processRefereeCases to set
	 */
	public final void setProcessRefereeCases(final boolean processRefereeCases)
	{
		this.processRefereeCases = processRefereeCases;
	}
}
