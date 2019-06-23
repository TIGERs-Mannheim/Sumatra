function [ yuv ] = rgb2yuv( rgb )
%RGB2YUV Summary of this function goes here
%   Detailed explanation goes here
  yuv = zeros(size(rgb));
  yuv(:,1) = rgb(:,1) *  .299000 + rgb(:,2) *  .587000 + rgb(:,3) *  .114000;
  yuv(:,2) = rgb(:,1) * -.168736 + rgb(:,2) * -.331264 + rgb(:,3) *  .500000 + 128;
  yuv(:,3) = rgb(:,1) *  .500000 + rgb(:,2) * -.418688 + rgb(:,3) * -.081312 + 128;


end

