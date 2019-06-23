D=importdata('/tmp/testControl.csv');
T=importdata('/tmp/state.csv');

time = D(:,1);
state.pos = T(:,1:2);
state.ori = T(:,3);
state.dir = T(:,4);
state.v = T(:,5);
state.o = T(:,6);
state.e = T(:,7);

if ~exist('lastState','var')
  lastState = state;
end

close all

figure
subplot(2,1,1)
plot(D(:,1),D(:,[5,11]))
hold on
plot(D(:,1),D(:,[14])/1000)
subplot(2,1,2)
plot(D(:,1),D(:,[2,8]))

figure
subplot(2,1,1)
plot(D(:,1),D(:,[7,13]))
hold on
plot(D(:,1),D(:,[16]))
legend('wp vel','set vel','ctrl');
subplot(2,1,2)
plot(D(:,1),D(:,[4,10]))
legend('wp pos','set pos')

figure
subplot(2,3,1)
plot(time,state.dir)
hold on;
plot(time,lastState.dir);
title('dir')

subplot(2,3,2)
plot(time,state.v)
hold on;
plot(time,lastState.v);
title('v')

subplot(2,3,3)
plot(time,state.o)
hold on;
plot(time,lastState.o);
title('avel')

subplot(2,3,4)
plot(time,D(:,10));
hold on
plot(time,state.ori)
plot(time,lastState.ori);
title('orientation')

subplot(2,3,5)
plot(time,D(:,8));
hold on
plot(time,state.pos(:,1))
plot(time,lastState.pos(:,1))
title('x')

subplot(2,3,6)
plot(time,D(:,9));
hold on
plot(time,state.pos(:,2))
plot(time,lastState.pos(:,2))
title('y')

lastState = state;