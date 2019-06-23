hold all;
grid on;
box on;

X = importdata('splineTraj.csv'); %load csv
Y = X.data; 

t = Y(:,1);
px = Y(:,2);
py = Y(:,3);
vx = Y(:,4);
vy = Y(:,5);
ax = Y(:,6);
ay = Y(:,7);
v = sqrt(vx.^2+vy.^2);
a = sqrt(ax.^2+ay.^2);
dataSize = size(Y);
points = dataSize(1);

fig1 = figure(1);
set(fig1, 'Name', cat(2, 'Move Viewer'));
clf(fig1)

sp1 = subplot(2, 2, 1);
hold(sp1, 'on');
for i = 1:2:points
   line([px(i), px(i)+vx(i)], [py(i), py(i)+vy(i)], 'Color', 'green');
end
plot(px, py)
axis(gca, 'equal');
grid(gca, 'on');

sp2 = subplot(2, 2, 2);
hold (sp2, 'on');
plot(t, px, 'r');
plot(t, py, 'g');
xlabel('t [s]');
ylabel('p_x [m]');
grid(gca, 'on');

sp3 = subplot(2, 2, 3);
hold (sp3, 'on');
plot(t, vx, 'r');
plot(t, vy, 'g');
plot(t, v, 'b');
xlabel('t [s]');
ylabel('v_x [m/s]');
grid(gca, 'on');

sp4 = subplot(2, 2, 4);
hold (sp4, 'on');
plot(t, ax, 'r');
plot(t, ay, 'g');
plot(t, a, 'b');
xlabel('t [s]');
ylabel('a_x [m/s^2]');
grid(gca, 'on');
