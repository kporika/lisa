(ns lisa.common
  (:require [lisa.gloss.extension :as e]
            [clojure.string :as str]
            [gloss.core :refer :all]
            [gloss.io :refer :all] )
   (:use    [lisa.menum]))







;;;
(defcodec port-name  (compile-frame
                   (string :ascii :length 16)
                   (fn [s]
                     (->> 0 char
                          repeat
                          (concat s)
                          (take 16)
                          (apply str)))
                   (fn [s]
                     (->> s
                          (take-while (fn [s] (not= s (char 0))))
                          (apply str)))))


;;IP V4 Address
(defcodec ip-addr (compile-frame (repeat 4 :ubyte)
                                 (fn [s]
                                   (map
                                    #(Integer/parseInt %)
                                    (str/split s  #"\.")))
                                 (fn [b]
                                   (apply str (interpose "."  b)))))


;; MAC-Address
(defcodec mac-addr (compile-frame (repeat 6 :ubyte)
                                  (fn [s]
                                     (map
                                      #(Integer/parseInt % 16)
                                      (str/split s  #":")))
                                  (fn [b]
                                     (apply str (interpose ":" (map #(Integer/toString % 16) b))))))


;; Ethernet header and header size
(defcodec  eth-byte (compile-frame  :ubyte
                                  ( fn [s]
                                    (bit-or (bit-shift-left (s :version) 4)  (/  (s :size) 4 )))
                                  (fn [b]
                                    {:version (bit-shift-right b 4) :size (* 4  (bit-and b 0x0f))})))




