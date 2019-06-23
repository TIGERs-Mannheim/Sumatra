pCov = data.loadMatrix('../data/ball/model/pcov.csv', 2, 2);
vCov = data.loadMatrix('../data/ball/model/vcov.csv', 2, 2);
aCov = data.loadMatrix('../data/ball/model/acov.csv', 2, 2);
state = cell2mat(data.loadMatrix('../data/ball/model/state.csv', 1, 9));
pos = state(:,1:3);
vel = state(:,4:6);
acc = state(:,7:9);


figure;
hold all
title('pos');
for i=1:15:size(pCov,1)
  plot(pos(i,1),pos(i,2),'.');
  plotCovariance(pCov{i},pos(i,1),pos(i,2));
end
axis equal

figure;
hold all
title('vel');
for i=1:25:size(vCov,1)
  plot(vel(i,1),vel(i,2),'.');
  plotCovariance(vCov{i},vel(i,1),vel(i,2));
end
axis equal

figure;
hold all
title('acc');
for i=1:50:size(aCov,1)
  plot(acc(i,1),acc(i,2),'.');
  plotCovariance(aCov{i},acc(i,1),acc(i,2));
end
axis equal