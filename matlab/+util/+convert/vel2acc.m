function acc = vel2acc(vel, timeNs)
  acc = zeros(size(vel));

  tmp = vel(2:end,:) - vel(1:end-1,:);
  dts = [0; (timeNs(2:end) - timeNs(1:end-1)) * 1e-9];
  acc(2:end,:) = tmp(:,:) ./ repmat(dts(2:end), [1,3]);
  lastAcc = zeros(1, size(vel,2));
  for i=2:size(acc,1)
    if dts(i) < 1e-4 
      acc(i,:) = lastAcc;
      continue
    end
    if all(abs(acc(i,1:2)) > 10)
      acc(i,1:2) = lastAcc(1:2);
      continue
    end
    if acc(i,3) > 100
      acc(i,3) = lastAcc(3);
      continue
    end
    
    lastAcc = acc(i,:);
  end
end

