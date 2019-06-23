%
clc;
clear;

filebase = cell(1,0);
% filebase{end+1} = {'1442998723475','Default Model'};
% filebase{end+1} = {'1443018086616','combined model'};
% filebase{end+1} = {'1443020291294','LINone(1) Noise(1.5) based on 1442998723475-filtered'};
% filebase{end+1} = {'1443022954469','LINone(1) Noise(1.5) based on 1443020291294-filtered'};
% filebase{end+1} = {'1443025052815','CovSEiso(1,1) LINone(1) Noise(1.5) based on 1443022954469-filtered'};

% filebase{end+1} = {'1443034797304','Sumatra Default Model'};
% filebase{end+1} = {'1443038366583','Sumatra GP'};

% filebase{end+1} = {'1443186736959','Default Model after sample fix'};
% filebase{end+1} = {'1443187610239','combined Model after sample fix'};

% filebase{end+1} = {'1443196822587','combined Model with noise=5, continuous'};
% filebase{end+1} = {'1443197302243','combined Model with noise=5, 1,0,0'};
% filebase{end+1} = {'1443197617889','combined Model with noise=5, 0,1,0'};
% filebase{end+1} = {'1443197777121','combined Model with noise=0, 0,1,0'};
% filebase{end+1} = {'1443197848001','combined Model with noise=0, 1,0,0'};

% filebase{end+1} = {'fullSampler/1443443337664','Default Model, default tires'};
% filebase{end+1} = {'fullSampler/1443444589710','combined Model, default tires'};
% filebase{end+1} = {'fullSampler/1443446210286','GP Model 1443443337664, default tires'};
% filebase{end+1} = {'fullSampler/1443447658334','GP Model 1443446210286, default tires'};
% filebase{end+1} = {'fullSampler/1443449170510','Default Model, small x-tires'};
% filebase{end+1} = {'fullSampler/1443450398270','GP Model 1443449170510, small x-tires'};
% filebase{end+1} = {'fullSampler/1443452027150','GP Model 1443450398270, small x-tires'};
% filebase{end+1} = {'fullSampler/1443453229070','GP Model 1443449170510,1443450398270,1443452027150, small x-tires (low+high vels)'};
% filebase{end+1} = {'fullSampler/1443453658319','GP Model 1443449170510,1443450398270,1443452027150, small x-tires (high vels)'};
% filebase{end+1} = {'fullSampler/1443455403007','GP Model 1443449170510,1443450398270,1443452027150, large x-tires'};
% filebase{end+1} = {'fullSampler/1443456107486','GP Model 1443449170510,1443450398270,1443452027150, large x-tires (high vels)'};
% filebase{end+1} = {'fullSampler/1443457491695','Default Model, large x-tires '};
% filebase{end+1} = {'fullSampler/1443457995119','GP Model 1443449170510,1443450398270,1443452027150, small x-tires (high vels), bot15-4S'};
% filebase{end+1} = {'fullSampler/1443458784382','Default Model, small x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1443459916190','GP Model 1443449170510,1443450398270,1443452027150, small x-tires, bot15-4S, high vel side'};
% filebase{end+1} = {'fullSampler/1443460294975','GP Model 1443449170510,1443450398270,1443452027150, large x-tires, bot15-4S, high vel side'};
% filebase{end+1} = {'fullSampler/1443461783390','GP Model 1443449170510,1443450398270,1443452027150, large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1443709032494','Default Model, large x-tires, bot15-4S, incomplete'};
% filebase{end+1} = {'fullSampler/1443709574894','Default Model, large x-tires, bot15-4S, incomplete'};
% filebase{end+1} = {'fullSampler/1443714258384','Default Model, large x-tires, bot15-4S, incomplete'};
% filebase{end+1} = {'gpOptimizer/1443715108570','? Model, large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1443786240745','Default Model, large x-tires, bot15-4S'};
% filebase{end+1} = {'fullSampler/1443786563881','Default Model, large x-tires, bot15-4S'};
% filebase{end+1} = {'fullSampler/1443787718600','GP Model 1443786240745,1443786563881, large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1443789174552','Default Model, large x-tires, bot15-4S'}; % samples/files-1443789174552
% filebase{end+1} = {'gpOptimizer/1443794196159','GP Model 1443789174552, large x-tires, bot15-4S'};
% filebase{end+1} = {'gpOptimizer/1443795549125','GP Model 1443789174552, large x-tires, bot15-4S'};
% filebase{end+1} = {'fullSampler/1443797003780','GP Model 1443789174552,1443794196159,1443795549125, large x-tires, bot15-4S'};
% filebase{end+1} = {'fullSampler/1443805586072','GP Model 1443789174552,1443794196159,1443795549125,1443797003780, large x-tires, bot15-4S'};
% 
% filebase{end+1} = {'fullSampler/1443806506970','combination Model, large x-tires, bot15-4S'};
% 
% % filebase{end+1} = {'imm','interpolation'};
% % filebase{end+1} = {'mm','matrix motor model'};
% 
% filebase{end+1} = {'fullSampler/1444381979281','interpolation Model, large x-tires, bot15-4S'};
% filebase{end+1} = {'fullSampler/1444383695510','interpolation Model, large x-tires, bot15-4S'};
% 
% filebase{end+1} = {'fullSampler/1444395505552','interpolation Model, large x-tires, bot15-4S'};
% filebase{end+1} = {'fullSampler/1444396186781','interpolation Model, large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1444399076462','GP V2 Model interp., large x-tires, bot15-4S'};
% filebase{end+1} = {'fullSampler/1444400439982','GP V1 Model interp., large x-tires, bot15-4S'};
% filebase{end+1} = {'fullSampler/1444402463777','GP V1 Model., large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1444405672835','GP V1 Model interp., large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1444407850609','matrix rotate, large x-tires, bot15-4S'};

