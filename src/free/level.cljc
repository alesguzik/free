;; Based on
;; Rafal Bogacz, "A Tutorial on the Free-energy Framework for Modelling
;; Perception and Learning", *Journal of Mathematical Psychology*,
;; http://dx.doi.org/10.1016/j.jmp.2015.11.003 .)

;; Conventions:
;; The derivative of x is called x' .
;; A value of x from the next level down is called x-.
;; A value of x from the next level up is called x+.

(ns free.level
  (:require
    ;[free.scalar-arithmetic :refer [e* m* e+ e-]]   ; use only one
    [free.matrix-arithmetic :refer [e* m* e+ e-]] ; of these (this seems to work with scalars!)
   ))

;; phi update

(defn g'-fn
  [h' theta]
  (fn [phi] (e* theta (h' phi)))) ; ADD TRANSPOSE?

(defn phi-inc
  "Equation (53) in Bogacz's \"Tutoria\"."
  [phi eps eps- g']
  (e+ (e- eps)
      (m* (g' phi) eps-))) ; IS THIS RIGHT?

(defn next-phi 
  "Usage e.g. (next-phi phi eps eps- (g'-fn h theta))."
  [phi eps eps- g']
  (e+ phi 
      (phi-inc phi eps eps- g')))


;; epsilon update

(defn g-fn
  [h theta]
  (fn [phi] (m* theta (h phi))))

(defn eps-inc 
  "Equation (54) in Bogacz's \"Tutoria\"."
  [eps phi phi+ sigma g] 
  (e- phi 
      (g phi+)
      (m* sigma eps)))

(defn next-eps
  "Usage e.g. (next-eps eps phl phi+ sigma (g-fn h theta))."
  [eps phi phi+ sigma g]
  (+ eps 
     (eps-inc eps phi phi+ sigma g)))


;; from ex. 3
(def v-p 3)
(def sigma-p 1)
(def sigma-u 1)
(def u 2)
(def dt 0.01)
(def phi v-p)
(def error-p 0)
(def error-u 0)


;(defn phi-inc [eps h-tick phi theta- eps-] 
;  "Equation (53) in Bogacz's \"Tutoria\"."
;  (e+ (e- eps)
;      (e* (h-tick phi)
;          (m* theta- eps-))))
;
;(defn eps-inc [eps h phi theta sigma phi+] 
;  "Equation (54) in Bogacz's \"Tutoria\"."
;  (e- phi 
;      (m* theta (h phi+))
;      (m* sigma eps)))
