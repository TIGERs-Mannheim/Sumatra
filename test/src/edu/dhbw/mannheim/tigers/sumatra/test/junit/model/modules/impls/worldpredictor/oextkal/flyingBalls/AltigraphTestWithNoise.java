package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Altigraph;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.PredBall;

public class AltigraphTestWithNoise
{
	@Test
	public void test()
	{
		Altigraph a = new Altigraph();

		
		a.addKickerZoneIdentified(Def.t.roboPos.x(), Def.t.roboPos.y(),
				Def.t.viewAngle);

		for (int i = 0; i < Def.t.size; i++)
		{
			//set some noise values
			double noiseFaktor  = 0.05; //10% Schwankung
			if(i== 0)
			{
				noiseFaktor = 0;
			}
			double noiUpDo  = 0;
			if((Math.random()-0.5) > 0)
				noiUpDo = 1;
			else
				noiUpDo = -1;

			//System.out.println(a.toString());
			double xNoise = Def.t.ballBottom[i].x()*(1+noiseFaktor*noiUpDo*Math.random());
			double yNoise = Def.t.ballBottom[i].y()*(1+noiseFaktor*noiUpDo*Math.random());
			
			//System.out.println("AbweichungX: "+xNoise/Def.t.ballBottom[i].x()+" absolut: "+(xNoise-Def.t.ballBottom[i].x()));
			//System.out.println("AbweichungY: "+yNoise/Def.t.ballBottom[i].y()+" absolut: "+(yNoise-Def.t.ballBottom[i].y()));
			
			a.addCamFrame(
					xNoise, 
					yNoise,
					Def.t.camID);
			

			PredBall ball = a.getCorrectedFrame();
			
			// er darf nicht fliegen bis inklusive 3 Bälle: 0-2
			if (i < 3)
			{
				assertFalse(a.isBallFlying());
				
				assertEquals(Def.t.ballBottom[i].x(), ball.x(), 1/noiseFaktor*Math.abs(Def.hund*3*Def.t.ballBottom[i].x()));
				assertEquals(Def.t.ballBottom[i].y(), ball.y(), 1/noiseFaktor*Math.abs(Def.hund*3*Def.t.ballBottom[i].y()));
				assertEquals(0,                       ball.z(), 1);
				
				//System.out.println("fehlerbx: "+Def.t.ballBottom[i].x()/ball.x());
				//System.out.println("fehlerby: "+Def.t.ballBottom[i].y()/ball.y());
			}
			else
			// i >= 4
			{
				assertTrue(a.isBallFlying());

				assertEquals(Def.t.ballFly[i].x(), ball.x(), 1/noiseFaktor*Math.abs(Def.hund*3*Def.t.ballFly[i].x()));
				assertEquals(Def.t.ballFly[i].y(), ball.y(), 1/noiseFaktor*Math.abs(Def.hund*3*Def.t.ballFly[i].y()));
				assertEquals(Def.t.height[i]     , ball.z(), 1/noiseFaktor*Math.abs(3*Def.zehn*Def.t.height[i]));
				
				//System.out.println("fehlerfx: "+Def.t.ballFly[i].x()/ball.x());
				//System.out.println("fehlerfy: "+Def.t.ballFly[i].y()/ball.y());
				//System.out.println("fehlerfz: "+Def.t.height[i]/ball.z());
			}
		}
	}
}
