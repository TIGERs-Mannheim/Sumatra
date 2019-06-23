function [ vOut ] = vectorScale( vIn, length )
%ANGLESCALE Scale vectors
%   vIn     Nx2
%   length  scalar length of new vector(s)
  assert(size(vIn,2) == 2);
  vOut = vIn * length / util.math.vectorNorm(vIn);

end

