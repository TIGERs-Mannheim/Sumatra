function plotMotorSpeed2dRotation( motorModel, motorVel, outVel, setVel )
%
  aSpeedStep = 1;
  aSpeedMax = 20;
  aSpeedRange = -aSpeedMax:aSpeedStep:aSpeedMax;
  
  % rotation
  vel = zeros(length(aSpeedRange),3);
  vel(:,3) = aSpeedRange;

  [ws, ws_s2] = motorModel.getWheelSpeed(vel);
  
  validIdx = abs(setVel(:,3)) ~= 0;
  
  hFig = figure;
  for w=1:4
    subplot(2,2,w); title(sprintf('rotation - wheel %d', w));
    hold all;
    plot(aSpeedRange, ws(:,w));
    plot(aSpeedRange, ws(:,w)-2*ws_s2(:,w),'M:');
    plot(aSpeedRange, ws(:,w)+2*ws_s2(:,w),'M:');

    if exist('outVel','var')
      plot(outVel(validIdx,3), motorVel(validIdx,w),'.');
    end

    xlabel('speed [rad/s]');
    ylabel('motor speed');
    ylim([-150,150]);
  end
  set(hFig, 'Position', [10 10 1600 800])
end
