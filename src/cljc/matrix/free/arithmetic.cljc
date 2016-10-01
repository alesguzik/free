;; matrix arithmetic
;; there's another free.arithmetic with scalar operations

#?(:clj  (ns free.arithmetic
	   (:require [clojure.core.matrix :as mx]))
   :cljs (ns free.arithmetic
	    (:require [clojure.core.matrix :as mx])))
                      ;[thinktopic.aljabr.core :as imp])))

#?(:clj  (mx/set-current-implementation :vectorz)
   :cljs (mx/set-current-implementation :persistent-vector)) ; won't load it, but set default for e.g. mx/matrix
   ;:cljs (mx/set-current-implementation :aljabr)) ; won't load it, but set default for e.g. mx/matrix

;; List of all namespaces of implementations in KNOWN-IMPLEMENTATIONS in
;; https://github.com/mikera/core.matrix/blob/develop/src/main/clojure/clojure/core/matrix/implementations.cljc

;; Clojurescript options:
;; [clojure.core.matrix.impl.ndarray-object :as imp] ;; (why did I think this worked in Clojurescript?)
;; [thinktopic.aljabr.core :as imp]

;; Clojure options:
;; (mx/set-current-implementation :ndarray)
;; (mx/set-current-implementation :aljabr)
;; (mx/set-current-implementation :vectorz)
;; (mx/set-current-implementation :clatrix)
;; (mx/set-current-implementation :nd4clj)

(println "Loading core.matrix operators.  Matrix implementation:" (mx/current-implementation))


;; Note that these are functions, but in free.scalar-arithmetic, I define 
;; them as macros for the sake of performance.  So don't e.g. map the functions 
;; below over a sequence, if you want to preserve substitutability with their 
;; scalar analogues.
(def m* mx/mmul) ; matrix multiplication and inner product
(def e* mx/mul)  ; elementwise (Hadamard) and scalar multiplication
(def m+ mx/add)  ; elementwise addition
(def m- mx/sub)  ; elementwise subtraction, or elementwise negation
(def tr mx/transpose)
(def inv mx/inverse)
(def make-identity-obj mx/identity-matrix)
(def pm mx/pm)

(defmacro m* 
  "Scalar analogue of matrix multiplication and inner product, i.e. scalar
  multiplication."
  ([x] x) ; is this right?
  ([x y] `(mx/mmul ~x ~y))
  ([x y z] `(mx/mmul ~x ~y ~z)))

(defmacro e* 
  "Scalar analogue of elementwise (Hadamard) multiplication, i.e. scalar 
  multiplication."
  ([x] x)
  ([x y] `(mx/mul ~x ~y))
  ([x y z] `(mx/mul ~x ~y ~z)))

(defmacro m+ 
  "Scalar analogue of elementwise addition, i.e. scalar addition."
  ([x] x)
  ([x y] `(mx/add ~x ~y))
  ([x y z] `(mx/add ~x ~y ~z)))

(defmacro m- 
  "Scalar analogue of elementwise subtraction, i.e. scalar subtraction."
  ([x] `(mx/sub ~x))
  ([x y] `(mx/sub ~x ~y))
  ([x y z] `(mx/sub ~x ~y ~z)))

(defmacro tr
  "Scalar analogue of transposition; returns the argument unchanged."
  [x]
  `(mx/transpose x))

(defmacro inv
  "Scalar analogue of matrix inversion, i.e. scalar reciprocal, divide into 1.0."
  [x]
  `(mx/inverse ~x))

(defmacro make-identity-obj
  "Returns 1, the identity operator for scalar multiplication.  Throws exception
  if anything other than 1 is passed as dims."
  [dims]
  (mx/identity-matrix dims))

;#?(:clj   (def pm clojure.pprint/pprint)
;    :cljs (def pm cljs.pprint/pprint))

;; btw There's also an e* in core.matrix (which is *almost* identical to mul)
;; but by qualifying core.matrix with mx, it doesn't matter.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; These have no (and need no) equivalents in scalar-arithmetic:

(defn col-mat
  "Turns a sequence of numbers xs into a column vector."
  [xs]
  (mx/matrix (map vector xs)))

(defn row-mat
  "Turns a sequence of numbers xs into a row vector."
  [xs]
  (mx/matrix (vector xs)))