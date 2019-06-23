/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s):
 * Oliver Steinbrecher
 * Daniel Waigand
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotConnection;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;
import edu.dhbw.mannheim.tigers.sumatra.util.FrameFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.IAIModeChanged;


/**
 * This class chooses a play according to the current situation and adds it to the current {@link AIInfoFrame}.
 * 
 * @author OliverS, DanielW, Gero
 */
public class Athena
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger			log				= Logger.getLogger(Athena.class.getName());
	
	/** Role-assigner instance */
	private final Lachesis					lachesis;
	
	/** AI control from GUI */
	private AAthenaAdapter					athenaAdapter;
	
	private EAIControlState					controlState	= EAIControlState.MATCH_MODE;
	
	private final List<IAIModeChanged>	observers		= new CopyOnWriteArrayList<IAIModeChanged>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public Athena()
	{
		log.trace("Creating");
		lachesis = new Lachesis();
		athenaAdapter = new MatchModeAthenaAdapter();
		
		GlobalShortcuts.register(EShortcut.MATCH_MODE, new Runnable()
		{
			@Override
			public void run()
			{
				changeMode(EAIControlState.MATCH_MODE);
			}
		});
		log.trace("Created");
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IAIModeChanged observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IAIModeChanged observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyAiModeChanged(final EAIControlState mode)
	{
		synchronized (observers)
		{
			for (IAIModeChanged observer : observers)
			{
				observer.onAiModeChanged(mode);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- process --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param mode
	 */
	public void changeMode(final EAIControlState mode)
	{
		switch (mode)
		{
			case EMERGENCY_MODE:
				athenaAdapter = new EmergencyModeAthenaAdapter();
				break;
			case MATCH_MODE:
			case MIXED_TEAM_MODE:
				athenaAdapter = new MatchModeAthenaAdapter();
				break;
			case TEST_MODE:
				athenaAdapter = new TestModeAthenaAdapter();
				break;
			default:
				throw new IllegalStateException();
		}
		controlState = mode;
		notifyAiModeChanged(mode);
	}
	
	
	/**
	 * @return the controlState
	 */
	public final EAIControlState getControlState()
	{
		return controlState;
	}
	
	
	/**
	 * perform play selection and call update on every play
	 * 
	 * @param metisAiFrame
	 * @return
	 */
	public AthenaAiFrame process(final MetisAiFrame metisAiFrame)
	{
		final PlayStrategy.Builder playStrategyBuilder = new PlayStrategy.Builder();
		return process(metisAiFrame, playStrategyBuilder);
	}
	
	
	/**
	 * Perform play selection. This is public for {@link FrameFactory}
	 * 
	 * @param metisAiFrame
	 * @param playStrategyBuilder
	 * @return
	 */
	public AthenaAiFrame process(final MetisAiFrame metisAiFrame, final PlayStrategy.Builder playStrategyBuilder)
	{
		final AthenaAiFrame athenaAiFrame;
		synchronized (athenaAdapter)
		{
			processBotDetection(metisAiFrame.getWorldFrame(), metisAiFrame.getPrevFrame().getWorldFrame(),
					playStrategyBuilder);
			
			athenaAdapter.process(metisAiFrame, playStrategyBuilder);
			playStrategyBuilder.setAIControlState(controlState);
			athenaAiFrame = new AthenaAiFrame(metisAiFrame, playStrategyBuilder.build());
			
			processRoleAssignment(athenaAiFrame);
			
			updatePlays(athenaAiFrame);
		}
		return athenaAiFrame;
	}
	
	
	private void processBotDetection(final WorldFrame wFrame, final WorldFrame preWFrame,
			final PlayStrategy.Builder playStrategyBuilder)
	{
		// All tigers connected?
		final boolean tigerDisconnected = !wFrame.tigerBotsAvailable.keySet().containsAll(
				preWFrame.tigerBotsAvailable.keySet());
		final boolean tigerConnected = !preWFrame.tigerBotsAvailable.keySet().containsAll(
				wFrame.tigerBotsAvailable.keySet());
		
		// Same tigers as last frame?
		final Collection<BotID> preTigersVisible = preWFrame.tigerBotsVisible.keySet();
		final Collection<BotID> tigersVisible = wFrame.tigerBotsVisible.keySet();
		final boolean tigersRemoved = !tigersVisible.isEmpty() && !tigersVisible.containsAll(preTigersVisible);
		final boolean tigersAdded = !preTigersVisible.isEmpty() && !preTigersVisible.containsAll(tigersVisible);
		
		// Same enemies as last frame?
		final Collection<BotID> preEnemies = preWFrame.foeBots.keySet();
		final Collection<BotID> enemies = wFrame.foeBots.keySet();
		final boolean enemyRemoved = !enemies.isEmpty() && !enemies.containsAll(preEnemies);
		final boolean enemyAdded = !preEnemies.isEmpty() && !preEnemies.containsAll(enemies);
		
		
		// Conclusion
		playStrategyBuilder.setBotConnection(new BotConnection(tigerConnected, tigerDisconnected, tigersAdded,
				tigersRemoved, enemyAdded, enemyRemoved));
	}
	
	
	private void processRoleAssignment(final AthenaAiFrame frame)
	{
		lachesis.assignRoles(frame);
	}
	
	
	private void updatePlays(final AthenaAiFrame frame)
	{
		// update all plays with the new frame and remove plays which failed or succeeded
		for (APlay play : new ArrayList<APlay>(frame.getPlayStrategy().getActivePlays()))
		{
			for (ARole role : play.getRoles())
			{
				role.update(frame);
			}
			
			play.update(frame);
		}
	}
	
	
	/**
	 * @return the athenaAdapter
	 */
	public final AAthenaAdapter getAthenaAdapter()
	{
		return athenaAdapter;
	}
}
