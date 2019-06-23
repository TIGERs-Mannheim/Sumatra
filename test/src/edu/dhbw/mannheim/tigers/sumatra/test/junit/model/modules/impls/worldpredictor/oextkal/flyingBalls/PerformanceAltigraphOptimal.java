package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.worldpredictor.oextkal.flyingBalls;


import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Altigraph;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.Def;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.flyingBalls.PredBall;



public class PerformanceAltigraphOptimal
{
	protected long				startMillis;
	protected long				endMillis;
	
	
	@Test
	public void test()
	{
		startMillis = System.currentTimeMillis();
		
		int size = 50;
		Matrix A = new Matrix(size,3);
		
		int redo = (int) Math.pow(10,3);
		
		for(int el = 0; el < redo; el++)
		{
			//System.out.println(el);
			Altigraph a = new Altigraph();

			a.addKickerZoneIdentified(Def.t.roboPos.x(), Def.t.roboPos.y(),
				Def.t.viewAngle);

			for (int i = 0; i < Def.t.size; i++)
			{
				//System.out.println(a.toString());
				a.addCamFrame(Def.t.ballBottom[i].x(), Def.t.ballBottom[i].y(), Def.t.camID);

				PredBall ball = a.getCorrectedFrame();
			
				int row = el%size;
				
				int updown = 0;
				if(el%2 == 0)
					updown = 1;
				else
					updown = -1;
	
				A.set(row, 0, ball.x()+updown*A.get(row,0));
				A.set(row, 1, ball.y()+updown*A.get(row,1));
				A.set(row, 2, ball.z()+updown*A.get(row,2));
			}
		}
		
		endMillis = System.currentTimeMillis();
		//long diff = endMillis - startMillis;

		//System.out.println(A);
		
		//System.out.println("Time: " + diff+" entspricht "+((double)redo)/((double)diff)+" durchläufen pro ms mit 6 bällen");
	}
}
