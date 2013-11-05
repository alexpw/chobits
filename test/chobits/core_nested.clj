(ns chobits.core-nested
  (:require [midje.sweet :refer :all]
            [chobits.core :refer :all]))

(def approval {
    :answ {
      :approved 2r1
      :rejected 2r10
      :waiting  2r100}
    :csr {
      :approved 2r1000
      :rejected 2r10000
      :waiting  2r100000}})

(with-props approval 2 ;; note the 2, indicates the key-depth

  ;; ->bit
  (tabular (fact (->bit ?k1 ?k2) => ?bit)
    ?k1   ?k2         ?bit
    :answ :rejected   2r10
    :answ "rejected"  2r10
    :csr  "waiting"   2r100000)

  ;; bit->
  (tabular (fact (bit-> ?bit) => ?prop)
    ?bit     ?prop
    2r1      [:answ :approved]
    2r10     [:answ :rejected]
    2r100000 [:csr  :waiting])

  ;; ->field
  (tabular (fact (->field ?prop) => ?field)
    ?prop                                 ?field
    [:answ "rejected"]                    2r10
    [:answ "not-found" :csr  "approved"]  2r1000)

  ;; ->field
  (tabular (fact (->field ?k1 ?k2 ?s1 ?s2) => ?field)
    ?k1   ?k2         ?s1   ?s2         ?field
    :answ "rejected"  nil   nil         2r10
    :answ "not-found" :csr  "approved"  2r1000
    :answ "rejected"  :csr  :approved   2r1010)

  ;; field->
  (tabular (fact (field-> ?field) => ?prop)
    ?field     ?prop
    2r10      (just ['(:answ :rejected)]                   :in-any-order)
    2r1010    (just ['(:answ :rejected) '(:csr :approved)] :in-any-order))

  ;; field-> byte-array
  (tabular (fact (field-> ?field) => ?prop)
    ?field                           ?prop
    (byte-array 1 (byte 2r10))       (just ['(:answ :rejected)]                   :in-any-order)
    (byte-array 1 (byte 2r1010))     (just ['(:answ :rejected) '(:csr :approved)] :in-any-order)
    (byte-array 1 (byte 2r1010))     (just ['(:answ :rejected) '(:csr :approved)] :in-any-order))

  ;; in-field?
  (tabular (fact (in-field? ?field ?k1 ?k2) => ?out)
    ?field    ?k1   ?k2        ?out
    2r10     :answ :rejected  true
    2r11     :answ :rejected  true
    2r1      :answ :rejected  false)

  ;; in-field? byte-array
  (tabular (fact (in-field? ?field ?k1 ?k2) => ?out)
    ?field                      ?k1   ?k2        ?out
    (byte-array 1 (byte 2r10)) :answ :rejected  true
    (byte-array 1 (byte 2r11)) :answ :rejected  true
    (byte-array 1 (byte 2r1))  :answ :rejected  false)

)
