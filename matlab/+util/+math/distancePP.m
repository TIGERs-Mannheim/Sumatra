function [ dist ] = distancePP( p1, p2 )
%DISTANCEPP Summary of this function goes here
%   Detailed explanation goes here

  vec = p2-p1;
  dist = sqrt(vec(1)*vec(1) + vec(2)*vec(2));
end

