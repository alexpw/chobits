(ns chobits.core-flatmap
  (:require [midje.sweet :refer :all]
            [chobits.core :refer :all]))

(def days-of-week {
  :sun 2r1
  :mon 2r10
  :tue 2r100
  :wed 2r1000
  :thu 2r10000
  :fri 2r100000
  :sat 2r1000000
  ;; multi-byte test: 10 bits is larger than int (> 7)
  :xmas 2r1000000000})

;; Wrap the following tests in this context once.

(with-props days-of-week 1

  ;; ->bit
  (tabular (fact (->bit ?in) => ?out)
    ?in   ?out
    :sun  2r1
    :mon  2r10
    :tue  2r100
    :wed  2r1000
    :thu  2r10000
    :fri  2r100000
    :sat  2r1000000)

  ;; bit->
  (tabular (fact (bit-> ?in) => ?out)
    ?in ?out
    2r1 :sun
    2r10 :mon
    2r100 :tue
    2r1000 :wed
    2r10000 :thu
    2r100000 :fri
    2r1000000 :sat)

  ;; ->field
  (tabular (fact (->field ?k1) => ?out)
    ?k1   ?out
    :sun  2r1
    :mon  2r10
    :sat  2r1000000)

  ;; ->field
  (tabular (fact (->field ?k1 ?k2) => ?out)
    ?k1  ?k2    ?out
    :sun :mon   2r11
    :mon :wed   2r1010
    :sun :sat   2r1000001)

  ;; ->field
  (tabular (fact (->field ?k1 ?k2 ?k3) => ?out)
    ?k1  ?k2  ?k3   ?out
    :sun :mon :tue  2r111
    :mon :wed :fri  2r101010
    :sun :sat nil   2r1000001)

  ;; ->field
  (tabular (fact (->field ?k1) => ?out)
    ?k1    ?out
    [:sun] 2r1
    [:mon] 2r10
    [:sat] 2r1000000)

  ;; ->field
  (tabular (fact (->field ?k1) => ?out)
    ?k1           ?out
    [:sun :mon]   2r11
    [:mon :wed]   2r1010
    [:sun :sat]   2r1000001)

  ;; ->field
  (tabular (fact (->field ?k1) => ?out)
    ?k1               ?out
    [:sun :mon :tue]  2r111
    [:mon :wed :fri]  2r101010
    [:sun :sat nil ]  2r1000001)

  ;; field->
  (tabular (fact (field-> ?field) => ?props)
    ?field        ?props
    2r111         (just [:sun :mon :tue] :in-any-order)
    2r101010      (just [:mon :wed :fri] :in-any-order)
    2r1000001     (just [:sun :sat]      :in-any-order))

  ;; field-> byte-array
  (tabular (fact (field-> ?field) => ?props)
    ?field                             ?props
    (byte-array 1 (byte 1))            (just [:sun])
    (byte-array 1 (byte 7))            (just [:sun :mon :tue] :in-any-order)
    (byte-array 1 (byte 42))           (just [:mon :wed :fri] :in-any-order)
    (byte-array 1 (byte 127))          (just [:sun :mon :tue :wed :thu :fri :sat] :in-any-order)
    ;; multi-byte test
    (byte-array 2 [(byte 2) (byte 0)]) (just [:xmas])
  )

  ;; in-field?
  (tabular (fact (in-field? ?field ?key) => ?out)
    ?field  ?key    ?out
    2r11    :mon    true
    2r100   :mon    false)

  ;; in-field? byte-array
  (tabular (fact (in-field? ?field ?key)  => ?out)
    ?field                       ?key    ?out
    (byte-array 1 (byte 2r11))   :mon    true
    (byte-array 1 (byte 2r100))  :mon    false)

)
