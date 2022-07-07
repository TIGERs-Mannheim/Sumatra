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
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;


/**
 * The offensive action gives instructions to the attacker
 */
@Persistent(version = 6)
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OffensiveAction
{
	@NonNull
	EOffensiveActionMove move;
	@NonNull
	OffensiveActionViability viability;

	Pass pass;
	Kick kick;
	IVector2 ballContactPos;
	DribbleToPos dribbleToPos;

	@SuppressWarnings("unused") // used by berkeley
	private OffensiveAction()
	{
		move = EOffensiveActionMove.PROTECT_MOVE;
		pass = null;
		kick = null;
		dribbleToPos = null;
		ballContactPos = null;
		viability = new OffensiveActionViability(EActionViability.FALSE, 0.0);
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
}
