function [ wpBots ] = loadWpBots( file )
    T = importdata(file);
    wpBots.id = T(:,1);
    wpBots.color = T(:,2);
    wpBots.pos = T(:,3:5);
    wpBots.vel = T(:,6:8);
    wpBots.acc = T(:,9:11);
    if size(T,2) > 11
      wpBots.frameId = T(:,12);
    end
    if size(T,2) > 12
      wpBots.timestamp = T(:,13);
    end
end

