function [ nAngle ] = normalizeAngle( angle )
%NORMALIZEANGLE Normalize angle, to make sure angle is in (-pi/pi] interval.
%	 New angle is returned, parameter stay unaffected.
  nAngle = angle - (round((angle / (2*pi)) - 1e-6) * 2 * pi);
end

