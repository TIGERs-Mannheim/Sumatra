directory = '../logs/ballChip/labWithLength/';
ls(directory);
dirlist = dir(strcat(directory,'*.csv'));

X = zeros(length(dirlist),2);
Y_dur = zeros(length(dirlist),1);
Y_dribble = zeros(length(dirlist),1);
for i = 1:length(dirlist)
  fileName = strcat(directory, dirlist(i).name);
  D = importdata(fileName);
  [chipDist rollDist] = chipDistApprox(dirlist(i).name);
  X(i,1:2) = [chipDist rollDist];
  dribbleSpeed = D(end,1);
  duration = D(end,2);
  Y_dur(i) = duration;
  Y_dribble(i) = dribbleSpeed;
end

% X = D(:,1:2);
% Y_dur = D(:,3);
% Y_dribble = D(:,4);

PHI = zeros(4);

phi = @(x,y) [x x.^2 y y.^2]';

for i=1:length(Y_dur)
  PHI(i,:) = phi(X(i,1), X(i,2))';
end

theta_dur = (PHI'*PHI)^-1 * PHI' * Y_dur;
theta_dribble = (PHI'*PHI)^-1 * PHI' * Y_dribble;


fy_dur = @(x,y) phi(x,y)'*theta_dur;
fy_dribble = @(x,y) phi(x,y)'*theta_dribble;

% Create a grid of x and y points
points = linspace(0, 4, 50);
[Xs, Ys] = meshgrid(points, points);

Zs_dur = zeros(length(points));
Zs_dribble = zeros(length(points));
for y=1:length(points)
  for x=1:length(points)
    Zs_dur(y,x) = fy_dur(points(x),points(y));
    Zs_dribble(y,x) = fy_dribble(points(x),points(y));
  end
end

figure;
hold all;
plot3(X(:,1),X(:,2),Y_dur,'.');
plot3(Xs, Ys, Zs_dur);
xlabel('distance');
ylabel('roll');
zlabel('duration');

figure;
hold all;
plot3(X(:,1),X(:,2),Y_dribble,'.');
plot3(Xs, Ys, Zs_dribble);
xlabel('distance');
ylabel('roll');
zlabel('dribble');