(ns chobits.core
  (:require [chobits.map :as m :only (invert)]
            [chobits.bytearray  :as ba :only (bytes->number)]))

;; Dynamic overrides for (with-props) bindings

(def ^{:doc "A mapping of keys to bits."
       :dynamic true :private true} *props* {})

(def ^{:doc "An inverted mapping of bits to flattened keys."
       :dynamic true :private true} *inverted* {})

(def ^{:doc "Allows structuring props as a nested map."
       :dynamic true :private true} *key-depth* 1)

;; We'll trade memory to make deconstruction methods like (field->) quick and easy.
(def memo-invert (memoize m/invert))

(defmacro with-props
  "Use me with everything else.  Keeps the context behind
  the scenes so it isn't continually passed around."
  [props key-depth & body]
  `(binding [*props* ~props
             *inverted* (memo-invert ~props)
             *key-depth* ~key-depth]
    ~@body))

(defn ->bit
  "Find the bit for a property."
  ([arg]
    (if (coll? arg)
        (get-in *props* (mapv keyword arg))
        (get *props* (keyword arg))))
  ([x & args]
    (get-in *props* (mapv keyword (into [x] args)))))

(defn bit->
  "Deconstruct a bit back to a property."
  [bit]
  (when-let [prop (get *inverted* bit)]
    (if (= *key-depth* 1)
        (first prop)
        prop)))

(defn bytes->field
  "Normalize varied input (byte-array or number) to produce an bit field (number).
  This is entirely because JDBC MySQL likes to return byte[].
  Used internally, but left public."
  [ba]
  (if (= "class [B" (str (class ba)))
      (ba/bytes->number ba)
      ba))

(defn ->field
  "Construct a bit-field using bit properties.
  (->field :sun :mon :tue) => 2r111
  (->field [:sun :mon :tue]) => 2r111
  (->field [:answ :approved] [:csr :approved]) => 2r1001
  (->field :answ :approved :csr :approved) => 2r1001
  (->field ['(:answ :approved) '(:csr :approved)]) => 2r1001"
  [x & args]
  (let [props (if (empty? args)
                  (if (not (coll? x))
                      [x] x)
                  (into [x] args))
        bits  (into []
                (remove nil?
                  (map ->bit
                       (if (coll? (first props))
                           props
                           (partition *key-depth* props)))))]
    (if (> (count bits) 1)
        (apply bit-or bits)
        (first bits))))

(defn field->
  "Deconstruct a bit-field into an unordered vector of properties.
   2r11 => [:sun :mon]
   2r11 => [(:answ :approved) (:answ :rejected)]"
  [field]
  (let [field (bytes->field field)]
    (mapv bit-> (filter #(not= 0 (bit-and field %))
                        (keys *inverted*)))))

(defn in-field?
  "bool: is the bit corresponding to the property in the bit field?
  (in-field? 2r11 :sun) => true"
  [field & args]
  (not= 0 (bit-and (bytes->field field)
                   (apply ->bit args))))
