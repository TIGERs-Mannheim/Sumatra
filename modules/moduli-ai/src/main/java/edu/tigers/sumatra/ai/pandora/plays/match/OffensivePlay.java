/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.match;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.KickFactory;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.redirector.ERecommendedReceiverAction;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.DelayedAttackRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.DisruptOpponentRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.FreeSkirmishRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.KeepDistToBallRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OneOnOneShooterRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OpponentInterceptionRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.PassReceiverRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.SupportiveAttackerRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;


/**
 * The offensive play coordinates its roles
 */
@Log4j2
public class OffensivePlay extends APlay
{

	@Configurable(defValue = "true", comment = "enables the primary that tackles enemy receivers")
	private static boolean enableDisruptor = true;

	private final KickFactory kickFactory = new KickFactory();

	static
	{
		ConfigRegistration.registerClass("plays", OffensivePlay.class);
	}


	public OffensivePlay()
	{
		super(EPlay.OFFENSIVE);
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		new ArrayList<>(getRoles()).forEach(this::assignRoleForStrategy);
	}


	private void assignRoleForStrategy(final ARole role)
	{
		var playConfig = getAiFrame().getTacticalField().getOffensiveStrategy().getCurrentOffensivePlayConfiguration();
		var strategy = playConfig.get(role.getBotID());
		switch (strategy)
		{
			case PENALTY_KICK -> reassignRole(role, OneOnOneShooterRole.class, OneOnOneShooterRole::new);
			case KICK -> handleKickStrategy(role);
			case STOP -> reassignRole(role, KeepDistToBallRole.class, KeepDistToBallRole::new);
			case INTERCEPT -> reassignRole(role, OpponentInterceptionRole.class, OpponentInterceptionRole::new);
			case RECEIVE_PASS ->
			{
				var passReceiverRole = reassignRole(role, PassReceiverRole.class, PassReceiverRole::new);
				findPassForReceiver(role.getBotID()).ifPresent(passReceiverRole::setIncomingPass);
				kickFactory.update(getWorldFrame());
				passReceiverRole.setOutgoingKick(
						kickFactory.goalKick(passReceiverRole.getPos(), Geometry.getGoalTheir().getCenter()));
			}
			case DELAY -> reassignRole(role, DelayedAttackRole.class, DelayedAttackRole::new);
			case SUPPORTIVE_ATTACKER -> reassignRole(role, SupportiveAttackerRole.class, SupportiveAttackerRole::new);
			case FREE_SKIRMISH -> reassignRole(role, FreeSkirmishRole.class, FreeSkirmishRole::new);
			default ->
			{
				log.warn("No valid Offensive Role strategy found, this is some serious garbage, better call Mark");
				reassignRole(role, MoveRole.class, MoveRole::new);
			}
		}
	}


	private Optional<Pass> findPassForReceiver(BotID botID)
	{
		return getAiFrame().getTacticalField().getOffensiveActions().values().stream()
				.map(e -> e.getAction().getPass())
				.filter(Objects::nonNull)
				.filter(pass -> pass.getReceiver().equals(botID))
				.findAny();
	}


	private void handleKickStrategy(ARole role)
	{
		var recommendedAction = getTacticalField().getRedirectorDetectionInformation().getRecommendedAction();
		if (recommendedAction == ERecommendedReceiverAction.DISRUPT_OPPONENT && enableDisruptor)
		{
			reassignRole(role, DisruptOpponentRole.class, DisruptOpponentRole::new);
			return;
		}

		var action = getAiFrame().getTacticalField().getOffensiveActions().get(role.getBotID());
		var attacker = reassignRole(role, AttackerRole.class, AttackerRole::new);

		assert action != null;
		assert action.getAction().getType() != null;

		attacker.setAction(action.getAction());

		attacker.setUseSingleTouch(getAiFrame().getGameState().isStandardSituation()
				|| getAiFrame().getGameState().isKickoffOrPrepareKickoffForUs());
	}
}
