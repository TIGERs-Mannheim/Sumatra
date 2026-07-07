package edu.tigers.sumatra.ai.metis.offense.action;

import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.dribble.DribbleToPos;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.ai.metis.pass.target.IPassTarget;
import edu.tigers.sumatra.ai.metis.pass.target.PassTarget;
import edu.tigers.sumatra.math.vector.IVector2;
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
public class OffensiveAction
{
	EOffensiveActionType type;
	RatedPass pass;
	Kick kick;
	IVector2 ballContactPos;
	DribbleToPos dribbleToPos;
	boolean forcePushDuringKick;


	public OffensiveAction()
	{
		this.pass = null;
		this.kick = null;
		this.dribbleToPos = null;
		this.ballContactPos = null;
		this.type = null;
		this.forcePushDuringKick = false;
	}


	public static OffensiveAction buildRedirectKick(
			@NonNull Kick kick
	)
	{
		return OffensiveAction.builder()
				.kick(kick)
				.ballContactPos(kick.getSource())
				.type(EOffensiveActionType.REDIRECT_KICK)
				.forcePushDuringKick(false)
				.build();
	}


	public Optional<Kick> getKickOpt()
	{
		return Optional.ofNullable(pass)
				.map(e -> e.getPass().getKick())
				.or(() -> Optional.ofNullable(kick));
	}


	public Optional<IVector2> getTarget()
	{
		return getKickOpt().map(Kick::getTarget);
	}


	public Optional<IPassTarget> getPassTarget()
	{
		return Optional.ofNullable(pass)
				.map(p -> new PassTarget(p.getPass().getKick().getTarget(), p.getPass().getReceiver()));
	}


	public static OffensiveAction buildProtect(
			@NonNull DribbleToPos dribbleToPos
	)
	{
		return OffensiveAction.builder()
				.type(EOffensiveActionType.PROTECT)
				.dribbleToPos(dribbleToPos)
				.forcePushDuringKick(false)
				.build();
	}

	public static OffensiveAction buildFinisherPass(
			@NonNull DribbleToPos dribbleToPos,
			@NonNull RatedPass pass
	)
	{
		return OffensiveAction.builder()
				.type(EOffensiveActionType.FINISHER_PASS)
				.dribbleToPos(dribbleToPos)
				.pass(pass)
				.build();
	}


	public static OffensiveAction buildPass(
			@NonNull RatedPass pass
	)
	{
		return OffensiveAction.builder()
				.pass(pass)
				.kick(pass.getPass().getKick())
				.ballContactPos(pass.getPass().getKick().getSource())
				.type(EOffensiveActionType.PASS)
				.forcePushDuringKick(false)
				.build();
	}


	public static OffensiveAction buildProtectPass(
			@NonNull RatedPass pass
	)
	{
		return OffensiveAction.builder()
				.pass(pass)
				.kick(pass.getPass().getKick())
				.ballContactPos(pass.getPass().getKick().getSource())
				.type(EOffensiveActionType.PASS)
				.forcePushDuringKick(true)
				.build();
	}


	public static OffensiveAction buildKick(
			@NonNull Kick kick
	)
	{
		return OffensiveAction.builder()
				.kick(kick)
				.ballContactPos(kick.getSource())
				.type(EOffensiveActionType.KICK)
				.forcePushDuringKick(false)
				.build();
	}


	public static OffensiveAction buildDribbleKick(
			@NonNull Kick kick,
			@NonNull DribbleToPos dribbleToPos
	)
	{
		return OffensiveAction.builder()
				.kick(kick)
				.type(EOffensiveActionType.DRIBBLE_KICK)
				.dribbleToPos(dribbleToPos)
				.forcePushDuringKick(false)
				.build();
	}


	public static OffensiveAction buildReceive(
			IVector2 ballContactPos
	)
	{
		return OffensiveAction.builder()
				.type(EOffensiveActionType.RECEIVE)
				.ballContactPos(ballContactPos)
				.forcePushDuringKick(false)
				.build();
	}
}
