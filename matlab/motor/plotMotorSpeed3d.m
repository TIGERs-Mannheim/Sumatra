function plotMotorSpeed3d(motorModel, sampleX, sampleY)

  angleStep = 0.2;
  speedStep = 0.2;
  speedMax = 2;

  angleRange = -pi:angleStep:pi;
  speedRange = 0:speedStep:speedMax;

  [angles, speeds] = meshgrid(angleRange, speedRange);

  vel = zeros(length(speedRange), length(angleRange), 3);
  vel(:,:,1) = cos(angles) .* speeds;
  vel(:,:,2) = sin(angles) .* speeds;
  vel(:,:,3) = 0 .* speeds;

  vel2d = reshape(vel,[size(vel,1)*size(vel,2), 3]);
  [ws2d, ws2d_s2] = motorModel.getWheelSpeed(vel2d);
  ws = reshape(ws2d, [size(vel,1), size(vel,2), 4]);
  ws_s2 = reshape(ws2d_s2, [size(vel,1), size(vel,2), 4]);

  maxRot = 1;
  filteredSamplesIdx = abs(sampleX(:,3)) < maxRot;
  sampleX = sampleX(filteredSamplesIdx,:);
  sampleY = sampleY(filteredSamplesIdx,:);
  
  sampleAngle = util.math.angleOfVector(sampleX(:,1:2));
  sampleSpeed = sqrt( sampleX(:,1) .* sampleX(:,1) + sampleX(:,2) .* sampleX(:,2) );
  sampleRot = abs(sampleX(:,3)) / maxRot;

  figure;
  for w=1:4
    subplot(2,2,w); title(sprintf('wheel %d',w));
    hold on;
    scatter3(sampleAngle, sampleSpeed, sampleY(:,w), 10 + 70*sampleRot);
    surf(angles, speeds, ws(:,:,w), ws_s2(:,:,w));
    xlabel('angle [rad]');
    ylabel('speed [m/s]');
    zlabel('wheel speed');
  end
