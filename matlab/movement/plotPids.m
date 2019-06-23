T = importdata('tmp');

t = 1:length(T);
in = T(:,2:4);
out = T(:,5:7);
set = T(:,8:10);

figure

subplot(2,2,1);
title('PID in/set xy');
hold all;
plot(t, in(:,1:2),'.');
plot(t, set(:,1:2),'.');
legend('x in','y in','x set','y set');
xlabel('t [s]');
ylabel('pos [m]');

subplot(2,2,3);
title('PID out xy');
plot(t, out(:,1:2),'.');
legend('x out','y out');
xlabel('t [s]');
ylabel('[m/s]');

subplot(2,2,2);
title('PID in/set w');
hold all;
plot(t, in(:,3));
plot(t, set(:,3));
legend('w in','w set');
xlabel('t [s]');
ylabel('pos [rad]');

subplot(2,2,4);
title('PID out w');
plot(t, out(:,3));
legend('w out');
xlabel('t [s]');
ylabel('[rad/s]');