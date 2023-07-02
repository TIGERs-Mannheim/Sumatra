/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.integration;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.snapshot.SnapObject;
import edu.tigers.sumatra.snapshot.Snapshot;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class SnapshotGenerator
{
	private Snapshot.SnapshotBuilder snapshotBuilder = Snapshot.builder();
	private int numRobots = 1;


	public SnapshotGenerator()
	{
		snapshotBuilder.ball(new SnapObject(Vector3.zero(), Vector3.zero()));
	}


	public static void main(String[] args) throws IOException
	{
		new SnapshotGenerator().randomNear32Bots(0).writeTo(Paths.get("randomNear32Bots.json"));
		new SnapshotGenerator().random16vs16Bots(0).writeTo(Paths.get("random16vs16Bots.json"));

		Path ppPath = Path.of("modules/sumatra-ai-integration-test/src/integrationTest/resources/snapshots/pathplanning");
		new SnapshotGenerator().forceStart().fromSideToCenter()
				.writeTo(ppPath.resolve("fromSideToCenter.json"));
		new SnapshotGenerator().forceStart().fromSideToCenterCrossed()
				.writeTo(ppPath.resolve("fromSideToCenterCrossed.json"));
		new SnapshotGenerator().forceStart().fromSideToOtherSideCrossed()
				.writeTo(ppPath.resolve("fromSideToOtherSideCrossed.json"));
		new SnapshotGenerator().forceStart().fromFrontToBackCrossed()
				.writeTo(ppPath.resolve("fromFrontToBackCrossed.json"));
	}


	public Snapshot buildSnapshot()
	{
		return snapshotBuilder.build();
	}


	public Snapshot.SnapshotBuilder snapshotBuilder()
	{
		return snapshotBuilder;
	}


	private void writeTo(Path path) throws IOException
	{
		buildSnapshot().save(path);
	}


	public SnapshotGenerator ballPos(IVector2 pos)
	{
		snapshotBuilder.ball(new SnapObject(Vector3.from2d(pos, 0), Vector3.zero()));
		return this;
	}


	public SnapshotGenerator maintenance(ETeamColor teamColor, int numBots)
	{
		double distance = Geometry.getBotRadius() * 4;
		double x = (teamColor == ETeamColor.BLUE ? -1 : 1) * Geometry.getFieldLength() / 4;
		double y = distance / 2;
		double s = 1;
		for (int i = 0; i < numBots; i++)
		{
			snapshotBuilder.bot(BotID.createBotId(i, teamColor),
					new SnapObject(Vector3.fromXYZ(x, s * y, 0), Vector3.zero()));
			if (s < 0)
			{
				y += distance;
			}
			s *= -1;
		}
		return this;
	}


	public SnapshotGenerator randomNear32Bots(long seed)
	{
		Random rnd = new Random(seed);
		IRectangle field = Geometry.getFieldHalfTheir().withMargin(-1000);
		final BotID chosenBotId = BotID.createBotId(0, ETeamColor.YELLOW);

		SnapObject ball = new SnapObject(Vector3.from2d(field.getRandomPointInShape(rnd), 0), Vector3.zero());

		Map<BotID, SnapObject> bots = new HashMap<>();
		List<IVector2> botPositions = new ArrayList<>();
		for (BotID botID : BotID.getAll())
		{
			if (Objects.equals(botID, chosenBotId))
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
		snapshotBuilder.bots(bots).ball(ball);

		return this;
	}


	public SnapshotGenerator random16vs16Bots(long seed)
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

		snapshotBuilder.bots(bots).ball(ball);

		return this;
	}


	private SnapshotGenerator forceStart()
	{
		snapshotBuilder.command(SslGcRefereeMessage.Referee.Command.FORCE_START);
		snapshotBuilder.stage(SslGcRefereeMessage.Referee.Stage.NORMAL_FIRST_HALF);
		return this;
	}


	private List<IVector3> line(int numBots, Pose start, IVector2 direction)
	{
		List<IVector3> positions = new ArrayList<>();
		IVector2 pos = start.getPos();
		for (int i = 0; i < numBots; i++)
		{
			positions.add(Vector3.from2d(pos, start.getOrientation()));
			pos = pos.addNew(direction);
		}
		return positions;
	}


	private Map<BotID, IVector3> mapPositionsToBots(List<IVector3> positions, ETeamColor teamColor)
	{
		AtomicInteger id = new AtomicInteger();
		return positions.stream().collect(Collectors.toMap(
				p -> BotID.createBotId(id.getAndIncrement(), teamColor),
				p -> p
		));
	}


	private Map<BotID, SnapObject> mapToSnapObject(Map<BotID, IVector3> positionMapping)
	{
		return positionMapping.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				e -> new SnapObject(e.getValue(), Vector3.zero())
		));
	}


	private Map<BotID, IVector3> lineOnSide(ETeamColor teamColor, double xSign, double ySign)
	{
		double distance = Geometry.getBotRadius() * 4;
		double x = xSign * (Geometry.getFieldLength() / 2 - distance);
		double y = ySign * (Geometry.getFieldWidth() / 2 - distance);
		Pose start = Pose.from(Vector2.fromXY(x, y), 0);
		List<IVector3> positions = line(numRobots, start, Vector2.fromX(xSign * -distance));
		return mapPositionsToBots(positions, teamColor);
	}


	private Map<BotID, IVector3> lineOnCenter(ETeamColor teamColor, double xSign)
	{
		double distance = Geometry.getBotRadius() * 4;
		double x = xSign * (Geometry.getFieldLength() / 4);
		return lineOnX(teamColor, x, distance);
	}


	private Map<BotID, IVector3> lineOnX(ETeamColor teamColor, double x, double distance)
	{
		double startOffset = numRobots / 2.0 * distance;
		double y = -startOffset;
		Pose start = Pose.from(Vector2.fromXY(x, y), 0);
		List<IVector3> positions = line(numRobots, start, Vector2.fromY(distance));
		return mapPositionsToBots(positions, teamColor);
	}


	private SnapshotGenerator fromSideToCenter()
	{
		snapshotBuilder.bots(mapToSnapObject(lineOnSide(ETeamColor.BLUE, -1, -1)));
		snapshotBuilder.bots(mapToSnapObject(lineOnSide(ETeamColor.YELLOW, 1, -1)));
		snapshotBuilder.moveDestinations(lineOnCenter(ETeamColor.BLUE, -1));
		snapshotBuilder.moveDestinations(lineOnCenter(ETeamColor.YELLOW, 1));
		return this;
	}


	private SnapshotGenerator fromSideToCenterCrossed()
	{
		snapshotBuilder.bots(mapToSnapObject(lineOnSide(ETeamColor.BLUE, -1, -1)));
		snapshotBuilder.bots(mapToSnapObject(lineOnSide(ETeamColor.YELLOW, 1, -1)));
		snapshotBuilder.moveDestinations(lineOnCenter(ETeamColor.BLUE, 1));
		snapshotBuilder.moveDestinations(lineOnCenter(ETeamColor.YELLOW, -1));
		return this;
	}


	private SnapshotGenerator fromSideToOtherSideCrossed()
	{
		snapshotBuilder.bots(mapToSnapObject(lineOnSide(ETeamColor.BLUE, -1, -1)));
		snapshotBuilder.bots(mapToSnapObject(lineOnSide(ETeamColor.YELLOW, 1, -1)));
		snapshotBuilder.moveDestinations(lineOnSide(ETeamColor.BLUE, 1, 1));
		snapshotBuilder.moveDestinations(lineOnSide(ETeamColor.YELLOW, -1, 1));
		return this;
	}


	private SnapshotGenerator fromFrontToBackCrossed()
	{
		double x = Geometry.getFieldLength() / 2 - Geometry.getPenaltyAreaDepth() - 200;
		double distance = 600;
		snapshotBuilder.bots(mapToSnapObject(lineOnX(ETeamColor.BLUE, x, distance)));
		snapshotBuilder.bots(mapToSnapObject(lineOnX(ETeamColor.YELLOW, -x, distance)));
		snapshotBuilder.moveDestinations(lineOnX(ETeamColor.BLUE, -x, distance));
		snapshotBuilder.moveDestinations(lineOnX(ETeamColor.YELLOW, x, distance));
		return this;
	}
}
