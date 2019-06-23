function [ rotation ] = angleBetweenVectors( v1,v2 )
%ANGLEBETWEENVECTORS alculates the angle between two vectors with respect to the rotation direction.
%   http://stackoverflow.com/questions/2663570/how-to-calculate-both-positive-and-negative-angle-between-two-lines
% rotation = [-pi,pi]
  angleA = atan2(v1(1),v1(2));
  angleB = atan2(v2(1),v2(2));
  rotation = (angleB - angleA);
  if rotation < -pi - 1e-4
    rotation = rotation + 2*pi;
  else
    if rotation > pi + 1e-4
      rotation = rotation - 2 * pi;
    end
  end
end

