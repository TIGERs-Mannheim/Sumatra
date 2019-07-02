package edu.tigers.sumatra.ai.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.snapshot.SnapObject;
import edu.tigers.sumatra.snapshot.Snapshot;


public class SnapshotGenerator
{
	
	public static void main(String[] args)
	{
		Snapshot snapshot = randomNear32Bots(0);
		System.out.println(snapshot.toJSON());
		snapshot = random16vs16Bots(0);
		System.out.println(snapshot.toJSON());
	}
	
	
	public static Snapshot randomNear32Bots(long seed)
	{
		Random rnd = new Random(seed);
		IRectangle field = Geometry.getFieldHalfTheir().withMargin(-1000);
		final BotID chosenBotId = BotID.createBotId(0, ETeamColor.YELLOW);
		
		SnapObject ball = new SnapObject(Vector3.from2d(field.getRandomPointInShape(rnd), 0), Vector3.zero());
		
		Map<BotID, SnapObject> bots = new HashMap<>();
		List<IVector2> botPositions = new ArrayList<>();
		for (BotID botID : BotID.getAll())
		{
			if (botID == chosenBotId)
			{
				continue;
			}
			while (true)
			{
				IVector2 p = field.getRandomPointInShape(rnd);
				if (botPositions.stream().allMatch(bp -> bp.distanceTo(p) > 250))
				{
					botPositions.add(p);
					SnapObject snapObject = new SnapObject(Vector3.from2d(p, 0), Vector3.zero());
					bots.put(botID, snapObject);
					break;
				}
			}
		}
		
		SnapObject snapObject = new SnapObject(
				Vector3.fromXY(Geometry.getFieldLength() / 2, Geometry.getFieldWidth() / 2), Vector3.zero());
		bots.put(chosenBotId, snapObject);
		
		return new Snapshot(bots, ball);
	}
	
	
	public static Snapshot random16vs16Bots(long seed)
	{
		Random rnd = new Random(seed);
		
		SnapObject ball = new SnapObject(
				Vector3.fromXYZ(0, rnd.nextDouble() * Geometry.getFieldWidth() - Geometry.getFieldWidth() / 2, 0),
				Vector3.zero());
		
		double maxX = Geometry.getFieldLength() / 2 - Geometry.getBotRadius() - Geometry.getPenaltyAreaDepth();
		double maxY = Geometry.getFieldWidth() / 2 - Geometry.getBotRadius();
		
		Map<BotID, SnapObject> bots = new HashMap<>();
		List<IVector2> botPositions = new ArrayList<>();
		for (BotID botID : BotID.getAll())
		{
			int inv = botID.getTeamColor() == ETeamColor.YELLOW ? -1 : 1;
			while (true)
			{
				double x = rnd.nextDouble() * maxX * inv;
				double y = rnd.nextDouble() * maxY * 2 - maxY;
				IVector2 p = Vector2.fromXY(x, y);
				if (botPositions.stream().allMatch(bp -> bp.distanceTo(p) > Geometry.getBotRadius() * 2 + 10))
				{
					botPositions.add(p);
					SnapObject snapObject = new SnapObject(Vector3.from2d(p, 0), Vector3.zero());
					bots.put(botID, snapObject);
					break;
				}
			}
		}
		
		return new Snapshot(bots, ball);
	}
}
