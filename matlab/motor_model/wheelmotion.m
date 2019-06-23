function wheelmotion()
%
	global botRadius;
	botRadius = 0.072; % 0.09
	global wheelRadius;
	wheelRadius = 0.02; % 0.024
	global numWheels;
	numWheels = 4;

	alpha = degToRad(60);
	beta = degToRad(45);
	theta = abToThetaGrSim(alpha,beta);
	%theta = abToThetaBot(alpha,beta);
	
	VX = 0:0.1:3;
	i=1;
	DWG = zeros(length(VX),numWheels);
	DWB = zeros(length(VX),numWheels);
	for vx = VX
		theta = abToThetaGrSim(alpha,beta);
		DWG(i,1:numWheels) = wheelSpeedGrSim([vx;0;0],theta);
		theta = abToThetaBot(alpha,beta);
		DWB(i,1:numWheels) = wheelSpeedGrSim([vx;0;0],theta);
		i=i+1;
	end
	doPlot(1,VX,DWG,DWB);
	dwg_x = DWG(end,1:4)
	dwb_x = DWB(end,1:4)

	VY = 0:0.1:3;
	i=1;
	DWG = zeros(length(VY),numWheels);
	DWB = zeros(length(VY),numWheels);
	for vy = VY
		theta = abToThetaGrSim(alpha,beta);
		DWG(i,1:numWheels) = wheelSpeedGrSim([0;vy;0],theta);
		theta = abToThetaBot(alpha,beta);
		DWB(i,1:numWheels) = wheelSpeedGrSim([0;vy;0],theta);
		i=i+1;
	end
	doPlot(3,VY,DWG,DWB);
	dwg_y = DWG(end,1:4)
	dwb_y = DWB(end,1:4)
end

function doPlot(i,X,Y1,Y2)
	figure(i);
	clf;
	hold all;
	plot(X,Y1);
	legend('show');
	title('grSim');

	figure(i+1);
	clf;
	hold all;
	plot(X,Y2);
	legend('show');
	title('Bot');
end

function a = degToRad(b)
	a = b * pi / 180.0;
end

function theta = abToThetaBot(alpha, beta)
	% 30 150 225 315
	%theta = [ 2*pi-beta ; alpha ; pi-alpha ; pi + beta ];
	theta = [ 2*pi-alpha ; alpha ; pi-beta ; pi + beta ];
end

function theta = abToThetaGrSim(alpha, beta)
	% 60 135 225 300 (a=60, b=45
	theta = [ alpha ; pi-beta ; pi + beta ; 2*pi-alpha ];
end

function outM = wheelSpeedBot(outXYW,theta)
	global botRadius;
	global wheelRadius;
	forceXYW2Motor = [-sin(theta),cos(theta),ones(4,1) * 1.0/botRadius];
	outM = forceXYW2Motor * outXYW * wheelRadius * (1/11.76470588235294) * 1.2;
end

function outM = wheelSpeedGrSim(outXYW, theta)
	global botRadius;
	global wheelRadius;
	forceXYW2Motor = [-sin(theta),cos(theta),ones(4,1) * 1.0/botRadius];
	outM = forceXYW2Motor * outXYW * (1/wheelRadius);
end
	
