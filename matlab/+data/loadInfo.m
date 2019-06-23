function [ info ] = loadInfo( filename )
%LOADINFO Load JSON file with additional info
  info = loadjson(filename);
  info.filename = filename;
end

