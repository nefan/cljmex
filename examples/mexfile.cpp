/*
 * This file is part of cljmex.
 *
 * Copyright (C) 2012, Stefan Sommer (sommer@diku.dk)
 * https://github.com/nefan/cljmex
 *
 * cljmex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * cljmex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cljmex.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

#include "mexfile.hpp"
#include "mexfile.cljmex.hpp"

#include <complex>

/* 
 * example mex file using cljmex header
 */
cljmex_start()

    // Input data is defined in mexfile.cljmex:
    // arguments m, n, R, C, p, pred, and name are thus 
    // available. Basic input validity checks are performed
    // using flags in the cljmex file. Structures for holding
    // array data are defined in cljmex.hpp. 
    // The :zip flags makes cljmex copy complex data into
    // std::complex arrays.

    mexPrintf("The test %s ",name);
    mexPrintf("got two ints m and n with values %d and %d.\n",m,n);

    // sum the entries in the sparse array R
    double sumR = 0;
    for (int col=0; col<R.cols; col++)
        for (int rowp=R.p[col]; rowp<R.p[col+1]; rowp++) {
            int row = R.i[rowp];
            sumR += R.x[rowp];
        }
    mexPrintf("The %d elements of the %dx%d sparse array R sum to %f.\n",R.nz,R.rows,R.cols,sumR);

    // sum through the square complex sparse array C
    // Note that the entries of C are copied into the std::complex
    // array C.v because of the :zip flags
    double sumC = 0;
    for (int col=0; col<C.cols; col++)
        for (int rowp=C.p[col]; rowp<C.p[col+1]; rowp++) {
            int row = C.i[rowp];
            sumC += imag(C.v[rowp]);
        }
    mexPrintf("The imaginary parts of the %d elements of the %dx%d square sparse array C sum to %f.\n", \
            C.nz,C.rows,C.cols,sumC);

    // p is a row vector with n integers
    mexPrintf("p contains the following ints:");
    for (int i=0; i<p.cols; i++) {
        int v = p.x[i];
        mexPrintf(" %d",v);
    }
    mexPrintf(".\n");

    // p is a predicate
    mexPrintf("pred must be true.\n");
    mxAssert(pred,"predicate pred must be true");
    mexPrintf("it is true.\n");

    // do something interesting....
    std::complex<double> *S = (std::complex<double>*)malloc(m*m*sizeof(std::complex<double>)); // must be explicityly freed
    mxAssert(S,"failed to allocate memory for S");
    for (int i=0; i<m*m; i++)
        S[i] = std::complex<double>(i,-i);
    double *v = (double*)mxMalloc(n*sizeof(double)); // MATLAB deallocates mxMalloc allocated memory
    for (int i=0; i<n; i++)
        v[i] = (double)2*p.x[i];

    double time = 0.1;

    // Output data is set up by cljmex and copied from local
    // variables S, v, and time using :copy and :unzip flags
    // in mexfile.cljmex
    
    // free only locally allocated memory (cljmex uses only
    // mxMalloc allocated memory that MATLAB deallocates)
    free(S);

cljmex_end()
