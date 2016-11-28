%% Machine Learning (based on the on line course of Prof. Andrew NG)
%

%% Initialization
clear ; close all; clc

input_layer_size  = 576;    % each image contains 576 pixels (32 x 18).
hidden_layer_size = 64;     % 64 hidden units
num_labels = 7;             % 7 labels: the seven possible angles of the virtual car steer.

% y.dat contains the corresponding values of steer angles of a virtual car corresponding to
% captured images in X.dat.

load('Y.dat');

fprintf("y size: ");
size(y)

load('X.dat');

m = size(X, 1);

%duo debug 17 Out 2016
fprintf("X size: ");
size(X)

% Randomly select 8 data points to display
sel = randperm(size(X, 1));
sel = sel(1:8);

% To use displayData, you need to modify the displayData function to expect 32 x 18 pixels samples.
displayData(X(sel, 1:end), 32);

initial_Theta1 = randInitializeWeights(input_layer_size, hidden_layer_size);
initial_Theta2 = randInitializeWeights(hidden_layer_size, num_labels);

% Unroll parameters
initial_nn_params = [initial_Theta1(:) ; initial_Theta2(:)];

fprintf('\nTraining Neural Network... \n')
options = optimset('MaxIter', 100);
lambda = 1000;

% Create "short hand" for the cost function to be minimized
costFunction = @(p) nnCostFunction(p, ...
                                   input_layer_size, ...
                                   hidden_layer_size, ...
                                   num_labels, X, y, lambda);

% Now, costFunction is a function that takes in only one argument (the
% neural network parameters)
[nn_params, cost] = fmincg(costFunction, initial_nn_params, options);

% Obtain Theta1 and Theta2 back from nn_params
Theta1 = reshape(nn_params(1:hidden_layer_size * (input_layer_size + 1)), ...
                 hidden_layer_size, (input_layer_size + 1));

Theta2 = reshape(nn_params((1 + (hidden_layer_size * (input_layer_size + 1))):end), ...
                 num_labels, (hidden_layer_size + 1));

% To use displayData, you need to modify the displayData function to expect 32 x 18 pixels samples.
displayData(Theta1(:, 2:end), 32);

pred = predict(Theta1, Theta2, X);

fprintf('\nTraining Set Accuracy: %f\n', mean(double(pred == y)) * 100);

save "/tmp/Theta1.dat" Theta1
save "/tmp/Theta2.dat" Theta2
