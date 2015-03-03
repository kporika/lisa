(ns lisa.l2switch
  (:use
        [lisa.ofcodec]
       ;; [lisa.dbr]
      
     
       
        )
  (:require
             [manifold.deferred :as d]
             [manifold.stream :as s]
             [clojure.edn :as edn]
             [aleph.tcp :as tcp]
             [gloss.core :as gloss]
             [gloss.io :as io]
             [clojure.string :as string]
             [clojure.tools.cli :refer [parse-opts]]
             [clojure.core.match :refer [match]]
         ;;    [taoensso.carmine :as car :refer (wcar)]
         ;;   [taoensso.timbre :as timbre]
            )
           
 (:import (java.net InetAddress))
 (:gen-class :main true))


;;(timbre/refer-timbre)

;; This function takes a raw TCP **duplex stream** which represents bidirectional communication
;; via a single stream.  Messages from the remote endpoint can be consumed via `take!`, and
;; messages can be sent to the remote endpoint via `put!`.  It returns a duplex stream which
;; will take and emit arbitrary Clojure data, via the protocol we've just defined.
;;
;; First, we define a connection between `out` and the raw stream, which will take all the
;; messages from `out` and encode them before passing them onto the raw stream.
;;
;; Then, we `splice` together a separate sink and source, so that they can be presented as a
;; single duplex stream.  We've already defined our sink, which will enocde all outgoing
;; messages.  We must combine that with a decoded view of the incoming stream, which is
;; accomplished via `gloss.io/decode-stream`.

