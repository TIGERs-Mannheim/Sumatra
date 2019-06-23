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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame.WorldFrameModifier;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.IAIProcessor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.AthenaControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.AthenaGuiAdapter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.Lachesis;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IAthenaControlHandler;


/**
 * 
 * This class chooses a play according to the current situation and adds it to the current {@link AIInfoFrame}.
 * 
 * @author OliverS, DanielW, Gero
 */
public class Athena implements IAIProcessor, IAthenaControlHandler
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	protected final Logger				log								= Logger.getLogger(getClass());
	

	/** Playfinder for real matches. */
	private final IPlayFinder			playFinder;
	/** Role-assigner instance */
	private final Lachesis				lachesis;
	
	private final ABotManager			botManager;
	

	/** Has Athena been controlled by GUI last frame? */
	private boolean						lastControlState				= false;
	
	// AI control from GUI
	private final AthenaGuiAdapter	athenaAdapter;
	

	private final List<TigerBot>		tigersConnectedLastFrame	= new ArrayList<TigerBot>(10);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public Athena(Lachesis lachesis, ABotManager botManager)
	{
		this.lachesis = lachesis;
		this.playFinder = new MatchPlayFinder();
		this.athenaAdapter = new AthenaGuiAdapter();
		this.botManager = botManager;
	}
	

	// --------------------------------------------------------------------------
	// --- process --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * perform play selection and call update on every play
	 */
	@Override
	public AIInfoFrame process(AIInfoFrame frame, AIInfoFrame preFrame)
	{
		synchronized (athenaAdapter)
		{
			// ##### GUI
			athenaAdapter.beforePlayFinding(frame, preFrame);
			

			// ################################### Change-detection
			// Whether any circumstances changed and a new play seems appropriate
			final Collection<TrackedTigerBot> preTigers = preFrame.worldFrame.tigerBots.values();
			final Collection<TrackedTigerBot> tigers = frame.worldFrame.tigerBots.values();
			final Collection<TrackedBot> preEnemies = preFrame.worldFrame.foeBots.values();
			final Collection<TrackedBot> enemies = frame.worldFrame.foeBots.values();
			

			// All tigers connected?
			final Map<Integer, ABot> bots = botManager.getAllBots();
			final WorldFrameModifier wfModifier = new WorldFrameModifier();
			final Iterator<Entry<Integer, TrackedTigerBot>> tigerIt = wfModifier
					.getMutableTigersIterator(frame.worldFrame);
			final List<TigerBot> tigersConnected = new ArrayList<TigerBot>();
			
			while (tigerIt.hasNext())
			{
				Entry<Integer, TrackedTigerBot> trackedTiger = tigerIt.next();
				TigerBot connectedTiger = (TigerBot) bots.get(trackedTiger.getKey());
				
				if (connectedTiger == null || connectedTiger.getNetworkState() == ENetworkState.OFFLINE)
				{
					// Remove from frame, as the KI is unable to work with it!
//					log.fatal("about to remove bot " + trackedTiger.getKey());
					tigerIt.remove();
				} else
				{
					tigersConnected.add(connectedTiger);
				}
			}
			
			final boolean tigerDisconnected = !tigersConnected.containsAll(tigersConnectedLastFrame);
			final boolean tigerConnected = !tigersConnectedLastFrame.containsAll(tigersConnected);
			
			tigersConnectedLastFrame.clear();
			tigersConnectedLastFrame.addAll(tigersConnected);
			

			// Same tigers as last frame?
			final boolean tigersRemoved = !tigers.containsAll(preTigers) && !tigers.isEmpty();
			final boolean tigersAdded = !preTigers.containsAll(tigers) && !preTigers.isEmpty();
			
			// Same enemies as last frame?
			final boolean enemyRemoved = !enemies.containsAll(preEnemies) && !enemies.isEmpty();
			final boolean enemyAdded = !preEnemies.containsAll(enemies) && !preEnemies.isEmpty();
			

			// Conclusion
			frame.playStrategy.setBotConnection(new edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.BotConnection(tigerConnected, tigerDisconnected, tigersAdded, tigersRemoved, enemyAdded, enemyRemoved));


			// ################################### Play-Finding
			// If switched from auto-control to manual (GUI) or the other way round
			boolean switched = false;
			
			// Should GUI override Athena?
			if (athenaAdapter.overridePlayFinding())
			{
				switched = !lastControlState; // Switched from auto to GUI
				
				athenaAdapter.choosePlays(frame, preFrame);
				
				lastControlState = true; // Remember state for next cycle
			} else
			{
				if (lastControlState)
				{
					switched = true; // Switched from GUI to auto
					// In case the override has been deactivated: We need a new play!
					frame.playStrategy.setForceNewDecision();
				}
				

				// Choose plays
				playFinder.choosePlays(frame.playStrategy.getActivePlays(), frame, preFrame);
				

				// Did plays changed?
				if (!preFrame.playStrategy.getActivePlays().containsAll(frame.playStrategy.getActivePlays()))
				{
					frame.playStrategy.setChangedPlay();
				}
				
				lastControlState = false; // Remember state for next cycle
			}
			
			// Print
			debugActivePlays(frame);
			

			// ##### GUI
			athenaAdapter.betweenPlayRole(frame, preFrame);
			

			// ################################### Role-Assignment
			if (!athenaAdapter.overrideRoleAssignment())
			{
				// Role assignment
				if (switched || frame.playStrategy.getBotConnection().isSomethingTrue() || frame.playStrategy.hasPlayChanged())
				{
					lachesis.assignRoles(frame);
				} else
				{
					frame.assignedRoles.putAll(preFrame.assignedRoles);
				}
			} else
			{
				athenaAdapter.assignRoles(frame, preFrame);
			}
			

			// ##### GUI
			athenaAdapter.afterRoleAssignment(frame, preFrame);
			

			// TODO Gero: There is the possibility (#roles > #bots, e.g.) that a role has not been assigned (thus have no
			// IDs), resulting in a warning every cycle. Do we have to do anything about this? (Gero)
			
			// Update
			if (!frame.assignedRoles.isEmpty())
			{
				// update all plays with the new frame and remove plays which failed or succeeded
				Iterator<APlay> playIt = frame.playStrategy.getActivePlays().iterator();
				
				while (playIt.hasNext())
				{
					APlay play = playIt.next();
					
					switch (play.update(frame))
					{
						case RUNNING:
							break;
						
						case SUCCEEDED:
						case FAILED:
//							System.out.println("ATHENA play " + play.getType() + " succeeded/failed");
							
						default:
							frame.playStrategy.getFinishedPlays().add(play);
							// playIt.remove();
					}
				}
			}
			return frame;
		}
	}
	

	/**
	 * Prints current active plays if changed
	 * 
	 * @param frame
	 */
	private void debugActivePlays(AIInfoFrame frame)
	{
		if (frame.playStrategy.hasPlayChanged())
		{
			String msg = "";
			for (APlay play : frame.playStrategy.getActivePlays())
			{
				msg += play.getType() + ", ";
			}
			
			if (msg.length() > 0)
			{
				msg = msg.substring(0, msg.length() - 2);
			}
			
			log.debug(msg);
		}
	}
	

	@Override
	public void onNewAthenaControl(AthenaControl newControl)
	{
		athenaAdapter.onNewAthenaControl(newControl);
	}
}
