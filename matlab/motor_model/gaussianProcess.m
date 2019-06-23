% create GP model and visualize/evaluate
% run plotMotorMovement before, to plot sample data

filebase = cell(1,0);
% filebase{end+1} = {'1443457491695','Default Model, large x-tires '};
% filebase{end+1} = {'1443452027150','GP Model 1443450398270, small x-tires'};
% filebase{end+1} = {'1443453229070','GP Model 1443449170510,1443450398270,1443452027150, small x-tires (low+high vels)'};
% filebase{end+1} = {'1443453658319','GP Model 1443449170510,1443450398270,1443452027150, small x-tires (high vels)'};

% filebase{end+1} = {'fullSampler/1443714258384','Default Model, large x-tires, bot15-4S'};
% filebase{end+1} = {'gpOptimizer/1443715108570','Default Model, large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1443786240745','Default Model, large x-tires, bot15-4S'};
% filebase{end+1} = {'fullSampler/1443786563881','Default Model, large x-tires, bot15-4S'};

filebase{end+1} = {'fullSampler/1443789174552','Default Model, large x-tires, bot15-4S'}; % samples/files-1443789174552
filebase{end+1} = {'gpOptimizer/1443794196159','GP Model 1443789174552, large x-tires, bot15-4S'};
filebase{end+1} = {'gpOptimizer/1443795549125','GP Model 1443789174552, large x-tires, bot15-4S'};
filebase{end+1} = {'fullSampler/1443797003780','GP Model 1443789174552,1443794196159,1443795549125, large x-tires, bot15-4S'};
filebase{end+1} = {'fullSampler/1443806506970','combination Model, large x-tires, bot15-4S'};
filebase{end+1} = {'fullSampler/1444381979281','interpolation Model, large x-tires, bot15-4S'};
filebase{end+1} = {'fullSampler/1444383695510','interpolation Model, large x-tires, bot15-4S'};

filebase{end+1} = {'fullSampler/1444395505552','interpolation Model, large x-tires, bot15-4S'};
filebase{end+1} = {'fullSampler/1444396186781','interpolation Model, large x-tires, bot15-4S'};

filebase{end+1} = {'fullSampler/1444399076462','GP V2 Model, large x-tires, bot15-4S'};
filebase{end+1} = {'fullSampler/1444407850609','matrix rotate, large x-tires, bot15-4S'};

filebase{end+1} = {'gpOptimizer/1444409089169','GP sample, large x-tires, bot15-4S'};
filebase{end+1} = {'gpOptimizer/1444409588433','GP sample, large x-tires, bot15-4S'};

% filebase{end+1} = {'fullSampler/1444411041026','GP V2 Model, large x-tires, bot15-4S'};

filebase{end+1} = {'fullSampler/1444720702765','matrix rotate, large x-tires, bot15-4S'};
filebase{end+1} = {'fullSampler/1444720619941','matrix rotate, large x-tires, bot15-4S'};

X = []; Y = [];
for i=1:size(filebase,2)
  T = importdata(strcat('../logs/',filebase{i}{1},'.csv'));
  X = [X; T(:,5:7)];
  Y = [Y; T(:,1:4)];
end

fprintf('Number of samples: %d\n', size(X,1));

% gm = GpModel(X, Y);
gm = GpV2Model(X, Y);
gm.train();
mm = gm;

%%
mm = MatrixModel();


%%
imm=InterpolationMotorModel.fromMotorModel(mm, pi/16);
imm.save('../data/interpolationModel/gp2.interpol');

%%
close all;


%%
plotMotorSpeed2dSpeed(mm, motorVel, outVel, setVel);

%%
plotMotorSpeed2dAngle(mm, motorVel, outVel, setVel);

%%
plotMotorSpeed2dRotation(mm, motorVel, outVel, setVel);


%%
plotMotorSpeed3d(mm, outVel, motorVel);


%%
evalGpModel( mm, motorVel, outVel, setVel );



%%
evalGpInvModel( mm, motorVel, outVel, setVel );