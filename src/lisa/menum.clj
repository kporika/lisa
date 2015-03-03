(ns lisa.menum
  (:use
    [gloss.data bytes string primitives]
    [gloss.core protocols structure formats]))

(defn multi-enum-val
  "Takes a list of enumerations, or a map of enumerations onto values, and returns
   a codec which associates each enumeration with a unique encoded value.
   (enum :byte :a :b :c)
   (enum :int32 {:a 100, :b 200, :c 300})

  value can be combination of any of the enums (union or or of the values)
  takes an array of enums as input [:a :b :c]
  returns an array of enums from decoded value
  "
  [primitive-type & map-or-seq]
  (assert (primitive-codecs primitive-type))
  (let [coerce #(if (char? %)
                  (long (int %))
                  (long %))
        n->v (if (and (= 1 (count map-or-seq)) (map? (first map-or-seq)))
	       (let [m (first map-or-seq)]
		 (zipmap
		   (map coerce (vals m))
		   (keys m)))
	       (zipmap
		 (map coerce (range (count map-or-seq)))
		 map-or-seq))
	v->n (zipmap (vals n->v) (keys n->v))
	codec (primitive-codecs primitive-type)]
    (reify
      Reader
      (read-bytes [this b]
	(let [[success x b] (read-bytes codec b)]
	  (if success
        ( let [value (coerce x) ]
            [true (into [] (map  #(n->v %) (filter #(> (bit-and value %) 0) (keys n->v))))  b])
            ;;[true (n->v (coerce x)) b]))
	      [false this b])))
      Writer
      (sizeof [_]
	(sizeof codec))
      (write-bytes [_ buf key-args]
	(if-let [n (reduce + (map #(v->n %) key-args))]
	  (write-bytes codec buf n)
	  (throw (Exception. (str "Expected one or more of " (keys v->n) ", but got " key-args))))))))


(defn enum-mask
  "Takes a list of enumerations, or a map of enumerations onto values, and returns
   a codec which associates each enumeration with a unique encoded value.
   (enum :byte :a :b :c)
   (enum :int32 {:a 100, :b 200, :c 300})"
  [primitive-type & map-or-seq]
  (assert (primitive-codecs primitive-type))
  (let [coerce #(if (char? %)
                  (long (int %))
                  (long %))
        n->v (if (and (= 1 (count map-or-seq)) (map? (first map-or-seq)))
	       (let [m (first map-or-seq)]
		 (zipmap
		   (map coerce (vals m))
		   (keys m)))
	       (zipmap
		 (map coerce (range (count map-or-seq)))
		 map-or-seq))
	v->n (zipmap (vals n->v) (keys n->v))
	codec (primitive-codecs primitive-type)]
    (reify
      Reader
      (read-bytes [this b]
	(let [[success x b] (read-bytes codec b)]
	  (if success
	    [true [(n->v (bit-and 0x7f  (bit-shift-right  (coerce x) 1) ))  (bit-and 0x01 (coerce x) ) ]   b]
	    [false this b])))
      Writer
      (sizeof [_]
	(sizeof codec))
      (write-bytes [_ buf v]
	(if-let [n (v->n (first v))]
	  (write-bytes codec buf (bit-or (bit-shift-left n 1) (bit-and 0x01 (second v))))
	  (throw (Exception. (str "Expected one of " (keys v->n) ", but got " v))))))))


;;;

(defn ordered-map-defaults
  "Creates a codec which consumes and emits standard Clojure hash-maps, but
   ensures that the values are encoded in the specified order.  Useful for
   interop with C structs and network protocols. Should have 4 columns , 
   :key codec-definition :default value
   :id :uint32 :default 0
   :port-status :uint32 :default :PORT-STATUS-UP
   "
  [& key-value-pairs]
  (assert (even? (count key-value-pairs)))
  (let [pairs (partition 4 key-value-pairs)
	ks (map first pairs)
	vs (map compile-frame (map second pairs))
        dvs (map #(nth %  3) pairs)
        dkvs (zipmap ks dvs)
	codec (convert-sequence vs)
	read-codec (compose-callback
		     codec
		     (fn [v b] [true (zipmap ks v) b]))]
    (reify
      Reader
      (read-bytes [_ b]
	(read-bytes read-codec b))
      Writer
      (sizeof [_]
	(sizeof codec))
      (write-bytes [_ buf v]
        (let [mvs (merge dkvs v)] 
          (when-not (map? mvs)
            (throw (Exception. (str "Expected a map, but got " mvs))))
          (write-bytes codec buf (map #(get mvs  %) ks)))))))








