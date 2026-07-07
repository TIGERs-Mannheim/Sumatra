package edu.tigers.sumatra.sim.collision.ball;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static edu.tigers.sumatra.geometry.Geometry.getBallRadius;
import static edu.tigers.sumatra.geometry.Geometry.getBotRadius;
import static java.lang.Math.acos;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
class BotCollisionObjectTest
{
	private double obsRadius;
	private double obsFront;
	private double center2Dribbler;
	private BotCollisionObject colHandler;


	@BeforeEach
	void setUp()
	{
		center2Dribbler = 75;
		obsRadius = getBotRadius() + getBallRadius();
		obsFront = center2Dribbler + getBallRadius();
		var pos = Pose.zero();
		var vel = Vector3.zero();
		var botID = BotID.createBotId(1, ETeamColor.YELLOW);
		colHandler = new BotCollisionObject(pos, vel, center2Dribbler, botID);
	}


	@Test
	void testNoCollisionAtCenter()
	{
		var collision = colHandler.getCollision(Vector3.fromXY(0, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isNotPresent();
	}


	@Test
	void testCollisionFromBehindToCenter()
	{
		var collision = colHandler.getCollision(Vector3.fromXY(-120, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(-obsRadius, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(-1, 0));
	}


	@Test
	void testCollisionAtBackSurface()
	{
		var collision = colHandler.getCollision(Vector3.fromXY(-obsRadius, 0), Vector3.fromXY(-obsRadius, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(-obsRadius, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(-1, 0));
	}


	@Test
	void testNoCollisionMovingOutward()
	{
		var collision = colHandler.getCollision(Vector3.fromXY(-120, 0), Vector3.fromXY(-119, 0));
		assertThat(collision).isNotPresent();
	}


	@Test
	void testCollisionFromBelow()
	{
		var collision = colHandler.getCollision(Vector3.fromXY(0, -120), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(0, -obsRadius));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(0, -1));
	}


	@Test
	void testCollisionFromFront()
	{
		var collision = colHandler.getCollision(Vector3.fromXY(120, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
	}


	@Test
	void testCollisionAtFrontSurfaceToCenter()
	{
		var collision = colHandler.getCollision(Vector3.fromXY(obsFront, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
	}


	@Test
	void testCollisionAtFrontSurfaceStationary()
	{
		var collision = colHandler.getCollision(Vector3.fromXY(obsFront, 0), Vector3.fromXY(obsFront, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
	}


	@Test
	void testCollisionSlightlyPastFront()
	{
		var collision = colHandler.getCollision(Vector3.fromXY(obsFront + 1, 1), Vector3.fromXY(obsFront, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
	}


	@Test
	void testCollisionCrossingFront()
	{
		var collision = colHandler.getCollision(Vector3.fromXY(obsFront + 1, 1), Vector3.fromXY(obsFront - 1, -1));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
	}


	@Test
	void testCollisionCrossingFrontWithOffset()
	{
		var collision = colHandler.getCollision(Vector3.fromXY(obsFront + 1, 2), Vector3.fromXY(obsFront - 1, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 1));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
	}


	@Test
	void testCollisionAtInnerCircleBoundary()
	{
		double theta = acos(center2Dribbler / getBotRadius()) + 0.001;
		IVector2 mostInnerCircleColPre = Vector2.fromAngle(theta).scaleTo(120);

		var collision = colHandler.getCollision(Vector3.from2d(mostInnerCircleColPre, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos().subtractNew(Vector2.fromAngle(theta).scaleTo(obsRadius)).getLength2())
				.isCloseTo(0, within(0.1));
		assertThat(collision.get().getNormal().getAngle()).isEqualTo(theta, within(0.001));
	}


	@Test
	void testCollisionAtOuterLineBoundary()
	{
		double theta = acos(center2Dribbler / getBotRadius()) + 0.001;
		double theta2 = theta - 0.01;
		IVector2 outerLineColPre = Vector2.fromAngle(theta2).scaleTo(120);

		var collision = colHandler.getCollision(Vector3.from2d(outerLineColPre, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos().x()).isCloseTo(obsFront, within(1e-5));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromX(1));
	}


	@Test
	void testInsideCollisionAtCenter()
	{
		var collision = colHandler.getInsideCollision(Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
	}


	@Test
	void testInsideCollisionSlightlyForward()
	{
		var collision = colHandler.getInsideCollision(Vector3.fromXY(10, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
	}


	@Test
	void testInsideCollisionSlightlyBackward()
	{
		var collision = colHandler.getInsideCollision(Vector3.fromXY(-10, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(-obsRadius, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(-1, 0));
	}


	@Test
	void testInsideCollisionNearFrontWithPositiveOffset()
	{
		var collision = colHandler.getInsideCollision(Vector3.fromXY(obsFront - 1, 10));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos().x()).isCloseTo(obsFront, within(1e-5));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
	}


	@Test
	void testInsideCollisionNearFrontWithNegativeOffset()
	{
		var collision = colHandler.getInsideCollision(Vector3.fromXY(obsFront - 1, -10));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos().x()).isCloseTo(obsFront, within(1e-5));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
	}


	@Test
	void testInsideCollisionNearFrontWithLargeOffset()
	{
		var collision = colHandler.getInsideCollision(Vector3.fromXY(obsFront - 1, 30));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos().x()).isCloseTo(obsFront, within(1e-5));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
	}


	@Test
	void testNoInsideCollisionBeyondFront()
	{
		var collision = colHandler.getInsideCollision(Vector3.fromXY(obsFront + 1, 0));
		assertThat(collision).isNotPresent();
	}


	@Test
	void testNoInsideCollisionBeyondFrontWithPositiveOffset()
	{
		var collision = colHandler.getInsideCollision(Vector3.fromXY(obsFront + 1, 10));
		assertThat(collision).isNotPresent();
	}


	@Test
	void testNoInsideCollisionBeyondFrontWithNegativeOffset()
	{
		var collision = colHandler.getInsideCollision(Vector3.fromXY(obsFront + 1, -10));
		assertThat(collision).isNotPresent();
	}
}