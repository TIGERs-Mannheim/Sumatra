function params = fric(filename)

%% Load data
csv = csvread(filename);
% csv = csvread('2016-09-26_23-43-07.csv');
% csv = csvread('2016-09-27_23-48-13.csv');
% csv = csvread('2016-09-27_23-58-22.csv');
% csv = csvread('2016-09-28_00-06-43.csv');
% csv = csvread('2016-09-28_00-20-14.csv');
% csv = csvread('2016-09-28_00-23-50.csv');
% csv = csvread('2016-09-28_00-39-00.csv');
% csv = csvread('2016-09-28_19-55-56.csv');

%% Extract fields and convert to SI units
startPos = csv(:,1:2)*1e-3;
endPos = csv(:,4:5)*1e-3;
targetPos = csv(:,7:8)*1e-3;
orient = csv(:,9);

numSamples = length(orient);
diff = (targetPos-endPos);
diffSet = targetPos-startPos;
dist = sqrt(diffSet(:,1).^2+diffSet(:,2).^2);
diff = diff./repmat(dist, 1, 2);
orientSumatra = orient + repmat([0; -pi], numSamples/2, 1);
orientBot = pi/2 - orientSumatra;

%% Rotate difference vectors to local robot frame
for i = 1:numSamples
    a = pi/2-orient(i);
    rotMat = [cos(a) -sin(a); sin(a) cos(a)];
    
    diff(i,:) = rotMat*diff(i,:)';
end

data = sortrows([orientBot diff(:,1) diff(:,2)]);

%% Fit functions to X/Y differences
fitXErr = fittype('a1*cos(x) + a3*cos(3*x) + a5*cos(5*x)');
[modXErr, gofX] = fit(data(:,1), data(:,2), fitXErr, 'StartPoint', [0.2 0.1 0.1])

fitYErr = fittype('b1*sin(x) + b3*sin(3*x) + b5*sin(5*x)');
[modYErr, gofY] = fit(data(:,1), data(:,3), fitYErr, 'StartPoint', [0.2 0.1 0.1])

params = [modXErr.a1, modXErr.a3, modXErr.a5, ...
    modYErr.b1, modYErr.b3, modYErr.b5, ...
    gofX.adjrsquare*100, gofY.adjrsquare*100];

return;

%% Output C code for embedded correction function
fprintf('const float a[3] = {%.8f, %.8f, %.8f};\n', ...
    modXErr.a1, modXErr.a3, modXErr.a5);
fprintf('const float b[3] = {%.8f, %.8f, %.8f};\n', ...
    modYErr.b1, modYErr.b3, modYErr.b5);

%% Sample data and display functions
modY = modYErr(data(:,1));
modX = modXErr(data(:,1));

figure(1);
plot(data(:,1), data(:,2), 'rx', data(:,1), data(:,3), 'bo', ...
    data(:,1), modX, 'm-', data(:,1), modY, 'c-');
title('90 deg front');
legend('X sample', 'Y sample', 'X model', 'Y model');
axis tight;
grid on;
grid minor;
