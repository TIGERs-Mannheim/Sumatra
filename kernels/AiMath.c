// void getBotKickerPos(Vector2* botPos, float orientation, Vector2* kickerPos)
// {
// 	Vector2 tmp = {.x=1,.y=0};
// 	vec_turnTo(&tmp, orientation, &tmp);
// 	vec_scaleTo(&tmp, DRIBBLER_DIST, &tmp);
// 	vec_add(botPos, &tmp, kickerPos);
// }

/*
 * Get the target angle of the bot, which is simply the bisection of incoming
 * and outgoing vectors.
 */
float getTargetAngle(Vector2* pos, Vector2* senderPos, Vector2* shootTarget)
{
	Vector2 targetAngleVec,p2Sender, p2Target;
	vec_sub(senderPos, pos, &p2Sender);
	vec_sub(shootTarget, pos, &p2Target);
	vec_bisection(&p2Sender, &p2Target, &targetAngleVec);
	return vec_angle(&targetAngleVec);
}

// float checkAngle(Vector2* pos, Vector2* senderPos, Vector2* shootTarget)
// {
// 	float targetAngle = getTargetAngle(pos, senderPos, shootTarget);
// 	Vector2 kickerPos;
// 	getBotKickerPos(pos, targetAngle, &kickerPos);
// 	Vector2 shootDir;
// 	vec_sub(shootTarget, &kickerPos, &shootDir);
// 	Vector2 kicker2Sender;
// 	vec_sub(senderPos, &kickerPos, &kicker2Sender);
// 
// //	float shortestRotation = fabs(getShortestRotation(vec_angle(&shootDir), vec_angle(&kicker2Sender)));
// 	float shortestRotation = fabs(getShortestRotation(targetAngle, vec_angle(&kicker2Sender)));
// 
// 	return shortestRotation;
// }
