__kernel void fieldRaster_v1(__global const int *botPos,
                     __global const int *parameter,
                     __global float *goal)
        {
        
	    /* Parameter Beschreibung:
        paramet[0]=m (rows)
        parameter[1]=n (colums)
        parameter[2]=k (Laenge des Arrays von botposition)
        parameter[3]=botOurCount (Laenge des Arrays von botposition)
        
        botPos[n]: gibt die ID des Arrays an in dem ein Bot steht
       
        goal: ist das Ziel-Array welches parallel erarbeitet werden soll, jedes work-item berechnet 1-stelle*/

            int gid = get_global_id(0);
      		goal[gid]=0.0f;
      		
            int m=parameter[0];
            int n=parameter[1];
            int k=parameter[2];
            int botOurCount=parameter[3];
            
            float shortestTiger = MAXFLOAT;
			float shortestFoe = MAXFLOAT;
            for(int i=0;i<k;++i)
            {
            	
         	   /* ID des Rechtecks */
				int recID = botPos[i];
				/* m koordinate des Rechtecks */
				int rec_n = 0;
				/* n koordinate des Rechteks */
				int rec_m = recID;
				if (recID >= n)
				{
					rec_n = recID / n;
					rec_m = recID - (rec_n * n);
				}
				/* m koordinate des goalArray */
				/* n koordinate des goalArray */
				int goalArray_n = 0;
				int goalArray_m = gid;
				if (gid >= n)
				{
					goalArray_n = gid / n;
					goalArray_m = gid - (goalArray_n * n);
				}
            
         	    int x = rec_m - goalArray_m;
				int y = rec_n - goalArray_n;
            	
            	/* Berechnen des Abstandes von goalArray und recPos*/
            	float value=(sqrt((float)(x*x+y*y)));
            	
				if (i < botOurCount)
				{
					if (value < shortestTiger)
					{
						shortestTiger = value;
					}
				} else
				{
					if (value < shortestFoe)
					{
						shortestFoe = value;
					}
				}
			goal[gid] = shortestFoe - shortestTiger;
			}
};