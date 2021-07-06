(ns demo.core
  (:require [tech.droit.fset :as fset]
            [criterium.core :refer [quick-bench bench]])
  (:import
    [java.util Iterator]
    [clojure.lang
     ITransientSet
     ITransientCollection
     IEditableCollection
     IPersistentSet]))

(defn union-0
  "This is the same as clojure.set/union with just one arity."
  [s1 s2]
  (if (< (count s1) (count s2))
    (reduce conj s2 s1)
    (reduce conj s1 s2)))

#_(let [s1 (set (range 200))
      s2 (set (range 100 300))]
  (quick-bench (union-0 s1 s2)))
;; Execution time mean : 26.754516 µs
;; this is our baseline

(defn union-1
  "One of the arguments is repeatedly mutated.
  Let's go transient."
  [s1 s2]
  (persistent!
    (if (< (count s1) (count s2))
      (reduce conj! (transient s2) s1)
      (reduce conj! (transient s1) s2))))

#_(let [s1 (set (range 200))
      s2 (set (range 100 300))]
  (quick-bench (union-1 s1 s2)))
;; Execution time mean : 20.209692 µs
;; Somewhat better.

;; Time to set this on.
(set! *warn-on-reflection* true)

(defn union-2
  "If it's only set we are talking about, can we take
  advantage of this information? We can then use Java interop."
  [^IEditableCollection s1 ^IPersistentSet s2]
  (if (< (count s1) (count s2))
    (recur s2 s1)
    (.persistent ^ITransientSet
      (reduce
        (fn [^ITransientCollection s item]
          (.conj s item))
        (.asTransient s1)
        s2))))

#_(let [s1 (set (range 200))
      s2 (set (range 100 300))]
  (quick-bench (union-2 s1 s2)))
;; Execution time mean : 19.038790 µs
;; A bit of a small increment for all the added noise.

(defn union-3
  "Let's get rid of reduce, we can replace it with a loop."
  [^IEditableCollection s1 ^IPersistentSet s2]
  (if (< (count s1) (count s2))
    (recur s2 s1)
    (let [^Iterator items (.iterator ^Iterable s2)]
      (loop [^ITransientSet s (.asTransient s1)]
        (if (.hasNext items)
          (recur (.conj s (.next items)))
          (.persistent s))))))

#_(let [s1 (set (range 200))
      s2 (set (range 100 300))]
  (quick-bench (union-3 s1 s2)))
;; Execution time mean : 15.361588 µs
;; Interesting. Looping is improving quite a bit.
