#ifndef __CLRMEX_H__
#define __CLRMEX_H__

#include "mex.h"
#include <complex>

typedef long mInt;
typedef double Entry;
typedef std::complex<Entry> cljmex_complex;

/* 
 * various structs for extracted data 
 */
typedef struct {
    mInt rows;
    mInt cols;
    mInt *x; 
} cljmex_int_matrix;

typedef struct {
    mInt rows;
    mInt cols;
    Entry *x; 
} cljmex_real_matrix;

typedef struct {
    mInt rows;
    mInt cols;
    Entry *x; 
    Entry *z; 
} cljmex_complex_matrix;

typedef struct {
    mInt rows;
    mInt cols;
    mInt *p;
    mInt *i;
    mInt nz;
    Entry *x; 
} cljmex_real_sparse_matrix;

typedef struct {
    mInt rows;
    mInt cols;
    mInt *p;
    mInt *i;
    mInt nz;
    Entry *x; 
    Entry *z; 
    cljmex_complex *v; // zipped copy of x and z
} cljmex_complex_sparse_matrix;

typedef struct {
    char *s;
    mInt len;
} cljmex_string;

/* 
 * Signal error and return to MATLAB
 */
#define cljmex_error(msg)\
    mexErrMsgTxt(msg);\

/* 
 * Start MEX function
 */
#define cljmex_mex_fun() \
    void mexFunction\
        (\
         int nargout,\
         mxArray *pargout [ ],\
         int nargin,\
         const mxArray *pargin [ ]\
        )\
{\


/* 
 * Check number of input and output arguments
 */
#define cljmex_check_args(nrInArgs,nrOutArgs,fileName)\
    if (nargin != nrInArgs ||  nargout != nrOutArgs) {\
        mexPrintf("%s error: Called with %d/%d input arguments and %d/%d output arguments\n",\
                fileName,nargin,nrInArgs,nargout,nrOutArgs);\
        cljmex_error("argument error");\
    }\

/* 
 * Setup stuff
 */
#define cljmex_setup()\
    mxClassID mx_int;\
    /* determine size of the MATLAB integer */\
    if (sizeof (long) == sizeof (INT32_T))\
        mx_int = mxINT32_CLASS;\
    else\
        mx_int = mxINT64_CLASS;\

#endif /* __CLRMEX_H__ */
