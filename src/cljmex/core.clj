;
; This file is part of cljmex.
;
; Copyright (C) 2012, Stefan Sommer (sommer@diku.dk)
; https://github.com/nefan/cljmex
;
; cljmex is free software: you can redistribute it and/or modify
; it under the terms of the GNU General Public License as published by
; the Free Software Foundation, either version 3 of the License, or
; (at your option) any later version.
;
; cljmex is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU General Public License for more details.
;
; You should have received a copy of the GNU General Public License
; along with cljmex.  If not, see <http://www.gnu.org/licenses/>.
;

(ns cljmex.core
  (:use [cljmex defs lang genmexc util]))

; root bindings
(def filename "")
(def inArgs ())
(def outArgs ())

; define input argument
(defn argument [& args]
  (def inArgs (cons args inArgs)))

; define output argument
(defn output [& args]
  (def outArgs (cons args outArgs)))

; define runtime checks for input arguments
(defn checkInArg [name type format parg args]
  (let []
    (str
      (if-let [typeCheckFun (typeCheckFuns type)]
        (str 
          (mexAssert (evalfun typeCheckFun parg)  (str name " should be of type " type)))
        )
      (if-let [nrRows (args :rows)]
        (str 
          (mexAssert (str (mxGetM parg) (csym :eq) nrRows) (str name " should have " nrRows " rows")))
        )
      (if-let [nrCols (args :columns)]
        (str 
          (mexAssert (str (mxGetN parg) (csym :eq) nrCols) (str name " should have " nrCols " columns")))
        )
      (if (args :nonempty)
        (str 
          (mexAssert (str (mxGetN parg) (csym :>) 0 (csym :and) 
                          (mxGetM parg) (csym :>) 0)
                          (str name " should be non-empty")))
        )
      (if (args :square)
        (str 
          (mexAssert (str (mxGetN parg) (csym :eq) (mxGetM parg))
                          (str name " should be square")))
        )
      (if (= format :single)
        (str 
          (mexAssert (str (mxGetN parg) (csym :eq) 1 (csym :and) 
                          (mxGetM parg) (csym :eq) 1)
                          (str name " should be single element")))
        )
      (if (= format :row-vector)
        (str 
          (mexAssert (str (mxGetM parg) (csym :eq) 1) (str name " should be row vector"))))
      (if (= format :column-vector)
        (str 
          (mexAssert (str (mxGetN parg) (csym :eq) 1) (str name " should be column vector"))))
        )))

(defn if-supported [support type format s]
  (do
    (assert (contains? support type) (str "unsupported " format " type " type))
    s))

(defn evalInSingle [name type parg args]
  (if-supported #{:int :real :logical} type :single
    (str 
      (defVal name parg type "mxGetData"))
    ))

(defn evalInMatrix [name type parg args]
  (if-supported #{:int :real} type :matrix
    (let [rows (str name ".rows")
          cols (str name ".cols")
          x (str name ".x")
          z (str name ".z")
          ]
      (str
        (defStruct name type :matrix)
        (str rows (csym :assign) (mxGetM parg) ";" mnewline)
        (str cols (csym :assign) (mxGetN parg) ";" mnewline)
        (str x (csym :assign) (getPointer parg type "mxGetData") ";" mnewline)
    ))
    ))

(defn evalInSparse [name type parg args]
  (if-supported #{:real :complex} type :sparse
    (let [rows (str name ".rows")
          cols (str name ".cols")
          p (str name ".p")
          i (str name ".i")
          x (str name ".x")
          z (str name ".z")
          v (str name ".v")
          nz (str name ".nz")]
      (str
        (defStruct name type :sparse)
        (str rows (csym :assign) (mxGetM parg) ";" mnewline)
        (str cols (csym :assign) (mxGetN parg) ";" mnewline)
        (str p (csym :assign) (getPointer parg :index "mxGetJc") ";" mnewline)
        (str i (csym :assign) (getPointer parg :index "mxGetIr") ";" mnewline)
        (str x (csym :assign) (getPointer parg type "mxGetPr") ";" mnewline)
        (if (= type :complex) (str z (csym :assign) (getPointer parg type "mxGetPi") ";" mnewline))
        (str nz (csym :assign) p "[" (mxGetN parg) "];" mnewline)
        (if (and (= type :complex) (args :zip))
          (str 
            (str v (csym :assign) (malloc :complex :complex nz) ";" mnewline)
            (str "for (int k=0; k<" nz "; k++)" mnewline
                 \tab v "[k]" (csym :assign) (evalfun (ctypes :complex)
                                                      (str x "[k]")
                                                      (str z "[k]")) ";" mnewline))))
    )))

