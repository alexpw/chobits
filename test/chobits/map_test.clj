(ns chobits.map-test
  (:require [midje.sweet :refer :all]
            [chobits.map :refer :all]))

(facts
  (invert {:a 1})      => (just {1 '(:a)})
  (invert {:a 1 :b 2}) => (just {1 '(:a) 2 '(:b)} :in-any-order))

(facts

  (invert {:a {:b 1}})

   => (just {1 '(:a :b)})

  (invert {:a {:b {:c 1
                   :d 2}}})

   => (just {1 '(:a :b :c)
             2 '(:a :b :d)} :in-any-order)

  (invert {:a {:c 1}
           :b {:d 2}})

   => (just {1 '(:a :c)
             2 '(:b :d)} :in-any-order))
