ls /home/geforce/workspace/Sumatra/logs/movement*
T = importdata('/home/geforce/workspace/Sumatra/logs/movement_bot_0_10.csv');

% pos vel acc setvel velOut error motor
% 2: 1m straight, PID=1,0,0
% 3: 1m straight, PID=1,0.1,0
% 4: xvel=1->- yvel=1->0, PID=1,0.1,0
% 5: xvel=1->- yvel=1->0, PID=1,0,0
% 6: manual vel, each dir; PID=0,0,0
% 7: straight (x) 0.1->0.2->0.3->0.4->0.3->0.2->0.1->0.5->0
% 8: no w controlling
% 50: data capturing
% 9_2: data capturing from field 9.1.14

pos = T(:,1:3);
vel = T(:,4:6);
acc = T(:,7:9);
setVel = T(:,10:12);
velOut = T(:,13:15);
error = T(:,16:18);
% slippage = T(:,19:22);
motorSet = T(:,19:22);
motorCur = T(:,23:26);
motorOut = T(:,27:30);
time = T(:,31);



% t1=80;
% t2=140;
% dt=time(t2)-time(t1);
% dx=pos(t2,1)-pos(t1,1);
% dy=pos(t2,2)-pos(t1,2);
% ds=sqrt(dx^2+dy^2);
% v=ds/(dt/1000);
% v

for i=1:size(T,1)-1
  DT(i)=time(i+1)-time(i);
end
mut = mean(DT);
dt=mut/1000; % s

ddy=0;
for i=1:size(T)
  ddy=ddy+pos(i,2)*dt;
end
ddy

f = @(x) 1.7*x;
compSetVel = f(setVel);

monitors = [2 1];

f=figure(1);
placefigure('NW',[1/3 1/2],monitors(1),f);
clf;
plot(pos);
title('state.pos xyw');
legend('x','y','w');

f=figure(2);
placefigure('N',[1/3 1/2],monitors(1),f);
clf;
plot(vel);
title('state.vel xyw');
legend('x','y','w');

f=figure(3);
placefigure('NE',[1/3 1/2],monitors(1),f);
clf;
plot(acc);
title('state.acc xyw');
legend('x','y','w');

f=figure(4);
placefigure('SW',[1/3 1/2],monitors(1),f);
clf;
plot(setVel);
title('setvel');
legend('x','y','w');

f=figure(5);
placefigure('S',[1/3 1/2],monitors(1),f);
clf;
plot(velOut);
title('velOutput');
legend('x','y','w');

f=figure(6);
placefigure('SE',[1/3 1/2],monitors(1),f);
clf;
plot(error);
title('error');
legend('x','y','w');

f=figure(7);
placefigure('NW',[1/2 1/2],monitors(2),f);
clf;
plot(setVel(:,1) ./ velOut(:,1));
title('setvel / outVel');

f=figure(8);
placefigure('NE',[1/2 1/2],monitors(2),f);
clf;
plot(motorSet);
title('motorSet');

f=figure(9);
placefigure('SW',[1/2 1/2],monitors(2),f);
clf;
plot(motorCur);
title('motorCur');

f=figure(10);
placefigure('SE',[1/2 1/2],monitors(2),f);
clf;
plot(motorOut);
title('motorOut');

% f=figure(10);
% placefigure('SE',[1/2 1/2],monitors(2),f);
% clf;
% plot(velOut(:,1), vel(:,1));
% title('outVel->realVel');


