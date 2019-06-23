clear, close all;

directory = '../logs/startEndCollector/';
% dirlist = dir(strcat(directory,'*.csv'));
% for i = 1:length(dirlist)
%   sampleFiles(i,:) = strcat(directory, dirlist(i).name);
% end

sampleFiles = strcat(directory, '1443781091775.csv');
% sampleFiles(end+1,:) = strcat(directory, '1443708107133.csv');
% sampleFiles(end+1,:) = strcat(directory, '1443708113213.csv');
% sampleFiles(end+1,:) = strcat(directory, '1443708119069.csv');
% sampleFiles(end+1,:) = strcat(directory, '1443714055690.csv');

for sf=1:size(sampleFiles,1)
  file = sampleFiles(sf,:);
  bot = data.loadRawBots(file);



figure(1)
hold all;
plot(bot.pos(:,1), bot.pos(:,2))
P = [bot.pos(1,1:2); bot.pos(int32(end/2),1:2); bot.pos(end,1:2)];
plot(P(:,1),P(:,2),'*')
axis equal

dtDiff = (bot.timestamp(2:end,:) - bot.timestamp(1:end-1,:)) * 1e-9;
velDiff = (bot.pos(2:end,:) - bot.pos(1:end-1,:)) ./ repmat(dtDiff, [1,3]);
velDiff = [velDiff(:,1:2) / 1000, velDiff(:,3)];
velDiffLocal = [util.math.convertGlobalBotVector2Local(velDiff(:,1:2), bot.pos(1:end-1,3)), velDiff(:,3)];
time = (bot.timestamp-bot.timestamp(1))/1e9;
% fr = fit(time,bot.pos(:,3),fittype('poly1'));
fr = fit((bot.timestamp(2:end)-bot.timestamp(1))/1e9, ...
  util.math.normalizeAngle(bot.pos(2:end,3) - bot.pos(1,3)), ...
  fittype('poly1'));
aVelMu = fr.p1;

velSE = zeros(size(bot.pos));
dt = zeros(size(bot.pos,1),1);
angleDiff = zeros(size(bot.pos,1),1);
arcLen = zeros(size(bot.pos,1),1);
radius = zeros(size(bot.pos,1),1);
compVelGlob = zeros(size(bot.pos,1),2);
for i=3:size(bot.pos,1)
%   P = [bot.pos(1,1:2); bot.pos(int32(i/2),1:2); bot.pos(i,1:2)];
%   [c,r] = util.math.circleFrom3Points(P);
  [c,r] = util.math.circleFrom3Points(bot.pos(1:i,1:2));
  radius(i) = r;
  
  dt(i) = (bot.timestamp(i) - bot.timestamp(1) ) * 1e-9;
  aVel = util.math.normalizeAngle(bot.pos(i,3) - bot.pos(1,3)) / dt(i);
  angleDiff(i) = util.math.normalizeAngle(bot.pos(i,3) - bot.pos(1,3));
%   angleDiff(i) = aVelMu * dt(i);
  arcLen(i) = r * angleDiff(i);
  
  startToCenter = c' - bot.pos(1,1:2);
  dir1 = util.math.vectorScale(util.math.vectorTurn(startToCenter, pi/2), arcLen(i));
  dir2 = util.math.vectorScale(util.math.vectorTurn(startToCenter, -pi/2), arcLen(i));
%   dir1 = util.math.vectorScale([startToCenter(2), -startToCenter(1)], arcLen(i));
%   dir2 = util.math.vectorScale([-startToCenter(2), startToCenter(1)], arcLen(i));
  
  startToEnd = bot.pos(i,1:2) - bot.pos(1,1:2);
  
  dir = dir2;
  if(norm(dir1 - startToEnd) < norm(dir2 - startToEnd))
    dir = dir1;
  end
  
  compVelGlob(i,:) = dir .* 1/(dt(i)*1000);
  velSE(i,1:2) = util.math.convertGlobalBotVector2Local(compVelGlob(i,:), bot.pos(1,3));
  velSE(i,3) = aVel;
  
%   plotCircle( c,r );
end




mu = mean(velSE(end-40:end,:));
muDiff = mean(velDiffLocal);
fprintf('%s %d %f %f %f; %f;  %f %f %f\n',file, size(velSE,1), mu, aVelMu, muDiff);


%
figure(2);
subplot(2,2,1); 
hold all;
plot(velSE(:,3))
title('w vel');

subplot(2,2,2); 
hold all;
plot(time,angleDiff)
plot(fr)
title('angleDiff');

subplot(2,2,3); 
hold all;
plot(dt);
title('dt');

subplot(2,2,4); 
hold all;
plot(time, bot.pos(:,3))
title('orientation');

figure(3)
subplot(2,2,1);
hold all
plot(velSE(:,1:2))
title('xy vel');

subplot(2,2,2);
hold all;
plot(bot.pos(:,1), bot.pos(:,2));
axis equal
title('x -> y pos')

subplot(2,2,3);
hold all;
plot(time,bot.pos(:,1:2));
title('time -> x,y pos');

subplot(2,2,4);
hold all;
% plot(time,arcLen);
plot(velDiffLocal(:,1:2));
title('vel diff');

end
