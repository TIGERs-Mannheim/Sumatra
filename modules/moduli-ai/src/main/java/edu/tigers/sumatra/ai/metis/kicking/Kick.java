/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.kicking;

import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;


@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Kick
{
	@NonNull
	IVector2 source;
	@NonNull
	IVector2 target;
	@NonNull
	KickParams kickParams;
	@NonNull
	IVector3 kickVel;
	double aimingTolerance;


	Kick()
	{
		source = Vector2.zero();
		target = Vector2.zero();
		kickParams = KickParams.disarm();
		aimingTolerance = 0;
		kickVel = Vector3.zero();
	}


	public List<IDrawableShape> createDrawables()
	{
		List<IDrawableShape> shapes = new ArrayList<>(2);
		var direction = target.subtractNew(source);
		var lineCenter = source.addNew(direction.multiplyNew(0.5));
		var deviceId = kickParams.getDevice().name().charAt(0);
		shapes.add(new DrawableArrow(source, direction));
		String msg = String.format("kick: %.1fm/s %s (%.2f)", kickParams.getKickSpeed(), deviceId, aimingTolerance);
		shapes.add(new DrawableAnnotation(lineCenter, msg).withOffset(Vector2f.fromX(100)));
		return shapes;
	}
}
