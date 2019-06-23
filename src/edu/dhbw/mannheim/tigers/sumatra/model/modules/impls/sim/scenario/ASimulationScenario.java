/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.AresData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.AutoReferee;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee.RefereeHandler;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.SimulationParameters;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.SimulationParameters.SimulationObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.stopcrit.ASimStopCriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ASimulationScenario
{
	private static final Logger				log							= Logger.getLogger(ASimulationScenario.class
																								.getName());
	private final List<ASimStopCriterion>	stopCriteria				= new ArrayList<>();
	private final SimulationParameters		params						= new SimulationParameters();
	private long									startTimeReal, startTimeSim;
	private boolean								stopSimulation				= false;
	
	private boolean								enableBlue					= false, enableYellow = false;
	private boolean								enableAutoReferee			= true;
	
	private AutoReferee							autoReferee					= null;
	
	private boolean								paused						= false;
	
	private boolean								stopAfterCompletition	= false;
	
	
	/**
	 * @return
	 */
	public final SimulationParameters setupSimulation()
	{
		setupBots(params.getInitBots());
		params.setInitBall(setupBall());
		setupStopCriteria(stopCriteria);
		return params;
	}
	
	
	/**
	 */
	public final void start()
	{
		try
		{
			autoReferee = (AutoReferee) SumatraModel.getInstance().getModule(AutoReferee.MODULE_ID);
			autoReferee.setProcessRefereeCases(true);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find autoReferee module.", err);
		}
		
		startTimeSim = SumatraClock.nanoTime();
		startTimeReal = System.nanoTime();
	}
	
	
	/**
	 * @param wf
	 * @return
	 */
	public final AIInfoFrame createInitialAiFrame(final WorldFrame wf)
	{
		RefereeMsg refMsg = null;
		BaseAiFrame bFrame = new BaseAiFrame(wf, refMsg, refMsg, null, ETeamColor.YELLOW);
		TacticalField tacticalField = new TacticalField(wf);
		setupInitialTacticalField(tacticalField);
		
		newRefereeMsg(getInitialRefereeCmd(), 0, 0, 100);
		
		MetisAiFrame mFrame = new MetisAiFrame(bFrame, tacticalField);
		AthenaAiFrame aFrame = new AthenaAiFrame(mFrame, new PlayStrategy(new PlayStrategy.Builder()));
		AIInfoFrame aiFrame = new AIInfoFrame(aFrame, new AresData(), 0);
		return aiFrame;
	}
	
	
	/**
	 * Stop the simulation
	 */
	public final void stopSimulation()
	{
		stopSimulation = true;
	}
	
	
	/**
	 * Check if the simulation should be stopped
	 * 
	 * @param aiFrame
	 * @return
	 */
	public final boolean checkStopSimulation(final AIInfoFrame aiFrame)
	{
		if (stopSimulation)
		{
			return true;
		}
		for (ASimStopCriterion crit : stopCriteria)
		{
			if (crit.checkStopSimulation(aiFrame))
			{
				return true;
			}
		}
		return false;
	}
	
	
	protected void setupStopCriteria(final List<ASimStopCriterion> criteria)
	{
		
	}
	
	
	protected void setupBots(final Map<BotID, SimulationObject> bots)
	{
		
	}
	
	
	protected final void setupBotsFormation1(final Map<BotID, SimulationObject> bots)
	{
		int side = 1;
		ETeamColor[] tcs = new ETeamColor[] { ETeamColor.YELLOW, ETeamColor.BLUE };
		for (ETeamColor tc : tcs)
		{
			bots.put(BotID.createBotId(0, tc), new SimulationObject(new Vector3(side * 3900, 0, 0)));
			bots.put(BotID.createBotId(1, tc), new SimulationObject(new Vector3(side * 2800, 300, 0)));
			bots.put(BotID.createBotId(2, tc), new SimulationObject(new Vector3(side * 2800, -300, 0)));
			bots.put(BotID.createBotId(3, tc), new SimulationObject(new Vector3(side * 1500, 1000, 0)));
			bots.put(BotID.createBotId(4, tc), new SimulationObject(new Vector3(side * 1500, -1000, 0)));
			bots.put(BotID.createBotId(5, tc), new SimulationObject(new Vector3(side * 1000, 0, 0)));
			
			side *= -1;
		}
	}
	
	
	protected void setupInitialTacticalField(final TacticalField tacticalField)
	{
		
	}
	
	
	protected Command getInitialRefereeCmd()
	{
		return Command.FORCE_START;
	}
	
	
	protected SimulationObject setupBall()
	{
		SimulationObject ball = new SimulationObject();
		return ball;
	}
	
	
	/**
	 * @param aiFrame
	 * @param caseMsgs
	 */
	public void onUpdate(final AIInfoFrame aiFrame, final List<RefereeCaseMsg> caseMsgs)
	{
	}
	
	
	/**
	 * @return [s]
	 */
	public final float getSimTimeSinceStart()
	{
		return (SumatraClock.nanoTime() - startTimeSim) * 1e-9f;
	}
	
	
	/**
	 * @return [s]
	 */
	public final float getRealTimeSinceStart()
	{
		return (System.nanoTime() - startTimeReal) * 1e-9f;
	}
	
	
	/**
	 * @return the params
	 */
	public final SimulationParameters getParams()
	{
		return params;
	}
	
	
	protected final void newRefereeMsg(final Command cmd, final int goalsBlue, final int goalsYellow,
			final int timeLeft)
	{
		try
		{
			RefereeHandler rh = (RefereeHandler) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			rh.sendOwnRefereeMsg(cmd, goalsBlue, goalsYellow, timeLeft);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find referee module", err);
		}
	}
	
	
	/**
	 * Get a string representing useful information about the bot and its AI state
	 * 
	 * @param aiFrame
	 * @param botId
	 * @return
	 */
	protected String getBotAiInfo(final AIInfoFrame aiFrame, final BotID botId)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(botId.getTeamColor().name());
		sb.append('_');
		sb.append(botId.getNumber());
		TrackedTigerBot bot = aiFrame.getWorldFrame().getBot(botId);
		if (bot == null)
		{
			return sb.toString();
		}
		sb.append(' ');
		sb.append(bot.getPos());
		
		ARole role = aiFrame.getPlayStrategy().getActiveRoles().getWithNull(botId);
		if (role == null)
		{
			return sb.toString();
		}
		sb.append(' ');
		sb.append(role.getType().name());
		sb.append(' ');
		sb.append(role.getCurrentState().name());
		
		ISkill skill = role.getCurrentSkill();
		if (skill == null)
		{
			return sb.toString();
		}
		sb.append(' ');
		sb.append(skill.getSkillName().name());
		return sb.toString();
	}
	
	
	/**
	 * @param wFrame
	 * @param refMsg
	 * @return
	 */
	public List<RefereeCaseMsg> processAutoReferee(final WorldFrame wFrame, final RefereeMsg refMsg)
	{
		if (!enableAutoReferee)
		{
			return new ArrayList<>();
		}
		
		return autoReferee.process(wFrame, refMsg);
	}
	
	
	/**
	 * @param aiFrame
	 * @param msgs
	 * @return
	 */
	public List<String> getRuleViolationsForAI(final AIInfoFrame aiFrame, final List<RefereeCaseMsg> msgs)
	{
		List<String> errors = new ArrayList<>();
		if (!msgs.isEmpty())
		{
			for (RefereeCaseMsg msg : msgs)
			{
				if (msg.getTeamAtFault() != aiFrame.getTeamColor())
				{
					continue;
				}
				StringBuilder stringMsg = new StringBuilder();
				stringMsg.append("Rule violation: ");
				stringMsg.append(msg.getMsgType().name());
				stringMsg.append(" by ");
				stringMsg.append(msg.getTeamAtFault().toString());
				if (msg.getBotAtFault().isBot())
				{
					stringMsg.append(" (");
					stringMsg.append(msg.getBotAtFault());
					stringMsg.append(")");
				}
				if (!msg.getAdditionalInfo().isEmpty())
				{
					stringMsg.append(" ");
					stringMsg.append(msg.getAdditionalInfo());
				}
				if (aiFrame.getTeamColor() == msg.getBotAtFault().getTeamColor())
				{
					stringMsg.append(" AI bot: ");
					stringMsg.append(getBotAiInfo(aiFrame, msg.getBotAtFault()));
				}
				errors.add(stringMsg.toString());
			}
		}
		return errors;
	}
	
	
	/**
	 */
	public void togglePause()
	{
		paused = !paused;
	}
	
	
	/**
	 */
	public void pause()
	{
		paused = true;
	}
	
	
	/**
	 */
	public void resume()
	{
		paused = false;
	}
	
	
	/**
	 * Called once after simulation finished
	 * 
	 * @param aiFrame
	 */
	public void afterSimulation(final AIInfoFrame aiFrame)
	{
		
	}
	
	
	/**
	 * @return the enableBlue
	 */
	public final boolean isEnableBlue()
	{
		return enableBlue;
	}
	
	
	/**
	 * @param enableBlue the enableBlue to set
	 */
	protected final void setEnableBlue(final boolean enableBlue)
	{
		this.enableBlue = enableBlue;
	}
	
	
	/**
	 * @return the enableYellow
	 */
	public final boolean isEnableYellow()
	{
		return enableYellow;
	}
	
	
	/**
	 * @param enableYellow the enableYellow to set
	 */
	protected final void setEnableYellow(final boolean enableYellow)
	{
		this.enableYellow = enableYellow;
	}
	
	
	/**
	 * @return the enableAutoReferee
	 */
	public final boolean isEnableAutoReferee()
	{
		return enableAutoReferee;
	}
	
	
	/**
	 * @param enableAutoReferee the enableAutoReferee to set
	 */
	public final void setEnableAutoReferee(final boolean enableAutoReferee)
	{
		this.enableAutoReferee = enableAutoReferee;
	}
	
	
	/**
	 * @return the paused
	 */
	public final boolean isPaused()
	{
		return paused;
	}
	
	
	/**
	 * @param stopAfterCompletition the stopOnError to set
	 */
	public final void setStopOnError(final boolean stopAfterCompletition)
	{
		this.stopAfterCompletition = stopAfterCompletition;
	}
	
	
	/**
	 * @return the stopOnError
	 */
	public final boolean isStopAfterCompletition()
	{
		return stopAfterCompletition;
	}
	
	
	/**
	 * @return the stopCriteria
	 */
	public final List<ASimStopCriterion> getStopCriteria()
	{
		return stopCriteria;
	}
}
