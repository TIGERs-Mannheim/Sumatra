function plotBallData( folder )
if ~exist('folder','var')
  folder = '/home/geforce/workspace/Sumatra/data/ball/moduli_2015/ballKick/2015-05-22_19-38-53';
end

addpath('plotting');

D = data.loadAll(folder);
if isfield(D,'rawBallCorrected')
  rawBall = D.rawBallCorrected;
else
  rawBall = D.rawBall;
end
if isfield(D,'wpBallUpdated')
  wpBall = D.wpBallUpdated;
else
  wpBall = D.wpBall;
end
cam = rawBall.camId;
timeNs = rawBall.timestamp;
pos = rawBall.pos;
wpVelDir = wpBall.vel(:,1:2);
wpVel = sqrt( wpVelDir(:,1).*wpVelDir(:,1) + wpVelDir(:,2).*wpVelDir(:,2));
wpPos = wpBall.pos;
wpAcc = wpBall.acc;

dists = zeros(size(cam));
vel = zeros(size(cam));

tmp = pos(2:end,:) - pos(1:end-1,:);
dists(2:end,:) = sqrt(tmp(:,1).*tmp(:,1) + tmp(:,2).*tmp(:,2));
dts = [0; (timeNs(2:end) - timeNs(1:end-1)) * 1e-9];
vel(2:end,:) = dists(2:end) ./ (1000 * dts(2:end));
validTimedBasedIdxs = find(dts > 1e-4);
lastVel = 0;
for i=2:length(vel)
  if dts(i) < 1e-4
    vel(i) = lastVel;
  else
    lastVel = vel(i);
  end
end
wpAccAbs = sqrt(wpAcc(:,1).*wpAcc(:,1) + wpAcc(:,2).*wpAcc(:,2));

time = (timeNs-timeNs(1))/1e9;

wpVelTheta = zeros(size(cam));
vxNonZero = find(wpVelDir(:,1)~=0);
wpVelTheta(vxNonZero) = atan(wpVelDir(vxNonZero,2)./wpVelDir(vxNonZero,1));
wpVelTheta(wpVelDir(:,2)~=0 & wpVelDir(:,1)==0) = pi/2;

allRawBallsCam = D.rawBalls.camId;
allRawBallsPos = D.rawBalls.pos;

if isfield(D, 'nearestBot')
  wpBotPos = D.nearestBot.pos;
else
  wpBotPos = D.wpBots.pos;
end
[~,idxMaxVel] = max(wpVel);
kickerBotPos = wpBotPos(idxMaxVel,:);

%%

figure;
subplot(2,2,1); title('WP and raw pos with cam info'); hold all;
plot(wpPos(:,1),wpPos(:,2));
plot([pos(1,1);pos(end,1)],[pos(1,2);pos(end,2)]);
plot(pos(1,1),pos(1,2),'*');
leg = cell(1,length(unique(cam))+3);
leg{1} = 'WP';
leg{2} = 'raw linear line';
leg{3} = 'raw start';
i=4;
for c=unique(cam)'
  plot(pos(cam==c,1),pos(cam==c,2),'.');
  leg{i} = sprintf('raw cam %d', c);
  i = i + 1;
end
legend(leg);
xlabel('x [mm]'); ylabel('y [mm]');
axis equal;

subplot(2,2,2); title('all raw pos with cam info'); hold all;
plot(wpPos(:,1),wpPos(:,2));
plot(wpPos(1,1),wpPos(1,2),'*');
leg = cell(1,length(unique(allRawBallsCam))+2);
leg{1} = 'WP';
leg{2} = 'raw start';
i=3;
for c=unique(allRawBallsCam)'
  plot(allRawBallsPos(allRawBallsCam==c,1),allRawBallsPos(allRawBallsCam==c,2),'.');
  leg{i} = sprintf('raw cam %d', c);
  i = i + 1;
end
legend(leg);
xlabel('x [mm]'); ylabel('y [mm]');
axis equal;


subplot(2,2,3); title('time -> pos'); hold all;
plot(time,wpPos(:,1),'.');
plot(time,wpPos(:,2),'.');
plot(time,pos(:,1),'.');
plot(time,pos(:,2),'.');
legend('wp x','wp y', 'raw x', 'raw y')
xlabel('time [s]');
ylabel('pos [mm]');

subplot(2,2,4); title('pos x->y'); hold all;
plot(pos(:,1),pos(:,2));
plot(wpPos(:,1),wpPos(:,2));
plotBot(kickerBotPos);
plotField(0,1000);
legend('raw','WP');
xlabel('x [mm]'); ylabel('y [mm]');


figure; 
subplot(2,2,1); title('time -> WP acc'); hold all;
plot(time, wpAcc(:,1:2));
plot(time, wpAccAbs);
legend('x','y', 'abs');
xlabel('time [s]'); ylabel('acc [m/s^2]');

subplot(2,2,2); title('time -> dist'); hold all;
plot(time(validTimedBasedIdxs), dists(validTimedBasedIdxs));
xlabel('time [s]'); ylabel('dist [mm]');

subplot(2,2,3); title('dt'); hold all;
plot(dts(validTimedBasedIdxs));
xlabel('frameId'); ylabel('dt [s]');

subplot(2,2,4); title('frameId -> dists'); hold all;
plot(dists(validTimedBasedIdxs));
xlabel('frameId'); ylabel('dist [mm]');


figure;
subplot(2,2,1); title('time -> vel'); hold all;
plot(time, wpVel);
plot(time, vel);
plot(time, -1-cam);
legend('WP Vel','raw vel','-camId-1');
xlabel('time [s]'); ylabel('vel [m/s]');

subplot(2,2,2); title('wp vel orientation'); hold all;
plot(time, wpVelTheta);
xlabel('time [s]'); ylabel('angle [rad]');
ylim([-pi,pi]);

subplot(2,2,3); title('time -> wp vel xy'); hold all;
plot(time, wpVelDir);
xlabel('time [s]'); ylabel('vel [m/s]');

subplot(2,2,4); title('wp vel x->y'); hold all;
plot(wpVelDir(:,1),wpVelDir(:,2),'.');
[~, maxVeli] = max(wpVel);
plot([0,wpVelDir(maxVeli,1)],[0,wpVelDir(maxVeli,2)]);
legend('xy','0 to maxVel');
xlabel('vel [m/s]'); ylabel('vel [m/s]');