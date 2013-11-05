(ns chobits.bytearray)

(defn- <<
  "Arithmetic bitwise shift left"
  {:inline (fn [x n] `(. clojure.lang.Numbers (shiftLeft ~x ~n)))}
  [x n]
  (. clojure.lang.Numbers shiftLeft x n))

(defn- left-pad-byte-array
  [#^bytes ba pad]
  (byte-array pad
              (concat (mapv (fn [i] (byte 0))
                            (range 0 (- pad (count ba))))
                      (seq ba))))

(def ^{:private true} FFI (Integer/decode "#FF"))
(def ^{:private true} FFL (Long/decode "#FF"))

(defn- bytes->int
  [#^bytes ba]
  (bit-or (<< (aget ba 0) 24)
          (<< (bit-and (aget ba 1) FFI) 16)
          (<< (bit-and (aget ba 2) FFI) 8)
              (bit-and (aget ba 3) FFI)))

(defn- bytes->long
  [#^bytes ba]
  (bit-or (<< (bit-and (aget ba 0) FFL) 56)
          (<< (bit-and (aget ba 1) FFL) 48)
          (<< (bit-and (aget ba 2) FFL) 40)
          (<< (bit-and (aget ba 3) FFL) 32)
          (<< (bit-and (aget ba 4) FFL) 24)
          (<< (bit-and (aget ba 5) FFL) 16)
          (<< (bit-and (aget ba 6) FFL) 8)
              (bit-and (aget ba 7) FFL)))

(defn bytes->number
  [ba]
  (if (<= (count ba) 4)
      (bytes->int  (left-pad-byte-array ba 4))
      (bytes->long (left-pad-byte-array ba 8))))
