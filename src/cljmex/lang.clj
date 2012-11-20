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

(ns cljmex.lang)

; newline for use in C macros
(def mnewline "\\\n")

(def ctypes {
             :int "mInt"
             :real "Entry"
             :complex "cljmex_complex"
             :logical "bool"
             :string "char"
             })
(def entry-types {
             :int "mInt"
             :index "mInt"
             :real "Entry"
             :complex "Entry"
             :logical "bool"
             :string "char"
             })


(def mtypes {
;             :int 
             :real "mxREAL"
             :complex "mxCOMPLEX"
;             :logical 
;             :string
             :char  "mxChar"
             })

(def typeCheckFuns {
           :int "mxIsInt64"
           :real "!mxIsComplex"
           :complex "mxIsComplex"
           :logical "mxIsLogical"
           :string "mxIsChar"
           })

(def csym {
           :eq " == "
           :> " > "
           :assign " = "
           :and " && "
           })

(def structs {
             :string {:string "cljmex_string"}
             :int {
                    :matrix "cljmex_int_matrix"
                    }
             :real {
                    :matrix "cljmex_real_matrix"
                    :sparse "cljmex_real_sparse_matrix"
                    }
             :complex {
                       :matrix "cljmex_complex_matrix"
                       :sparse "cljmex_complex_sparse_matrix"
                       }
             })

