//#pragma OPENCL EXTENSION cl_khr_byte_addressable_store : enable

#define DEG_TO_RAD 0.017453292

int globalIndex() {
	int gidX = get_global_id(0);
	int gidY = get_global_id(1);
	int gidB = get_global_id(2);

	return gidB * get_global_size(0) * get_global_size(1)
			+ gidY * get_global_size(0) + gidX;
}

__kernel void redirect_pos(
		__global const int   *botPosesX,
		__global const int   *botPosesY,
		__global const int   *ballPos,
		__global const int   *params,
		__global const float *weights,
		__global float *dstArray)
{
	int gidX = get_global_id(0);
	int gidY = get_global_id(1);
	float scores[NUM_SCORES];
	
	// fill bots to check (all opponents)
	Bot botsToCheck[16];
	uint8_t numBots2Check=0;
	for(uint8_t i=params[NUM_TIGERS];i<params[NUM_TIGERS]+params[NUM_OPPONENTS];i++)
	{
		botsToCheck[numBots2Check].pos.x = botPosesX[i];
		botsToCheck[numBots2Check].pos.y = botPosesY[i];
		numBots2Check++;
	}
	
	Vector2 vBallPos = {.x=ballPos[0], .y=ballPos[1]};

	Vector2 point;
	point.x = (int16_t) (FIELD_LENGTH * gidX/(get_global_size(0)-1) - FIELD_LENGTH/2);
	point.y = (int16_t) (FIELD_WIDTH * gidY/(get_global_size(1)-1) - FIELD_WIDTH/2);

	// distance goal
	Vector2 goalCenter = {.x=FIELD_LENGTH/2, .y=0};
	float distTarget = distancePP(&point, &goalCenter);
	if(distTarget < PEN_AREA_RADIUS) {
		dstArray[globalIndex()] = 1;
		barrier(CLK_LOCAL_MEM_FENCE);
		return;
	}
	if(distTarget < params[MIN_DIST_GOAL])
		scores[DIST_GOAL] = 1 - fitRange((distTarget-PEN_AREA_RADIUS) / (params[MIN_DIST_GOAL] - PEN_AREA_RADIUS));
	else 
		scores[DIST_GOAL] = fitRange((distTarget-PEN_AREA_RADIUS-params[MIN_DIST_GOAL]) / (params[MAX_DIST_GOAL] - PEN_AREA_RADIUS - params[MIN_DIST_GOAL]));
	

	// distance to ball
	float distBall = distancePP(&point, &vBallPos);
	if(distBall < params[MIN_DIST_BALL]) 
		scores[DIST_TO_BALL] = 1 - fitRange(distBall / params[MIN_DIST_BALL]);
	else 
		scores[DIST_TO_BALL] = fitRange((distBall-params[MIN_DIST_BALL]) / (params[MAX_DIST_BALL] - params[MIN_DIST_BALL]));
	
	// visibility to ball
	scores[VIS_TO_BALL] = p2pVisibility(&point, &vBallPos, botsToCheck, numBots2Check, params[VIS_RAY_SIZE], BALL_RADIUS + BOT_RADIUS, params[MAX_VIS_DIST]);


	// check several points on the goal
	scores[ANGLE_BALL_GOAL] = scores[VIS_GOAL] = 1;
	const int goalResolution = 10;
	const float minAngle = (DEG_TO_RAD * params[MIN_ANGLE]);
	const float maxAngle = (DEG_TO_RAD * params[MAX_ANGLE]);
	for(float y = -GOAL_WIDTH/2.0f;y<GOAL_WIDTH/2.0f;y += GOAL_WIDTH/(float) goalResolution)
	{
		Vector2 target = {.x=FIELD_LENGTH/2, .y=y};
		// redirect angle
		float angleBallGoal = getAngleInTriangle(&point, &vBallPos, &target);
		float scoreAngleBallGoal = fitRange((fabs(angleBallGoal)-minAngle) / (maxAngle-minAngle));
		scores[ANGLE_BALL_GOAL] = min( scores[ANGLE_BALL_GOAL], scoreAngleBallGoal );
		
		
		float scoreVisGoal = p2pVisibility(&point, &target, botsToCheck, numBots2Check, params[VIS_RAY_SIZE], BALL_RADIUS + BOT_RADIUS, params[MAX_VIS_DIST]);
		
		// model an obstacle behind the ball in pass direction
		Vector2 ball2Target;
		vec_sub(&target, &vBallPos, &ball2Target);
		vec_scaleTo(&ball2Target, 200, &ball2Target);
		Vector2 virtualObstacle;
		vec_add(&vBallPos, &ball2Target, &virtualObstacle);
		botsToCheck[numBots2Check].pos.x = virtualObstacle.x;
		botsToCheck[numBots2Check].pos.y = virtualObstacle.y;
		scoreVisGoal += p2pVisibility(&point, &target, &botsToCheck[numBots2Check], 1, params[VIS_RAY_SIZE], BALL_RADIUS, params[MAX_VIS_DIST]);
		
		scores[VIS_GOAL] = min( scores[VIS_GOAL], scoreVisGoal);
	}

	// calculate the angle between the goal posts
	Vector2 rightGoalPost = {.x=FIELD_LENGTH/2,.y=-GOAL_WIDTH/2};
	Vector2 leftGoalPost = {.x=FIELD_LENGTH/2,.y=GOAL_WIDTH/2};
	Vector2 p2Right, p2Left;
	vec_sub(&rightGoalPost, &point, &p2Right);
	vec_sub(&leftGoalPost, &point, &p2Left);
	float goalViewAngle = angleBetweenVectors(&p2Left, &p2Right);
	// angle can not get that high. Assume max 45 degree (~0.8rad)
	scores[ANGLE_GOAL_VIEW] = 1 - fitRange(goalViewAngle / 0.2f);

	// collect scores
	float score = 0;
	for(int i=0;i<NUM_SCORES;i++)
	{
		score += weights[i] * scores[i];
	}
	dstArray[globalIndex()] = score;

	// make all work items wait, this is apparently needed on OS X
	barrier(CLK_LOCAL_MEM_FENCE);
}
