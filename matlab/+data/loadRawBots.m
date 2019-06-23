function [ rawBots ] = loadRawBots( file )
  T = importdata(file);
  rawBots.timestamp = T(:,1);
  rawBots.camId = T(:,2);
  rawBots.id = T(:,3);
  rawBots.color = T(:,4);
  rawBots.pos = T(:,5:7);
    if size(T,2) > 7
      rawBots.frameId = T(:,8);
    end
end

