function [ angle ] = angleOfVector( vector )
%ANGLEOFVECTOR angle between x-axis and vector
%   vector    Nx2
%   angle     Nx1
  assert(size(vector,2) == 2);

  angle = util.math.normalizeAngle( acos(vector(:,1) ./ (sqrt( vector(:,1) .* vector(:,1) + vector(:,2) .* vector(:,2)))) );
  angle(vector(:,2)<0) = -angle(vector(:,2)<0);
  
  
end

