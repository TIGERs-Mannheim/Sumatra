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

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotConnection;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.IAIProcessor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.ApollonControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.AthenaControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.AthenaGuiAdapter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.IApollonControlHandler;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.PlayFinderAdapter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlayState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IAthenaControlHandler;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;
import edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.IStatisticsObserver;


/**
 * 
 * This class chooses a play according to the current situation and adds it to the current {@link AIInfoFrame}.
 * 
 * @author OliverS, DanielW, Gero
 */
public class Athena implements IAIProcessor, IAthenaControlHandler, IApollonControlHandler
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger		log							= Logger.getLogger(Athena.class.getName());
	
	/** Playfinder for real matches. */
	private final PlayFinderAdapter	playFinderAdapter;
	/** Role-assigner instance */
	private final Lachesis				lachesis;
	
	/** Has Athena been controlled by GUI last frame? */
	private boolean						lastControlState			= false;
	
	// AI control from GUI
	private final AthenaGuiAdapter	athenaAdapter;
	
	private int								noPlaysButRolesCounter	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public Athena()
	{
		lachesis = new Lachesis();
		playFinderAdapter = new PlayFinderAdapter();
		athenaAdapter = new AthenaGuiAdapter();
	}
	
	
	// --------------------------------------------------------------------------
	// --- process --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * perform play selection and call update on every play
	 */
	@Override
	public void process(AIInfoFrame frame, AIInfoFrame preFrame)
	{
		synchronized (athenaAdapter)
		{
			frame.setControlState(athenaAdapter.getControl().getControlState());
			
			processBotDetection(frame, preFrame);
			
			athenaAdapter.beforePlayFinding(frame, preFrame);
			boolean switchedControlMode = processPlayFinding(frame, preFrame);
			
			athenaAdapter.betweenPlayRole(frame, preFrame);
			
			processRoleAssignment(frame, preFrame, switchedControlMode);
			athenaAdapter.afterRoleAssignment(frame, preFrame);
			
			updatePlays(frame, preFrame);
		}
	}
	
	
	private void processBotDetection(AIInfoFrame frame, AIInfoFrame preFrame)
	{
		// All tigers connected?
		final boolean tigerDisconnected = !frame.worldFrame.tigerBotsAvailable.keySet().containsAll(
				preFrame.worldFrame.tigerBotsAvailable.keySet());
		final boolean tigerConnected = !preFrame.worldFrame.tigerBotsAvailable.keySet().containsAll(
				frame.worldFrame.tigerBotsAvailable.keySet());
		
		// Same tigers as last frame?
		final Collection<BotID> preTigersVisible = preFrame.worldFrame.tigerBotsVisible.keySet();
		final Collection<BotID> tigersVisible = frame.worldFrame.tigerBotsVisible.keySet();
		final boolean tigersRemoved = !tigersVisible.isEmpty() && !tigersVisible.containsAll(preTigersVisible);
		final boolean tigersAdded = !preTigersVisible.isEmpty() && !preTigersVisible.containsAll(tigersVisible);
		
		// Same enemies as last frame?
		final Collection<BotID> preEnemies = preFrame.worldFrame.foeBots.keySet();
		final Collection<BotID> enemies = frame.worldFrame.foeBots.keySet();
		final boolean enemyRemoved = !enemies.isEmpty() && !enemies.containsAll(preEnemies);
		final boolean enemyAdded = !preEnemies.isEmpty() && !preEnemies.containsAll(enemies);
		
		
		// Conclusion
		frame.playStrategy.setBotConnection(new BotConnection(tigerConnected, tigerDisconnected, tigersAdded,
				tigersRemoved, enemyAdded, enemyRemoved));
	}
	
	
	/**
	 * @param frame
	 * @param preFrame
	 * @return if mode changed from automatic to manual or vice versa
	 */
	private boolean processPlayFinding(AIInfoFrame frame, AIInfoFrame preFrame)
	{
		// If switched from auto-control to manual (GUI) or the other way round
		boolean switched = false;
		
		// Should GUI override Athena?
		if (athenaAdapter.overridePlayFinding())
		{
			// Switched from auto to GUI
			switched = !lastControlState;
			
			athenaAdapter.choosePlays(frame, preFrame);
			
			// Remember state for next cycle
			lastControlState = true;
		} else
		{
			if (lastControlState)
			{
				// Switched from GUI to auto
				switched = true;
				// In case the override has been deactivated: We need a new play!
				frame.playStrategy.setForceNewDecision();
			}
			
			// Choose plays
			playFinderAdapter.choosePlays(frame, preFrame, frame.playStrategy.getActivePlays());
			
			// Remember state for next cycle
			lastControlState = false;
		}
		
		// Did plays change?
		if (!preFrame.playStrategy.getActivePlays().containsAll(frame.playStrategy.getActivePlays()))
		{
			frame.playStrategy.setChangedPlay();
		}
		
		return switched;
	}
	
	
	private void processRoleAssignment(AIInfoFrame frame, AIInfoFrame preFrame, boolean switched)
	{
		if (!athenaAdapter.overrideRoleAssignment())
		{
			// Role assignment
			if (switched || frame.playStrategy.getBotConnection().isSomethingTrue() || frame.playStrategy.hasPlayChanged())
			{
				lachesis.assignRoles(frame);
			} else
			{
				if (frame.playStrategy.getActivePlays().isEmpty() && !preFrame.getAssigendERoles().isEmpty())
				{
					noPlaysButRolesCounter++;
					if (noPlaysButRolesCounter > 1)
					{
						log.warn("Although there is no play left, there are assigned roles. Seems to be a defect in a play");
					}
				} else
				{
					noPlaysButRolesCounter = 0;
					frame.putAllAssignedRoles(preFrame.getAssigendRoles());
				}
			}
		} else
		{
			athenaAdapter.assignRoles(frame, preFrame);
		}
	}
	
	
	private void updatePlays(AIInfoFrame frame, AIInfoFrame preFrame)
	{
		if (!frame.getAssigendERoles().isEmpty())
		{
			// update all plays with the new frame and remove plays which failed or succeeded
			final Iterator<APlay> playIt = frame.playStrategy.getActivePlays().iterator();
			
			while (playIt.hasNext())
			{
				final APlay play = playIt.next();
				final EPlayState playState = play.update(frame);
				
				switch (playState)
				{
					case RUNNING:
						break;
					
					case SUCCEEDED:
					case FAILED:
					default:
						frame.playStrategy.getFinishedPlays().add(play);
				}
			}
		}
	}
	
	
	@Override
	public void onNewAthenaControl(AthenaControl newControl)
	{
		athenaAdapter.onNewAthenaControl(newControl);
		if (newControl.getControlState() == EAIControlState.MIXED_TEAM_MODE)
		{
			playFinderAdapter.setMixedTeam(true);
		} else
		{
			playFinderAdapter.setMixedTeam(false);
		}
	}
	
	
	@Override
	public void onNewApollonControl(ApollonControl newControl)
	{
		playFinderAdapter.onNewApollonControl(newControl);
	}
	
	
	@Override
	public void onSaveKnowledgeBase()
	{
		playFinderAdapter.onSaveKnowledgeBase();
	}
	
	
	/**
	 */
	public void onStop()
	{
		playFinderAdapter.onStop();
	}
	
	
	/**
	 * @param o
	 */
	public void addPlayStatisticsObserver(IStatisticsObserver o)
	{
		playFinderAdapter.addStatisticsObserver(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removePlayStatisticsObserver(IStatisticsObserver o)
	{
		playFinderAdapter.removeStatisticsObserver(o);
	}
	
}