(defn evalInString [name type parg args]
  (if-supported #{:string} type :string
    (let [len (str name ".len")
          s (str name ".s")]
      (str
        (defStruct name type :string)
        (str len (csym :assign)
             (mxGetN parg) "*"
             (mxGetM parg) "*"
             (evalfun "sizeof" (mtypes :char))
             "+1;" mnewline)
        (str s (csym :assign) (malloc type false len) ";" mnewline)
        (evalfun "mxGetString" parg s len) ";" mnewline)
      )
    ))

(defn evalInArg [arg argNr]
  (let [args (apply hash-map arg)
        {:keys [name type format]} args
        format (if-not (= type :string) format :string)
        parg (pargin (- argNr 1))]
    (str
      (checkInArg name type format parg args)
      (case format
        :single (evalInSingle name type parg args)
        :string (evalInString name type parg args)
        :sparse (evalInSparse name type parg args)
        :matrix (evalInMatrix name type parg args) 
        :row-vector (evalInMatrix name type parg args) 
        :column-vector (evalInMatrix name type parg) 
        (assert false (str "missing or unsupported input format " format))
        )
      )
    )
  )

(defn evalOutSingle [name type parg args]
  (if-supported #{:real} type :single
    [(str parg (csym :assign) (evalfun "mxCreateDoubleMatrix" 1 1 (mtypes type)) ";" mnewline)
     (if-let [copy (args :copy)]
       (str "*" (getPointer parg type "mxGetPr") (csym :assign) copy ";" mnewline)
       "")
     ]
    ))

(defn evalOutMatrix [name type parg args]
  (if-supported #{:real :complex} type :single
    (let [rows (str name ".rows")
          cols (str name ".cols")
          x (str name ".x")
          z (str name ".z")
          nrRows (if-not (= (args :format) :column-vector) (args :rows) 1)
          nrCols (if-not (= (args :format) :row-vector) (args :columns) 1)
          ]
      (assert (and nrRows nrCols) (str "invalid dimension for output " name))
      [(str 
         parg (csym :assign) (evalfun "mxCreateDoubleMatrix" nrRows nrCols (mtypes type)) ";" mnewline
         (defStruct name type :matrix)
         (str rows (csym :assign) nrRows ";" mnewline)
         (str cols (csym :assign) nrCols ";" mnewline)
         (str x (csym :assign) (getPointer parg type "mxGetPr") ";" mnewline)
         (if (= type :complex) (str z (csym :assign) (getPointer parg type "mxGetPi") ";" mnewline))
         )
       (case type
         :real (if-let [copy (args :copy)]
                 (str "for (int k=0; k<" rows "*"cols "; k++)" mnewline
                      \tab x "[k]" (csym :assign) copy "[k];" mnewline)
                 ""
                 )
         :complex
           (if-let [unzip (args :unzip)]
             (str "for (int k=0; k<" rows "*"cols "; k++) {" mnewline
                  \tab x "[k]" (csym :assign) (evalfun "real" (str unzip "[k]")) ";" mnewline
                  \tab z "[k]" (csym :assign) (evalfun "imag" (str unzip "[k]")) ";" mnewline
                  "}" mnewline)
             ""
             ))
         ]
    )))

