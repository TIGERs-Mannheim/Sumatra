function plotMotorSpeed2dSpeed( motorModel, motorVel, outVel, setVel )
%
  speedStep = 0.01;
  speedMax = 2.5;
  speedRange = -speedMax:speedStep:speedMax;  

  % xy vel
  targetAngles = [0,pi/4,pi/2]';
  for i=1:size(targetAngles,1);
    vel = zeros(length(speedRange),3);
    for sr=1:length(speedRange)
      vel(sr,1:2) = util.math.vectorOfAngle(targetAngles(i)) * speedRange(sr);
    end
    
    [ws, ws_s2] = motorModel.getWheelSpeed(vel);
    
    targetAngle = targetAngles(i);
    
    if exist('outVel','var')
      angles = util.math.angleOfVector(setVel(:,1:2));
      validIdx = (abs(util.math.normalizeAngle( angles - targetAngle )) < 0.1) ...
              |(abs(util.math.normalizeAngle( angles + pi - targetAngle )) < 0.1);
      invIdx = (abs(util.math.normalizeAngle( angles(validIdx) + pi - targetAngle )) < 0.1);
        weight = sum(abs(outVel(validIdx,3)),2);
        x = util.math.vectorNorm(outVel(validIdx,1:2));
        x(invIdx) = -x(invIdx);
    end
    
    hFig = figure;
    for w=1:4
      subplot(2,2,w); title(sprintf('angle %.2f - wheel %d', targetAngle, w));
      hold all;
      plot(speedRange, ws(:,w));
      plot(speedRange, ws(:,w)-2*ws_s2(:,w),'M:');
      plot(speedRange, ws(:,w)+2*ws_s2(:,w),'M:');

      if exist('outVel','var')
        scatter(x, motorVel(validIdx,w),50,weight,'.');
      end
      
      xlabel('speed [m/s]');
      ylabel('motor speed');
      ylim([-150,150]);
    end
    set(hFig, 'Position', [10 10 1600 800])
  end
end
