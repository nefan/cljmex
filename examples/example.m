%
% This file is part of cljmex.
%
% Copyright (C) 2012, Stefan Sommer (sommer@diku.dk)
% https://github.com/nefan/cljmex.git
%
% cljmex is free software: you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation, either version 3 of the License, or
% (at your option) any later version.
%
% cljmex is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License
% along with cljmex.  If not, see <http://www.gnu.org/licenses/>.
%

%
% Example matlab file calling file generated from
% mexfile.cpp and mexfile.cljmex
%

fprintf('Calling mex file generated from mexfile.cpp and mexfile.cljmex\n');

m = 3;
n = 2;

R = sparse([1 2; 0 0; 0 3]);
C = 1i*(R*R');

p = int64(1:10);

pred = true;

name = 'cljtest';

[M v t] = mexfile(int64(m),int64(n),R,C,p,pred,name);

% output results
M, v, t

fprintf('Done :-)\n');
