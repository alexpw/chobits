(ns chobits.map)

(defn- collapse-keys
  ""
  [data & [keys-coll]]
  (if (map? data)
      (mapcat (fn [[k v]]
                  (collapse-keys v (if (nil? keys-coll)
                                       (conj [] k)
                                       (conj (into [] keys-coll) k))))
              data)
      (hash-map (flatten keys-coll) data)))

(defn invert
  "Takes a hash-map of unknown nesting and returns a flat map keyed by the
  leaves. The new value is the key path needed to traverse the original map.
  See tests for examples."
  [tree]
  (->> (collapse-keys tree)
       (map (juxt second first))
       (into {})))
