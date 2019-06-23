function [ gm ] = loadGpModel(filebase)
%

  if ~exist('filebase','var')
    filebase = '1443443337664';
  end

  T = importdata(strcat('../logs/fullSampler/',filebase,'.csv'));
  X = T(:,5:7);
  Y = T(:,1:4);

  gm = GpModel(X, Y);
  gm.train();

end

