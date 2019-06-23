function [ rawBall ] = loadRawBall( file )
  T =  importdata(file);
  rawBall.timestamp = T(:,1);
  rawBall.camId = T(:,2);
  rawBall.pos = T(:,3:5);
  rawBall.frameId = T(:,6);
  rawBall.pixelx = T(:,7);
  rawBall.pixely = T(:,8);
  rawBall.area = T(:,9);
  rawBall.confidence = T(:,10);
  rawBall.tSent = T(:,11);
  
  rawBall.time = (rawBall.timestamp)/1e9;
end

