function [ nvec ] = vectorTurn( vec, angle )
%UNTITLED5 Summary of this function goes here
%   Detailed explanation goes here
  nAngle = util.math.normalizeAngle(angle);
  nvec(:,1) = vec(:,1) .* cos(nAngle) - vec(:,2) .* sin(nAngle);
  nvec(:,2) = vec(:,2) .* cos(nAngle) + vec(:,1) .* sin(nAngle);

end

