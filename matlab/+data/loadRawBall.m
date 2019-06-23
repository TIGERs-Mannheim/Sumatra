function [ rawBall ] = loadRawBall( file )
  T =  importdata(file);
  rawBall.timestamp = T(:,1);
  rawBall.camId = T(:,2);
  rawBall.pos = T(:,3:5);
  if size(T,2) > 5
    rawBall.frameId = T(:,6);
  end
end

