/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.kicking;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;


@Persistent
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Pass
{
	@NonNull
	Kick kick;
	@NonNull
	BotID receiver;
	@NonNull
	BotID shooter;
	double receivingSpeed;
	double duration;


	@SuppressWarnings("unused") // berkeley
	Pass()
	{
		kick = new Kick();
		receiver = BotID.noBot();
		shooter = BotID.noBot();
		receivingSpeed = 0;
		duration = 0;
	}


	public List<IDrawableShape> createDrawables()
	{
		List<IDrawableShape> kickDrawables = kick.createDrawables();
		List<IDrawableShape> shapes = new ArrayList<>(kickDrawables.size() + 1);
		shapes.addAll(kickDrawables);
		String msg = String.format("pass: %.1fs -> %s %.1fm/s", duration, receiver, receivingSpeed);
		shapes.add(new DrawableAnnotation(kick.getTarget(), msg).withOffset(Vector2f.fromX(100)));
		return shapes;
	}
}
