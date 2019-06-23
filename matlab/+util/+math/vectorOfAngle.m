function [ vector ] = vectorOfAngle( angle )
%VECTOROFANGLE create a vector based on an angle
%   angle   Nx1
%   vector  Nx2
  assert(size(angle,2) == 1);
  vector = zeros(size(angle,1),2);
  vector(:,1) = cos(angle);
  vector(:,2) = sin(angle);

end

