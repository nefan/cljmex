;
; This file is part of cljmex.
;
; Copyright (C) 2012, Stefan Sommer (sommer@diku.dk)
; https://github.com/nefan/cljmex.git
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

(ns cljmex.genmexc
  (:use [cljmex defs lang]))

(defn evalfun 
  ([fun] (str fun "()"))
  ([fun arg1] (str fun "("  arg1 ")"))
  ([fun arg1 & more] (str fun "("  arg1 (reduce (fn [s v] (str s "," v)) "" more) ")"))
  )

(defn defStruct [name type format]
  (let [struct ((structs type) format)]
    (assert struct (str "missing struct: " type " " format))
    (str struct " " name ";" mnewline)))

(defn getPointer [parg type mexFun]
  (let [entry-type (entry-types type)]
    (str "(" entry-type "*)" (evalfun mexFun parg))))

(defn defPointer [name parg type mexFun]
  (let [ctype (ctypes type)]
    (str ctype " *" name (csym :assign) (getPointer parg type mexFun) ";" mnewline)))

(defn defVal [name parg type mexFun]
  (let [ctype (ctypes type)]
    (str ctype " " name (csym :assign) "*(" (getPointer parg type mexFun) ");" mnewline)))

(defn defMacro [name s]
  (str "#define " (evalfun name) " " mnewline s \newline))

(defn pargin [argNr]
  (str "pargin[" argNr "]"))

(defn pargout [argNr]
  (str "pargout[" argNr "]"))

(defn malloc [type len]
  (let [ctype (ctypes type)]
    (str "(" ctype "*)" (evalfun "mxMalloc" (str len "*" (evalfun "sizeof" ctype))))))

(defn getMalloc [name type len]
  (let [ctype (ctypes type)]
    (str ctype " *" name (csym :assign) (malloc type len) ";" mnewline)))

(defn mxGetN [parg]
  (evalfun "mxGetN" parg))

(defn mxGetM [parg]
  (evalfun "mxGetM" parg))

(defn mexAssert [cond msg]
  (str (evalfun "mxAssert" (str cond ",\"" msg "\"")) ";" mnewline))

