float normalizeAngle(float angle)
{
	return (angle - (round(angle / (2*M_PI) - 0.000001) * 2*M_PI));
}

void vec_add(Vector2* pSrc1, Vector2* pSrc2, Vector2* pDest)
{
	pDest->x = pSrc1->x + pSrc2->x;
	pDest->y = pSrc1->y + pSrc2->y;
}

void vec_sub(Vector2* pSrc1, Vector2* pSrc2, Vector2* pDest)
{
	pDest->x = pSrc1->x - pSrc2->x;
	pDest->y = pSrc1->y - pSrc2->y;
}

void vec_mult_scalar(Vector2* vSrc, float scalar, Vector2* vDest)
{
	vDest->x = vSrc->x * scalar;
	vDest->y = vSrc->y * scalar;
}

float vec_length2(Vector2* v)
{
	return sqrt((float) (v->x*v->x + v->y*v->y));
}

void vec_turnTo(Vector2* vec, float angle, Vector2* vDest)
{
	float len = vec_length2(vec);
	vDest->y = sin(angle) * len;
	vDest->x = cos(angle) * len;
}

void vec_scaleTo(Vector2* vec, float newLength, Vector2* vDest)
{
	float oldLength = vec_length2(vec);
	if (oldLength != 0)
	{
		vec_mult_scalar(vec, newLength/oldLength, vDest);
	}
}

void vec_norm(Vector2* vec, Vector2* vDest)
{
	float length = vec_length2(vec);
	if(length > 0)
	{
		vDest->x = vec->x / length;
		vDest->y = vec->y / length;
	} else {
		vDest->x = vec->x;
		vDest->y = vec->y;
	}
}

float vec_scalarProduct(Vector2* v1, Vector2* v2)
{
	return ((v1->x * v2->x) + (v1->y * v2->y));
}

float vec_angle(Vector2* vec)
{
	Vector2 xAxis = {.x=1,.y=0};
	float result = acos(vec_scalarProduct(vec,&xAxis)/vec_length2(vec));
	result = normalizeAngle(result);
	if (vec->y < 0)
	{
		result = -result;
	}
	return result;
}

float vec_simpleAngle(Vector2* vec)
{
	Vector2 xAxis = {.x=1,.y=0};
	float result = acos(vec_scalarProduct(vec,&xAxis)/vec_length2(vec));
	return result;
}

void vec_bisection(Vector2* v1, Vector2* v2, Vector2* vDest)
{
	Vector2 vt1, vt2;
	vec_norm(v1, &vt1);
	vec_norm(v2, &vt2);
	vec_add(&vt1, &vt2, vDest);
}

float distancePP(Vector2* p1, Vector2* p2)
{
	Vector2 v;
	vec_sub(p2,p1,&v);
	return vec_length2(&v);
}

float distancePPSqr(Vector2* a, Vector2* b)
{
	float abX = a->x - b->x;
	float abY = a->y - b->y;
	return (abX * abX) + (abY * abY);
}

uint8_t isPointInRect(Rect* rect, Vector2* point)
{
	return point->x >= rect->x && point->x <= rect->x + rect->xExtend && point->y <= rect->y  && point->y >= rect->y - rect->yExtend;
}

uint8_t isPointInRectMargin(Rect* rect, Vector2* point, float margin)
{
	return point->x + margin >= rect->x && point->x - margin <= rect->x + rect->xExtend && point->y - margin <= rect->y  && point->y + margin >= rect->y - rect->yExtend;
}

Rect createRect(Vector2* p1, Vector2* p2)
{
	Rect rect = {.x=min(p1->x, p2->x), .y=max(p1->y, p2->y), .xExtend=fabs(p1->x - p2->x), .yExtend=fabs(p1->y - p2->y)};
	return rect;
}

uint8_t isPointInCircle(Circle* circle, Vector2* point)
{
		Vector2 tmp;
		vec_sub(point, &circle->center, &tmp);

		return (vec_length2(&tmp) <= circle->radius);
}

