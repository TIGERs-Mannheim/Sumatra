T = importdata('tmp');
% T = importdata('../data/simBot5.csv');

time = (T(:,1)-T(1,1))/1e9;

figure(1);
subplot(221);
plot(time);

subplot(222);
plot(time,T(:,2:3));
legend('t','trans t');

subplot(223);
plot(diff(time));
legend('t diff');

figure(2);
subplot(221);
plot(time,T(:,[4 7 10]));
legend('dest','set','is')
title('x');

subplot(223);
plot(time,T(:,[5 8 11]));
legend('dest','set','is')
title('y');

subplot(222);
plot(time,T(:,[6 9 12,31]));
legend('dest','set','is')
title('w');

subplot(224);
plot(time,T(:,[15 18 21]));
legend('err','out','in')
title('w');


figure(3);
subplot(221);
plot(time,T(:,[12,21]));

subplot(222);
