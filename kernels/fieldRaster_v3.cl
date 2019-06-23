	__kernel void fieldRaster_v3(__global const int *botPos,
					 __global const float *botSpeed,
                     __global const int *parameter,
                     __global float *dstArray)
        {

  		/* Parameter Beschreibung:
        parameter[0]=m (rows)
        parameter[1]=n (colums)
        parameter[2]=k (Laenge des Arrays von botposition)
        parameter[2]=ourBot  (wie viele der übgerbeen bots gehören uns)
        botPos[n]: gibt die ID des Arrays an in dem ein Bot steht
       
        goal: ist das Ziel-Array welches parallel erarbeitet werden soll, jedes work-item berechnet 1-stelle
        */

            int gid = get_global_id(0);
      		dstArray[gid]=0.0;
      		
            int m=parameter[0];
            int n=parameter[1];
            int k=parameter[2];
            int ourBot=parameter[3];
            
 			float shortestTiger = MAXFLOAT;
			float shortestFoe = MAXFLOAT;
			for (int i = 0; i < 12; i++)
			{
				/* m koordinate des goalArray */
				/* n koordinate des goalArray */
				float goalArray_n = 0;
				float goalArray_m = gid;
				if (gid >= n)
				{
					goalArray_n = gid / n;
					goalArray_m = gid - (goalArray_n * n);
				}
				
				// Geschwindigkeitsvektordes Roboters
				float vX = botSpeed[2 * i];
				float vY = botSpeed[(2 * i) + 1];
				float vS = (float) sqrt((vX * vX) + (vY * vY));
				
				/* ID des Rechtecks in dem der Bot steht */
				/* n koordinate des Rechteks */
				/* m koordinate des Rechtecks */
				int recID = botPos[i];
				float rec_n = 0;
				float rec_m = recID;
				if (recID >= n)
				{
					rec_n = recID / n;
					rec_m = recID - (rec_n * n);
				}
				
				if (vS > 0.0001)
				{
					float a = (float) acos(vX / (sqrt((vX * vX) + (vY * vY))));
					// Fallunterscheidung ob Negativ oder Positv Rotiertwerden muss
					if (vY > 0)
					{
						a = -a;
					}
					float rotMatrix[9]; 
					rotMatrix[0]=(float) cos(a);
					rotMatrix[1]=(float) -sin(a);
					rotMatrix[2]=-rec_n;
					rotMatrix[3]=(float) sin(a);
					rotMatrix[4]=(float) cos(a);
					rotMatrix[5]=-rec_m;
					rotMatrix[6]=0;
					rotMatrix[7]=0;
					rotMatrix[8]=1;
					
					float temp = rec_n;
					rec_n = (rotMatrix[0] * rec_n) + (rotMatrix[1] * rec_m) + (rotMatrix[2] * 1);
					rec_m = (rotMatrix[3] * temp) + (rotMatrix[4] * rec_m) + (rotMatrix[5] * 1);
					temp = goalArray_n;
					goalArray_n = (rotMatrix[0] * goalArray_n) + (rotMatrix[1] * goalArray_m) + (rotMatrix[2] * 1);
					goalArray_m = (rotMatrix[3] * temp) + (rotMatrix[4] * goalArray_m) + (rotMatrix[5] * 1);
				}
				// --------
				
				if (vS <= 0.0001)
				{
					vS = 0.0001f;
				}
				
				float s_neg[4]; 
				s_neg[0]=50 + (20 * vS);
				s_neg[1]=0;
				s_neg[2]=0;
				s_neg[3]=50 - (25 * vS);
				if(s_neg[3]<=1)
				 s_neg[3]=1;
				 if(s_neg[0]>75)
				 	s_neg[0]=75;
				
				float rVec[2];
				rVec[0]=rec_n - goalArray_n;
				rVec[1]=rec_m - goalArray_m;
				
				float x = (rVec[0] * s_neg[0]) + (rVec[1] * s_neg[2]);
				float y = (rVec[0] * s_neg[1]) + (rVec[1] * s_neg[3]);
				
				float temp = (float) sqrt((x * rVec[0]) + (y * rVec[1]));
				

				temp = ((-1.f / 7.5f) * temp * temp) + 1000.f;
				if (temp < 0)
				{
					temp = 0;
				}
	
				if (i < ourBot)
				{
					dstArray[gid] -= temp;
				} else
				{
					dstArray[gid] += temp;
				}
	
	}

};