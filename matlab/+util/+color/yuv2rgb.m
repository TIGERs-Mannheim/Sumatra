function [ rgb ] = yuv2rgb( yuv )
%YUV2RGB Summary of this function goes here
%   Detailed explanation goes here
  rgb = zeros(size(yuv));
  rgb(:,1) = yuv(:,1) + 1.4075 * (yuv(:,3) - 128);
  rgb(:,2) = yuv(:,1) - 0.3455 * (yuv(:,2) - 128) - (0.7169 * (yuv(:,3) - 128));
  rgb(:,3) = yuv(:,1) + 1.7790 * (yuv(:,2) - 128);


end

