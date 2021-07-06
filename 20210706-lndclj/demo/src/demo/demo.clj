(ns demo.demo
  (:require [tech.droit.fset :as fset]
            [criterium.core :refer [quick-bench]]
            [clojure.pprint :refer [pprint]]
            [clojure.set :as cset]))

; (cset/difference #{1 3 5 10 2 4} #{2 4 11})
; (fset/difference #{1 3 5 10 2 4} #{2 4 11})

(defn symmetric-difference
  "Everything, except if it's in the intersection."
  [s1 s2]
  (cset/difference (cset/union s1 s2) (cset/intersection s1 s2)))

(defn symmetric-difference [s1 s2]
  (fset/difference (fset/union s1 s2) (fset/intersection s1 s2)))

; (symmetric-difference #{1 3 5 10 2 4} #{2 4 11})
; (let [s1 #{1 3 5 10 2 4} s2 #{2 4 11}] (quick-bench (symmetric-difference s1 s2)))

(def users
  #{{:user-id 1 :name "john"   :age 22 :type "personal"}
    {:user-id 2 :name "jake"   :age 28 :type "company"}
    {:user-id 3 :name "amanda" :age 63 :type "personal"}})

(def accounts
  #{{:acc-id 1 :user-id 1 :amount 300.45 :type "saving"}
    {:acc-id 2 :user-id 2 :amount 1200.0 :type "saving"}
    {:acc-id 3 :user-id 1 :amount 850.1 :type "debit"}})

;; SELECT users.user-id, accounts.acc-id,
;;        users.type as type, accounts,type as atype
;; FROM users
;; INNER JOIN accounts ON users.user-id = accounts.user-id;

(defn q []
  (cset/project
    (cset/join users (cset/rename accounts {:type :atype}))
    [:user-id :acc-id :type :atype]))

(defn q []
  (fset/project
    (fset/join users (fset/rename accounts {:type :atype}))
    [:user-id :acc-id :type :atype]))

; (pprint (q))
; (quick-bench (q))
