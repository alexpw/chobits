(ns chobits.bytearray-test
  (:require [midje.sweet :refer :all]
            [chobits.bytearray :as ba :refer :all]))

(def b0 (repeat (byte 0)))
(def b1 (repeat (byte 1)))

(defn has-n-bytes-of [n b]
  (fn [actual]
    (= n (count (filterv #(= % b) actual)))))

;; A "common" Clojure idiom is to call a private through a var. That looks like this:

(tabular (facts (#'ba/left-pad-byte-array ?in ?pad) => ?out)
  ?in                          ?pad  ?out
  (byte-array 0)                  4  (has-n-bytes-of 4 (byte 0))
  (byte-array 1 (take 1 b1))      4  (has-n-bytes-of 3 (byte 0))
  (byte-array 2 (take 2 b1))      4  (has-n-bytes-of 2 (byte 0))
  (byte-array 3 (take 3 b1))      4  (has-n-bytes-of 1 (byte 0))
  (byte-array 4 (take 4 b1))      4  (has-n-bytes-of 0 (byte 0))
  (byte-array 8 (take 8 b1))      8  (has-n-bytes-of 0 (byte 0))
  (byte-array 8 (take 8 b0))      8  (has-n-bytes-of 8 (byte 0)))
