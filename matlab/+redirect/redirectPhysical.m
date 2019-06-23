function [ rotation ] = redirectPhysical( ballSpeed, angle )
%REDIRECTPHYSICAL Summary of this function goes here
%   Detailed explanation goes here
  BALL_DAMP_FACTOR = 0.004;
  
  kickerPos = [0;0];
  shootSpeed = 2; % fixed
  approxOrientation = 0;
  ballVel = ballSpeed;
  shootTarget = [1;0];
  
  ballPos = util.math.vectorOfAngle(angle);
  
  % x = ballSpeed
  % y = angle
  % out = rotation
  ballVel = max(0.1,ballVel);
  shootAngle = util.math.angleOfVector(shootTarget - kickerPos);
  ballSpeedDir = -util.math.vectorScale(ballPos,ballVel);
  rotation = calcOrientation(shootSpeed,ballSpeedDir,approxOrientation,shootAngle,BALL_DAMP_FACTOR);
end

function [destAngle] = calcOrientation(shootSpeed,incomingSpeedVec,initialOrientation,targetAngle,ballDampFactor)
  destAngle = initialOrientation;
  for i=1:100
    vShootSpeed = util.math.vectorScale(util.math.vectorOfAngle(destAngle),shootSpeed);
    outVec = ballDamp(vShootSpeed,incomingSpeedVec,ballDampFactor);
    diff = targetAngle - util.math.angleOfVector(outVec);
    if abs(diff) < 0.005
      break
    end
    destAngle = util.math.normalizeAngle(destAngle + diff);
  end
end

function outVec = ballDamp(shootSpeed,incomingSpeedVec,ballDampFactor)
  vec1 = incomingSpeedVec * -1;
  diff2 = util.math.normalizeAngle( ...
              util.math.normalizeAngle(util.math.angleOfVector(vec1)) ...
            - util.math.normalizeAngle(util.math.angleOfVector(shootSpeed)) ...
            );
  outVec = util.math.vectorScale( ...
              util.math.vectorOfAngle(util.math.angleOfVector(vec1) - (diff2*2)), ...
              norm(incomingSpeedVec));
  outVec = outVec * (1-ballDampFactor);
  outVec = outVec + shootSpeed;
end