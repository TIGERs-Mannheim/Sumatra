function [ dist ] = distancePP( p1, p2 )
%DISTANCEPP Distance between p1 and p2
%   p1,p2     Nx2
%   dist      Nx1

  vec = p2-p1;
  dist = sqrt(vec(:,1)*vec(:,1) + vec(:,2)*vec(:,2));
end

