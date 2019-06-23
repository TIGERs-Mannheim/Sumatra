directory = '../data/ball/moduli_2015/ballKick/';
dirlist = dir(strcat(directory,'2015*'));
sampleFiles = cell(length(dirlist),1);
for i = 1:length(dirlist)
  sampleFiles{i} = strcat(directory, dirlist(i).name);
end

figure
hold all;
for f=length(sampleFiles):-1:(length(sampleFiles)-60)
  folder = sampleFiles{f};
  wpBall = data.loadWpBall(strcat(sampleFiles{f}, '/wpBallTest.csv'));
  vel = sqrt(wpBall.vel(:,1) .* wpBall.vel(:,1) + wpBall.vel(:,2) .* wpBall.vel(:,2));
  acc = sqrt(wpBall.acc(:,1) .* wpBall.acc(:,1) + wpBall.acc(:,2) .* wpBall.acc(:,2));
  
  [~,maxVelIdx] = max(vel);
  lowVelIdxes = find(vel(maxVelIdx+1:end)<1.5);
  if numel(lowVelIdxes) == 0
    continue;
  end
  lowVelIdx = maxVelIdx + lowVelIdxes(1) - 1;
  
  idx = (maxVelIdx:lowVelIdx);
  
  
%   idx = vel > 0.5;
%   if ~any(idx)
%     continue
%   end
  timestamp = wpBall.timestamp(idx);
  vel = vel(idx);
  time = (timestamp - timestamp(1)) * 1e-9;
  plot(time, vel)
%     plot(time, acc(idx));


%   dt = (timestamp(2:end) - timestamp(1:end-1)) * 1e-9;
%   idx = dt>1e-3;
%   dt = dt(idx);
%   vel = vel(idx);
%   velDiff = [vel(2:end)-vel(1:end-1); 0];
%   timestamp = timestamp(idx);
%   
%   acc = velDiff ./ dt;
%   plot(vel, acc,'.');

%   plot(time(idxx), vel(idxx))
%   plot(time(idxx), acc(idxx));
end