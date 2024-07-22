/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.kicking;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;


@Persistent
@Value
@AllArgsConstructor
public class Pass
{
	Kick kick;
	BotID receiver;
	BotID shooter;
	double receivingSpeed;
	double duration;
	double preparationTime;
	EBallReceiveMode receiveMode;


	@SuppressWarnings("unused")
		// berkeley
	Pass()
	{
		kick = new Kick();
		receiver = null;
		shooter = null;
		receivingSpeed = 0;
		duration = 0;
		preparationTime = 0;
		receiveMode = EBallReceiveMode.RECEIVE;
	}


	public boolean isChip()
	{
		return kick.getKickVel().z() > 0;
	}


	public List<IDrawableShape> createDrawables()
	{
		List<IDrawableShape> kickDrawables = kick.createDrawables();
		List<IDrawableShape> shapes = new ArrayList<>(kickDrawables.size() + 1);
		shapes.addAll(kickDrawables);
		String msg = String.format("pass: %.1f+%.1fs %s -> %s %.1fm/s %s", duration, preparationTime, shooter, receiver,
				receivingSpeed, receiveMode);
		shapes.add(new DrawableAnnotation(kick.getTarget(), msg).withOffset(Vector2f.fromX(100)));
		return shapes;
	}
}
