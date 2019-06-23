function DD = loadBatch(folder)
%LOADBATCH Load all time series data from given folder into one cell array
    files = dir(folder);
    directoryNames = {files([files.isdir]).name};
    directoryNames = directoryNames(~ismember(directoryNames,{'.','..'}));
    
    n = length(directoryNames);
    DD = cell(n,1);
    for i=1:n
        DD{i} = data.loadAll([folder, '/', directoryNames{i}]);
        fprintf('Loaded %s\n', directoryNames{i});
    end    
end

