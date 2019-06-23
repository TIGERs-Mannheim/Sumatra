rootPath = '/home/geforce/git/Sumatra/data/vision/moduli_robocup/ballKick';

dirs = dir(rootPath);

x = zeros(0,1);
y = zeros(0,1);

figure

for i=1:size(dirs,1)
  if strncmp(dirs(i).name, '2016', 4)
    wpBall = data.loadWpBall(sprintf('%s/%s/wpBall.csv', rootPath, dirs(i).name));
    info = data.loadInfo(sprintf('%s/%s/info.json', rootPath, dirs(i).name));
    
    vel = sqrt(wpBall.vel(:,1).^2 + wpBall.vel(:,2).^2);
    hvel = vel(vel>0.1);
%     maxVel = vel(1);
    maxVel = max(vel);
    kickVel = info.kickSpeed / 1000;
    
    if maxVel > 8
      continue;
    end
    x(end+1) = kickVel;
    y(end+1) = maxVel;
    
%     plot(wpBall.time, vel)
    
    fprintf('%f %f\n', kickVel, maxVel);
    
%     pause
%     D = data.loadAll(thisDir);
  end
end

%%

figure
plot(x,y,'.')


