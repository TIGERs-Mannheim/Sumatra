/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.collision;

import static edu.tigers.sumatra.geometry.Geometry.getBallRadius;
import static edu.tigers.sumatra.geometry.Geometry.getBotRadius;
import static java.lang.Math.acos;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Optional;

import org.junit.Test;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class BotCollisionTest
{
	
	@Test
	public void testCollision()
	{
		IVector3 pos = Vector3.zero();
		IVector3 vel = Vector3.zero();
		double center2Dribbler = 75;
		double obsRadius = getBotRadius() + getBallRadius();
		double obsFront = center2Dribbler + getBallRadius();
		BotID botID = BotID.createBotId(1, ETeamColor.YELLOW);
		BotCollision colHandler = new BotCollision(pos, vel, center2Dribbler, botID);
		
		Optional<ICollision> collision;
		
		collision = colHandler.getCollision(Vector3.fromXY(0, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isNotPresent();
		
		collision = colHandler.getCollision(Vector3.fromXY(-120, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(-obsRadius, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(-1, 0));
		
		collision = colHandler.getCollision(Vector3.fromXY(-obsRadius, 0), Vector3.fromXY(-obsRadius, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(-obsRadius, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(-1, 0));
		
		collision = colHandler.getCollision(Vector3.fromXY(-120, 0), Vector3.fromXY(-119, 0));
		assertThat(collision).isNotPresent();
		
		collision = colHandler.getCollision(Vector3.fromXY(0, -120), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(0, -obsRadius));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(0, -1));
		
		collision = colHandler.getCollision(Vector3.fromXY(120, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		
		collision = colHandler.getCollision(Vector3.fromXY(obsFront, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		
		collision = colHandler.getCollision(Vector3.fromXY(obsFront, 0), Vector3.fromXY(obsFront, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		
		collision = colHandler.getCollision(Vector3.fromXY(obsFront + 1, 1), Vector3.fromXY(obsFront, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		
		collision = colHandler.getCollision(Vector3.fromXY(obsFront + 1, 1), Vector3.fromXY(obsFront - 1, -1));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		
		collision = colHandler.getCollision(Vector3.fromXY(obsFront + 1, 2), Vector3.fromXY(obsFront - 1, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 1));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		
		double theta = acos((center2Dribbler + getBallRadius()) / (getBallRadius() + getBotRadius())) + 0.001;
		IVector2 mostInnerCircleColPre = Vector2.fromAngle(theta).scaleTo(120);
		
		collision = colHandler.getCollision(Vector3.from2d(mostInnerCircleColPre, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos().subtractNew(Vector2.fromAngle(theta).scaleTo(obsRadius)).getLength2())
				.isCloseTo(0, within(0.1));
		assertThat(collision.get().getNormal().getAngle()).isEqualTo(theta, within(0.001));
		
		double theta2 = theta - 0.01;
		IVector2 outerLineColPre = Vector2.fromAngle(theta2).scaleTo(120);
		collision = colHandler.getCollision(Vector3.from2d(outerLineColPre, 0), Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos().x()).isEqualTo(obsFront);
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromX(1));
	}
	
	
	@Test
	public void testInsideCollision()
	{
		IVector3 pos = Vector3.zero();
		IVector3 vel = Vector3.zero();
		double center2Dribbler = 75;
		double obsRadius = getBotRadius() + getBallRadius();
		double obsFront = center2Dribbler + getBallRadius();
		BotID botID = BotID.createBotId(1, ETeamColor.YELLOW);
		BotCollision colHandler = new BotCollision(pos, vel, center2Dribbler, botID);
		
		Optional<ICollision> collision;
		
		collision = colHandler.getInsideCollision(Vector3.fromXY(0, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		
		collision = colHandler.getInsideCollision(Vector3.fromXY(10, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(obsFront, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		
		collision = colHandler.getInsideCollision(Vector3.fromXY(-10, 0));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos()).isEqualTo(Vector2.fromXY(-obsRadius, 0));
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(-1, 0));
		
		collision = colHandler.getInsideCollision(Vector3.fromXY(obsFront - 1, 10));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos().x()).isEqualTo(obsFront);
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		
		collision = colHandler.getInsideCollision(Vector3.fromXY(obsFront - 1, -10));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos().x()).isEqualTo(obsFront);
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		
		collision = colHandler.getInsideCollision(Vector3.fromXY(obsFront - 1, 30));
		assertThat(collision).isPresent();
		assertThat(collision.get().getPos().x()).isEqualTo(obsFront);
		assertThat(collision.get().getNormal().normalizeNew()).isEqualTo(Vector2.fromXY(1, 0));
		
		collision = colHandler.getInsideCollision(Vector3.fromXY(obsFront + 1, 0));
		assertThat(collision).isNotPresent();
		
		collision = colHandler.getInsideCollision(Vector3.fromXY(obsFront + 1, 10));
		assertThat(collision).isNotPresent();
		
		collision = colHandler.getInsideCollision(Vector3.fromXY(obsFront + 1, -10));
		assertThat(collision).isNotPresent();
	}
}