void leadPointOnLine(Vector2* point, Vector2* line1, Vector2* line2, Vector2* leadPoint)
{
	if (line1->x == line2->x)
	{
		// special case 1. line is orthogonal to x-axis
		leadPoint->x = line1->x;
		leadPoint->y = point->y;
		return;
	} else if (line1->y == line2->y)
	{
		// special case 2. line is orthogonal to y-axis
		leadPoint->x = point->x;
		leadPoint->y = line1->y;
		return;
	}
	// create straight line A from line1 to line2
	float mA = (line2->y - line1->y) / (line2->x - line1->x);
	float nA = line2->y - (mA * line2->x);

	// calculate straight line B
	float mB = -1 / mA;
	float nB = point->y - (mB * point->x);

	// cut straight lines A and B
	leadPoint->x = (coord) ((nB - nA) / (mA - mB));
	leadPoint->y = (coord) ((mA * leadPoint->x) + nA);
}

float distancePL(Vector2* p, Vector2* pl1, Vector2* pl2)
{
	Vector2 leadPoint;
	leadPointOnLine(p, pl1, pl2, &leadPoint);
	return distancePP(p, &leadPoint);
}

/*
 * Calculate a score of the visibility between pStart and pEnd.
 * The score is 0 if no visibility and 1 if obstacle is at least raySize away (bot radius considered).
 * If there are multiple obstacles, the sum of the score is considered.
 * The score will always be between 0 and 1.
 */
float p2pVisibility(Vector2* pStart, Vector2* pEnd, Bot* botsToCheck, uint8_t numBotsToCheck, coord raySize, float minDistance, float maxVisDist)
{
	// below this distance, there is no visibility
	//float minDistance = BALL_RADIUS + BOT_RADIUS;

	float score = 0;
	for (uint8_t i=0;i<numBotsToCheck;i++)
	{
		Bot bot = botsToCheck[i];
		Vector2 leadPoint;
		leadPointOnLine(&(bot.pos), pStart, pEnd, &leadPoint);
		Rect rect = createRect(pStart, pEnd);
		// only check those bots that possibly can be in between start and end
		if(isPointInRectMargin(&rect, &leadPoint, 2*BOT_RADIUS))
		{
			float distanceBotLine = distancePP(&leadPoint, &(bot.pos));
			score += 1 - fitRange((distanceBotLine-minDistance) / raySize);
		}
	}
	float dist = distancePP(pStart, pEnd);
	score *= 1 - fitRange(dist/maxVisDist);

	return fitRange(score);
}

float p2pVisibilityIgnore(Vector2* pStart, Vector2* pEnd, Bot* botsToCheck, uint8_t numBotsToCheckFrom, uint8_t numBotsToCheckTo, uint8_t ignoreID, coord raySize)
{
	float minDistance = BALL_RADIUS + BOT_RADIUS + raySize;

	// checking free line
	float sum=0;
	for (uint8_t i=numBotsToCheckFrom;i<numBotsToCheckTo;i++)
	{
		if (i == ignoreID)
		{
			continue;
		}
		Bot bot = botsToCheck[i];
		Vector2 leadPoint;
		leadPointOnLine(&(bot.pos), pStart, pEnd, &leadPoint);
		Vector2 startToLead;
		vec_sub(&leadPoint, pStart, &startToLead);
		Vector2 startToEnd;
		vec_sub(pEnd, pStart, &startToEnd);
		if(startToLead.x/startToEnd.x>0 && startToLead.x/startToEnd.x<1)
		{
			// only check those bots that possibly can be in between start and end
			float distanceBotLine = distancePL(&(bot.pos), pStart, pEnd);
			sum+=max(0.0f, (minDistance-distanceBotLine)/minDistance);
		}
	}

	return sum;
}

float getShortestRotation(float angle1, float angle2)
{
	float rotateDist = 0;

	rotateDist = angle2 - angle1;
	if (rotateDist < -M_PI)
	{
		rotateDist = 2*M_PI + rotateDist;
	}
	if (rotateDist > M_PI)
	{
		rotateDist -= 2*M_PI;
	}
	return rotateDist;
}

float angleBetweenVectors(Vector2 *v1, Vector2 *v2)
{
	return fabs(getShortestRotation(vec_angle(v1), vec_angle(v2)));
}

/**
 * Get the angle between the vectors p1->p2 and p1->p3
 */
float getAngleInTriangle(Vector2* p1, Vector2* p2, Vector2* p3)
{
	Vector2 p1p2,p1p3;
	vec_sub(p2,p1,&p1p2);
	vec_sub(p3,p1,&p1p3);
	return angleBetweenVectors(&p1p2,&p1p3);
}
