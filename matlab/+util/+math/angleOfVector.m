function [ angle ] = angleOfVector( vector )
%ANGLEOFVECTOR angle between x-axis and vector
%   Detailed explanation goes here
  angle = util.math.normalizeAngle( acos(vector(1) / norm(vector)) );
  if vector(2) < 0
    angle = -angle;
  end
end

