function [ D ] = loadAll( folder )
  %loadBallData Load all data from folder
  
  addpath('jsonlab');
  D.info = data.loadInfo(strcat(folder, '/info.json'));
  
  file = strcat(folder, '/wpBall.csv');
  if exist(file, 'file')
    D.wpBall = data.loadWpBall(file);
  end
  
  file = strcat(folder, '/wpBallTest.csv');
  if exist(file, 'file')
    D.wpBallUpdated = data.loadWpBall(file);
  end
  
  file = strcat(folder, '/rawBall.csv');
  if exist(file, 'file')
    D.rawBall = data.loadRawBall(file);
  end
  
  file = strcat(folder, '/rawBallCorrected.csv');
  if exist(file, 'file')
    D.rawBallCorrected = data.loadRawBall(file);
  end
  
  file = strcat(folder, '/rawBalls.csv');
  if exist(file, 'file')
    D.rawBalls = data.loadRawBall(file);
  end
  
  file = strcat(folder, '/wpBots.csv');
  if exist(file, 'file')
    D.wpBots = data.loadWpBots(file);
  end
  
  file = strcat(folder, '/wpBotsTest.csv');
  if exist(file, 'file')
    D.wpBotsUpdated = data.loadWpBots(file);
  end
  
  file = strcat(folder, '/rawBots.csv');
  if exist(file, 'file')
    D.rawBots = data.loadRawBots(file);
  end
  
  file = strcat(folder, '/nearestBot.csv');
  if exist(file, 'file')
    D.nearestBot = data.loadWpBots(file);
  end
  
  file = strcat(folder, '/skillBots.csv');
  if exist(file, 'file')
    D.skillBots = data.loadSkillBots(file);
  end
  
  file = strcat(folder, '/isBots.csv');
  if exist(file, 'file')
    D.isBots = data.loadWpBots(file);
  end
end

