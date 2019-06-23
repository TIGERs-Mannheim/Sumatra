function [result, fitDuration, fitSpeed] = learnKickSpeeds(varargin)


if isempty(varargin)
  directory = '../data/ball/moduli_2015/ballKick/';
  dirlist = dir(strcat(directory,'2015*'));
  sampleFiles = cell(length(dirlist),1);
  for i = 1:length(dirlist)
    sampleFiles{i} = strcat(directory, dirlist(i).name);
  end
else
  sampleFiles = varargin;
end

durations = zeros(length(sampleFiles),1);
speeds = zeros(length(sampleFiles),1);

for i=1:length(sampleFiles)
  file = strcat(sampleFiles{i}, '/wpBallTest.csv');
  if exist( file, 'file' )
    wpBall = data.loadWpBall(file);
  else
    file = strcat(sampleFiles{i}, '/wpBall.csv');
    wpBall = data.loadWpBall(file);
  end
  info = data.loadInfo(strcat(sampleFiles{i}, '/info.json'));
  vel = sqrt(wpBall.vel(:,1) .* wpBall.vel(:,1) + wpBall.vel(:,2) .* wpBall.vel(:,2));
  [~, maxvelIdx] = max(vel);
  maxvel = vel(min(maxvelIdx+10,length(vel)));
  fprintf('%s duration: %d maxvel: %f\n', sampleFiles{i}, info.duration, maxvel);
  durations(i) = info.duration;
  speeds(i) = maxvel;
end

idx = speeds > 0 & speeds < 9;
durations = durations(idx);
speeds = speeds(idx);

result = zeros(12,1);

%% Fit: 'duration -> speed'.
[xData, yData] = prepareCurveData( durations, speeds );

% Set up fittype and options.
ft = fittype( 'poly3' );
opts = fitoptions( 'Method', 'LinearLeastSquares' );
opts.Normalize = 'on';
opts.Robust = 'Bisquare';
% opts.Robust = 'LAR';

% Fit model to data.
[fitresult, ~] = fit( xData, yData, ft, opts );
fitDuration = fitresult;

% Plot fit with data.
figure( 'Name', 'duration -> speed' );
h = plot( fitresult, xData, yData );
legend( h, 'speeds vs. durations', 'duration -> speed', 'Location', 'NorthEast' );
% Label axes
xlabel durations
ylabel speeds
grid on

result(1:6) = [fitresult.p4,fitresult.p3,fitresult.p2,fitresult.p1,mean(xData),std(xData)];
fprintf('duration -> speed: ');
fprintf('%f ',result(1:6));
fprintf('\n');


%% Fit: 'speeds -> duration'.
[xData, yData] = prepareCurveData( speeds, durations );

% Set up fittype and options.
ft = fittype( 'poly3' );
opts = fitoptions( 'Method', 'LinearLeastSquares' );
opts.Normalize = 'on';
% opts.Robust = 'LAR';
opts.Robust = 'Bisquare';

% Fit model to data.
[fitresult, ~] = fit( xData, yData, ft, opts );
fitSpeed = fitresult;

% Plot fit with data.
figure( 'Name', 'speeds -> duration' );
h = plot( fitresult, xData, yData );
legend( h, 'durations vs. speeds', 'speeds -> duration', 'Location', 'NorthEast' );
% Label axes
xlabel speeds
ylabel durations
grid on

result(7:12) = [fitresult.p4,fitresult.p3,fitresult.p2,fitresult.p1,mean(xData),std(xData)];
fprintf('speed -> duration: ');
fprintf('%f ',result(7:12));
fprintf('\n');
fprintf('\n');


figure
hold all;
xx=0:10:7000;
plot(xx,fitSpeed(fitDuration(xx)));
plot(xx,xx);

figure
hold all;
xx=0:0.1:9;
plot(xx,fitDuration(fitSpeed(xx)));
plot(xx,xx);
