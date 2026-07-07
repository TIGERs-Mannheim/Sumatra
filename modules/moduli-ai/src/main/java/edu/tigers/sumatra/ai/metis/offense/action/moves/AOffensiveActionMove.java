package edu.tigers.sumatra.ai.metis.offense.action.moves;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Base class for all action moves
 */
@Log4j2
public abstract class AOffensiveActionMove
{

	@Getter(AccessLevel.PROTECTED)
	private BaseAiFrame aiFrame;

	// holds shapes that need to be drawn later by the drawScheduled()-Method.
	// This needs to be done when processing in parallel. In that case, use scheduleDraw() instead of getShapes.add()
	private Map<IShapeLayerIdentifier, List<IDrawableShape>> shapeBuffer = new HashMap<>();

	@Setter
	private double scoreMultiplier = 1;


	public final void update(BaseAiFrame aiFrame)
	{
		this.aiFrame = aiFrame;
	}


	public abstract Optional<RatedOffensiveAction> calcAction(BotID botId);


	protected double applyMultiplier(double baseScore)
	{
		return SumatraMath.cap(baseScore * scoreMultiplier, 0, 1);
	}


	protected Optional<RatedPass> findPassForMe(Map<KickOrigin, RatedPass> passes, BotID botID)
	{
		return passes.entrySet().stream()
				.filter(k -> k.getKey().getShooter().equals(botID) || k.getKey().getShooter().isUninitializedID())
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


	/**
	 * This getShapes is a little special.
	 * Instead of directly drawing the shapes, it puts them into a buffer to be drawn later
	 * to actually draw them, you need to call drawScheduled()
	 *
	 * @param shapeLayer
	 * @return
	 */
	protected final List<IDrawableShape> getShapes(IShapeLayerIdentifier shapeLayer)
	{
		// if the list is currently not there, create it
		shapeBuffer.putIfAbsent(shapeLayer, new ArrayList<>());
		return shapeBuffer.get(shapeLayer);
	}


	protected double getAntiToggleValue(BotID botId, EOffensiveActionMove currentActionMove, double antiToggleBonus)
	{
		var lastAction = getAiFrame()
				.getPrevFrame()
				.getTacticalField()
				.getOffensiveActions()
				.get(botId);
		if (lastAction != null && lastAction.getMove() == currentActionMove)
		{
			return antiToggleBonus;
		}
		return 0;
	}


	/**
	 * This method takes all buffered shapes, draws them and removes them from the buffer
	 * It should be called after parallel processing has finished, otherwise expect race conditions
	 */
	public void drawScheduled()
	{
		// iterate over the entire buffer
		shapeBuffer.forEach(
				// each element consists of a shape Layer and a list of shapes
				(layer, shapes) ->
						getAiFrame().getShapeMap().get(layer).addAll(shapes)
		);
		shapeBuffer.clear();
	}
}
