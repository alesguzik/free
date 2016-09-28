(ns free.random
  (:import [ec.util MersenneTwisterFast]))

(defn make-long-seed
  [] 
  (- (System/currentTimeMillis)
     (rand-int Integer/MAX_VALUE)))

(defn flush-rng
  [rng]
  (dotimes [_ 1500] (.nextInt rng)))  ; see ;; https://listserv.gmu.edu/cgi-bin/wa?A1=ind1609&L=MASON-INTEREST-L#1

(defn make-rng-mtf
  "Make an instance of a MersenneTwisterFast RNG and flush out its initial
  minimal lack of entropy."
  ([] (make-rng-mtf (make-long-seed)))
  ([long-seed] 
   (let [rng (MersenneTwisterFast. long-seed)]
     (flush-rng rng)
     rng))) 

(def make-rng make-rng-mtf)

(defn make-rng-print-seed
  "Make a seed, print it to stdout, then pass it to make-rng."
  []
  (let [seed (make-long-seed)]
    (println seed)
    (make-rng seed)))

(defn rand-idx [rng n] (.nextInt rng n))

(defn next-long [rng] (.nextLong rng))

(defn next-double
"Returns a random double in the half-open range from [0.0,1.0)."
  [rng]
  (.nextDouble rng))

(defn next-gaussian
  "Returns a random double from a Gaussian distribution.  If mean and sd aren't
  supplied, uses a standard Gaussian distribution with mean = 0 and sd = 1,
  otherwise uses the supplied mean and standard deviation."
  ([rng] (.nextGaussian rng))
  ([rng mean sd]
   (+ mean (* sd (next-gaussian rng)))))