% filebase{end+1} = {'gpOptimizer/1444409089169','GP sample, large x-tires, bot15-4S'};
% filebase{end+1} = {'gpOptimizer/1444409588433','GP sample, large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1444411041026','GP V2 Model, large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1444411390146','GP V2 Model, large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1444720702765','matrix rotate, large x-tires, bot15-4S'};
% filebase{end+1} = {'fullSampler/1444720619941','matrix rotate, large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1444721989723','GP V2 Model, large x-tires, bot15-4S'};
% filebase{end+1} = {'fullSampler/1444723538526','GP V2 Model interpolation 1;1, large x-tires, bot15-4S'};
% filebase{end+1} = {'fullSampler/1444725098656','GP V2 Model interpolation 2;0.5, large x-tires, bot15-4S'};
% filebase{end+1} = {'fullSampler/1444726936720','GP V2 Model interpolation 2;0.1, large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1445000888007','clever one interpolation, large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1445006227519','Default Model, large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1459685840657','old bot 5 - 3c '};
% filebase{end+1} = {'fullSampler/1459686275644','old bot 5-2 - 3c '};
% filebase{end+1} = {'fullSampler/1459687240563','old bot 11 - 3c '};

% filebase{end+1} = {'fullSampler/1459669783492','New Bot - 4S'};
% filebase{end+1} = {'fullSampler/1459686732436','new bot 8- 4c '};
% filebase{end+1} = {'fullSampler/1459687759488','New bot 10 - 4c '};
% filebase{end+1} = {'fullSampler/1459688624987','New bot 10 - 4c fine'};

% filebase{end+1} = {'fullSampler/1459857787698','New bot 7 - A'};
% filebase{end+1} = {'fullSampler/1459858201216','New bot 7 - B'};

% filebase{end+1} = {'fullSampler/1460028859960','old bot 2 - 4c vel cmd'};
% filebase{end+1} = {'fullSampler/1460029716118','old bot 2 - 4c vel cmd'};
filebase{end+1} = {'fullSampler/1460030605709','old bot 2 - 4c vel cmd'};

outVel = [];
motorVel = [];
setVel = [];
heading = [];
for i=1:size(filebase,2)
  T = importdata(strcat('../../logs/',filebase{i}{1},'.csv'));
  outVel   = [outVel; T(:,5:7)];
  motorVel = [motorVel; T(:,1:4)];
  setVel   = [setVel; T(:,8:10)];
  heading = [heading, filebase{i}{2}, char(10)];
end
nonRotationIdx = abs(setVel(:,3)) < 0.1;
rotationIdx = setVel(:,3) ~= 0;


