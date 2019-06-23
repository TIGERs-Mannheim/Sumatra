function [ lp ] = leadpoint( p, pl1, pl2 )
%LEADPOINT Calculate lead point between a point and a line
%   p point
%   pl1 first point on line
%   pl2 second point on line
  lp = zeros(2,1);

  % create straight line A from line1 to line2
  mA = (pl2(2) - pl1(2)) / (pl2(1)-pl1(1));
  nA = pl2(2) - (mA * pl2(1));
  
	% calculate straight line B
  mB = -1/mA;
  nB = p(2) - (mB * p(1));
  
	% cut straight lines A and B
  lp(1) = (nB-nA) / (mA - mB);
  lp(2) = (mA * lp(1)) + nA;

end

