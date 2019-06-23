% timestamp = '1441897066164';
% timestamp = '1441897070277';
% timestamp = '1441897397317';
timestamp = '1443791392967';
vel.cam = importdata(strcat('../logs/samples/',timestamp, '_LOCAL_VEL_CAM.csv'));
% vel.wp = importdata(strcat('../logs/samples/',timestamp, '_LOCAL_VEL_WP.csv'));
% vel.bot = importdata(strcat('../logs/samples/',timestamp, '_LOCAL_VEL_BOT.csv'));
% vel.dist = importdata(strcat('../logs/samples/',timestamp, '_LOCAL_VEL_BY_DIST_CAM.csv'));
vel.se = importdata(strcat('../logs/samples/',timestamp, '_LOCAL_VEL_BY_START_END_CAM.csv'));

% gpos.cam = importdata(strcat('../logs/samples/',timestamp, '_GLOBAL_POS_CAM.csv'));
% gpos.wp = importdata(strcat('../logs/samples/',timestamp, '_GLOBAL_POS_WP.csv'));
% gpos.bot = importdata(strcat('../logs/samples/',timestamp, '_GLOBAL_POS_BOT.csv'));

close all

figure;
hold all;
plot(vel.cam(:,1)-vel.cam(1,1), vel.cam(:,2:4));
% plot(vel.wp(:,1)-vel.wp(1,1),  vel.wp(:,2:4));
% plot(vel.bot(:,1)-vel.bot(1,1), vel.bot(:,2:4));
% plot(vel.dist(:,1)-vel.dist(1,1), vel.dist(:,2:4));
plot(vel.se(:,1)-vel.se(1,1), vel.se(:,2:4));
% legend('cam x','cam y','cam w',...
%       'wp x','wp y','wp w', ...
%       'bot x','bot y','bot w');
% legend('cam x','cam y','cam w',...
%       'bot x','bot y','bot w',...
% );
% legend('cam x','cam y','cam w',...
%       'bot x','bot y','bot w',...
%       'se x','se y','se w');
% legend('se x','se y','se w');
% legend('cam x','cam y','cam w',...
%       'dist x','dist y','dist w',...
%       'se x','se y','se w');
legend('cam x','cam y','cam w',...
      'se x','se y','se w');
xlabel('time [ms]');
ylabel('vel [m/s] / [rad/s]');


% figure;
% hold all;
% plot(gpos.cam(:,1)-gpos.cam(1,1), gpos.cam(:,2:4));
% plot(gpos.wp(:,1)-gpos.wp(1,1),  gpos.wp(:,2:4));
% plot(gpos.bot(:,1)-gpos.bot(1,1), gpos.bot(:,2:4));
% legend('cam x','cam y','cam w',...
%       'wp x','wp y','wp w',...
%       'bot x','bot y','bot w');
% xlabel('time [ms]');
% ylabel('pos [m] / [rad]');


mu = zeros(size(vel.se,1),3);
for i=1:size(vel.se,1)
  mu(i,:) = mean(vel.se(1:i,2:4),1);
end
figure
plot(mu);
ylim([min([mu(:); 0] * 1.1),max(mu(:))*1.1]);