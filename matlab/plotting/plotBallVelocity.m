function plotBallVelocity( folder )
if ~exist('folder','var')
  folder = '/home/geforce/workspace/Sumatra/data/ball/moduli_2015/ballKick/2015-05-29_15-28-41';
end

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

dists = zeros(size(cam));
vel = zeros(size(cam));

tmp = pos(2:end,:) - pos(1:end-1,:);
dists(2:end,:) = sqrt(tmp(:,1).*tmp(:,1) + tmp(:,2).*tmp(:,2));
dts = [0; (timeNs(2:end) - timeNs(1:end-1)) * 1e-9];
vel(2:end,:) = dists(2:end) ./ (1000 * dts(2:end));
lastVel = 0;
for i=2:length(vel)
  if dts(i) < 1e-4
    vel(i) = lastVel;
  else
    lastVel = vel(i);
  end
end

time = (timeNs-timeNs(1))/1e9;

wpVelTheta = zeros(size(cam));
vxNonZero = find(wpVelDir(:,1)~=0);
wpVelTheta(vxNonZero) = atan(wpVelDir(vxNonZero,2)./wpVelDir(vxNonZero,1));
wpVelTheta(wpVelDir(:,2)~=0 & wpVelDir(:,1)==0) = pi/2;

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

end

