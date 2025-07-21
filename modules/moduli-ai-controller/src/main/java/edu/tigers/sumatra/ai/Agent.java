/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai;

import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * This is the one-and-only agent implementation, which controls the AI-subtract-modules, and sends out our MechWarriors
 * in
 * the endless battle for fame and glory!
 *
 * @author Gero
 */
public class Agent extends AAgent
{
	private static final Logger log = LogManager.getLogger(Agent.class.getName());

	private WorldFrameObserver worldFrameObserver = new WorldFrameObserver();
	private ASkillSystem skillSystem;

	private Map<EAiTeam, AiManager> aiManagers = new EnumMap<>(EAiTeam.class);


	@Override
	public void startModule()
	{
		try
		{
			AWorldPredictor predictor = SumatraModel.getInstance().getModule(AWorldPredictor.class);
			predictor.addObserver(worldFrameObserver);
		} catch (final ModuleNotFoundException err)
		{
			log.warn("No WP module found.", err);
		}

		try
		{
			skillSystem = SumatraModel.getInstance().getModule(ASkillSystem.class);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find skill system.", err);
		}

		addAis();
	}


	private void addAis()
	{
		for (EAiTeam eAiTeam : EAiTeam.values())
		{
			boolean active = Boolean.parseBoolean(SumatraModel.getInstance().getUserProperty(
					Agent.class.getCanonicalName() + "." + eAiTeam.name(),
					"true"));
			if (active)
			{
				AiManager aiManager = new AiManager(this, eAiTeam, skillSystem);
				aiManagers.put(eAiTeam, aiManager);
				aiManager.start();
			}
		}
	}


	private void removeAis()
	{
		for (EAiTeam eAiTeam : EAiTeam.values())
		{
			AiManager aiManager = aiManagers.remove(eAiTeam);
			boolean active = false;
			if (aiManager != null)
			{
				active = true;
				aiManager.stop();
			}
			SumatraModel.getInstance().setUserProperty(
					Agent.class.getCanonicalName() + "." + eAiTeam.name(),
					String.valueOf(active));
		}
	}


	@Override
	public void stopModule()
	{
		try
		{
			AWorldPredictor predictor = SumatraModel.getInstance().getModule(AWorldPredictor.class);
			predictor.removeObserver(worldFrameObserver);
		} catch (final ModuleNotFoundException err)
		{
			log.warn("No WP module found.", err);
		}

		removeAis();
	}


	@Override
	public void reset()
	{
		Map<EAiTeam, EAIControlState> previousControlStates = aiManagers.values().stream().collect(Collectors.toMap(
				a -> a.getAi().getAiTeam(),
				a -> a.getAi().getAthena().getControlState())
		);
		removeAis();
		addAis();
		previousControlStates.forEach(this::changeMode);
	}


	@Override
	public void changeMode(final EAiTeam aiTeam, final EAIControlState mode)
	{
		log.debug("Changing AI mode for {} to {}", aiTeam, mode);
		if (mode == EAIControlState.OFF)
		{
			AiManager aiManager = aiManagers.remove(aiTeam);
			if (aiManager != null)
			{
				aiManager.stop();
			}
		} else
		{
			AiManager aiManager = aiManagers.computeIfAbsent(aiTeam, team -> new AiManager(this, team, skillSystem));
			if (!aiManager.isRunning())
			{
				aiManager.start();
			}
			aiManager.changeMode(mode);
		}

		notifyAiModeChanged(aiTeam, mode);
		log.debug("Changed AI mode for {} to {}", aiTeam, mode);
	}


	@Override
	public void changeMode(final EAIControlState mode)
	{
		for (EAiTeam eAiTeam : EAiTeam.values())
		{
			AiManager aiManager = aiManagers.get(eAiTeam);
			// only send mode to running AIs
			if (aiManager != null)
			{
				changeMode(eAiTeam, mode);
			}
		}
	}


	@Override
	public Optional<Ai> getAi(final EAiTeam selectedTeam)
	{
		AiManager aiManager = aiManagers.get(selectedTeam);
		if (aiManager != null)
		{
			return Optional.of(aiManager.getAi());
		}
		return Optional.empty();
	}


	private class WorldFrameObserver implements IWorldFrameObserver
	{
		@Override
		public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
		{
			for (AiManager aiManager : aiManagers.values())
			{
				if (SumatraModel.getInstance().isSimulation())
				{
					// process all frames, waiting and blocking if necessary
					try
					{
						aiManager.getFreshWorldFrames().putFirst(wfWrapper);
					} catch (InterruptedException e)
					{
						Thread.currentThread().interrupt();
					}
				} else
				{
					aiManager.getFreshWorldFrames().pollLast();
					aiManager.getFreshWorldFrames().addFirst(wfWrapper);
				}
			}
		}
	}

}
