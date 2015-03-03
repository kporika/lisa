(ns  lisa.gloss.extension
  (:require [lisa.gloss.extension.codecs :as codecs]
            [gloss.core :refer [compile-frame]]))

(defn header
  "I copied ztellman here. codecs/header is slightly different."
  [frame header->body body->header]
  (codecs/header
   (compile-frame frame)
   header->body
   body->header))
