function [ skillBots ] = loadSkillBots( file )
    T = importdata(file);
    skillBots.id = T(:,1);
    skillBots.color = T(:,2);
    skillBots.timestamp = T(:,3);
    skillBots.trajVel = T(:,4:6);
    skillBots.trajPos = T(:,6:8);
    skillBots.setVel = T(:,8:10);
    skillBots.setPos = T(:,11:13);
    skillBots.localVel = T(:,14:16);
    
    if size(T,2) > 16
      skillBots.trajVel(:,3) = T(:,17);
      skillBots.trajPos(:,3) = T(:,18);
    else
      skillBots.trajVel(:,3) = 0;
      skillBots.trajPos(:,3) = 0;
    end
      
end

