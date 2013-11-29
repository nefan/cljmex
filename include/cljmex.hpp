#ifndef __CLRMEX_H__
#define __CLRMEX_H__

#include "mex.h"
#include <complex>

typedef long cljmexInt;
typedef double cljmexEntry;
typedef std::complex<cljmexEntry> cljmexComplex;

/* 
 * various structs for extracted data 
 */
typedef struct {
    cljmexInt rows;
    cljmexInt cols;
    cljmexInt *x; 
} cljmex_int_matrix;

typedef struct {
    cljmexInt rows;
    cljmexInt cols;
    cljmexEntry *x; 
} cljmex_real_matrix;

typedef struct {
    const int *dims;
    const cljmexInt *dimsI; // for indexing
    cljmexEntry *x; 
} cljmex_real_multidimarray;

typedef struct {
    cljmexInt rows;
    cljmexInt cols;
    cljmexEntry *x; 
    cljmexEntry *z; 
} cljmexComplex_matrix;

typedef struct {
    cljmexInt rows;
    cljmexInt cols;
    cljmexInt *p;
    cljmexInt *i;
    cljmexInt nz;
    cljmexEntry *x; 
} cljmex_real_sparse_matrix;

typedef struct {
    cljmexInt rows;
    cljmexInt cols;
    cljmexInt *p;
    cljmexInt *i;
    cljmexInt nz;
    cljmexEntry *x; 
    cljmexEntry *z; 
    cljmexComplex *v; // zipped copy of x and z
} cljmexComplex_sparse_matrix;

typedef struct {
    char *s;
    cljmexInt len;
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
#define cljmex_check_inargs(nrInArgs,fileName)\
    if (nargin != nrInArgs) {\
        mexPrintf("%s error: Called with %d/%d input arguments\n",\
                fileName,nargin,nrInArgs);\
        cljmex_error("argument error");\
    }\

#define cljmex_check_outargs(nrOutArgs,fileName)\
    if (nargout != nrOutArgs) {\
        mexPrintf("%s error: Called with %d/%d output arguments\n",\
                fileName,nargout,nrOutArgs);\
        cljmex_error("argument error");\
    }\

#define cljmex_check_varinargs(nrInArgs,fileName)\
    if (nargin > nrInArgs) {\
        mexPrintf("%s error: Called with %d of max. %d input arguments\n",\
                fileName,nargin,nrInArgs);\
        cljmex_error("argument error");\
    }\

#define cljmex_check_varoutargs(nrOutArgs,fileName)\
    if (nargout > nrOutArgs) {\
        mexPrintf("%s error: Called with %d of max. %d output arguments\n",\
                fileName,nargout,nrOutArgs);\
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
