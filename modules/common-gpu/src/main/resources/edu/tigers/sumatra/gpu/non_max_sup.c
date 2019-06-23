#define FILTER_SIZE 3

__kernel void non_max_sup(
		__global const 	float *arrIn,
		__global 		float *arrOut)
{
	int idx = get_global_id(1) * get_global_size(0) + get_global_id(0);
	arrOut[idx] = 0;
	//for(int i=-FILTER_SIZE/2; i<=FILTER_SIZE/2;i++)
	{
		int j = 0, i = -1;
		//for(int j=-FILTER_SIZE/2; j<=FILTER_SIZE/2;j++)
		{
			int x = get_global_id(0) + i;
			int y = get_global_id(1) + j;
			if(x < 0 || y < 0 || x >= get_global_size(0) || y >= get_global_size(1))
				return;
			//	continue;
			//if(arrIn[idx] - arrIn[y*get_global_size(0)+x] > 0.0001f)
			if(arrIn[idx] > arrIn[y*get_global_size(0)+x])
			{
				arrOut[idx] = 1+arrIn[y*get_global_size(0)+x] - arrIn[idx];
				//arrOut[idx] = y*get_global_size(0)+x;
				return;
			}
			//arrOut[idx] = arrOut[idx] + 1;
		}
	}
	arrOut[idx] = arrIn[idx];
}