%%
close all;


%%
figure
title(heading);
hold all;
plot(setVel(nonRotationIdx,1), setVel(nonRotationIdx,2), '.');
plot(outVel(nonRotationIdx,1), outVel(nonRotationIdx,2), '.');
scatter(setVel(nonRotationIdx,1), setVel(nonRotationIdx,2), max(1,100 * abs(outVel(nonRotationIdx,3))));
axis equal;
legend('set','out', 'rotation');
xlabel('vel x');
ylabel('vel y');
xlim([-2.5,2.5]);
ylim([-2.5,2.5]);
grid on;


%%
figure
title(heading);
hold all;
quiver(setVel(nonRotationIdx,1), setVel(nonRotationIdx,2), outVel(nonRotationIdx,1) - setVel(nonRotationIdx,1), outVel(nonRotationIdx,2) - setVel(nonRotationIdx,2), 0);
axis equal;
xlabel('vel x');
ylabel('vel y');
xlim([-2.5,2.5]);
ylim([-2.5,2.5]);
grid on;

%% Andre's velocity mapping stuff
% comment out this section to restore original functionality

setVelOrig = setVel(nonRotationIdx,:);
setVel = setVelOrig;
outVelOrig = outVel(nonRotationIdx,:);
outvel = outVelOrig;
outVel(:,3) = outVel(:, 3) .* ((setVel(:,1) > 0)-0.5)*2; %invert Z
setVel(:,1) = abs(setVel(:, 1));
outVel(:,1) = abs(outVel(:,1));
err = setVel-outVel;
orient = atan2(setVel(:,2), setVel(:,1));
speed = sqrt(setVel(:,1).^2+setVel(:,2).^2);
outSpeed = sqrt(outVel(:,1).^2+outVel(:,2).^2);

outSlow = outSpeed(speed < 0.6 & speed > 0.2,:);
outMedium = outSpeed(speed == 1,:);
outFast = outSpeed(speed > 1.2,:);
orientSlow = orient(speed < 0.6 & speed > 0.2,:);
orientMedium = orient(speed == 1,:);
orientFast = orient(speed > 1.2,:);
orientAll = orient;

% clf;
% figure;
% plot(orientSlow, outSlow/0.5, 'x', orientMedium, outMedium, 'o', orientFast, outFast/1.5, '*');
% return;


minSpeed = 0.4;
setVel = setVel(speed > minSpeed,:);
outVel = outVel(speed > minSpeed,:);
err = err(speed > minSpeed,:);
orient = orient(speed > minSpeed,:);
speed = speed(speed > minSpeed);

normErr = err./repmat(speed, 1, 3);
normErrX = normErr(:,1);
normErrY = normErr(:,2);
normErrW = normErr(:,3);

ft = fittype('fourier5');
fitY = fit(orient, normErrY, ft);

orientOrig = orient;

orient = [ones(100,1)*-pi/2; ones(100,1)*pi/2; orient];
normErrX = [zeros(100,1); zeros(100,1); normErrX];
normErrW = [zeros(100,1); zeros(100,1); normErrW];

fitX = fit(orient, normErrX, ft);
fitW = fit(orient, normErrW, ft);

sampleAngles = -pi/2:pi/128:pi/2;
numSamples = length(sampleAngles);
sampleXYW = [cos(sampleAngles') sin(sampleAngles') zeros(numSamples,1)];
outXYW = zeros(numSamples, 3);

for i = 1:numSamples
    outXYW(i,:) = evalFit(fitX, fitY, fitW, sampleXYW(i,:));
end

% plot approximated function result
quiver(sampleXYW(:,1), sampleXYW(:,2), outXYW(:,1) - sampleXYW(:,1), outXYW(:,2) - sampleXYW(:,2), 0);

% evalFit(fitX, fitY, fitW, [1 0 0])

inverXY = zeros(numSamples, 2);

for i = 1:numSamples
    errFunc = @(vel)norm(sampleXYW(i,1:2) - evalFitXY(fitX, fitY, vel));
    [x, fval] = fminsearch(errFunc, sampleXYW(i,1:2));
    inverXY(i,:) = x;
end

