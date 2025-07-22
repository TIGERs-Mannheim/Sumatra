/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleToPos;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;


/**
 * The offensive action gives instructions to the attacker
 */
@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RatedOffensiveAction
{
	EOffensiveActionMove move;
	OffensiveActionViability viability;
	OffensiveAction action;


	public Optional<IVector2> getTarget()
	{
		return action.getKickOpt().map(Kick::getTarget);
	}


	public Optional<KickParams> getKickParams()
	{
		return action.getKickOpt().map(Kick::getKickParams);
	}


	public static RatedOffensiveAction buildProtect(
			@NonNull EOffensiveActionMove move,
			@NonNull OffensiveActionViability viability,
			@NonNull DribbleToPos dribbleToPos)
	{
		return RatedOffensiveAction.builder()
				.move(move)
				.viability(viability)
				.action(OffensiveAction.buildProtect(dribbleToPos))
				.build();
	}


	public static RatedOffensiveAction buildPass(
			@NonNull EOffensiveActionMove move,
			@NonNull OffensiveActionViability viability,
			@NonNull Pass pass)
	{
		return RatedOffensiveAction.builder()
				.move(move)
				.viability(viability)
				.action(OffensiveAction.buildPass(pass))
				.build();
	}

	public static RatedOffensiveAction buildProtectPass(
			@NonNull EOffensiveActionMove move,
			@NonNull OffensiveActionViability viability,
			@NonNull Pass pass)
	{
		return RatedOffensiveAction.builder()
				.move(move)
				.viability(viability)
				.action(OffensiveAction.buildProtectPass(pass))
				.build();
	}


	public static RatedOffensiveAction buildKick(
			@NonNull EOffensiveActionMove move,
			@NonNull OffensiveActionViability viability,
			@NonNull Kick kick)
	{
		return RatedOffensiveAction.builder()
				.move(move)
				.viability(viability)
				.action(OffensiveAction.buildKick(kick))
				.build();
	}


	public static RatedOffensiveAction buildRedirectKick(
			@NonNull EOffensiveActionMove move,
			@NonNull OffensiveActionViability viability,
			@NonNull Kick kick)
	{
		return RatedOffensiveAction.builder()
				.move(move)
				.viability(viability)
				.action(OffensiveAction.buildRedirectKick(kick))
				.build();
	}


	public static RatedOffensiveAction buildDribbleKick(
			@NonNull EOffensiveActionMove move,
			@NonNull OffensiveActionViability viability,
			@NonNull Kick kick,
			@NonNull DribbleToPos dribbleToPos)
	{
		return RatedOffensiveAction.builder()
				.move(move)
				.viability(viability)
				.action(OffensiveAction.buildDribbleKick(kick, dribbleToPos))
				.build();
	}


	public static RatedOffensiveAction buildReceive(
			@NonNull EOffensiveActionMove move,
			@NonNull OffensiveActionViability viability,
			IVector2 ballContactPos)
	{
		return RatedOffensiveAction.builder()
				.move(move)
				.viability(viability)
				.action(OffensiveAction.buildReceive(ballContactPos))
				.build();
	}
}