(defn wrap-duplex-stream
  [protocol s]
  (let [out (s/stream)]
    (s/connect
      (s/map #(io/encode protocol %) out)
      s)

    (s/splice
      out
      (io/decode-stream s protocol))))



;; common messsage structures, no neeed to create them at run time
;; look for a way to pre-compile them in to a binary buffer also, if needed for
;; performance optimization
;; Basic Hello

(def OFPT-HELLO-MESSAAGE
  { :header
   {
    :version OFP-VERSION
    :type  :OFPT-HELLO
    :length 8
    :xid 0}})


;; ECHO-REPLY

(def ECHO-REPLY-MESSAGE
  { :header
   {
    :version OFP-VERSION
    :type  :OFPT-ECHO-REPLY
    :length 8
    :xid 0}})


;; FEATURES-REQUEST

(def OFPT-FEATURES-REQUEST-MESSAGE
  { :header
   {
    :version OFP-VERSION
    :type  :OFPT-FEATURES-REQUEST
    :length 8
    :xid 0}})


;; MULTI-PART Data request , specific case to get  ports data from switch

(def MULTI-PART-PORT-REQUEST
  {:header
   {
    :version OFP-VERSION
    :type  :OFPT-MULTIPART-REQUEST
    :length 16
    :xid 123456}
   :mp-req-type :OFPMP-PORT-DESC
   :flags 0
   :pad 0})


;; initial switch config
(def OFP-SET-CONFIG-MESSAGE
  {:header
   {:version 4 :type :OFPT-SET-CONFIG :length 12 :xid 234567 }
   :flags [:OFPC-FRAG-NORMAL]
   :miss-send-len OFP-DEFAULT-MISS-SEND-LEN })



;; flow mod message that floods the local network, 

(def FLOW-MOD-INIT-MESSAGE
  {:header
   {:version OFP-VERSION :type  :OFPT-FLOW-MOD :length 80 :xid 2345678 } 
                :table-id 0
                :cookie-mask 0
                :out-group 0
                :command :OFPFC-ADD
                :hard-timeout 0
                :pad 0
                :priority 0
                :cookie 0
                :out-port 0
                :flags []
                :buffer-id OFP-NO-BUFFER
                :idle-timeout 0
                :match {
                       :header {:type :OFPMT-OXM  :length 4 }
                       :oxm-fields []
                        :pad [0 0 0 0]
                        }
                                  :instruction {
                                                :pad 0
                              :len 24
                              :type :OFPIT-APPLY-ACTIONS
                 :actions
                              { :header { :type :OFPAT-OUTPUT :length 16 }
                               :pad [0 0 0 0 0 0]
                               :max-len 0xffff
                               :port 0xfffffffd
                               }}})


;; (defn client
;;   [host port]
;;   (d/chain (tcp/client {:host host, :port port})
;;     #(wrap-duplex-stream openflow %)))


;; publishes the data to the redis queue, before sending it to the client not used at this time

;; (defn publish-send
;;   [ch data]
;;   (do
;;     (publish-of-message data)
;;     (s/put! ch data)))



;; creates an OFPT-PACKET-OUT message from :OFPT-PACKET-IN message
;; floods the local network
;; need ARP and the hash at the controller to send the flow mod meesage
;; see the next function

(defn get-ofp-packet-out-local
  [inp]
  (let [bf  (:buffer-id inp)
        inport (-> inp :match :oxm-fields (get 0) :port )
        xid (+ (rand-int 65509) inport) ]
      { :header {:version OFP-VERSION :type  :OFPT-PACKET-OUT :length 40 :xid xid} 
                                   :buffer-id bf
                                   :in-port inport
                                   :actions-len 16
                                   :pad [0 0 0 0 0 0]
                                   :actions [{ :header {:type :OFPAT-OUTPUT :length 16 }
                                             :max-len 65509
                                             :port 0xfffffffb
                                             :pad [0 0 0 0 0 0]
                                             }]
       }))





;; invoked by the d-handler
;; handles the message ,
;; matches the incoming message and processes them
;; for ECHO-REQUEST responds with ECHO-REPLY-MESSAGE
;; for new client (Switch ) connections enquires about client features
;; and then asks for port list etc.
;; out going messages can be read from a buffer or a message queue before
;; sent to the client

(defn message-handler
  [data ci]
  (do
    (println data)
    ( match [data]
            [{:version _ :type :OFPT-ECHO-REQUEST }] ECHO-REPLY-MESSAGE
            [{:version _  :type :OFPT-HELLO}]  (do
                                                ;; (add-switch-property ci "version" (:version data))
                                                 [OFPT-HELLO-MESSAAGE OFPT-FEATURES-REQUEST-MESSAGE])
                                                 
            [{:version OFP-VERSION :type :OFPT-FEATURES-REPLY}]  (do
                                                                 ;;  (add-switch-property ci "features" data)
                                                                   MULTI-PART-PORT-REQUEST)
                                                                 
            [{:version OFP-VERSION :type :OFPT-MULTIPART-REPLY}] (do
                                                                  ;; (mapv #(add-switch-port ci %) (:ports data))
                                                                   [OFP-SET-CONFIG-MESSAGE FLOW-MOD-INIT-MESSAGE])
            [{:version OFP-VERSION :type :OFPT-PACKET-IN}]  (get-ofp-packet-out-local data)                                                                                             
            [_]  (println data)
            )))



;; (defn of-client-init
;;   [ch ci]
;;   (let [disc (add-switch-property ci "discovery" false) ]
;;     (do
;;       (println ci)
;;       (send-sym-message ch :OFPT-FEATURES-REQUEST)
;;       (publish-send ch { :header {:version 4 :type :OFPT-SET-CONFIG  :length 12 :xid 234567}
;;                          :flags [:OFPC-FRAG-NORMAL]
;;                          :miss-send-len OFP-DEFAULT-MISS-SEND-LEN }) 
;;       (send-multi-part-request ch)
;;       (send-flow-mod-init ch))))
    


(defn d-handler
  [f]
  (fn [s info]
    (do
      ;;(of-client-init s info)
      (println info)
      (d/loop []
        ;; take a message, and define a default value that tells us if the connection is closed
        (-> (s/take! s ::none)
            (d/chain
             ;; first, check if there even was a message, and then transform it on another thread
             (fn [msg]
               (if (= ::none msg)
                 ::none
                 (do
                 (println msg)
                 (d/future (f msg info)))))
             ;; once the transformation is complete, write it back to the client
             (fn [msg']
               (when-not (or  (= ::none msg') (= nil msg') )
                 (do
                   (println msg')
                   ;; (publish-of-message msg')
                   (if (vector? msg') (mapv #(s/put! s %) msg') (s/put! s msg'))
                   ;;(s/put! s msg')
                   )))
             ;; if we were successful in our response, recur and repeat
             (fn [result]
               (when result
                 (d/recur))))
            ;; if there were any issues on the far end, send a stringified exception back
            ;; and close the connection
            (d/catch
                (fn [ex]
                  ;; (s/put! s (str "ERROR: " ex))
                  (s/close! s))))))))





;; Takes a two-argument `handler` function, which takes a stream and information about the
;; connection, and sets up message handling for the stream.  The raw stream is wrapped in the
;; openflow protocol  before being passed into `handler`.







(def cli-options
  [
   ["-p" "--port PORT" "Port number"
    :default 6633
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-H" "--host  HOST" "Controller Host"
    :default "localhost"
    ;; Specify a string to output in the default column in the options summary
    ;; if the default value's string representation is very ugly
    :default-desc "localhost" ]
   ;; If no required argument description is given, the option is assumed to
   ;; be a boolean option defaulting to nil
   [nil "--detach" "Detach from controlling process"]
   ["-v" nil "Verbosity level; may be specified multiple times to increase value"
    ;; If no long-option is specified, an option :id must be given
    :id :verbosity
    :default 0
    ;; Use assoc-fn to create non-idempotent options
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]])



(defn usage [options-summary]
  (->> ["OpenFlow controller, written in Clojure"
        ""
        "Usage: program-name [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  start    Start the  Openflow controller"
        "  stop     Stop an existing controller"
        "  status   Print controller's status"
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))



;; starts the tcp server on default port 6633 if no port is given
;; openflow is the default encoder/decoder for this tcp server
;; handler function with a message wrapper does the client handling
;; see d-handler for example
;; 


(defn start-server
  [handler host port]
  (tcp/start-server
    (fn [s info]
      (handler (wrap-duplex-stream openflow s) info))
    {:host host :port port}))


;; main function that handles the arguments
;; port is defaulted 6633
;; host is defaulted to localhost
;; Client handler function: d-handler
;; message handler : message-handler
;; 
;;

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 (usage summary))
      (not= (count arguments) 1) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))
    ;; Execute program with options
    (case (first arguments)
      "start"
      (start-server
       (d-handler message-handler)
       (options :host)
       (options :port))
      (exit 1 (usage summary)))))