% plot invers calculations
quiver(inverXY(:,1), inverXY(:,2), sampleXYW(:,1)-inverXY(:,1), sampleXYW(:,2)-inverXY(:,2), 0);

invNormErrXY = (sampleXYW(:,1:2)-inverXY(:,1:2));
invNormErrX = invNormErrXY(:,1);
invNormErrY = invNormErrXY(:,2);

% figure;
% plot(sampleAngles, invNormErrXY(:,2), 'x');

ft = fittype('linearinterp');
fitX = fit(sampleAngles(1:4:end)', invNormErrX(1:4:end), ft)
fitY = fit(sampleAngles(1:4:end)', invNormErrY(1:4:end), ft)

setXY = zeros(numSamples, 2);

for i = 1:numSamples
    setXY(i,:) = evalFitXY(fitX, fitY, sampleXYW(i,1:2));
end

quiver(setXY(:,1), setXY(:,2), sampleXYW(:,1)-setXY(:,1), sampleXYW(:,2)-setXY(:,2), 0);

% print interpolation tables
% put in heading here and get set speed for 1m/s, scale accordingly
fprintf('static const float out2MotX[%d] = {\n', numSamples);
for i = 1:numSamples
    fprintf('%10.8f, ', setXY(i,1));
    
    if mod(i,8) == 0
        fprintf('\n');
    end
end
fprintf(' };\n');

fprintf('static const float out2MotY[%d] = {\n', numSamples);
for i = 1:numSamples
    fprintf('%10.8f, ', setXY(i,2));
    
    if mod(i,8) == 0
        fprintf('\n');
    end
end
fprintf(' };\n');

% add this value to W for rotation correction
fprintf('static const float out2MotW[%d] = {\n', numSamples);
for i = 1:numSamples
    fprintf('%10.8f, ', -outXYW(i,3));
    
    if mod(i,8) == 0
        fprintf('\n');
    end
end
fprintf(' };\n');

fprintf('static const float mot2InX[%d] = {\n', numSamples);
for i = 1:numSamples
    fprintf('%10.8f, ', outXYW(i,1));
    
    if mod(i,8) == 0
        fprintf('\n');
    end
end
fprintf(' };\n');

fprintf('static const float mot2InY[%d] = {\n', numSamples);
for i = 1:numSamples
    fprintf('%10.8f, ', outXYW(i,2));
    
    if mod(i,8) == 0
        fprintf('\n');
    end
end
fprintf(' };\n');



return;

x = -1;
y = 0;

mod = 1;
if x < 0
    mod = -1;
end
w = atan2(y,mod*x);
sp = sqrt(x^2+y^2);
errX = sp*fitX(w)*mod
errY = sp*fitY(w)

samples = -pi:0.01:pi;
fitXSamples = fitX(samples);

figure
plot(samples, fitXSamples);

return;


figure
plot((orient), err(:,2)./speed, 'x');
xlim([-pi/2 pi/2]);
grid on;

return;

%%
figure
title(heading);
hold all;
plot(motorVel(nonRotationIdx,:));


%% compare two
T = importdata(strcat('../logs/',filebase{1}{1},'.csv'));
outVel = T(:,5:7);
setVel = T(:,8:10);
T = importdata(strcat('../logs/',filebase{2}{1},'.csv'));
outVelRef = T(:,5:7);
setVelRef = T(:,8:10);

vel = [];
velRef = [];
velSet = [];
for i=1:size(setVel,1)
  idxRef = find(all(abs(repmat(setVel(i,:),[size(setVelRef,1),1]) - setVelRef) <0.001,2));
  for idx=idxRef'
    vel(end+1,:) = outVel(i,:);
    velRef(end+1,:) = outVelRef(idx,:);
    velSet(end+1,:) = setVel(i,:);
  end
end

nonRotationIdx = velSet(:,3) == 0;

figure
hold all;
title(heading);
quiver(vel(nonRotationIdx,1), vel(nonRotationIdx,2), velRef(nonRotationIdx,1) - vel(nonRotationIdx,1), velRef(nonRotationIdx,2) - vel(nonRotationIdx,2), 0);
axis equal;
xlabel('vel x');
ylabel('vel y');
xlim([-3,3]);
ylim([-3,3]);
grid on;

