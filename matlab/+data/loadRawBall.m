function [ rawBall ] = loadRawBall( file )
    D = importdata(file);
    if(isstruct(D))
        T = D.data;
    else
        T = D;
    end
    
    % "frameId", "camId", "tCapture", "tAssembly", "pixel_x", "pixel_y",
    % "confidence", "pos_x", "pos_y", "area", "height"
    rawBall.frameId = T(:,1);
    rawBall.camId = T(:,2);
    rawBall.tCapture = T(:,3);
    rawBall.tAssembly = T(:,4);
    rawBall.pixel = T(:,5:6);
    rawBall.confidence = T(:,7);
    rawBall.pos = T(:,[8,9,11]);
    rawBall.area = T(:,10);
  
    % derived 
    rawBall.timestamp = rawBall.tCapture;
    rawBall.time = (rawBall.timestamp - rawBall.timestamp(1)) / 1e9;
    rawBall.vel = util.convert.pos2vel(rawBall.pos, rawBall.timestamp);
    rawBall.acc = util.convert.vel2acc(rawBall.vel, rawBall.timestamp);
end

