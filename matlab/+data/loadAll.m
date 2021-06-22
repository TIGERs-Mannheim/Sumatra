function [ D ] = loadAll( folder )
  %loadBallData Load all data from folder

  if ~isfolder(folder)
      error('%s is not a folder.', folder);
  end

  timeOffset = 1e20;

  file = strcat(folder, '/wpBall.csv');
  d = dir(file);
  if exist(file, 'file') && d.bytes > 0
    D.wpBall = data.loadWpBall(file);
    timeOffset = min(timeOffset, D.wpBall.timestamp(1));
  end

  file = strcat(folder, '/rawBall.csv');
  d = dir(file);
  if exist(file, 'file') && d.bytes > 0
    D.rawBall = data.loadRawBall(file);
    timeOffset = min(timeOffset, D.rawBall.timestamp(1));
  end

  file = strcat(folder, '/rawBalls.csv');
  d = dir(file);
  if exist(file, 'file') && d.bytes > 0
    D.rawBalls = data.loadRawBall(file);
    timeOffset = min(timeOffset, D.rawBalls.timestamp(1));
  end

  file = strcat(folder, '/filteredBall.csv');
  d = dir(file);
  if exist(file, 'file') && d.bytes > 0
    D.filteredBall = data.loadFilteredBall(file);
    timeOffset = min(timeOffset, D.filteredBall.timestamp(1));
  end

  file = strcat(folder, '/wpBots.csv');
  d = dir(file);
  if exist(file, 'file') && d.bytes > 0
    D.wpBots = data.loadWpBots(file);
    timeOffset = min(timeOffset, D.wpBots.timestamp(1));
  end

  file = strcat(folder, '/rawBots.csv');
  d = dir(file);
  if exist(file, 'file') && d.bytes > 0
    D.rawBots = data.loadRawBots(file);
    timeOffset = min(timeOffset, D.rawBots.timestamp(1));
  end

  file = strcat(folder, '/filteredBots.csv');
  d = dir(file);
  if exist(file, 'file') && d.bytes > 0
    D.filteredBots = data.loadFilteredBots(file);
    timeOffset = min(timeOffset, D.filteredBots.timestamp(1));
  end

  file = strcat(folder, '/nearestBot.csv');
  d = dir(file);
  if exist(file, 'file') && d.bytes > 0
    D.nearestBot = data.loadWpBots(file);
  end

  file = strcat(folder, '/botInput.csv');
  d = dir(file);
  if exist(file, 'file') && d.bytes > 0
    D.botInputs = data.loadBotInput(file);
    timeOffset = min(timeOffset, D.botInputs.timestamp(1));
  end

  file = strcat(folder, '/botOutput.csv');
  d = dir(file);
  if exist(file, 'file') && d.bytes > 0
    D.botOutputs = data.loadBotOutput(file);
    timeOffset = min(timeOffset, D.botOutputs.timestamp(1));
  end

  fields = fieldnames(D);
  for i = 1:numel(fields)
    D.(fields{i}).time = (D.(fields{i}).timestamp - timeOffset) / 1e9;
  end

  fieldMap = {{'wpBall','vel'}, ...
                {'wpBall','acc'}, ...
                {'botInputs','trajVel'}, ...
                {'botInputs','setVel'}, ...
                {'botInputs','localVel'}, ...
                {'botOutputs','vel'}, ...
                {'rawBall','vel'}, ...
                {'rawBall','acc'}, ...
                {'rawBalls','vel'}, ...
                {'rawBalls','acc'}, ...
                {'rawBots','vel'}, ...
                {'rawBots','acc'}, ...
                {'filteredBall','vel'}, ...
                {'filteredBall','acc'}, ...
                {'filteredBots','vel'}, ...
                {'filteredBots','acc'}, ...
                {'wpBots','vel'}, ...
                {'wpBots','acc'}, ...
                {'wpBots','buffered_vel'}, ...
                {'wpBots','feedback_vel'}};
  for i = 1:numel(fieldMap)
      field1 = fieldMap{i}{1};
      field2 = fieldMap{i}{2};
      if isfield(D, field1) && isfield(D.(field1), field2)
          field2Abs = strcat(field2,'Abs');
          values = D.(field1).(field2);
          D.(field1).(field2Abs) = sqrt( values(:,1) .* values(:,1) + values(:,2) .* values(:,2));
      end
  end


  D.info = data.loadInfo(strcat(folder, '/info.json'));
end

