function [ wpBall ] = loadWpBall( file )
    T = importdata(file);
    wpBall.pos = T(:,1:3);
    wpBall.vel = T(:,4:6);
    wpBall.acc = T(:,7:9);
    if size(T,2) > 9
      wpBall.frameId = T(:,10);
    end
    if size(T,2) > 10
      wpBall.timestamp = T(:,11);
    end
end

