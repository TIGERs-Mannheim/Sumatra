package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Altigraph;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.PredBall;

import static org.junit.Assert.*;

public class AltigraphTestOptimal
{
	@Test
	public void test()
	{
		Altigraph a = new Altigraph();

		a.addKickerZoneIdentified(Def.t.roboPos.x(), Def.t.roboPos.y(),
				Def.t.viewAngle);

		for (int i = 0; i < Def.t.size; i++)
		{
			//System.out.println(a.toString());
			a.addCamFrame(Def.t.ballBottom[i].x(), Def.t.ballBottom[i].y(), Def.t.camID);

			PredBall ball = a.getCorrectedFrame();
			
			// er darf nicht fliegen bis inklusive 3 Bälle: 0-2
			if (i < 3)
			{
				assertFalse(a.isBallFlying());
				
				assertEquals(Def.t.ballBottom[i].x(), ball.x(), Def.hund*Def.t.ballBottom[i].x());
				assertEquals(Def.t.ballBottom[i].y(), ball.y(), Def.hund*Def.t.ballBottom[i].y());
				assertEquals(0,                       ball.z(), 1);
			}
			else
			// i >= 4
			{
				assertTrue(a.isBallFlying());
				
				assertEquals(Def.t.ballFly[i].x(), ball.x(),  Def.hund*Def.t.ballFly[i].x());
				assertEquals(Def.t.ballFly[i].y(), ball.y(),  Def.hund*Def.t.ballFly[i].y());
				assertEquals(Def.t.height[i]     , ball.z(),  Def.hund*Def.t.height[i]*3);
			}
		}

	}

	@Test
	public void testCamFrameBeforeKickerZone()
	{
		Altigraph a = new Altigraph();

		a.addCamFrame(Def.t.ballBottom[0].x(), Def.t.ballBottom[0].y(),
				Def.t.camID);
	}

}
