/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleToPos;
import edu.tigers.sumatra.ai.metis.pass.target.IPassTarget;
import edu.tigers.sumatra.ai.metis.pass.target.PassTarget;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;


/**
 * The offensive action gives instructions to the attacker
 */
@Persistent(version = 8)
@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OffensiveAction
{
	EOffensiveActionType type;
	Pass pass;
	Kick kick;
	IVector2 ballContactPos;
	DribbleToPos dribbleToPos;


	public OffensiveAction()
	{
		this.pass = null;
		this.kick = null;
		this.dribbleToPos = null;
		this.ballContactPos = null;
		this.type = null;
	}


	public static OffensiveAction buildRedirectKick(
			@NonNull Kick kick)
	{
		return OffensiveAction.builder()
				.kick(kick)
				.ballContactPos(kick.getSource())
				.type(EOffensiveActionType.REDIRECT_KICK)
				.build();
	}


	public Optional<Kick> getKickOpt()
	{
		return Optional.ofNullable(pass)
				.map(Pass::getKick)
				.or(() -> Optional.ofNullable(kick));
	}


	public Optional<IVector2> getTarget()
	{
		return getKickOpt().map(Kick::getTarget);
	}


	public Optional<KickParams> getKickParams()
	{
		return getKickOpt().map(Kick::getKickParams);
	}


	public Optional<IPassTarget> getPassTarget()
	{
		return Optional.ofNullable(pass).map(p -> new PassTarget(p.getKick().getTarget(), p.getReceiver()));
	}


	public static OffensiveAction buildProtect(
			@NonNull DribbleToPos dribbleToPos)
	{
		return OffensiveAction.builder()
				.type(EOffensiveActionType.PROTECT)
				.dribbleToPos(dribbleToPos)
				.build();
	}


	public static OffensiveAction buildChopTrick()
	{
		return OffensiveAction.builder()
				.type(EOffensiveActionType.CHOP_TRICK)
				.build();
	}


	public static OffensiveAction buildPass(
			@NonNull Pass pass)
	{
		return OffensiveAction.builder()
				.pass(pass)
				.kick(pass.getKick())
				.ballContactPos(pass.getKick().getSource())
				.type(EOffensiveActionType.PASS)
				.build();
	}


	public static OffensiveAction buildKick(
			@NonNull Kick kick)
	{
		return OffensiveAction.builder()
				.kick(kick)
				.ballContactPos(kick.getSource())
				.type(EOffensiveActionType.KICK)
				.build();
	}


	public static OffensiveAction buildDribbleKick(
			@NonNull Kick kick,
			@NonNull DribbleToPos dribbleToPos)
	{
		return OffensiveAction.builder()
				.kick(kick)
				.type(EOffensiveActionType.DRIBBLE_KICK)
				.dribbleToPos(dribbleToPos)
				.build();
	}


	public static OffensiveAction buildReceive(
			IVector2 ballContactPos)
	{
		return OffensiveAction.builder()
				.type(EOffensiveActionType.RECEIVE)
				.ballContactPos(ballContactPos)
				.build();
	}
}
