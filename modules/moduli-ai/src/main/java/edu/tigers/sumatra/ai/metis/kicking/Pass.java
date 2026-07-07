package edu.tigers.sumatra.ai.metis.kicking;

import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;


@Value
@AllArgsConstructor
public class Pass
{
	EPassType type;
	Kick kick;
	BotID receiver;
	BotID shooter;
	double receivingBallSpeed;
	double duration;
	double opponentPreparationTime;
	EBallReceiveMode receiveMode;


	public List<IDrawableShape> createDrawables()
	{
		List<IDrawableShape> kickDrawables = kick.createDrawables();
		List<IDrawableShape> shapes = new ArrayList<>(kickDrawables.size() + 1);
		shapes.addAll(kickDrawables);
		String msg = String.format(
				"pass: %.1f+%.1fs %s -> %s %.1fm/s %s", duration, opponentPreparationTime, shooter, receiver,
				receivingBallSpeed, receiveMode
		);
		shapes.add(new DrawableAnnotation(kick.getTarget(), msg).withOffset(Vector2f.fromX(100)));
		return shapes;
	}
}
