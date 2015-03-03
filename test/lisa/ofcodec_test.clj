(ns lisa.ofcodec-test
  (:use [clojure.test]
        [lisa.ofcodec])
  
  (:require [clojure.test]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [clojure.edn :as edn]
            [aleph.tcp :as tcp]
            [gloss.core :as gloss]
            [gloss.io   :as io]))
  
  
  

(def TEST-PORT 6635)


(defn echo-handler [s info]
  (s/connect s s))



(defonce
  server1 (tcp/start-server  echo-handler  {:port TEST-PORT :host "localhost" }))


(defn wrap-duplex-stream
  [protocol s]
  (let [out (s/stream)]
    (s/connect
      (s/map #(io/encode protocol %) out)
      s)

    (s/splice
      out
      (io/decode-stream s protocol))))



(defn client
  [host port]
  (d/chain (tcp/client {:host host, :port port})
    #(wrap-duplex-stream openflow %)))



(defonce c @(client "localhost" TEST-PORT))

(defn get-message
  [data]
  (let [sn-status @(s/put! c data)  rx-message @(s/take! c)]
    rx-message))



(deftest hello-basic-test
  (let [ message (get-message  {
                             :header
                             {:version 4
                              :type :OFPT-HELLO
                              :length 8
                              :xid 0
                              }})]
    (is (= 4 (message :version)))
    (is (= :OFPT-HELLO (message :type)))
    (is (= 0  (message :xid)))
    (is (= 8  (message :length)))))


(deftest hello-bitmap
  (let [ message (get-message
                  {:header
                   {:version 4
                    :type :OFPT-HELLO
                    :length 16 :xid 0 }
                   :elements
                   {:type :OFPHET-VERSIONBITMAP :length 8 :bitmap [0x00 0x00 0x00 0x10]}})
        hello-extra (message :elements)]
    (is (= [0 0 0 16] (hello-extra :bitmap)))
    (is (= :OFPHET-VERSIONBITMAP (hello-extra :type)))))




(deftest echo-request
  (let [message (get-message  {:header {:version 4 :type :OFPT-ECHO-REQUEST :length 8 :xid 23 }}) ]
    (is (= 4 (message :version)))
    (is (= :OFPT-ECHO-REQUEST (message :type)))
    (is (= 23  (message :xid)))
    (is (= 8 (message :length)))))


(deftest echo-reply
  (let [message (get-message  {:header {:version 4 :type :OFPT-ECHO-REPLY :length 8 :xid 45 }}) ]
    (is (= 4 (message :version)))
    (is (= :OFPT-ECHO-REPLY (message :type)))
    (is (= 45  (message :xid)))
    (is (= 8 (message :length)))))


(deftest switch-config
  (let [ message  (get-message  {:header {:version 4 :type :OFPT-SET-CONFIG :length 12 :xid 234567 }
                             :flags [:OFPC-FRAG-NORMAL]
                             :miss-send-len OFP-DEFAULT-MISS-SEND-LEN }) ]
    (is (= 4 (message :version)))
    (is (= :OFPT-SET-CONFIG (message :type)))
    (is (= OFP-DEFAULT-MISS-SEND-LEN  (message :miss-send-len)))))




(deftest feature-request
  (let [message  (get-message {:header {:version 4 :type :OFPT-FEATURES-REQUEST :length 8 :xid 2345 }})]
    (is (= 4 (message :version)))
    (is (= :OFPT-FEATURES-REQUEST (message :type)))
    (is (= 2345  (message :xid)))
    (is (= 8 (message :length)))))



(deftest feature-reply
  (let [message  (get-message {:header
                             {:version 4
                              :type :OFPT-FEATURES-REPLY
                              :length 32
                              :xid 515695042 }
                              :datapath-id    0x01
	                      :n-buffers      256
	                      :n-tables	      0xfd
		              :auxiliary-id   0
	                      :pad	      0
	                      :capabilities   [:OFPC-FLOW-STATS :OFPC-TABLE-STATS :OFPC-PORT-STATS :OFPC-QUEUE-STATS]
	                      :reserved	      0 
                             })]
    (is (= 1 (message :datapath-id)))
    (is (= :OFPT-FEATURES-REPLY (message :type)))
    (is (= 256  (message :n-buffers)))
    (is (= 32 (message :length)))))





(deftest multipart-port-request
  (let [message  (get-message {
                             :header
                              {:version 4
                               :type :OFPT-MULTIPART-REQUEST
                               :length 16
                               :xid 2345 }
                             :mp-req-type :OFPMP-PORT-DESC
                             :flags 0
                             :pad 0
                             })]
    (is (= 4 (message :version)))
    (is (= :OFPMP-PORT-DESC (message :mp-req-type)))
    (is (= 2345  (message :xid)))
    (is (= 0     (message :pad)))
    (is (= 16 (message :length)))))





(deftest multipart-port-reply
  (let [message  (get-message {:header
                             {
                              :version 4
                              :type :OFPT-MULTIPART-REPLY
                              :length 208
                              :xid 2345
                              }
                              :mp-req-type :OFPMP-PORT-DESC
                              :flags 0
                              :pad 0
                              :ports [
                                      {} ;; port 1
                                      {} ;; port 2
                                      {} ;; port 3
                                      {} ;; port 4
                                      ]})]
    (is (= 4 (message :version)))
    (is (= :OFPMP-PORT-DESC (message :mp-req-type)))
    (is (= 2345  (message :xid)))
    (is (= 0     (message :pad)))
    (is (= 208   (message :length)))))





(deftest ofp-packet-in-arp
  (let [ message  (get-message {:header
                             {
                              :version 4
                              :type :OFPT-PACKET-IN
                              :length 140
                              :xid 1234567
                              }
                            })]
    (is (= 4 (message :version)))
    (is (= 1234567  (message :xid)))
    (is (= 140    (message :length)))))



(deftest ofp-packet-out-test
  (let [message  (get-message {:header
                             {
                              :version 4
                              :type :OFPT-PACKET-OUT
                              :length 40
                              :xid 2691580066
                              }
                            })]
    (is (= 4 (message :version)))
    (is (= 2691580066  (message :xid)))
    (is (= 40    (message :length)))))


(deftest ofp-flow-mod-test
  (let [message  (get-message {:header
                             {
                              :version 4
                              :type :OFPT-FLOW-MOD
                              :length 96
                              :xid 1234567
                              }
                            })]
    (is (= 4 (message :version)))
    (is (= 1234567  (message :xid)))
    (is (= 96   (message :length)))))



