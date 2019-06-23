function [ norms ] = vectorNorm( vectors )
%ANGLENORM Get length of vectors
%   vectors   Nx2 matrix

  assert(size(vectors,2) == 2);
  norms = sqrt( vectors(:,1) .* vectors(:,1) + vectors(:,2) .* vectors(:,2) );
end

