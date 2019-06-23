function [ vOut ] = vectorScale( vIn, length )
%ANGLESCALE Summary of this function goes here
%   Detailed explanation goes here
  vOut = vIn * length / norm(vIn);

end

