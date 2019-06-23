function [ local ] = convertGlobalBotVector2Local( glob, angle )
%UNTITLED6 Summary of this function goes here
%   Detailed explanation goes here
  local = util.math.vectorTurn( glob, pi/2 - angle );

end

