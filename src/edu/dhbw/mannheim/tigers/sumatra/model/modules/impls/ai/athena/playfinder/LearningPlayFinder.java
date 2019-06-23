/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.05.2012
 * Author(s): NicolaiO, DirkK, DanielAl
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EPossibleGoal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.ApollonControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.PlayAndRoleCount;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.AKnowledgeField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.AcceptableMatch;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.IComparisonResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.IKnowledgeBase;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.KnowledgeFieldRaster;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.KnowledgePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.persistence.IKnowledgeBasePersistence;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.persistence.ObjectDBPlaysPersistence;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.util.KnowledgeHelper;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.ESelectionReason;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EMatchBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlayState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.PlayType;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.moduli.listenerVariables.ModulesState;


/**
 * Playfinder will learn from result of plays and with information of field. <br />
 * COncept is that all situations (fields) will be saved and compared with the current one. The best historic situation
 * is a reference for the decision for new plays. The PlayFInder will choose the same as in the historic situation.
 * 
 * @author NicolaiO, DirkK, DanielAl
 * 
 */
public class LearningPlayFinder extends APlayFinder
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger			log									= Logger.getLogger(LearningPlayFinder.class
																									.getName());
	
	private static final int				DEFAULT_NUM_SUCCESSFUL_FIELDS	= 50;
	private static final double			DEFAULT_SUCCESS_TRESHOLD		= 0.05;
	
	private static final int				MAX_RETRY_ON_ERROR				= 5;
	
	private static final int				MAX_RETRY_SELECT_PLAY			= 5;
	
	/** the knowledgeBase contains our knowledge about successful and failed situations */
	private IKnowledgeBase					knowledgeBase;
	
	/** number of fields to loop through */
	private int									numSuccessfulFields				= DEFAULT_NUM_SUCCESSFUL_FIELDS;
	/** should we behave rather aggressive or rather conservative or what? */
	private final EMatchBehavior			matchBehavior;
	/**
	 * AcceptableMatch is the border, which the similarity of a field has to cross (must be higher) to get selected. <br />
	 * TODO unassigned An improved LearningPlayFinder should adapt this value automatically, based on its experience.
	 * Also it should be possible to make this changeable by operator. #814
	 */
	private AcceptableMatch					acceptableMatch;
	
	/** Threshold for unique success score */
	private double								successTreshold					= DEFAULT_SUCCESS_TRESHOLD;
	/** get random stuff */
	private static Random					random								= new Random(System.nanoTime());
	
	private AKnowledgeField					lastKnowledgeField;
	
	private final List<List<APlay>>		historyPlays						= new LinkedList<List<APlay>>();
	
	/** Apollon Control Object, contains different information @see {@link ApollonControl} */
	private ApollonControl					apollonControl;
	
	private IKnowledgeBasePersistence	kbPersistence;
	
	private final List<EPlay>				tmpIgnoredPlays					= new ArrayList<EPlay>();
	private int									securityCounterSelectPlays		= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public LearningPlayFinder()
	{
		ModuliStateAdapter.getInstance().addObserver(this);
		matchBehavior = AIConfig.getTactics().getTacticalOrientation();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private void loadDB()
	{
		if (apollonControl == null)
		{
			log.error("loadDB was called while apollonControl is still null!");
			return;
		}
		if ((knowledgeBase != null) && (kbPersistence != null) && kbPersistence.isOpen())
		{
			if (apollonControl.isSaveOnClose())
			{
				kbPersistence.saveKnowledgeBase(knowledgeBase, apollonControl.isPersistStrategyMerge());
			}
			kbPersistence.close();
		}
		kbPersistence = new ObjectDBPlaysPersistence(apollonControl.getDatabasePath(),
				apollonControl.getKnowledgeBaseName());
		kbPersistence.open();
		knowledgeBase = kbPersistence.loadKnowledgeBase(apollonControl.getKnowledgeBaseName());
		if (log.isInfoEnabled())
		{
			long count = KnowledgeHelper.countKnowledgeBase(knowledgeBase);
			log.info("Knowledge base loaded with " + count + " fields");
		}
		knowledgeBase.initialize();
		acceptableMatch = new AcceptableMatch(matchBehavior);
		acceptableMatch.setAcceptableMatch(apollonControl.getAcceptableMatch());
		knowledgeBase.setName(apollonControl.getKnowledgeBaseName());
	}
	
	
	/**
	 * Select a new set of plays
	 * 
	 * @param frame
	 * @param preFrame
	 * @param plays resulting plays
	 * @throws PlayFinderException if something went wrong
	 */
	private void selectPlays(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays) throws PlayFinderException
	{
		// we do not reuse old plays...
		plays.clear();
		
		// prepare knowledgeBase
		knowledgeBase.resetCounters();
		
		// add keeper play
		onKeeperSelection(frame, preFrame, plays);
		
		// find new plays
		final AKnowledgeField currentField = new KnowledgeFieldRaster(frame);
		
		final List<List<EPlay>> listOfListOfPlays = new LinkedList<List<EPlay>>();
		
		// if ((frame.getControlState() != EAIControlState.MIXED_TEAM_MODE))
		// // || !frame.tacticalInfo.getBallPossession().isPossessedByOtherSubTeam(frame.worldFrame))
		// {
		listOfListOfPlays.add(PlayType.getOffensivePlays());
		// }
		listOfListOfPlays.add(PlayType.getDefensivePlays());
		// the last list must be modifiable, see below why
		listOfListOfPlays.add(new LinkedList<EPlay>(PlayType.getSupportPlays()));
		
		int lolopCounter = 0;
		// Roles that have to be found -1 because the keeper has always to be used
		int rolesLeft = frame.worldFrame.tigerBotsAvailable.size() - 1;
		final List<PlayAndRoleCount> playAndRoleCounts = new LinkedList<PlayAndRoleCount>();
		while (rolesLeft > 0)
		{
			// filter plays that have too many roles
			final List<EPlay> validPlays = new LinkedList<EPlay>();
			outer: for (final EPlay ePlay : listOfListOfPlays.get(lolopCounter))
			{
				if (ePlay.getMinRoles() <= rolesLeft)
				{
					for (PlayAndRoleCount parc : playAndRoleCounts)
					{
						if ((parc.getePlay() == ePlay) && (parc.getNumRolesToAssign() >= ePlay.getMaxRoles()))
						{
							continue outer;
						}
					}
					validPlays.add(ePlay);
				}
			}
			log.trace("Valid plays for selection:" + validPlays + " rolesLeft: " + rolesLeft);
			applyFilter(frame, validPlays);
			log.trace("Plays after filter: " + validPlays);
			
			// choose play
			final PlayAndRoleCount theChosenOne = choosePlayIntelligent(frame, currentField, validPlays, rolesLeft);
			log.trace("The chosen one: " + theChosenOne);
			
			// increment lolopCounter until last element reached, then stay at last element
			if (lolopCounter < (listOfListOfPlays.size() - 1))
			{
				lolopCounter++;
			}
			
			final int newRoles = mergePlayAndRoleCounts(playAndRoleCounts, theChosenOne);
			
			if (newRoles == 0)
			{
				log.error("Not enough roles could be found! Maybe the play filter is too hard or there are too less plays/roles in general...");
				// prevents endless loop!
				break;
			}
			
			// decrement remaining roles counter
			rolesLeft -= newRoles;
		}
		
		for (PlayAndRoleCount parc : playAndRoleCounts)
		{
			APlay play = getPlayFactory().createPlay(parc.getePlay(), frame, parc.getNumRolesToAssign());
			if (play.getPlayState() != EPlayState.RUNNING)
			{
				// oh, one play has finished in the constructor already... Start from new...
				tmpIgnoredPlays.add(play.getType());
				securityCounterSelectPlays++;
				if (securityCounterSelectPlays < MAX_RETRY_SELECT_PLAY)
				{
					log.debug("One play finished early (in constructor), restart playfinding");
					selectPlays(frame, preFrame, plays);
				} else
				{
					log.warn("No plays found after " + MAX_RETRY_SELECT_PLAY + " retries.");
					securityCounterSelectPlays = 0;
					tmpIgnoredPlays.clear();
					continue;
				}
				securityCounterSelectPlays = 0;
				tmpIgnoredPlays.clear();
				return;
			}
			plays.add(play);
			play.setSelectionReason(parc.getSelectionReason());
		}
		
		lastKnowledgeField = currentField;
		
		log.info("selected plays: " + plays);
		
		return;
	}
	
	
	/**
	 * Merge two equal PlayAndRoleCount, if needed
	 * 
	 * @param playAndRoleCounts
	 * @param theChosenOne
	 * @return
	 */
	private int mergePlayAndRoleCounts(List<PlayAndRoleCount> playAndRoleCounts, PlayAndRoleCount theChosenOne)
	{
		for (PlayAndRoleCount parc : playAndRoleCounts)
		{
			if (parc.getePlay().equals(theChosenOne.getePlay()))
			{
				int maxRoles = parc.getePlay().getMaxRoles();
				int desRoles = parc.getNumRolesToAssign() + theChosenOne.getNumRolesToAssign();
				int numRoles = Math.min(maxRoles, desRoles);
				int newRoles = numRoles - parc.getNumRolesToAssign();
				parc.setNumRolesToAssign(numRoles);
				
				log.trace("Adding " + newRoles + " roles to existing parc (now " + numRoles + " roles)");
				return newRoles;
			}
		}
		
		playAndRoleCounts.add(theChosenOne);
		return theChosenOne.getNumRolesToAssign();
	}
	
	
	private void applyFilter(AIInfoFrame aiFrame, List<EPlay> plays)
	{
		final List<EPlay> tobeRemovedPlays = filterPlays(aiFrame, plays);
		final List<EPlay> mixedTeamPlays = filterMixedTeamPlays(aiFrame, plays);
		tobeRemovedPlays.addAll(mixedTeamPlays);
		tobeRemovedPlays.addAll(tmpIgnoredPlays);
		
		if (tobeRemovedPlays.containsAll(plays))
		{
			log.warn("All Plays would be filtered (" + tobeRemovedPlays + " - " + plays + " - " + tmpIgnoredPlays
					+ "). This should not happen!! Ignoring filter...");
			plays.removeAll(mixedTeamPlays);
		} else
		{
			plays.removeAll(tobeRemovedPlays);
		}
	}
	
	
	private PlayAndRoleCount findSuccessfulPlays(AIInfoFrame aiFrame, AKnowledgeField currentField, List<EPlay> plays,
			int maxRoles) throws PlayFinderException
	{
		final List<IComparisonResult> successfulSorted = knowledgeBase.findNextSuccessfulPlaysSorted(plays, currentField,
				numSuccessfulFields);
		
		while (successfulSorted.size() > 0)
		{
			final IComparisonResult bestMatch = successfulSorted.get(0);
			log.debug("Acceptable Match is " + acceptableMatch.getAcceptableMatch());
			if ((bestMatch.getPlay().getNumRolesToAssign() <= maxRoles)
					&& (bestMatch.calcResult() > acceptableMatch.getAcceptableMatch()))
			{
				// now we have a good match
				
				if ((successfulSorted.size() > 1)
						&& ((bestMatch.calcResult() - successfulSorted.get(1).calcResult()) < successTreshold))
				{
					// now we have a second almost equal match
					// start again. knowledgeBase will automatically find next results
					PlayAndRoleCount parc = choosePlayIntelligent(aiFrame, currentField, plays, maxRoles);
					parc.setSelectionReason(ESelectionReason.SUCCESSFUL_EQUAL_MATCH);
					return parc;
				}
				// now we have either only one result, or we have a quite unique match
				// lets see, what the failed situations look like
				final List<EPlay> bestPlays = new LinkedList<EPlay>();
				bestPlays.add(bestMatch.getPlay().getePlay());
				final List<IComparisonResult> failedSorted = knowledgeBase.findNextFailedPlaysSorted(bestPlays,
						currentField, 1);
				if ((failedSorted.size() > 0) && (failedSorted.get(0).calcResult() > acceptableMatch.getAcceptableMatch()))
				{
					// found a situation
					// we have a big match for failed situations for this play.
					// lets check the next play
					successfulSorted.remove(bestMatch);
				} else
				{
					// we have a winner!
					log.trace("Found successful play: " + bestMatch.getPlay());
					bestMatch.getPlay().setSelectionReason(ESelectionReason.SUCCESSFUL_FIRST_TRY);
					return bestMatch.getPlay();
				}
			} else
			{
				// we have a bad match or there are too many roles
				// ask knowledgeBase again
				PlayAndRoleCount parc = choosePlayIntelligent(aiFrame, currentField, plays, maxRoles);
				parc.setSelectionReason(ESelectionReason.SUCCESSFUL_MULTIPLE_TRIES);
				return parc;
			}
		}
		return null;
	}
	
	
	/**
	 * Choose a play from the given list of plays by querying the knowledgeBase with currentField.
	 * maxRoles will be used to filter any plays that have to many roles.
	 * 
	 * @param currentField the field to use for querying the knowledge base
	 * @param plays the set of plays to use
	 * @param maxRoles do not choose a play with more than maxRoles roles
	 * @return a play and its number of roles
	 */
	private PlayAndRoleCount choosePlayIntelligent(AIInfoFrame aiFrame, AKnowledgeField currentField, List<EPlay> plays,
			int maxRoles) throws PlayFinderException
	{
		if (plays.isEmpty())
		{
			throw new PlayFinderException("No plays to choose from!");
		}
		
		PlayAndRoleCount parc = findSuccessfulPlays(aiFrame, currentField, plays, maxRoles);
		if (parc != null)
		{
			log.trace("Found play: " + parc);
			return parc;
		}
		
		/*
		 * Ok. At this point, we tried all the fields we have in our knowledgeBase.
		 * But we still have no good match.
		 * We have now to options:
		 * 1. choose plays with least matching failed situations
		 * 2. choose randomly
		 */
		
		switch (matchBehavior)
		{
			case DEFENSIVE:
			case CONSERVATIVE:
			{
				// choose plays with least matching failed situations
				log.warn("conservative/defensive matchBehavior not implemented!");
			}
			case AGGRESSIVE:
			case CREATIVE:
			{
				return chooseRandomPlay(plays, maxRoles);
			}
			case NOT_DEFINED:
			default:
				break;
		}
		
		throw new PlayFinderException("We should not reach this line");
	}
	
	
	private PlayAndRoleCount chooseRandomPlay(List<EPlay> plays, int maxRoles) throws PlayFinderException
	{
		List<EPlay> possiblePlays = new LinkedList<EPlay>();
		for (EPlay play : plays)
		{
			if (play.getMinRoles() <= maxRoles)
			{
				possiblePlays.add(play);
			}
		}
		if (possiblePlays.isEmpty())
		{
			throw new PlayFinderException("No valid plays to choose from. invalid plays: " + plays + " maxRoles: "
					+ maxRoles);
		}
		// choose randomly
		EPlay play = possiblePlays.get(random.nextInt(possiblePlays.size()));
		
		final int max;
		if (play.getMaxRoles() == EPlay.MAX_BOTS)
		{
			max = maxRoles;
		} else
		{
			max = Math.min(play.getMaxRoles(), maxRoles);
		}
		final int roles = play.getMinRoles() + random.nextInt((max - play.getMinRoles()) + 1);
		PlayAndRoleCount parc = new PlayAndRoleCount(play, roles, ESelectionReason.RANDOM);
		
		log.trace("Random play chosen: " + parc);
		return parc;
	}
	
	
	/**
	 * Use playableScore to filter plays that are not appropriate for the current situation
	 * You will get a list of plays that should be removed.
	 * 
	 * It calculates the avg score and removes all plays, that are below the avg.
	 * If there is only one play and it has a score of 0, it will be added nevertheless
	 * If there is one with score 1 and one with 0, avg is 0.5, so only the one with 1 will be selected
	 * 
	 * @param aiFrame
	 * @param plays to filter
	 * @return list of plays to be removed
	 */
	private List<EPlay> filterPlays(AIInfoFrame aiFrame, List<EPlay> plays)
	{
		final ArrayList<EPlay> tobeRemoved = new ArrayList<EPlay>();
		final ArrayList<Float> scoresOfRemoved = new ArrayList<Float>();
		final Map<EPlay, Float> scores = new HashMap<EPlay, Float>();
		int sum = 0;
		for (final EPlay ePlay : plays)
		{
			final APlay play = getPlayFactory().getDummyPlay(ePlay);
			final float score = play.calcPlayableScore(aiFrame);
			scores.put(ePlay, score);
			sum += score;
		}
		int avg = sum / plays.size();
		for (Map.Entry<EPlay, Float> entry : scores.entrySet())
		{
			if (entry.getValue() < avg)
			{
				tobeRemoved.add(entry.getKey());
				scoresOfRemoved.add(entry.getValue());
			}
			
		}
		if (tobeRemoved.size() > 0)
		{
			log.info("Filtered some plays with playableScore: " + tobeRemoved + scoresOfRemoved);
		}
		
		return tobeRemoved;
	}
	
	
	private List<EPlay> filterMixedTeamPlays(AIInfoFrame aiFrame, List<EPlay> plays)
	{
		List<EPlay> toBeRemovedPlays = new LinkedList<EPlay>();
		if ((aiFrame.getControlState() == EAIControlState.MIXED_TEAM_MODE)
				&& !aiFrame.tacticalInfo.isOtherMixedTeamTouchedBall())
		{
			for (EPlay play : plays)
			{
				if (play.isGoalScorer())
				{
					toBeRemovedPlays.add(play);
				}
			}
		} else
		{
			for (EPlay play : plays)
			{
				if (play.isMixedTeamPlay())
				{
					toBeRemovedPlays.add(play);
				}
			}
		}
		return toBeRemovedPlays;
	}
	
	
	@Override
	public void onNewDecision(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		for (int i = 0; i < MAX_RETRY_ON_ERROR; i++)
		{
			try
			{
				selectPlays(frame, preFrame, plays);
				break;
			} catch (PlayFinderException err)
			{
				log.error("The PlayFinder had a problem during play choosing.", err);
			}
		}
	}
	
	
	@Override
	public void onFinishedPlays(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> finishedPlays)
	{
		super.onFinishedPlays(frame, preFrame, finishedPlays);
		
		// add all active plays (including finished, because we use preFrame) to history
		historyPlays.add(preFrame.playStrategy.getActivePlays());
		
		if (lastKnowledgeField == null)
		{
			// we do not know the last field, so we can not store anything in the knowledgeBase...
			// this is normal for the very first finished plays
			return;
		}
		// lets determine the overall playState (result) of all finished plays
		// if there is a failed play -> failed
		// else if there is a succeeded play -> success
		// else we just have a finished play
		EPlayState playState = EPlayState.FINISHED;
		for (final APlay play : finishedPlays)
		{
			if (play.getPlayState() == EPlayState.FAILED)
			{
				playState = EPlayState.FAILED;
				// this is our final result
				break;
			} else if (play.getPlayState() == EPlayState.SUCCEEDED)
			{
				playState = EPlayState.SUCCEEDED;
				// we will look for a failed play anyway
			}
		}
		
		log.info("Finished plays: " + playState + " " + finishedPlays);
		processHistoryPlays(playState);
		acceptableMatch.onFinishedPlays(finishedPlays);
	}
	
	
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case RESOLVED:
			{
				// Only save knowledgeBase if the flag Save on Close is true.
				if ((apollonControl != null) && apollonControl.isSaveOnClose())
				{
					try
					{
						kbPersistence.saveKnowledgeBase(knowledgeBase, apollonControl.isPersistStrategyMerge());
						kbPersistence.close();
					} catch (Exception err)
					{
						log.error("The knowledge base could not be saved.", err);
					}
				}
				ModuliStateAdapter.getInstance().removeObserver(this);
			}
				break;
			case ACTIVE:
			case NOT_LOADED:
			default:
				// nothing to do
				break;
		}
	}
	
	
	@Override
	public void onPossibleGoalScored(AIInfoFrame frame, AIInfoFrame preFrame, EPossibleGoal possibleGoal)
	{
		super.onPossibleGoalScored(frame, preFrame, possibleGoal);
		switch (possibleGoal)
		{
			case THEY:
				log.info("They scored a goal, historyPlays will be saved: " + historyPlays);
				processHistoryPlays(EPlayState.FAILED);
				break;
			case WE:
				log.info("We scored a goal, historyPlays will be saved: " + historyPlays);
				processHistoryPlays(EPlayState.SUCCEEDED);
				break;
			case NO_ONE:
			default:
				break;
		}
	}
	
	
	/**
	 * Save all plays in history Plays to knowledgeBase with given playState
	 * 
	 * @param playState should be one of FAILED or SUCCEEDED
	 */
	private void processHistoryPlays(EPlayState playState)
	{
		if ((playState == EPlayState.FAILED) || (playState == EPlayState.SUCCEEDED))
		{
			// save result for all plays
			for (final List<APlay> playList : historyPlays)
			{
				for (final APlay play : playList)
				{
					final KnowledgePlay knowledgePlay = new KnowledgePlay(new PlayAndRoleCount(play.getType(),
							play.getRoleCount(), play.getSelectionReason()));
					if (playState == EPlayState.FAILED)
					{
						knowledgePlay.addFailedField(lastKnowledgeField);
					} else
					{
						knowledgePlay.addSuccessField(lastKnowledgeField);
					}
					knowledgeBase.addKnowledgePlay(knowledgePlay);
				}
			}
			historyPlays.clear();
		}
	}
	
	
	@Override
	public void onNewApollonControl(ApollonControl newControl)
	{
		ApollonControl oldControl = apollonControl;
		apollonControl = newControl;
		
		if ((oldControl == null) || !newControl.getKnowledgeBaseName().equals(oldControl.getKnowledgeBaseName()))
		{
			loadDB();
		} else
		{
			acceptableMatch.setAcceptableMatch(apollonControl.getAcceptableMatch());
			knowledgeBase.setName(apollonControl.getKnowledgeBaseName());
		}
	}
	
	
	@Override
	public void onSaveKnowledgeBase()
	{
		if (apollonControl == null)
		{
			return;
		}
		try
		{
			kbPersistence.saveKnowledgeBase(knowledgeBase, apollonControl.isPersistStrategyMerge());
		} catch (Exception err)
		{
			log.error("The knowledge base could not be saved.", err);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
