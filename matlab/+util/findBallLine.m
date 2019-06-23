function [ line, found ] = findBallLine( ballPoss, minDist )
%FINDBALLLINE find support+direction vectors of ball moving line
%   out line = [sx,sy,dx,dy]
  line = zeros(4,1);
  found = false;
  for i=1:size(ballPoss,1)
    dist = norm(ballPoss(i,:)-ballPoss(1,:));
    if dist > minDist
      line(1:2) = ballPoss(1,:);
      line(3:4) = (ballPoss(i,:)-ballPoss(1,:));
      found = true;
      break;
    end
  end
end