(defn evalOutSparse [name type parg args]
  (if-supported #{:real :complex} type :sparse
    (let [rows (str name ".rows")
          cols (str name ".cols")
          p (str name ".p")
          i (str name ".i")
          x (str name ".x")
          z (str name ".z")
          v (str name ".v")
          nz (str name ".nz")
          nrRows (args :rows)
          nrCols (args :columns)
          nrNz (args :nz)]
      (assert (and nrRows nrCols nrNz) (str "invalid dimensions or nz for sparse output " name))
      [(str
         parg (csym :assign) (evalfun "mxCreateSparse" nrRows nrCols nrNz (mtypes type)) ";" mnewline
        (defStruct name type :sparse)
        (str rows (csym :assign) (mxGetM parg) ";" mnewline)
        (str cols (csym :assign) (mxGetN parg) ";" mnewline)
        (str p (csym :assign) (getPointer parg :index "mxGetJc") ";" mnewline)
        (str i (csym :assign) (getPointer parg :index "mxGetIr") ";" mnewline)
        (str x (csym :assign) (getPointer parg type "mxGetPr") ";" mnewline)
        (if (= type :complex) (str z (csym :assign) (getPointer parg type "mxGetPi") ";" mnewline))
        (str nz (csym :assign) nrNz ";" mnewline))
       (case type
         :real (if-let [copy (args :copy)]
                 (str "for (int k=0; k<" nz "; k++)" mnewline
                      \tab x "[k]" (csym :assign) copy ".x[k];" mnewline)
                 ""
                 )
         :complex
           (let [copy (args :copy)
                 unzip (args :unzip)]
             (if (or copy unzip)
               (str "for (int k=0; k<" nz "; k++) {" mnewline
                    (if copy
                      (str \tab x "[k]" (csym :assign) (str copy ".x[k]") ";" mnewline
                           \tab z "[k]" (csym :assign) (str copy ".z[k]") ";" mnewline)
                      (str \tab x "[k]" (csym :assign) (evalfun "real" (str unzip ".v[k]")) ";" mnewline
                           \tab z "[k]" (csym :assign) (evalfun "imag" (str unzip ".v[k]")) ";" mnewline))
                      "}" mnewline)
                 ""
                 )))
       ]
    )))

(defn evalOutArg [arg argNr]
  (let [args (apply hash-map arg)
        {:keys [name type format]} args
        parg (pargout (- argNr 1))]
    (case format
        :single (evalOutSingle name type parg args)
;        :string (evalOutString name type parg args)
        :sparse (evalOutSparse name type parg args)
        :matrix (evalOutMatrix name type parg args) 
        :row-vector (evalOutMatrix name type parg args) 
        :column-vector (evalOutMatrix name type parg) 
        :manual ""
        (assert false (str "missing or unsupported output format " format))
      )
    )
  )

(defn header []
  (str "#include \"cljmex.hpp\"" \newline))

; generate cljmex-start macro
(defn macro-cljmex-start []
  (let [inArgStr (reduce-with-index 
                   (fn [l val i] (str l (evalInArg val i) mnewline) )
                   ""
                   (reverse inArgs))
        [outArgStr copyOut] (reduce-with-index 
                                 (fn [l val i] 
                                   (let [[out copy] (evalOutArg val i)]
                                     [(str (first l) out mnewline)
                                      (str (second l) copy mnewline)]))
                                 '("" "")
                                 (reverse outArgs))
        ]
    (def copyOut copyOut)
    (str
      (defMacro "cljmex_start"
            (str 
              (evalfun "cljmex_mex_fun") ";" mnewline
              (evalfun "cljmex_check_args" (count inArgs) (count outArgs) (str "\"" filename "\"")) ";" mnewline
              (evalfun "cljmex_setup") ";" mnewline
              mnewline
              inArgStr
              outArgStr)))))

; generate cljmex-end macro
(defn macro-cljmex-end []
  (defMacro "cljmex_end" 
            (str 
              copyOut mnewline
              "}" mnewline)))

; init root bindings
(defn cljmex-init [fname]
  (def filename fname)
  (def inArgs ())
  (def outArgs ()))

; output everything
(defn cljmex-end []
  (print (header) \newline)
  (print (macro-cljmex-start) \newline)
  (print (macro-cljmex-end) \newline)
  )

