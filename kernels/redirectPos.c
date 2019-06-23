#pragma OPENCL EXTENSION cl_khr_byte_addressable_store : enable
#define MAX_VALUE 1

int globalIndex() {
	int gidX = get_global_id(0);
	int gidY = get_global_id(1);
	int gidB = get_global_id(2);

	return gidB * get_global_size(0) * get_global_size(1)
			+ gidY * get_global_size(0) + gidX;
}

__kernel void redirect_pos(__global const int *botPosesX,
		__global const int *botPosesY,
		__global const int *numBots,
		__global const int *ballPos,
		__global const int *gameStateID,
		__global const int *ballPossession,
		__global float *dstArray)
{
	int gidX = get_global_id(0);
	int gidY = get_global_id(1);
	int gidB = get_global_id(2);
	int numTigers = get_global_size(2)-1;
	int numFoeBots = (*numBots-1)-numTigers;

	if (numFoeBots == 0)
	{
		numFoeBots = 1;
	}

	dstArray[globalIndex()]=0.0;

	if(gidB >= *numBots)
	{
		return;
	}

	Rect field = {.x=-FIELD_LENGTH/2, .y=-FIELD_WIDTH/2, .xExtend=FIELD_LENGTH, .yExtend=FIELD_WIDTH};
	Rect foeField = {.x=0, .y=-FIELD_WIDTH/2, .xExtend=field.xExtend/2, .yExtend=field.yExtend};
	Rect ourField = {.x=-FIELD_LENGTH/2, .y=-FIELD_WIDTH/2, .xExtend=FIELD_LENGTH/2, .yExtend=FIELD_WIDTH};

	Bot bot;
	// TODO target
	Vector2 target = {.x=field.xExtend/2,.y=0};
	Vector2 ourGoalCenter = {.x=-field.xExtend/2,.y=0};
	Vector2 theirGoalCenter = {.x=field.xExtend/2,.y=0};
	Bot botsToCheck[16];
	Bot botsToCheckPlusBall[1];	
	Vector2 vBallPos = {.x=ballPos[0], .y=ballPos[1]};
	Vector2 vcenter = {.x=0, .y=0};

	Circle ballCircle = {.center=vBallPos, .radius=DIST_BALL_FREEKICK};
	Circle centerCircle = {.center=vcenter, .radius=CENTER_CIRCLE_RADIUS};


	int botId=0;
	for(uint8_t i=0;i<*numBots;i++)
	{
		if(i==gidB)
		{
			bot.pos.x = botPosesX[i];
			bot.pos.y = botPosesY[i];
		} else {
			botsToCheck[botId].pos.x = botPosesX[i];
			botsToCheck[botId].pos.y = botPosesY[i];

			botId++;
		}
	}
	botsToCheckPlusBall[0].pos.x = vBallPos.x;
	botsToCheckPlusBall[0].pos.y = vBallPos.y;

	Vector2 point;
	point.x = (int16_t) (field.xExtend * gidX/(get_global_size(0)-1) - field.xExtend/2);
	point.y = (int16_t) (field.yExtend * gidY/(get_global_size(1)-1) - field.yExtend/2);

	// Refree Messages 
	if (*gameStateID == DIST_BALL && isPointInCircle(&ballCircle, &point))
	{
		dstArray[globalIndex()]=MAX_VALUE;
		return;
	}

	if (*gameStateID == DIST_BALL_BLOCK && isPointInCircle(&ballCircle, &point))
	{
		dstArray[globalIndex()]=MAX_VALUE;
		return;
	}

	Rect ourFieldSmallLine = ourField;
	ourFieldSmallLine.xExtend -= 500;
	//bool forbiddenArea = isPointInRect(&foeField, &point) | isPointInCircle(&centerCircle, &point) | isPointInRect(&ourFieldSmallLine, &point);
	bool forbiddenArea = isPointInRect(&foeField, &point) | isPointInCircle(&centerCircle, &point);
	if (*gameStateID == DIST_BALL_OURFIELD && forbiddenArea)
	{
		dstArray[globalIndex()]=MAX_VALUE;
		return;
	}

	if (*gameStateID == PENALTY && (fabs(point.x)) > PENALTY_LINE_THEIR_X)
	{
		dstArray[globalIndex()]=MAX_VALUE;
		return;
	}

	// only in their half of field and not too close to goal line
	if ((point.x < -FIELD_LOWER_BOUND_X) || (point.x > (FIELD_LENGTH / 2) - FIELD_UPPER_BOUND_X))
	{
		dstArray[globalIndex()]=MAX_VALUE;
		return;
	}

	Vector2 kickerPos;
	float targetAngle = getTargetAngle(&point, &vBallPos, &target);
	getBotKickerPos(&point, targetAngle, &kickerPos);

	// not too close to target
	if (distancePP(&kickerPos, &target) < DIST_2_TARGET)
	{
		dstArray[globalIndex()]=MAX_VALUE;
		return;
	}

	// not too close to our goal
	if (distancePP(&kickerPos, &ourGoalCenter) < DIST_2_OUR_GOAL)
	{
		dstArray[globalIndex()]=MAX_VALUE;
		return;
	}

	// not too close to redirector/ball
	float dist2Ball = max(0.0f, (NEAR_BALL - distancePP(&kickerPos, &vBallPos))/NEAR_BALL*MAX_VALUE);

	// target visible?
	float p2pVisTarget = min((float) MAX_VALUE, MAX_VALUE*(p2pVisibility(&kickerPos, &target, botsToCheck, (*numBots)-1, RAY_SIZE, 0) + p2pVisibility(&kickerPos, &target, botsToCheckPlusBall, 1, RAY_SIZE, 0)));

	// direct shooting line
	float directShootingLine = 1-min((float) MAX_VALUE, MAX_VALUE*p2pVisibility(&kickerPos, &ourGoalCenter, botsToCheckPlusBall, 1, RAY_SIZE, 1));

	// receiver visible?
	float p2pVisReceiver = min((float) MAX_VALUE, MAX_VALUE*p2pVisibility(&kickerPos, &vBallPos, botsToCheck, (*numBots)-1, RAY_SIZE, 0));

	// point near other bot
	float dist2Bots = 0;
	for(int i=0;i<(*numBots)-1;i++)
	{
		dist2Bots += max(0.0f, (DIST_2_BOTS - distancePP(&botsToCheck[i].pos, &point))/DIST_2_BOTS*MAX_VALUE);
	}
	dist2Bots = min((float) MAX_VALUE, dist2Bots);

	// calculate angle score
	float rotationPenalty = checkAngle(&point, &vBallPos, &target)/(M_PI/2)*MAX_VALUE;

	float maxFieldDist = sqrt(field.xExtend*field.xExtend + field.yExtend*field.yExtend);
	float dist2Target = distancePP(&kickerPos, &target)/maxFieldDist*MAX_VALUE;
	float dist2Shooter = distancePP(&kickerPos, &vBallPos)/maxFieldDist*MAX_VALUE;

	// calculate block pos
	float blockPos = 0;
	float dist2BlockLinesValues = 1;
	for(uint8_t id=numTigers; id<(*numBots)-1; id++)
	{
		Bot foebot = botsToCheck[id];
		
		float p2pVisGoal = 1;
		float dist2BlockLine = distancePL(&point, &(foebot.pos), &ourGoalCenter);
		float valueDistance = 1;
	
		if(point.x > foebot.pos.x){
			p2pVisGoal = 1.0f;
			valueDistance = 1.0f;
		}else if (dist2BlockLine > DIST_2_BLOCKLINE) {
			p2pVisGoal = 1.0f;
			valueDistance = 1.0f;
		} else {
			p2pVisGoal = min((float) MAX_VALUE, (float) MAX_VALUE * p2pVisibilityIgnore(&(foebot.pos), &ourGoalCenter, botsToCheck, 0, (*numBots)-1, id ,DIST_2_BLOCKLINE));
			valueDistance = min((float) MAX_VALUE, MAX_VALUE*(dist2BlockLine / DIST_2_BLOCKLINE));
		}
		dist2BlockLinesValues += (MAX_VALUE*(1-p2pVisGoal)*(1-valueDistance)); // * (1-distancePP(&foebot, &target)/maxFieldDist*MAX_VALUE);
	}
	blockPos = min((float) MAX_VALUE, (1-(dist2BlockLinesValues/numFoeBots)));

	// calculate free way for our bots
	float freeWayPos = 0;
	dist2BlockLinesValues = 1;
	for(uint8_t id=0; id<numTigers; id++)
	{
		Bot tigerbot = botsToCheck[id];
		
		float p2pVisGoal = 1;
		float dist2BlockLine = distancePL(&point, &(tigerbot.pos), &theirGoalCenter);
		float valueDistance = 1;
	
		if(point.x < tigerbot.pos.x){
			p2pVisGoal = 1;
			valueDistance = 1;
		}else if (dist2BlockLine > DIST_2_BLOCKLINE) {
			p2pVisGoal = 1;
			valueDistance = 1;
		} else {
			p2pVisGoal = min((float) MAX_VALUE, (float) MAX_VALUE * p2pVisibilityIgnore(&(tigerbot.pos), &theirGoalCenter, botsToCheck, 0, (*numBots), id ,DIST_2_BLOCKLINE));
			valueDistance = min((float) MAX_VALUE, MAX_VALUE*(dist2BlockLine / DIST_2_BLOCKLINE));
		}
		dist2BlockLinesValues += (MAX_VALUE*(1-p2pVisGoal)*(1-valueDistance));// * (1-distancePP(&foebot, &target)/maxFieldDist*MAX_VALUE);
	}
	freeWayPos = min((float) MAX_VALUE, (dist2BlockLinesValues/(*numBots)));

	//calculate the angle between the goal posts
	Vector2 rightGoalPost = {.x=FIELD_LENGTH,.y=-GOAL_WIDTH/2};
	Vector2 leftGoalPost = {.x=FIELD_LENGTH,.y=GOAL_WIDTH/2};
	Vector2 leadPoint;
	float visibleGoalLine;
	if(point.y < 0) {
		leadPointOnLine(&rightGoalPost, &point, &leftGoalPost, &leadPoint);
		visibleGoalLine = distancePP(&leadPoint, &rightGoalPost);
	} else {
		leadPointOnLine(&leftGoalPost, &point, &rightGoalPost, &leadPoint);
		visibleGoalLine = distancePP(&leadPoint, &leftGoalPost);
	}
	float goalPostAngle = 1 - (visibleGoalLine / GOAL_WIDTH);

	if(*ballPossession == OFFENSE_SUPPORTER) {
		dstArray[globalIndex()] =
			W_DSHOOT_LINE * directShootingLine +
			W_ROTATION * rotationPenalty +
			W_DIST2BOTS * dist2Bots +
			W_P2P_VIS_TARGET * p2pVisTarget +
			W_P2P_VIS_RECEIVER * p2pVisReceiver+
			W_DIST_TARGET * dist2Target +
			W_DIST_SHOOTER * dist2Shooter +
			W_DIST_BALL * dist2Ball +
			W_GOAL_POST_ANGLE * goalPostAngle +
			W_BLOCK * blockPos +
			W_VIS_BOTS * freeWayPos;
			
	} else {
		dstArray[globalIndex()] =
			W_BLOCK_DEFENSE * blockPos + 
			W_DIST_TARGET * (1-dist2Target) +
			W_DIST2BOTS * dist2Bots +
			W_DSHOOT_LINE * directShootingLine;
			
	}

//dstArray[globalIndex()] = W_BLOCK_DEFENSE * blockPos;

}
