neuralwp = data.loadWpBall('../data/neuralBALLdata.csv');
kalmanwp = data.loadWpBall('../data/kalmanBALLdata.csv');
rawdata = data.loadRawBall('../data/rawBALLdata.csv');

neuralBot = data.loadWpBots('../data/neuralBOTdata.csv');
kalmanBot = data.loadWpBots('../data/kalmanBOTdata.csv');
rawBot = data.loadRawBots('../data/rawBOTdata.csv');

time = (neuralwp.timestamp-neuralwp.timestamp(1))/1e6;

figure;
subplot(2,2,1); title('x->y');hold all;
plot(neuralwp.pos(:,1),neuralwp.pos(:,2),'-');
plot(kalmanwp.pos(:,1),kalmanwp.pos(:,2),'-');
plot(rawdata.pos(:,1),rawdata.pos(:,2),'-');
legend( 'neural xy','kalman xy', 'actual xy');
xlabel('x [mm]');
ylabel('y [mm]');

subplot(2,2,2); title('time->pos');hold all;
plot(time,neuralwp.pos(:,1),'-');
plot(time,neuralwp.pos(:,2),'-');
plot(time,kalmanwp.pos(:,1),'-');
plot(time,kalmanwp.pos(:,2),'-');
plot(time,rawdata.pos(:,1),'-');
plot(time,rawdata.pos(:,2),'-');
legend( 'neural x','neural y','kalman x','kalman y','actual x','actual y');
xlabel('t [ms]');
ylabel('x,y [mm]');

subplot(2,2,3); title('idx->pos');hold all;
plot(neuralwp.pos(:,1),'-');
plot(neuralwp.pos(:,2),'-');
plot(kalmanwp.pos(:,1),'-');
plot(kalmanwp.pos(:,2),'-');
plot(rawdata.pos(:,1),'-');
plot(rawdata.pos(:,2),'-');
legend( 'neural x','neural y','kalman x','kalman y','actual x','actual y');
xlabel('idx [-]');
ylabel('y [mm]');



figure;
subplot(2,2,1); title('x->y');hold all;
plot(neuralBot.pos(:,1),neuralBot.pos(:,2),'-');
plot(kalmanBot.pos(:,1),kalmanBot.pos(:,2),'-');
plot(rawBot.pos(:,1),rawBot.pos(:,2),'-');
legend( 'neural xy','kalman xy', 'actual xy');
xlabel('x [mm]');
ylabel('y [mm]');

subplot(2,2,2); title('time->pos');hold all;
plot(time,neuralBot.pos(:,1),'-');
plot(time,neuralBot.pos(:,2),'-');
plot(time,kalmanBot.pos(:,1),'-');
plot(time,kalmanBot.pos(:,2),'-');
plot(time,rawBot.pos(:,1),'-');
plot(time,rawBot.pos(:,2),'-');
legend( 'neural x','neural y','kalman x','kalman y','actual x','actual y');
xlabel('t [ms]');
ylabel('x,y [mm]');

subplot(2,2,3); title('idx->pos');hold all;
plot(neuralBot.pos(:,1),'-');
plot(neuralBot.pos(:,2),'-');
plot(kalmanBot.pos(:,1),'-');
plot(kalmanBot.pos(:,2),'-');
plot(rawBot.pos(:,1),'-');
plot(rawBot.pos(:,2),'-');
legend( 'neural x','neural y','kalman x','kalman y','actual x','actual y');
xlabel('idx [-]');
ylabel('y [mm]');