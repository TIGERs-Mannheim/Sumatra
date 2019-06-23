function [ rotation ] = angleBetweenVectors( v1,v2 )
%ANGLEBETWEENVECTORS calculates the angle between two vectors with respect to the rotation direction.
%   http://stackoverflow.com/questions/2663570/how-to-calculate-both-positive-and-negative-angle-between-two-lines
%
% v1,v2     Nx2 vectors
% rotation  rotation between v1 and v2 in normalized range [-pi,pi]
  angleA = atan2(v1(:,1),v1(:,2));
  angleB = atan2(v2(:,1),v2(:,2));
  rotation = (angleB - angleA);
  rotation = util.math.normalizeAngle(rotation);
end

