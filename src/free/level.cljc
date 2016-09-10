;;; This software is copyright 2016 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;; Based on Rafal Bogacz's, "A Tutorial on the Free-energy Framework 
;; for Modelling Perception and Learning", _Journal of Mathematical 
;; Psychology_ (online 2015), http://dx.doi.org/10.1016/j.jmp.2015.11.003

;; SEE doc/level.md for documentation on general features of the code below.



(ns free.level
  (:require 
    ;[free.dists :as prob]
    [utils.string :as us]))

;; maybe move elsewhere so can be defined on command line?
(def ^:const use-core-matrix false)

(if use-core-matrix
  (require '[free.matrix-arithmetic :refer [e* m* m+ m- tr inv make-identity-obj]])
  (require '[free.scalar-arithmetic :refer [e* m* m+ m- tr inv make-identity-obj]]))


;;;;;;;;;;;;;;;;;;;;;
(declare phi-inc   next-phi 
         eps-inc   next-eps 
         sigma-inc next-sigma
         theta-inc next-theta
         next-level next-levels
         m-square)


;;;;;;;;;;;;;;;;;;;;;
;; Level

(defrecord Level [phi phi-dt 
                  eps eps-dt 
                  sigma sigma-dt 
                  theta theta-dt 
                  h h']) ; to add?: e for Hebbian sigma calculation

(def Level-docstring
  "\n  A Level records values at one level of a prediction-error/free-energy
  minimization model.  
  phi:   Current value of input at this level.
  eps:   Epsilon--the error at this level.
  sigma: Covariance matrix or variance of assumed distribution over inputs 
  at this level.  Variance should usually be >= 1 (p. 5 col 2).
  theta: When theta is multiplied by result of h(phi), the result is the 
  current estimated mean of the assumed distrubtion.  
  i.e. g(phi) = theta * h(phi), where '*' here is scalar or matrix 
  multiplication as appropriate.
  <x>-dt:  A scalar multiplier (e.g. 0.01) determining how fast <x> is updated.
  h, h': See theta; h' is the derivative of h.  These never change.
  Note that in Bogacz h had h' are generally the same across levels.
  All of these notations are defined in Bogacz's \"Tutorial\" paper.
  phi and eps can be scalars, in which case theta and sigma are as well.  
  Or phi and eps can be vectors of length n, in which case sigma and theta
  are n x n square matrices.  h and h' are functions that can be applied to 
  phi.  See doc/level.md for more information.")

(us/add-to-docstr! ->Level    Level-docstring)
(us/add-to-docstr! map->Level Level-docstring)

;;;;;;;;;;;;;;;;;;;;;
;; Functions to calculate next state of system

(defn next-level
  "Returns the value of this level for the next timestep."
  [[-level level +level]]
  (assoc level 
         :phi (next-phi  -level  level)
         :eps (next-eps   level +level)
         :sigma (next-sigma level)
         :theta (next-theta level +level)))

;; See notes in levels.md on this function.
(defn next-levels
  "Given a functions for updating h, h', and a bottom-level creation function
  that accepts two levels (its level and the next up), along with a sequence of 
  levels at one timestep, returns a vector of levels at the next timestep.  
  The top level will be used to calculate the next level down, but won't be 
  remade; it will be used again, as is, as the new top level."
  [next-bottom levels]
  (concat [(next-bottom (take 2 levels))] ; Bottom level is special case.
          (map next-level                 ; Each middle level depends on levels
               (partition 3 1 levels))    ;  immediately below and above it.
          [(last levels)]))               ; make sure top is carried forward

;; To see that it's necessary to calculate the error in the usual way
;; at the bottom level, cf. e.g. eq (14) in Bogacz.
(defn make-next-bottom
  "Returns a function similar to next-level, but in which the new phi is
  generated by phi-generator rather than being calculated in the normal way
  using the error epsilon from the next level down.  h' is not needed since
  it's only used by the normal phi calculation process.  The phi produced by
  phi-generator represents sensory input from outside the system."
  [phi-generator]
  (fn [[level +level]]
    (assoc level 
           :phi (phi-generator)
           :eps (next-eps level +level)
           :sigma (next-sigma level)
           :theta (next-theta level +level))))

;;;;;;;;;;;;;;;;;;;;;
;; phi update

(defn phi-inc
  "Calculates slope/increment to the next 'hypothesis' phi from the 
  current phi.  See equations (44), (53) in Bogacz's \"Tutorial\"."
  [phi eps -eps theta h']
  (m+ (m- eps)
      (e* (h' phi)
          (m* (tr theta) -eps))))

(defn next-phi 
  "Accepts three subsequent levels, but only uses this one and the one below. 
  Calculates the the next-timestep 'hypothesis' phi."
  [-level level]
  (let [{:keys [phi phi-dt eps theta h']} level
        -eps (:eps -level)]
    (m+ phi 
        (e* phi-dt
            (phi-inc phi eps -eps theta h')))))

;;;;;;;;;;;;;;;;;;;;;
;; epsilon update

(defn eps-inc 
  "Calculates the slope/increment to the next 'error' epsilon from 
  the current epsilon.  See equation (54) in Bogacz's \"Tutorial\"."
  [eps phi +phi sigma theta h]
  (m- phi 
      (m* theta (h +phi))
      (m* sigma eps)))

(defn next-eps
  "Accepts three subsequent levels, but only uses this one and the one above. 
  Calculates the next-timestep 'error' epsilon."
  [level +level]
  (let [{:keys [phi eps eps-dt sigma theta h]} level
        +phi (:phi +level)]
    (m+ eps
        (e* eps-dt
            (eps-inc eps phi +phi sigma theta h)))))

;;;;;;;;;;;;;;;;;;;;;
;; sigma update

(defn sigma-inc
  "Calculates the slope/increment to the next sigma from the current sigma,
  i.e.  the variance or the covariance matrix of the distribution of inputs 
  at this level.  See equation (55) in Bogacz's \"Tutorial\".  (Note uses 
  matrix inversion for vector/matrix calcualtions, a non-Hebbian calculation,
  rather than the local update methods of section 5.)"
  [eps sigma]
  (* 0.5 (m- (m-square eps)
             (inv sigma))))

(defn next-sigma
  "Accepts three subsequent levels, but only uses this one, not the ones
  above or below.  Calculates the next-timestep sigma, i.e. the variance 
  or the covariance matrix of the distribution of inputs at this level."
  [level]
  (let [{:keys [eps sigma sigma-dt]} level]
    (m+ sigma
        (e* sigma-dt
            (sigma-inc eps sigma)))))


;;;;;;;;;;;;;;;;;;;;;
;; theta update

(defn theta-inc
  "Calculates the slope/increment to the next theta component of the mean
  value function from the current theta.  See equation (56) in Bogacz's 
  \"Tutorial\"."
  [eps +phi h]
  (m* eps 
      (tr (h +phi))))

(defn next-theta
  "Accepts three subsequent levels, but only uses this one and the one above. 
  Calculates the next-timestep theta component of the mean value function."
  [level +level]
  (let [{:keys [eps theta theta-dt h]} level
        +phi (:phi +level)]
    (m+ theta
        (e* theta-dt
            (theta-inc eps +phi h)))))

;;;;;;;;;;;;;;;;;;;;;
;; Utility functions

(defn m-square
  [x]
  (m* x (tr x)))
