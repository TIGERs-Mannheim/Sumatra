function vel = pose2vel(pos, timeNs)
  vel = zeros(size(pos));

  delta = diff(pos);
  % [mm] to [m]
  delta(:,1:2) = delta(:, 1:2) / 1000;
  
  dts = diff(timeNs) * 1e-9;
  
  vel(2:end,:) = delta ./ repmat(dts, 1, 3);
    
  
  lastVel = 0;
  for i=2:size(vel,1)
    if dts(i-1) < 1e-4
      vel(i,:) = lastVel;
    else
      lastVel = vel(i,:);
    end
  end
end

