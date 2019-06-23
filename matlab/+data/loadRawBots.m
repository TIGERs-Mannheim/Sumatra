function [ rawBots ] = loadRawBots( file )
    D = importdata(file);
    if(isstruct(D))
        T = D.data;
    else
        T = D;
    end
  
    % "frameId", "camId", "tCapture", "tAssembly", "pixel_x", "pixel_y",
    % "confidence", "pos_x", "pos_y", "robotId", "robotColor",
    % "orientation", "height"
    rawBots.frameId = T(:,1);
    rawBots.camId = T(:,2);
    rawBots.tCapture = T(:,3);
    rawBots.tAssembly = T(:,4);
    rawBots.pixel = T(:,5:6);
    rawBots.confidence = T(:,7);
    rawBots.pos = T(:,8:10);
    rawBots.id = T(:,11);
    rawBots.color = T(:,12);
    rawBots.height = T(:,13);
  
    % derived 
    rawBots.timestamp = rawBots.tCapture;
    rawBots.time = (rawBots.timestamp - rawBots.timestamp(1)) / 1e9;
    rawBots.vel = util.convert.pose2vel(rawBots.pos, rawBots.timestamp);
    rawBots.acc = util.convert.vel2acc(rawBots.vel, rawBots.timestamp);
end

