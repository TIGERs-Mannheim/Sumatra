function [ mat ] = loadMatrix( file, dimx, dimy )
%LOADMATRIX Loads a matrix from csv file
  T = importdata(file);
  if size(T,2) ~= dimx*dimy
    error('Wrong dimensions!');
  end
  mat = cell(size(T,1),1);
  for i=1:size(T,1)
    mat{i} = reshape(T(i,:),[dimx, dimy]);
  end
end

