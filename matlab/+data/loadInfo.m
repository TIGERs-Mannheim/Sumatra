function [ info ] = loadInfo( filename )
%LOADINFO Load JSON file with additional info
  addpath('jsonlab');
  info = loadjson(filename);
end

