%% clear results
clear
dataSets = [];

%% batch load
batchFolder = '../data/timeSeries/moduli_nicolai/full';

folders = dir(batchFolder);
for f=1:length(folders)
   folderName = folders(f).name; 
   if startsWith(folderName, '2018')
      D = data.loadAll([folders(f).folder, '/', folderName]);
      [redirectAngle, receiveSpeed, kickSpeed, angleQuality, speedQuality] = detectAngle(D, false);
      desiredAngle = D.info.desiredRedirectAngle;
      if isfield(D.info, 'desiredKickSpeed')
      	desiredKickSpeed = D.info.desiredKickSpeed;
      else
      	desiredKickSpeed = 6.5;
      end
      dataSets(end+1,:) = [desiredAngle, redirectAngle, receiveSpeed, desiredKickSpeed, kickSpeed, angleQuality, speedQuality];
      if isfinite(redirectAngle)
          fprintf('%s: angle: %6.3f == %6.3f (diff:%6.3f | quality:%6.3f) | recv: %6.3f | speed: %6.3f == %6.3f (diff:%6.3f | quality:%6.3f)\n', ...
              folderName, ...
              desiredAngle, redirectAngle, redirectAngle - desiredAngle, angleQuality, ...
              receiveSpeed, ...
              desiredKickSpeed, kickSpeed, kickSpeed - desiredKickSpeed, speedQuality);
      else
          fprintf('%s: invalid\n', folderName);
      end
   end
end

fprintf('Found %d data sets.\n', size(dataSets,1));

dataSets = dataSets(isfinite(dataSets(:,2)), :);
fprintf('Found %d valid data sets.\n', size(dataSets,1));

result = dataSets;

%% remove data sets with bad quality

result = result(abs(result(:,6))<3*std(result(:,6)),:);
fprintf('Reduced to %d data sets with good angle quality\n', size(result,1));
result = result(abs(result(:,7))<3*std(result(:,7)),:);
fprintf('Reduced to %d data sets with good speed quality\n', size(result,1));


%% plot speed distribution
figure
subplot(1,2,1);
histogram(result(:,3), 30);
title('receive speed')

subplot(1,2,2);
histogram(result(:,4) - result(:,5), 30);
title('kick speed diff')

%% plot quality distribution
figure
subplot(1,2,1);
histogram(result(:,6))
title('Quality of angle')

subplot(1,2,2);
histogram(result(:,7))
title('Quality of speed')

%% plot data distribution
figure
X = [result(:,2), result(:,4)];
hist3(X,'CDataMode','auto','FaceColor','interp')
xlabel('angle')
ylabel('speed')

%% plot redirect angle -> angle difference
figure
scatter(result(:,1), result(:,1) - result(:,2), (normalize(result(:,4), 'range') * 100)+1)
xlabel('desired redirect angle');
ylabel('desired - actual');

%% plot desired kick speed -> kick speed difference
figure
scatter(result(:,4), result(:,4) - result(:,5), (normalize(result(:,1), 'range') * 100)+1); 
xlabel('desired kick speed')
ylabel('desired - actual');
legend('circle size: redirectAngle')

%% plot receive speed -> kick speed difference
figure
scatter(result(:,3), result(:,4) - result(:,5), (normalize(result(:,1), 'range') * 100)+1); 
xlabel('receive speed')
ylabel('desired - actual');
legend('circle size: redirectAngle')

%% plot two different receive speeds
low=result(:,3)<2.1;
high=~low;
figure
scatter(result(low,1), result(low,1) - result(low,2), (normalize(result(low,4), 'range') * 100)+1)
hold all;
scatter(result(high,1), result(high,1) - result(high,2), (normalize(result(high,4), 'range') * 100)+1)
xlabel('desired redirect angle');
ylabel('angle difference');
legend('low', 'high');

%% fit, assuming that receive speed has no influence on the angle
in = [result(:,2), result(:,4)];
out = result(:,2) - result(:,1);
[ft,gof] = fit(in, out, 'poly11', 'Robust', 'On')

figure
subplot(1,2,1)
plot(ft)
hold all
plot3(in(:,1), in(:,2), out, 'x')
xlabel('actual angle')
ylabel('velOut')
zlabel('correction')

subplot(1,2,2)
q = quiver3(in(:,1), in(:,2), ft(in), zeros(size(out)), zeros(size(out)), out - ft(in), 1);
q.ShowArrowHead = 'off';
q.AutoScale = 'off';
xlabel('actual angle')
ylabel('velOut')
zlabel('correction')

%% fit with ployfitn
w = result(:,2); % actual angle
x = result(:,3); % velIn
y = result(:,5); % velOut
z = result(:,1) - result(:,2);
p = polyfitn([w, x, y], z,'constant w w^2 x x^2 x^3 y y^2');

low=x<2.1;
high=~low;

figure;
[xg,yg]=meshgrid( 0 : 0.1 : 2.0 , 1 : 0.1 : 7 );
zg = polyvaln(p,[xg(:), ones(numel(xg),1)*3.5, yg(:)]);
zg = reshape(zg,size(xg));
surf(xg,yg,zg)
hold all
plot3(w(low),y(low),z(low),'o')
plot3(w(high),y(high),z(high),'o')

xlabel('actual angle')
ylabel('velOut')
zlabel('correction')
