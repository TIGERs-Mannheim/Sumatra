/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Publishes statemachine information.
 *
 * @author Mark Geiger <MarkGeiger@posteo.de>
 */
public class RoleStatemachinePublisherCalc extends ACalculator
{
	@Getter
	private Map<BotID, Map<IEvent, Map<IState, IState>>> roleStatemachineGraph;

	@Getter
	private Map<BotID, Map<IEvent, Map<IState, IState>>> skillStatemachineGraph;


	@Override
	public void doCalc()
	{
		// publish role graphs
		roleStatemachineGraph = getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles()
				.values().stream().collect(Collectors.toMap(ARole::getBotID, ARole::getStateGraph));

		// publish skill graphs
		skillStatemachineGraph = getAiFrame().getPrevFrame().getPlayStrategy().getActiveRoles().values()
				.stream()
				.map(ARole::getCurrentSkill)
				.filter(Objects::nonNull)
				.map(ASkill.class::cast)
				.filter(ASkill::isInitialized)
				.collect(Collectors.toMap(ASkill::getBotId, ASkill::getStateGraph));
	}


	@Override
	protected void reset()
	{
		roleStatemachineGraph = new HashMap<>();
		skillStatemachineGraph = new HashMap<>();
	}
}
