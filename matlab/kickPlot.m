%close all;

rootPath = '/home/dirk/tigers/ballKick (2)';
%/home/dirk/tigers/workspace/Sumatra/data/vision/moduli_sumatra/ballKick';

dirs = dir(rootPath);

x = zeros(0,1);
y = zeros(0,1);

figure

countAll = 0;
countAbort = 0;
countAbortLateRecordStart = 0;
for i=1:size(dirs,1)
  if strncmp(dirs(i).name, '2016', 4)
    countAll = countAll + 1;
    wpBall = data.loadWpBall(sprintf('%s/%s/wpBall.csv', rootPath, dirs(i).name));
    info = data.loadInfo(sprintf('%s/%s/info.json', rootPath, dirs(i).name));
    
    vel = sqrt(wpBall.vel(:,1).^2 + wpBall.vel(:,2).^2);
    if(size(vel,1) < 100 || any(vel(1:100) ~= 0))
        countAbortLateRecordStart = countAbortLateRecordStart + 1;
        %continue;
    end
    hvel = vel(vel>0.1);
%     maxVel = vel(1);
    maxVel = max(vel);
    kickVel = info.kickSpeed;
    
    if maxVel > 8 || maxVel < 1
      countAbort = countAbort + 1;
      continue;
    end
    x(end+1) = maxVel;
    y(end+1) = kickVel;
    
%     plot(wpBall.time, vel)
    
    fprintf('%f %f\n', kickVel, maxVel);
    
%     pause
%     D = data.loadAll(thisDir);
  end
end
countAll
countAbort
countAbortLateRecordStart

% Fit a line with RANSAC
% inilers percentage
p = 0.15;
% noise
sigma = 0.05;

% set RANSAC options
options.epsilon = 1e-6;
options.P_inlier = 0.99;
options.sigma = sigma;
options.mode = 'MSAC';
options.man_fun = @error_line;
options.est_fun = @estimate_line;
options.Ps = [];
options.notify_iters = [];
options.min_iters = 1000;
options.fix_seed = false;
options.reestimate = true;
options.stabilize = false;
[results, options] = RANSAC([x; y], options);
y_func = -results.Theta(1)/results.Theta(2)*x - results.Theta(3)/results.Theta(2);
%[x_func,y_func] = pol2cart(theta,rho);
% y_func = (rho - x* cos(theta) )/ sin(theta);
%%
x0 = 0;
b = -results.Theta(1)/results.Theta(2)*x0 - results.Theta(3)/results.Theta(2)

x1 = 1;
m = -results.Theta(1)/results.Theta(2)*x1 - results.Theta(3)/results.Theta(2) - b


figure
hold on
plot(x,y,'o')
plot(x,y_func,'-')
hold off


