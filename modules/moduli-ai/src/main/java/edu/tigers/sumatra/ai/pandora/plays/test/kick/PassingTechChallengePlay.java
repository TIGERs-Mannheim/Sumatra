/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.test.kick;


import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import lombok.Setter;
import org.apache.commons.lang.Validate;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Pass in a circle, while robots are optionally moving on the circle.
 * Robots pass to their opponents.
 */
public class PassingTechChallengePlay extends ARedirectPlay
{
	@Setter
	private IVector2 center;
	@Setter
	private double radius;

	private static Map<Integer, Integer> map4 = new HashMap<>();
	private static Map<Integer, Integer> map6 = new HashMap<>();
	private static Map<Integer, Integer> map8 = new HashMap<>();


	static
	{
		map4.put(0, 2); // 2
		map4.put(2, 1); // 1
		map4.put(1, 3); // 2
		map4.put(3, 0); // 1

		map6.put(0, 2); // 2
		map6.put(2, 4); // 2
		map6.put(4, 1); // 3
		map6.put(1, 5); // 4
		map6.put(5, 3); // 2
		map6.put(3, 0); // 3

		map8.put(0, 3); // 3
		map8.put(3, 6); // 3
		map8.put(6, 1); // 3
		map8.put(1, 4); // 3
		map8.put(4, 7); // 3
		map8.put(7, 2); // 3
		map8.put(2, 5); // 3
		map8.put(5, 0); // 3
	}

	public PassingTechChallengePlay()
	{
		super(EPlay.PASSING_TECH_CHALLENGE);
		setMaxDistBall2PassStart(10_000);
	}


	@Override
	protected boolean ready()
	{
		return super.ready()
				&& getAiFrame().getRefereeMsg().getCommand() == SslGcRefereeMessage.Referee.Command.FORCE_START;
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();

		var shapes = getAiFrame().getShapeMap().get(EAiShapesLayer.TEST_KICK);
		shapes.add(new DrawableCircle(Circle.createCircle(center, radius), Color.green));
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		if (ready())
		{
			super.doUpdateBeforeRoles();
		} else
		{
			findOtherRoles(MoveRole.class).forEach(r -> switchRoles(r, new MoveRole()));
			double offset = Geometry.getCenterCircle().radius();
			double step = Geometry.getBotRadius() * 4;
			for (MoveRole role : findRoles(MoveRole.class))
			{
				role.updateDestination(Vector2.fromY(offset));
				role.updateLookAtTarget(Vector2.zero());
				if (offset > 0)
				{
					offset *= -1;
				} else
				{
					offset = -offset + step;
				}
			}
		}
	}


	@Override
	protected List<IVector2> getOrigins()
	{
		double angleStep = AngleMath.PI_TWO / getRoles().size();

		List<IVector2> destinations = new ArrayList<>();
		for (int i = 0; i < getRoles().size(); i++)
		{
			destinations.add(center.addNew(Vector2.fromAngle((angleStep * i)).scaleTo(radius)));
		}
		return destinations;
	}


	@Override
	protected IVector2 getReceiverTarget(IVector2 origin)
	{
		var currentIdx = origins.indexOf(origin);
		Validate.isTrue(currentIdx >= 0, "Idx must be >=0: ", currentIdx);
		var targetIdx = getTargetIdx(currentIdx);
		var angleStep = AngleMath.PI_TWO / getRoles().size();
		return center.addNew(Vector2.fromAngle((angleStep * targetIdx)).scaleTo(radius));
	}


	@Override
	protected IVector2 getReceiverCatchPoint(IVector2 origin)
	{
		var circle = Circle.createCircle(center, radius);
		var travelLineSegment = getBall().getTrajectory().getTravelLineSegment();
		var intersections = circle.lineIntersections(travelLineSegment);
		return origin.nearestToOpt(intersections).orElse(origin);
	}


	@Override
	protected BotID getReceiverBot(IVector2 origin)
	{
		var currentIdx = origins.indexOf(origin);
		Validate.isTrue(currentIdx >= 0, "Idx must be >=0: ", currentIdx);
		var targetIdx = getTargetIdx(currentIdx);
		var target = origins.get(targetIdx);
		return originToBotIdMap.get(target);
	}


	@Override
	protected EReceiveMode getReceiveMode(IVector2 origin)
	{
		return EReceiveMode.REDIRECT;
	}


	@Override
	protected IVector2 getBallPlacementPos(BotID botID)
	{
		return Geometry.getField().nearestPointInside(getBall().getPos(), -200);
	}


	private int getTargetIdx(final int currentIdx)
	{
		switch (getRoles().size())
		{
			case 1:
				return currentIdx;
			case 2:
				// next bot
				return (currentIdx + 1) % getRoles().size();
			case 4:
				return map4.get(currentIdx);
			case 6:
				return map6.get(currentIdx);
			case 8:
				return map8.get(currentIdx);
			case 3:
			case 5:
			case 7:
			default:
				// opposite bot
				return (currentIdx + (getRoles().size() / 2)) % getRoles().size();
		}
	}
}
