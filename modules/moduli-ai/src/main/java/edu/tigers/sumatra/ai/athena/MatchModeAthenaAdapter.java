package edu.tigers.sumatra.ai.athena;

import com.github.g3force.instanceables.InstanceableClass;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Match mode
 */
@Log4j2
public class MatchModeAthenaAdapter implements IAthenaAdapter
{
	private final RoleAssigner roleAssigner = new RoleAssigner();
	private final Set<APlay> activePlays = new HashSet<>();


	@Override
	public PlayStrategy process(final MetisAiFrame metisAiFrame, final AthenaGuiInput athenaGuiInput)
	{
		syncTargetPlaySet(metisAiFrame.getTacticalField().getDesiredBotMap().keySet(), activePlays);
		var desiredBotMap = validateBotMap(metisAiFrame.getTacticalField().getDesiredBotMap(), metisAiFrame);
		roleAssigner.assignRoles(activePlays, desiredBotMap);
		checkRoleCount(metisAiFrame);
		return new PlayStrategy(Set.copyOf(activePlays));
	}


	private Map<EPlay, Set<BotID>> validateBotMap(Map<EPlay, Set<BotID>> desiredBotMap, MetisAiFrame frame)
	{
		Set<BotID> availableBots = frame.getWorldFrame().getTigerBotsAvailable().keySet();
		Map<EPlay, Set<BotID>> validatedBotMap = new EnumMap<>(EPlay.class);
		Set<BotID> removedBots = new HashSet<>();
		for (var entry : desiredBotMap.entrySet())
		{
			Set<BotID> validBots = new HashSet<>();
			for (BotID botID : entry.getValue())
			{
				if (availableBots.contains(botID))
				{
					validBots.add(botID);
				} else
				{
					removedBots.add(botID);
				}
			}
			validatedBotMap.put(entry.getKey(), validBots);
		}
		if (!removedBots.isEmpty())
		{
			log.warn("Removed desired bots that are not available: {}", removedBots);
		}
		return validatedBotMap;
	}


	@Override
	public void stop(final AthenaGuiInput athenaGuiInput)
	{
		activePlays.forEach(play -> play.removeRoles(play.getRoles().size()));
		activePlays.clear();
	}


	private void syncTargetPlaySet(final Set<EPlay> targetPlaySet, final Set<APlay> activePlays)
	{
		Map<EPlay, APlay> activePlaysMap = mapToPlayMap(activePlays);
		addNewPlays(targetPlaySet, activePlays, activePlaysMap);
		removeVanishedPlays(targetPlaySet, activePlays, activePlaysMap);
	}


	private void removeVanishedPlays(
			final Set<EPlay> targetPlaySet, final Set<APlay> activePlays,
			final Map<EPlay, APlay> activePlaysMap
	)
	{
		for (APlay aPlay : activePlaysMap.values())
		{
			if (!targetPlaySet.contains(aPlay.getType()))
			{
				aPlay.removeRoles(aPlay.getRoles().size());
				activePlays.remove(aPlay);
			}
		}
	}


	private void addNewPlays(
			final Set<EPlay> targetPlaySet, final Set<APlay> activePlays,
			final Map<EPlay, APlay> activePlaysMap
	)
	{
		for (EPlay ePlay : targetPlaySet)
		{
			if (!activePlaysMap.containsKey(ePlay))
			{
				try
				{
					APlay newPlay = (APlay) ePlay.getInstanceableClass().newDefaultInstance();
					activePlays.add(newPlay);
				} catch (InstanceableClass.NotCreateableException err)
				{
					log.warn("Could not instantiate play", err);
				}
			}
		}
	}


	private Map<EPlay, APlay> mapToPlayMap(final Set<APlay> activePlays)
	{
		Map<EPlay, APlay> activePlaysMap = new EnumMap<>(EPlay.class);
		for (APlay aPlay : activePlays)
		{
			activePlaysMap.put(aPlay.getType(), aPlay);
		}
		return activePlaysMap;
	}


	private void checkRoleCount(final MetisAiFrame frame)
	{
		if (!SumatraModel.getInstance().isTournamentMode()
				&& !frame.getGameState().isIdleGame())
		{
			int numBots = frame.getWorldFrame().getTigerBotsAvailable().size();
			List<ARole> roles = activePlays.stream()
					.map(APlay::getRoles)
					.flatMap(Collection::stream)
					.toList();
			if (numBots != roles.size())
			{
				log.warn(
						"Assigned role number does not match number of bots: numBots={}, numRoles={} | Roles: {}",
						numBots, roles.size(), roles
				);
			} else
			{
				Set<BotID> uniqueSetOfBots = roles.stream().map(ARole::getBotID).collect(Collectors.toSet());
				if (uniqueSetOfBots.size() != numBots)
				{
					log.warn("Bot ids are assigned multiple times: {}", roles);
				}
			}
		}
	}
}
