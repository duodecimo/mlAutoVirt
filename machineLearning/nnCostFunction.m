function [J grad] = nnCostFunction(nn_params, ...
                                   input_layer_size, ...
                                   hidden_layer_size, ...
                                   num_labels, ...
                                   X, y, lambda)
%NNCOSTFUNCTION Implements the neural network cost function for a two layer
%neural network which performs classification
%   [J grad] = NNCOSTFUNCTON(nn_params, hidden_layer_size, num_labels, ...
%   X, y, lambda) computes the cost and gradient of the neural network. The
%   parameters for the neural network are "unrolled" into the vector
%   nn_params and need to be converted back into the weight matrices. 
% 
%   The returned parameter grad should be a "unrolled" vector of the
%   partial derivatives of the neural network.
%

% Reshape nn_params back into the parameters Theta1 and Theta2, the weight matrices
% for our 2 layer neural network
Theta1 = reshape(nn_params(1:hidden_layer_size * (input_layer_size + 1)), ...
                 hidden_layer_size, (input_layer_size + 1));

Theta2 = reshape(nn_params((1 + (hidden_layer_size * (input_layer_size + 1))):end), ...
                 num_labels, (hidden_layer_size + 1));

% Setup some useful variables
m = size(X, 1);
         
% You need to return the following variables correctly 
J = 0;
Theta1_grad = zeros(size(Theta1));
Theta2_grad = zeros(size(Theta2));

% ====================== YOUR CODE HERE ======================
% Instructions: You should complete the code by working through the
%               following parts.
%
% Part 1: Feedforward the neural network and return the cost in the
%         variable J. After implementing Part 1, you can verify that your
%         cost function computation is correct by verifying the cost
%         computed in ex4.m
%
% Part 2: Implement the backpropagation algorithm to compute the gradients
%         Theta1_grad and Theta2_grad. You should return the partial derivatives of
%         the cost function with respect to Theta1 and Theta2 in Theta1_grad and
%         Theta2_grad, respectively. After implementing Part 2, you can check
%         that your implementation is correct by running checkNNGradients
%
%         Note: The vector y passed into the function is a vector of labels
%               containing values from 1..K. You need to map this vector into a 
%               binary vector of 1's and 0's to be used with the neural network
%               cost function.
%
%         Hint: We recommend implementing backpropagation using a for-loop
%               over the training examples if you are implementing it for the 
%               first time.
%
% Part 3: Implement regularization with the cost function and gradients.
%
%         Hint: You can implement this around the code for
%               backpropagation. That is, you can compute the gradients for
%               the regularization separately and then add them to Theta1_grad
%               and Theta2_grad from Part 2.
%

% Part 1: cost
% calculate hidden layer

X=[ones(m,1) X]; % add bias
AH = sigmoid(Theta1 * X'); % ex: 5000 X 25

% calculate output layer

% activation of hidden layer
AH = [ones(m, 1) AH']; %% add bias
% activation of output layer
A = sigmoid(AH*Theta2'); % ex: 5000 X 10

%debug
%fprintf("\nsize of AH: ");
%size(AH)
%fprintf("\nsize of A: ");
%size(A)
%pause


% cost calculation
for i = 1:m
    ym = converty(y(i), num_labels);
    for k = 1:num_labels
        J+= ( (-ym(k)) * log(A(i,k)) ) - ( ( 1 - ym(k) ) * (log(1-A(i,k))) ); 
    endfor
endfor
J = (1/m) * J;

% checked, ok

% add regularization

RH = 0;
for j=1:hidden_layer_size
    for k=2:input_layer_size+1
        RH+= Theta1(j,k)^2;
    endfor
endfor
RO = 0;
for j=1:num_labels
    for k=2:hidden_layer_size+1
        RO+= Theta2(j,k)^2;
    endfor
endfor

J+= (lambda/(2*m)) * (RH + RO);


% Part 2: gradients (with backpropagation)

Delta = zeros(num_labels, 1);
DeltaH = zeros(hidden_layer_size, 1);
% pTheta2 = Theta2;
pTheta2 = Theta2(:, 2:end);
pTheta2(:,[1]) = []; % strip first column
for t = 1:m % each trainning set
    ym = converty(y(t), num_labels);
    dax = X(t,:)';
    dzh = Theta1 * dax;
    dah = sigmoid(dzh);
    dah = [1; dah]; % add bias
    dzo = Theta2 * dah;
    dao = sigmoid(dzo);
    Delta = dao - ym;
    DeltaH = (Theta2' * Delta)(2:end, 1) .* sigmoidGradient(dzh);
    Theta1_grad+= DeltaH * dax'; % vectorized
    Theta2_grad+= Delta * dah'; % vectorized
endfor

% unregularized gradient
Theta1_grad *= 1/m;
Theta2_grad *= 1/m;

% regularization
Theta1_grad+= lambda*[zeros(hidden_layer_size , 1) Theta1(:,2:end)] / m;
Theta2_grad+= lambda*[zeros(num_labels , 1) Theta2(:,2:end)] / m;

% -------------------------------------------------------------

% =========================================================================

% Unroll gradients
grad = [Theta1_grad(:) ; Theta2_grad(:)];


end
