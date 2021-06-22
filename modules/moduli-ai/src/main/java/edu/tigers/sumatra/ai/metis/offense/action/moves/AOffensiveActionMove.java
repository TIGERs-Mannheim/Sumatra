/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IShapeLayer;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Base class for all action moves
 */
public abstract class AOffensiveActionMove
{
	@Getter(AccessLevel.PROTECTED)
	private BaseAiFrame aiFrame;

	@Setter
	private double scoreMultiplier = 1;


	public final void update(BaseAiFrame aiFrame)
	{
		this.aiFrame = aiFrame;
	}


	public abstract OffensiveAction calcAction(BotID botId);


	protected double applyMultiplier(double baseScore)
	{
		return SumatraMath.cap(baseScore * scoreMultiplier, 0, 1);
	}


	protected Optional<RatedPass> findPassForMe(Map<KickOrigin, RatedPass> passes, BotID botID)
	{
		return passes.entrySet().stream()
				.filter(k -> k.getKey().getShooter() == botID || k.getKey().getShooter().isUninitializedID())
				.map(Map.Entry::getValue)
				.findAny();
	}


	protected final WorldFrame getWFrame()
	{
		return getAiFrame().getWorldFrame();
	}


	protected final ITrackedBall getBall()
	{
		return getAiFrame().getWorldFrame().getBall();
	}


	protected final List<IDrawableShape> getShapes(IShapeLayer shapeLayer)
	{
		return getAiFrame().getShapeMap().get(shapeLayer);
	}
}
