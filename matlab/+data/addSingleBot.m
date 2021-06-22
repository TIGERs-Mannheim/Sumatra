function D = addSingleBot(D, botId, botColor)
%ADDSINGLEBOT Add data fields for a single given bot to D

  if isempty(D.wpBots.id)
    return
  end
  if isempty(botId)
    botId = D.wpBots.id(1);
    botColor = D.wpBots.color(1);
  end
  
  fields = fieldnames(D.wpBots);
  for i= 1:numel(fields)
    idx = D.wpBots.id == botId & D.wpBots.color == botColor;
    D.wpBot.(fields{i}) = D.wpBots.(fields{i})(idx,:);
  end
  
  if isfield(D, 'rawBots')
      fields = fieldnames(D.rawBots);
      idx = D.rawBots.id == botId & D.rawBots.color == botColor;
      for i= 1:numel(fields)
          D.rawBot.(fields{i}) = D.rawBots.(fields{i})(idx,:);
      end
      D.rawBot.vel = util.convert.pose2vel(D.rawBot.pos, D.rawBot.timestamp);
      D.rawBot.velAbs = sqrt( D.rawBot.vel(:,1) .* D.rawBot.vel(:,1) + D.rawBot.vel(:,2) .* D.rawBot.vel(:,2));
      D.rawBot.acc = util.convert.vel2acc(D.rawBot.vel, D.rawBot.timestamp);
      D.rawBot.accAbs = sqrt( D.rawBot.acc(:,1) .* D.rawBot.acc(:,1) + D.rawBot.acc(:,2) .* D.rawBot.acc(:,2));
  end
  
  if isfield(D, 'filteredBots')
      fields = fieldnames(D.filteredBots);
      idx = D.filteredBots.id == botId & D.filteredBots.color == botColor;
      for i= 1:numel(fields)
          D.filteredBot.(fields{i}) = D.filteredBots.(fields{i})(idx,:);
      end
  end
  
  if isfield(D, 'botInputs')
      fields = fieldnames(D.botInputs);
      idx = D.botInputs.id == botId & D.botInputs.color == botColor;
      for i= 1:numel(fields)
          D.botInput.(fields{i}) = D.botInputs.(fields{i})(idx,:);
      end
  end
  
  if isfield(D, 'botOutputs')
      fields = fieldnames(D.botOutputs);
      idx = D.botOutputs.id == botId & D.botOutputs.color == botColor;
      for i= 1:numel(fields)
          D.botOutput.(fields{i}) = D.botOutputs.(fields{i})(idx,:);
      end
  end
    
end
