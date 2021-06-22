/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.match;

import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.redirector.ERecommendedReceiverAction;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.AttackerRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.DelayedAttackRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.DisruptOpponentRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.FreeSkirmishRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.KeepDistToBallRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.OpponentInterceptionRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.PassReceiverRole;
import edu.tigers.sumatra.ai.pandora.roles.offense.SupportiveAttackerRole;
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
	public OffensivePlay()
	{
		super(EPlay.OFFENSIVE);
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		super.doUpdateBeforeRoles();
		new ArrayList<>(getRoles()).forEach(this::assignRoleForStrategy);
	}


	private void assignRoleForStrategy(final ARole role)
	{
		var playConfig = getAiFrame().getTacticalField().getOffensiveStrategy().getCurrentOffensivePlayConfiguration();
		var strategy = playConfig.get(role.getBotID());
		switch (strategy)
		{
			case KICK:
				handleKickStrategy(role);
				break;
			case STOP:
				reassignRole(role, KeepDistToBallRole.class, KeepDistToBallRole::new);
				break;
			case INTERCEPT:
				reassignRole(role, OpponentInterceptionRole.class, OpponentInterceptionRole::new);
				break;
			case RECEIVE_PASS:
				var passReceiverRole = reassignRole(role, PassReceiverRole.class, PassReceiverRole::new);
				findPassForReceiver(role.getBotID()).ifPresent(passReceiverRole::setIncomingPass);
				break;
			case DELAY:
				reassignRole(role, DelayedAttackRole.class, DelayedAttackRole::new);
				break;
			case SUPPORTIVE_ATTACKER:
				reassignRole(role, SupportiveAttackerRole.class, SupportiveAttackerRole::new);
				break;
			case FREE_SKIRMISH:
				reassignRole(role, FreeSkirmishRole.class, FreeSkirmishRole::new);
				break;
			default:
				log.warn("No valid Offensive Role strategy found, this is some serious garbage, better call Mark");
				reassignRole(role, MoveRole.class, MoveRole::new);
				break;
		}
	}


	private Optional<Pass> findPassForReceiver(BotID botID)
	{
		return getAiFrame().getTacticalField().getOffensiveActions().values().stream()
				.map(OffensiveAction::getPass)
				.filter(Objects::nonNull)
				.filter(pass -> pass.getReceiver() == botID)
				.findAny();
	}


	private void handleKickStrategy(ARole role)
	{
		var recommendedAction = getTacticalField().getRedirectorDetectionInformation().getRecommendedAction();
		if (recommendedAction == ERecommendedReceiverAction.DISRUPT_OPPONENT)
		{
			reassignRole(role, DisruptOpponentRole.class, DisruptOpponentRole::new);
			return;
		}

		var action = getAiFrame().getTacticalField().getOffensiveActions().get(role.getBotID());
		var attacker = reassignRole(role, AttackerRole.class, AttackerRole::new);
		var pass = action.getPass();
		var kick = action.getKick();
		var ballContactPos = action.getBallContactPos();
		attacker.setDribbleToPos(action.getDribbleToPos());
		if (pass != null)
		{
			attacker.setPass(pass);
		} else if (kick != null)
		{
			attacker.setKick(kick);
		} else
		{
			attacker.setBallContactPos(ballContactPos);
		}
		attacker.setUseSingleTouch(getAiFrame().getGameState().isStandardSituation()
				|| getAiFrame().getGameState().isKickoffOrPrepareKickoffForUs());
	}
}
