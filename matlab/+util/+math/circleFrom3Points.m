function [ center, radius ] = circleFrom3Points( p )
%UNTITLED4 Summary of this function goes here
%   Detailed explanation goes here

  A = [ones(size(p,1),1), p];
  B = p(:,1) .* p(:,1) + p(:,2) .* p(:,2);
  S = A\B;
  
  center = S(2:3) * 0.5;
  radius = sqrt(sum(abs(center.*center)) + S(1));

end

