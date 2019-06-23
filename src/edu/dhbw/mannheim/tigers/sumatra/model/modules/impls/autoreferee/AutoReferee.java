/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 14, 2014
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.AModule;
import edu.dhbw.mannheim.tigers.moduli.exceptions.InitModuleException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.exceptions.StartModuleException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AresData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.cases.BallSpeedingCase;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.cases.BotSpeedStopCase;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.cases.IRefereeCase;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.cases.MultipleDefendersCase;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.cases.OutOfBoundsCase;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.cases.TooNearToBallCase;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IRefereeObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IWorldFrameConsumer;


/**
 * AutoReferee module
 * 
 * @author Lukas Magel
 */
public class AutoReferee extends AModule implements IRefereeObserver, IWorldFrameConsumer
{
	/**  */
	public static final String		MODULE_ID				= "autoreferee";
	private static final String	REF_STRINGS_KEY		= "RefereeMsgStrings";
	private static final String	TEAM_AT_FAULT_KEY		= "TEAM_AT_FAULT";
	
	private List<IRefereeCase>		refereeCases			= null;
	private Logger						log						= Logger.getLogger(AutoReferee.class);
	private Properties				refMsgs					= null;
	private String						refMsgsPath				= "";
	private RefereeMsg				latestRefereeMsg		= null;
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
		refMsgsPath = (String) subconfig.getProperty(REF_STRINGS_KEY);
		
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
	public List<RefereeCaseMsg> process(final WorldFrame wFrame, final RefereeMsg refMsg)
	{
		List<RefereeCaseMsg> caseMsgs = new ArrayList<>();
		
		BaseAiFrame baif = new BaseAiFrame(wFrame, refMsg, latestRefereeMsg, lastAif, wFrame.getTeamColor());
		if (lastAif == null)
		{
			lastAif = generateAIInfoFrame(baif);
			return caseMsgs;
		}
		MetisAiFrame maif = refMetis.process(baif);
		AIInfoFrame newAif = new AIInfoFrame(new AthenaAiFrame(maif, new PlayStrategy(new PlayStrategy.Builder())),
				new AresData(),
				0);
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
		return new AIInfoFrame(new AthenaAiFrame(new MetisAiFrame(baseAiFrame, new TacticalField(
				baseAiFrame.getWorldFrame())), new PlayStrategy(new PlayStrategy.Builder())), new AresData(), 0);
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
	public void onNewRefereeMsg(final RefereeMsg msg)
	{
		latestRefereeMsg = msg;
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		RefereeMsg refMsg = null;
		if ((latestRefereeMsg != null) && !latestRefereeMsg.getCommand().equals(lastRefereeCmd))
		{
			refMsg = latestRefereeMsg;
			lastRefereeCmd = latestRefereeMsg.getCommand();
		}
		List<RefereeCaseMsg> caseMsgs = process(wFrameWrapper.getWorldFrame(ETeamColor.YELLOW), refMsg);
		
		for (RefereeCaseMsg msg : caseMsgs)
		{
			log.warn(buildLogString(msg));
		}
	}
	
	
	@Override
	public void onStop()
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
