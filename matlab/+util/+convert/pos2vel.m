function vel = pos2vel(pos, timeNs)
  vel = zeros(size(pos));

  tmp = pos(2:end,:) - pos(1:end-1,:);
  dts = [0; (timeNs(2:end) - timeNs(1:end-1)) * 1e-9];
  vel(2:end,1:3) = tmp(:,1:3) ./ repmat(1000 * dts(2:end), [1,3]);
  lastVel = 0;
  for i=2:size(vel,1)
    if dts(i) < 1e-4
      vel(i,:) = lastVel;
    else
      lastVel = vel(i,:);
    end
  end
end

