(ns lisa.ofcodec
  (:require [lisa.gloss.extension :as e]
            [clojure.string :as str]
            [gloss.core :refer :all]
            [gloss.io :refer :all]
            )
  (:use    [lisa.menum]
           [lisa.common]
           [lisa.ip]
           ;;[gloss.io ]
          ;; [gloss.core]
           ))






(def   OFP-VERSION   0x04 )
(def   OFP-MAX-TABLE-NAME-LEN 32 )
(def   OFP-MAX-PORT-NAME-LEN  16 )
(def   OFP-TCP-PORT  6633 )
(def   OFP-SSL-PORT  6633 )
(def   OFP-ETH-ALEN 6     )
(def   OFP-DEFAULT-MISS-SEND-LEN   128 )
(def   OFP-FLOW-PERMANENT 0 )
(def   OFP-DEFAULT-PRIORITY 0x8000 )
(def   OFP-NO-BUFFER 0xffffffff )
(def   DESC-STR-LEN   256 )
(def   SERIAL-NUM-LEN 32 )
(def   OFPQ-ALL      0xffffffff )
(def   OFPQ-MIN-RATE-UNCFG      0xffff )
(def   OFPQ-MAX-RATE-UNCFG      0xffff )








(defcodec  ofp-action-output
 	 (ordered-map
	      ;;  :header       ofp-action-header
		:port	      :uint32
		:max-len      :uint16
		:pad	      (repeat 6 :ubyte)
                ))


(defcodec  ofp-action-push
 	 (ordered-map
	;;	:type	        :uint16
	;;	:len	        :uint16
		:ethertype	:uint16
		:pad	        :uint16
			))





(defcodec  ofp-port-no
 (enum :uint32
  {

	:OFPP-MAX            4294967040
	:OFPP-IN-PORT        4294967288
	:OFPP-TABLE          4294967289
	:OFPP-NORMAL         4294967290
	:OFPP-FLOOD          4294967291
	:OFPP-ALL            4294967292
	:OFPP-CONTROLLER     4294967293
	:OFPP-LOCAL          4294967294
        :OFPP-ANY            4294967295
   }))


(defcodec  ofp-type

  (enum :ubyte
  {
        :OFPT-HELLO                        0
	:OFPT-ERROR                        1
	:OFPT-ECHO-REQUEST                 2
	:OFPT-ECHO-REPLY                   3
	:OFPT-EXPERIMENTER                 4
	:OFPT-FEATURES-REQUEST             5
	:OFPT-FEATURES-REPLY               6
	:OFPT-GET-CONFIG-REQUEST           7
	:OFPT-GET-CONFIG-REPLY             8
	:OFPT-SET-CONFIG                   9
	:OFPT-PACKET-IN                   10
	:OFPT-FLOW-REMOVED                11
	:OFPT-PORT-STATUS                 12
	:OFPT-PACKET-OUT                  13
	:OFPT-FLOW-MOD                    14
	:OFPT-GROUP-MOD                   15
	:OFPT-PORT-MOD                    16
	:OFPT-TABLE-MOD                   17
	:OFPT-MULTIPART-REQUEST           18
	:OFPT-MULTIPART-REPLY             19
	:OFPT-BARRIER-REQUEST             20
	:OFPT-BARRIER-REPLY               21
	:OFPT-QUEUE-GET-CONFIG-REQUEST    22
	:OFPT-QUEUE-GET-CONFIG-REPLY      23
	:OFPT-ROLE-REQUEST                24
	:OFPT-ROLE-REPLY                  25
	:OFPT-GET-ASYNC-REQUEST           26
	:OFPT-GET-ASYNC-REPLY             27
	:OFPT-SET-ASYNC                   28
        :OFPT-METER-MOD                   29
   }))




(defcodec  ofp-header
 	 (ordered-map
		:version	:ubyte   :default  OFP-VERSION
		:type		ofp-type :default  :OFPT-ECHO-REPLY
		:length		:uint16  :default  8         
		:xid	        :uint32  :default  0
	  ))


(defcodec  ofp-multipart-type
 (enum :uint16
  {
	:OFPMP-DESC               0
	:OFPMP-FLOW               1
	:OFPMP-AGGREGATE          2
	:OFPMP-TABLE              3
	:OFPMP-PORT-STATS         4
	:OFPMP-QUEUE              5
	:OFPMP-GROUP              6
	:OFPMP-GROUP-DESC         7
	:OFPMP-GROUP-FEATURES     8
	:OFPMP-METER              9
	:OFPMP-METER-CONFIG       10
	:OFPMP-METER-FEATURES     11
	:OFPMP-TABLE-FEATURES     12
	:OFPMP-PORT-DESC          13
	:OFPMP-EXPERIMENTER       65535
	}))


(defcodec  ofp-capabilities
 (multi-enum-val :uint32
  {
	:OFPC-FLOW-STATS  1
	:OFPC-TABLE-STATS 2
	:OFPC-PORT-STATS  4
	:OFPC-GROUP-STATS 8
	:OFPC-IP-REASM    32
	:OFPC-QUEUE-STATS 64
	:OFPC-PORT-BLOCKED 256
	}))



(defcodec  ofp-port-config
 (multi-enum-val :uint32
  {
	:OFPPC-PORT-DOWN     1
	:OFPPC-NO-RECV       4
	:OFPPC-NO-FWD        32
	:OFPPC-NO-PACKET-IN  64
	}))

(defcodec  ofp-port-state
 (multi-enum-val :uint32
  {
	:OFPPS-LINK-DOWN  1
	:OFPPS-BLOCKED    2
	:OFPPS-LIVE       4
	}))

(defcodec  ofp-port-features
 (multi-enum-val :uint32
  {
	:OFPPF-10MB-HD     1
	:OFPPF-10MB-FD     2
	:OFPPF-100MB-HD    4
	:OFPPF-100MB-FD    8
	:OFPPF-1GB-HD      16
	:OFPPF-1GB-FD      32
	:OFPPF-10GB-FD     64
	:OFPPF-40GB-FD     128
	:OFPPF-100GB-FD    256
	:OFPPF-1TB-FD      512
	:OFPPF-OTHER       1024
	:OFPPF-COPPER      2048
	:OFPPF-FIBER       4096
	:OFPPF-AUTONEG     8192
	:OFPPF-PAUSE       16384
	:OFPPF-PAUSE-ASYM  32768
	}))








(defcodec  ofp-port-reason
 (enum :uint32
  {
	:OFPPR-ADD     0
	:OFPPR-DELETE  1
	:OFPPR-MODIFY  2
	}))




;; (apply str-bytes ( encode  eth-byte   {:version 4 :size 20}))
;; ;;=>
;; "45"



;; 4 + 4 + 6 + 2 + 16 + 4 + 4 +4 + 4 + 4 + 4 + 4 + 4 = 64 bytes long
;; 

(defcodec  ofp-port
 	 (ordered-map-defaults
		:port-no	        :uint32               :default      0
		:pad	                :uint32               :default      0
		:hw-addr	        mac-addr              :default      "00:01:12:23:45:56"
		:pad2	                :uint16               :default      0
		:name	                port-name             :default      "loopback"
		:config	                ofp-port-config       :default      [:OFPPC-PORT-DOWN]
		:state	                ofp-port-state        :default      [:OFPPS-LINK-DOWN]
		:curr	                ofp-port-features     :default      [:OFPPF-10GB-FD :OFPPF-FIBER]
		:advertised	        ofp-port-features     :default      [:OFPPF-10GB-FD :OFPPF-FIBER]
		:supported	        ofp-port-features     :default      [:OFPPF-10GB-FD :OFPPF-FIBER]
		:peer	                ofp-port-features     :default      [:OFPPF-10GB-FD :OFPPF-FIBER]
		:curr-speed	        :uint32               :default      10000
		:max-speed	        :uint32               :default      10000
                ))




;;(map str-bytes ( encode  ofp-port {}) )
;;=>
;;("00 00 00 00" "00 00 00 00" "00 01 12 23 45 56" "00 00" "6C 6F 6F 70 62 61 63 6B 00 00 00 00 00 00 00 00" "00 00 00 01" "00 00 00 01" "00 00 10 40" "00 00 10 40" "00 00 10 40" "00 00 10 40" "00 00 27 10" "00 00 27 10")



(defcodec  ofp-flow-mod-command
 (enum :ubyte
  {

	:OFPFC-ADD            0
	:OFPFC-MODIFY         1
	:OFPFC-MODIFY-STRICT  2
	:OFPFC-DELETE         3
	:OFPFC-DELETE-STRICT  4
	}))

(defcodec  ofp-flow-mod-flags
 (multi-enum-val :uint16
  {

	:OFPFF-SEND-FLOW-REM  1
	:OFPFF-CHECK-OVERLAP  2
	:OFPFF-RESET-COUNTS   4
	:OFPFF-NO-PKT-COUNTS  8
	:OFPFF-NO-BYT-COUNTS  16
	}))



(defcodec  ofp-config-flags 
  (multi-enum-val :uint16
        {
         :OFPC-FRAG-NORMAL  0
         :OFPC-FRAG-DROP    1
         :OFPC-FRAG-REASM   2
         :OFPC-FRAG-MASK    3
         }))



(defcodec ofp-action-type
  ( enum  :uint16
          {
           :OFPAT-OUTPUT           0 
           :OFPAT-COPY-TTL-OUT     11 
           :OFPAT-COPY-TTL-IN      12 
           :OFPAT-SET-MPLS-TTL     15 
           :OFPAT-DEC-MPLS-TTL     16 
           :OFPAT-PUSH-VLAN        17 
           :OFPAT-POP-VLAN         18 
           :OFPAT-PUSH-MPLS        19 
           :OFPAT-POP-MPLS         20 
           :OFPAT-SET-QUEUE        21 
           :OFPAT-GROUP            22 
           :OFPAT-SET-NW-TTL       23 
           :OFPAT-DEC-NW-TTL       24 
           :OFPAT-SET-FIELD        25 
           :OFPAT-PUSH-PBB         26 
           :OFPAT-POP-PBB          27 
           :OFPAT-EXPERIMENTER     65535
           }))



(defcodec  ofp-action-header
 	 (ordered-map
		:type	        ofp-action-type
		:length 	:uint16
	  ))



(defcodec  ofp-queue-prop-header
 	 (ordered-map
		:property	:uint16
		:len	:uint16
		:pad	:ubyte
			))

(defcodec  ofp-packet-queue
 	 (ordered-map
		:queue-id      :uint32
		:port	       :uint32
		:len	       :uint16
		:pad	       :ubyte
		:properties	 ofp-queue-prop-header
			))

(defcodec  ofp-bucket
 	 (ordered-map
		:len	        :uint16
		:weight	      :uint16
		:watch-port	  :uint32
		:watch-group	:uint32
		:pad	        :ubyte
		:actions	    ofp-action-header
                ))




(defn return-ofp-action-codec
  [hd]
  (cond
   (= (:type hd) :OFPAT-OUTPUT )              ofp-action-output
   (= (:type hd) :OFPAT-PUSH-MPLS)            ofp-action-push
   :else                                      ofp-action-output
   ;; complete this 
   ))



(defcodec ofp-actions
  (e/header
    ofp-action-header
    #(return-ofp-action-codec %)
    #(% :header)
    ))




(defcodec  ofp-instruction-type
 (enum :uint16
  {

	:OFPIT-GOTO-TABLE  1
	:OFPIT-WRITE-METADATA  2
	:OFPIT-WRITE-ACTIONS  3
	:OFPIT-APPLY-ACTIONS  4
	:OFPIT-CLEAR-ACTIONS  5
	:OFPIT-METER  6
	:OFPIT-EXPERIMENTER  65535
   }))

(defcodec  ofp-instruction
 	 (ordered-map
		:type	ofp-instruction-type
		:len	:uint16
                ))


(defcodec  ofp-instruction-actions
 	 (ordered-map-defaults
		:type	ofp-instruction-type  :default  :OFPIT-APPLY-ACTIONS 
		:len	:uint16               :default  24
		:pad	:uint32               :default  0
		:actions	ofp-actions   :default  {
                                                           :header {
                                                               :type :OFPAT-OUTPUT
                                                               :length 16
                                                            }
                                                           :port 2
                                                           :max-len 65509
                                                           :pad [0x00 0x00 0x00 0x00 0x00 0x00 ]
                                                           }
                ))

;;(map str-bytes (encode ofp-instruction-actions {} ))
;;=>
;;("00 04" "00 18" "00 00 00 00" "00 00 00 10 00 00 00 02 FF E5 00 00 00 00 00 00")

;;(map str-bytes (encode ofp-actions {
;;                     :header {
;;                              :type :OFPAT-OUTPUT
;;                              :length 16
;;                              }
;;                     :port 0
;;                     :max-len 65509
;;                     :pad [0x00 0x00 0x00 0x00 0x00 0x00 ]
;;                                    } ))
;;=>
;;("00 00 00 10 00 00 00 00 00 10 00 00 00 00 00 00")






(defcodec  ofp-oxm-class
 (enum :uint16
  {
	:OFPXMC-NXM-0           0
	:OFPXMC-NXM-1           1
	:OFPXMC-OPENFLOW-BASIC  32768
	:OFPXMC-EXPERIMENTER    65535
	}))



(defcodec  oxm-ofb-match-fields
 (enum-mask :ubyte
  {

	:OFPXMT-OFB-IN-PORT              0
	:OFPXMT-OFB-IN-PHY-PORT          1
	:OFPXMT-OFB-METADATA             2
	:OFPXMT-OFB-ETH-DST              3
	:OFPXMT-OFB-ETH-SRC              4
	:OFPXMT-OFB-ETH-TYPE             5
	:OFPXMT-OFB-VLAN-VID             6
	:OFPXMT-OFB-VLAN-PCP             7
	:OFPXMT-OFB-IP-DSCP              8
	:OFPXMT-OFB-IP-ECN               9
	:OFPXMT-OFB-IP-PROTO            10
	:OFPXMT-OFB-IPV4-SRC            11
	:OFPXMT-OFB-IPV4-DST            12
	:OFPXMT-OFB-TCP-SRC             13
	:OFPXMT-OFB-TCP-DST             14
	:OFPXMT-OFB-UDP-SRC             15
	:OFPXMT-OFB-UDP-DST             16
	:OFPXMT-OFB-SCTP-SRC            17
	:OFPXMT-OFB-SCTP-DST            18
	:OFPXMT-OFB-ICMPV4-TYPE         19
	:OFPXMT-OFB-ICMPV4-CODE         20
	:OFPXMT-OFB-ARP-OP              21
	:OFPXMT-OFB-ARP-SPA             22
	:OFPXMT-OFB-ARP-TPA             23
	:OFPXMT-OFB-ARP-SHA             24
	:OFPXMT-OFB-ARP-THA             25
	:OFPXMT-OFB-IPV6-SRC            26
	:OFPXMT-OFB-IPV6-DST            27
	:OFPXMT-OFB-IPV6-FLABEL         28
	:OFPXMT-OFB-ICMPV6-TYPE         29
	:OFPXMT-OFB-ICMPV6-CODE         30
	:OFPXMT-OFB-IPV6-ND-TARGET      31
	:OFPXMT-OFB-IPV6-ND-SLL         32
	:OFPXMT-OFB-IPV6-ND-TLL         33
	:OFPXMT-OFB-MPLS-LABEL          34
	:OFPXMT-OFB-MPLS-TC             35
	:OFPXMT-OFP-MPLS-BOS            36
	:OFPXMT-OFB-PBB-ISID            37
	:OFPXMT-OFB-TUNNEL-ID           38
	:OFPXMT-OFB-IPV6-EXTHDR         39
   }))



;; OXM_HEADER__(CLASS, FIELD, HASMASK, LENGTH)

(defcodec oxm-header
  (ordered-map
   :oxm-class          ofp-oxm-class            
   :oxm-field-mask     oxm-ofb-match-fields     
   :length             :ubyte                   
   ))






(defcodec oxm-port
  (ordered-map
  ;; :header oxm-header
   :port   :uint32
   ))


(defcodec oxm-ipv4
  (ordered-map
  ;; :header oxm-header
   :address  ip-addr   
   ))


(defcodec oxm-mac
  (ordered-map
 ;;  :header oxm-header
   :mac   mac-addr 
   ))

(defn return-oxm-codec
  [hd]
  (cond
   (=  (:oxm-field-mask hd)  [:OFPXMT-OFB-IN-PORT 0] )              oxm-port
   (=  (:oxm-field-mask hd)  [:OFPXMT-OFB-IN-PHY-PORT  0] )         oxm-port
   (=  (:oxm-field-mask hd)  [:OFPXMT-OFB-ETH-SRC  0] )             oxm-mac
   (=  (:oxm-field-mask hd)  [:OFPXMT-OFB-ETH-DST 0] )              oxm-mac
   (=  (:oxm-field-mask hd)  [:OFPXMT-OFB-IPV4-SRC 0] )             oxm-ipv4
   (=  (:oxm-field-mask hd)  [:OFPXMT-OFB-IPV4-DST 0] )             oxm-ipv4
   (=  (:oxm-field-mask hd)  [:OFPXMT-OFB-UDP-SRC 0] )              oxm-port
   ;; add others as needed with mask set and other codecs
   :else  oxm-port ))


(defn return-header
  [body]
  (:header body))




(defcodec oxm-tlv
  (e/header
    oxm-header
    #(return-oxm-codec %)
    #(% :header)
    ))






(defcodec  ofp-error-type
 (enum :uint16
  {
	:OFPET-HELLO-FAILED              0
	:OFPET-BAD-REQUEST               1
	:OFPET-BAD-ACTION                2
	:OFPET-BAD-INSTRUCTION           3
	:OFPET-BAD-MATCH                 4
	:OFPET-FLOW-MOD-FAILED           5
	:OFPET-GROUP-MOD-FAILED          6
	:OFPET-PORT-MOD-FAILED           7
	:OFPET-TABLE-MOD-FAILED          8
	:OFPET-QUEUE-OP-FAILED           9
	:OFPET-SWITCH-CONFIG-FAILED      10
	:OFPET-ROLE-REQUEST-FAILED       11
	:OFPET-METER-MOD-FAILED          12
	:OFPET-TABLE-FEATURES-FAILED     13
	:OFPET-EXPERIMENTER              65535
	}))


(defcodec  ofp-match-type
 (enum :uint16
  {
	:OFPMT-STANDARD  0
	:OFPMT-OXM       1
	}))


(defcodec  ofp-match-header
 	 (ordered-map
		:type	      ofp-match-type  
		:length	      :uint16         
                ))






(defn return-tlv-pad
  [hd]
   (ordered-map
    :oxm-fields (repeat (int (/ (hd :length) 8))  oxm-tlv )  ;; need more work for a generic case
    :pad (repeat (- 8 (rem (hd :length) 8)) :ubyte) 
   ))



(defcodec ofp-match-frame
  (e/header
    ofp-match-header 
    #(return-tlv-pad %)
    #(% :header)
    ))




                        


(defcodec  ofp-flow-mod
 	 (ordered-map-defaults
	;;	:header	        ofp-header
		:cookie	        :uint64                 :default  0 
		:cookie-mask	:uint64                 :default  0
		:table-id	:ubyte                  :default  254
		:command	ofp-flow-mod-command    :default  :OFPFC-ADD
		:idle-timeout	:uint16                 :default  0
		:hard-timeout	:uint16                 :default  0
		:priority	:uint16                 :default  1
		:buffer-id	:uint32                 :default  0xffffffff
		:out-port	:uint32                 :default  0
		:out-group	:uint32                 :default  0
		:flags	        ofp-flow-mod-flags      :default  []
		:pad	        :uint16                 :default  0
		:match	        ofp-match-frame         :default   {
                                                                :header {:type :OFPMT-OXM :length 22}
                                                                :oxm-fields [
                                                                     {
                                                                        :header {:oxm-class :OFPXMC-OPENFLOW-BASIC :oxm-field-mask [:OFPXMT-OFB-IN-PORT 0]  :length 4 }
                                                                         :port 0
                                                                     }
                                                                    {
                                                                       :header {:oxm-class :OFPXMC-OPENFLOW-BASIC :oxm-field-mask [:OFPXMT-OFB-ETH-SRC 0] :length 6}
                                                                       :mac  "12:34:56:78:AB:BC"
                                                                    }
                                                                     ]
                                                                 :pad [0x00 0x00]
                        }
                :instruction    ofp-instruction-actions  :default {}
	       	))




;; (encode ofp-match-frame  {
;;                                                                 :header {:type :OFPMT-OXM :length 22}
;;                                                                 :oxm-fields [
;;                                                                      {
;;                                                                         :header {:oxm-class :OFPXMC-OPENFLOW-BASIC :oxm-field-mask [:OFPXMT-OFB-IN-PORT 0]  :length 4 }
;;                                                                          :port 0
;;                                                                      }
;;                                                                     {
;;                                                                        :header {:oxm-class :OFPXMC-OPENFLOW-BASIC :oxm-field-mask [:OFPXMT-OFB-ETH-SRC 0] :length 6}
;;                                                                        :mac  "00:00:00:00:00:02"
;;                                                                     }
;;                                                                      ]
;;                                                                  :pad [0x00 0x00]
;;                           } )
;; (decode ofp-match-frame *1)
;; (encode ofp-flow-mod {} )







(defcodec  ofp-error-msg
 	 (ordered-map
	;;	:header   ofp-header
		:type	  :uint16
		:code	  :uint16
		:data	  :ubyte
    ))



(defcodec  ofp-port-stats
 	 (ordered-map
		:port-no	      :uint32
		:pad		      :ubyte
		:rx-packets	    :uint64
		:tx-packets	    :uint64
		:rx-bytes	      :uint64
		:tx-bytes	      :uint64
		:rx-dropped	    :uint64
		:tx-dropped	    :uint64
		:rx-errors	    :uint64
		:tx-errors	    :uint64
		:rx-frame-err	  :uint64
		:rx-over-err	  :uint64
		:rx-crc-err	    :uint64
		:collisions	    :uint64
		:duration-sec	  :uint32
		:duration-nsec	:uint32
    ))


(defcodec  ofp-hello-elem-type
 (enum :uint16
  {
	:OFPHET-VERSIONBITMAP  1
	}))


(defcodec  ofp-hello-elem-header
 	 (ordered-map
		:type	  ofp-hello-elem-type
		:length	  :uint16
                :bitmap   (repeat 4 :ubyte)
                )) ;; need work here , It is one or more bit maps , not sure






(defcodec  ofp-hello
 	 (ordered-map
	;;	:header	        ofp-header
		:elements	ofp-hello-elem-header
    ))


(defcodec  ofp-table-feature-prop-header
 	 (ordered-map
		:type	  :uint16
		:length	:uint16
    ))


(defcodec  ofp-meter-band-header
 	 (ordered-map
		:type	      :uint16
		:len	      :uint16
		:rate	      :uint32
		:burst-size   :uint32
			))

(defcodec  ofp-aggregate-stats-reply
 	 (ordered-map
		:packet-count	:uint64
		:byte-count	:uint64
		:flow-count	:uint32
		:pad	        :ubyte
    ))


(defcodec  ofp-action-set-queue
 	 (ordered-map
	;;	:type	    :uint16
	;;	:len	    :uint16
		:queue-id   :uint32
			))


(defcodec  ofp-table-stats
 	 (ordered-map
		:table-id	  :ubyte
		:pad	          :ubyte
		:active-count	  :uint32
		:lookup-count	  :uint64
		:matched-count	  :uint64
			))

(defcodec  ofp-table-mod
 	 (ordered-map
	;;	:header	    ofp-header
		:table-id   :ubyte
		:pad	    :ubyte
		:config	    :uint32
                ))


(defcodec  ofp-flow-removed
 	 (ordered-map
		:header	        ofp-header
		:cookie	        :uint64
		:priority	:uint16
		:reason	        :ubyte
		:table-id	:ubyte
		:duration-sec	:uint32
		:duration-nsec	:uint32
		:idle-timeout	:uint16
		:hard-timeout	:uint16
		:packet-count	:uint64
		:byte-count	:uint64
		:match	        ofp-match-frame
			))


(defcodec  ofp-bucket-counter
 	 (ordered-map
		:packet-count	:uint64
		:byte-count	:uint64 ))



(defcodec  ofp-queue-stats
 	 (ordered-map
		:port-no	:uint32
		:queue-id	:uint32
		:tx-bytes	:uint64
		:tx-packets	:uint64
		:tx-errors	:uint64
		:duration-sec	:uint32
		:duration-nsec	:uint32 ))


(defcodec  ofp-meter-config
 	 (ordered-map
		:length	:uint16
		:flags	:uint16
		:meter-id	:uint32
		:bands	ofp-meter-band-header ))

(defcodec  ofp-error-experimenter-msg
 	 (ordered-map
		:header	ofp-header
		:type	:uint16
		:exp-type	:uint16
		:experimenter	:uint32
		:data	:ubyte ))

(defcodec  ofp-experimenter-header
 	 (ordered-map
		:header	ofp-header
		:experimenter	:uint32
		:exp-type	:uint32 ))

(defcodec  ofp-table-feature-prop-next-tables
 	 (ordered-map
		:type	:uint16
		:length	:uint16
		:next-table-ids	:ubyte ))

(defcodec  ofp-port-stats-request
 	 (ordered-map
		:port-no	:uint32
		:pad	:ubyte ))







(defcodec  ofp-table-features
 	 (ordered-map
		:length	:uint16
		:table-id	:ubyte
		:pad	:ubyte
		:name	:char
		:metadata-match	:uint64
		:metadata-write	:uint64
		:config	:uint32
		:max-entries	:uint32
		:properties	ofp-table-feature-prop-header ))


(defcodec  ofp-group-stats-request
 	 (ordered-map
		:group-id	:uint32
		:pad	:ubyte
                ))

(defcodec  ofp-table-feature-prop-oxm
 	 (ordered-map
		:type	:uint16
		:length	:uint16
		:oxm-ids	:uint32
                ))



(defcodec  ofp-aggregate-stats-request
 	 (ordered-map
		:table-id	:ubyte
		:pad	:ubyte
		:out-port	:uint32
		:out-group	:uint32
		:pad2	:ubyte
		:cookie	:uint64
		:cookie-mask	:uint64
		:match	ofp-match-frame ))

(defcodec  ofp-queue-get-config-request
 	 (ordered-map
		:header	ofp-header
		:port	:uint32
		:pad	:ubyte
			))

(defcodec  ofp-instruction-experimenter
 	 (ordered-map
		:type	:uint16
		:len	:uint16
		:experimenter	:uint32
			))

(defcodec  ofp-action-nw-ttl
 	 (ordered-map
	;;	:type	:uint16
	;;	:len	:uint16
		:nw-ttl	:ubyte
		:pad	(repeat 3 :ubyte)
			))

(defcodec    ofp-port-status
 	 (ordered-map-defaults
		:reason	    ofp-port-reason  :default :OFPPR-MODIFY
		:pad	    (repeat 7 :ubyte) :default [0x00 0x00 0x00 0x00 0x00 0x00 0x00] ;; padding 7 bytes 
		:desc	    ofp-port  :default {}
			))





(defcodec  ofp-meter-multipart-request
 	 (ordered-map
		:meter-id	:uint32
		:pad	:ubyte
			))




(defcodec  ofp-port-mod
 	 (ordered-map
		:header	      ofp-header
		:port-no	    :uint32
		:pad	        :ubyte
		:hw-addr	    :ubyte
		:pad2	        :ubyte
		:config	      :uint32
                :mask	        :uint32
		:advertise	  :uint32
		:pad3	        :ubyte
			))




(defcodec  ofp-switch-config
 	 (ordered-map
	;;	:header	        ofp-header
		:flags	        ofp-config-flags
		:miss-send-len	:uint16
			))

(defcodec  ofp-queue-prop-experimenter
 	 (ordered-map
		:prop-header	ofp-queue-prop-header
		:experimenter	:uint32
		:pad	:ubyte
		:data	:ubyte
			))

(defcodec  ofp-instruction-write-metadata
 	 (ordered-map
		:type	:uint16
		:len	:uint16
		:pad	:ubyte
		:metadata	:uint64
		:metadata-mask	:uint64
			))


(defcodec  ofp-meter-features
 	 (ordered-map
		:max-meter	:uint32
		:band-types	:uint32
		:capabilities	:uint32
		:max-bands	:ubyte
		:max-color	:ubyte
		:pad	:ubyte
			))

(defcodec  ofp-table-feature-prop-instructions
 	 (ordered-map
		:type	:uint16
		:length	:uint16
		:instruction-ids	ofp-instruction
			))

(defcodec  ofp-action-experimenter-header
 	 (ordered-map
	;;	:type	:uint16
	;;	:len	:uint16
		:experimenter	:uint32
			))

(defcodec  ofp-meter-band-drop
 	 (ordered-map
		:type	:uint16
		:len	:uint16
		:rate	:uint32
		:burst-size	:uint32
		:pad	:ubyte
			))

(defcodec  ofp-table-feature-prop-actions
 	 (ordered-map
		:type	:uint16
		:length	:uint16
		:action-ids	ofp-action-header
			))

(defcodec  ofp-meter-band-experimenter
 	 (ordered-map
		:type	:uint16
		:len	:uint16
		:rate	:uint32
		:burst-size	:uint32
		:experimenter	:uint32
			))

(defcodec  ofp-queue-get-config-reply
 	 (ordered-map
		:header	ofp-header
		:port	:uint32
		:pad	:ubyte
		:queues	ofp-packet-queue
			))

(defcodec  ofp-desc
 	 (ordered-map
		:mfr-desc	:char
		:hw-desc	:char
		:sw-desc	:char
		:serial-num	:char
		:dp-desc	:char
			))

(defcodec  ofp-meter-stats
 	 (ordered-map
		:meter-id	:uint32
		:len	        :uint16
		:pad     	:ubyte
		:flow-count	:uint32
		:packet-in-count	:uint64
		:byte-in-count	:uint64
		:duration-sec	:uint32
		:duration-nsec	:uint32
		:band-stats	:ofp-meter-band-stats
			))

(defcodec  ofp-packet-in-reason
  (enum :ubyte
        {
         :OFPR-NO-MATCH     0
         :OFPR-ACTION       1
         :OFPR-INVALID-TTL  2
         }))

(defcodec  ofp-packet-in
 	 (ordered-map-defaults
	;;	:header 	ofp-header
		:buffer-id	:uint32    :default 273
		:total-len	:uint16    :default 42
		:reason   	ofp-packet-in-reason  :default :OFPR-NO-MATCH
		:table-id	:ubyte     :default  0
		:cookie 	:uint64    :default  0
		:match  	ofp-match-frame :default   {
                                                             :header {:type  :OFPMT-OXM :length 12}
                                                            :oxm-fields
                                                            [
                                                               {
                                                               :header {:oxm-class :OFPXMC-OPENFLOW-BASIC  :oxm-field-mask [:OFPXMT-OFB-IN-PORT 0] :length 4}
                                                               :port 1
                                                               }
                                                             
                                                               ]
                                                            :pad [0x00 0x00 0x00 0x00]
                                                            }
                :pad            :uint16    :default  0
                :data           ip         :default   {:header {:ether-type :ARP }  }
                                                       ))



;;(map str-bytes (encode ofp-packet-in {}  ))
;;
;;=>
;; ("00 00 01 11" "00 2A" "00" "00" "00 00 00 00 00 00 00 00" "00 01 00 0C" "80 00 00 04 00 00 00 01" "00 00 00 00" "00 00")




;;=>



(defcodec  ofp-action-group
 	 (ordered-map
	;;	:type	:uint16
	;;	:len	:uint16
		:group-id	:uint32 ))



(defcodec  ofp-flow-stats
 	 (ordered-map
		:length	          :uint16
		:table-id	        :ubyte
		:pad	            :ubyte
		:duration-sec	    :uint32
		:duration-nsec	  :uint32
		:priority	        :uint16
		:idle-timeout	    :uint16
		:hard-timeout	    :uint16
		:flags	          :uint16
		:pad2	            :ubyte
		:cookie	          :uint64
		:packet-count	    :uint64
		:byte-count	      :uint64
		:match	          ofp-match-frame
    ))



(defcodec  ofp-meter-band-stats
 	 (ordered-map
		:packet-band-count	:uint64
		:byte-band-count	  :uint64
			))




(defcodec  ofp-role-request
 	 (ordered-map
		:header	        ofp-header
		:role	          :uint32
		:pad	          :ubyte
		:generation-id	:uint64
			))




(defcodec  ofp-group-desc
 	 (ordered-map
		:length	    :uint16
		:type	      :ubyte
		:pad	      :ubyte
		:group-id	  :uint32
		:buckets	  ofp-bucket
			))




(defcodec  ofp-flow-stats-request
 	 (ordered-map
		:table-id	      :ubyte
		:pad	              :ubyte
		:out-port	      :uint32
		:out-group	      :uint32
		:pad2	              :ubyte
		:cookie	              :uint64
		:cookie-mask	      :uint64
		:match	              ofp-match-frame
			))


(defcodec  ofp-multipart-request
 	 (ordered-map-defaults
	;;	:header	ofp-header
		:mp-req-type	ofp-multipart-type  :default :OFPMP-PORT-DESC
		:flags	:uint16                     :default 0
		:pad	:uint32                     :default 0
	     ;;	:body	(repeated :ubyte :prefix :none)  :default []
	      	))



(defcodec  ofp-queue-stats-request
 	 (ordered-map
		:port-no	  :uint32
		:queue-id	  :uint32
			))




(defcodec  ofp-group-mod
 	 (ordered-map
		:header	          ofp-header
		:command	  :uint16
		:type	          :ubyte
		:pad	          :ubyte
		:group-id	  :uint32
		:buckets	  (:repeated ofp-bucket)
			))



(defcodec ofp-switch-features
 	 (ordered-map
          ;;	:header	          ofp-header
          ;;  8 + 4 + 1 + 1+ 2 + 4 + 4
		:datapath-id	  :uint64
		:n-buffers	  :uint32
		:n-tables	  :ubyte
		:auxiliary-id	  :ubyte
		:pad	          :uint16   ;; 2 ubytes added to  allign with 64 bits
		:capabilities	  ofp-capabilities
		:reserved	  :uint32
			))





(defcodec  ofp-oxm-experimenter-header
 	 (ordered-map
		:oxm-header	:uint32
		:experimenter	:uint32
			))



(defcodec  ofp-experimenter-multipart-header
 	 (ordered-map
		:experimenter	:uint32
		:exp-type	:uint32
			))



(defcodec  ofp-meter-mod
 	 (ordered-map
		:header	    ofp-header
		:command    :uint16
		:flags	    :uint16
		:meter-id   :uint32
		:bands	    ofp-meter-band-header
			))





(defcodec  ofp-action-pop-mpls
 	 (ordered-map
	;;	:type	      :uint16
	;;	:len	      :uint16
		:ethertype    :uint16
		:pad	      :uint16
			))







(defcodec  ofp-group-features
 	 (ordered-map
		:types	          :uint32
		:capabilities	  :uint32
		:max-groups	  :uint32
		:actions	  :uint32
			))



(defcodec  ofp-async-config
 	 (ordered-map
	;;	:header	                ofp-header
		:packet-in-mask	        :uint32
		:port-status-mask	:uint32
		:flow-removed-mask	:uint32
			))


(defcodec  ofp-meter-band-dscp-remark
 	 (ordered-map
		:type	          :uint16
		:len	          :uint16
		:rate	          :uint32
		:burst-size	  :uint32
		:prec-level	  :ubyte
		:pad	          :ubyte
			))

  

(defcodec  ofp-packet-out
 	 (ordered-map-defaults
	;;	:header	        ofp-header
		:buffer-id	:uint32    :default  20
		:in-port	:uint32    :default  2
		:actions-len	:uint16    :default  16
		:pad	        (repeat 6 :ubyte)    :default [0x00 0x00 0x00 0x00 0x00 0x00]
		:actions	(repeated ofp-actions :prefix :none)   :default  [
                                                                                  {:header
                                                                                   {
                                                                                    :type :OFPAT-OUTPUT
                                                                                    :length 16
                                                                                   }
                                                                                   :port 0
                                                                                   :max-len 65509
                                                                                   :pad  [0x00 0x00 0x00 0x00 0x00 0x00]
                                                                                   }]
                ))



;;(map str-bytes (encode ofp-packet-out {} ))
;;=>
;; ("00 00 00 14" "00 00 00 02" "00 10" "00 00 00 00 00 00" "00 00 00 10 00 00 00 00 FF E5 00 00 00 00 00 00")




(defcodec  ofp-instruction-goto-table
 	 (ordered-map
		:type	        :uint16
		:len	        :uint16
		:table-id	:ubyte
		:pad	        :ubyte
			))

(defcodec  ofp-queue-prop-max-rate
 	 (ordered-map
		:prop-header	ofp-queue-prop-header
		:rate	        :uint16
		:pad	        :ubyte
			))

(defcodec  ofp-table-feature-prop-experimenter
 	 (ordered-map
		:type	                :uint16
		:length	                :uint16
		:experimenter	        :uint32
		:exp-type	        :uint32
		:experimenter-data	:uint32
			))

(defcodec  ofp-action-set-field
 	 (ordered-map
	;;	:type	:uint16
        ;;	:len	:uint16
        ;; look in more detail ,here , is this a single field or multiple fields    
		:field	oxm-tlv
			))

(defcodec  ofp-group-stats
 	 (ordered-map
		:length	        :uint16
		:pad	        :ubyte
		:group-id	:uint32
		:ref-count	:uint32
		:pad2	        :ubyte
		:packet-count	:uint64
		:byte-count	:uint64
		:duration-sec	:uint32
		:duration-nsec	:uint32
		:bucket-stats	ofp-bucket-counter
			))


(defcodec  ofp-instruction-meter
 	 (ordered-map
		:type	        :uint16
		:len	        :uint16
		:meter-id	:uint32
			))

(defcodec  ofp-queue-prop-min-rate
 	 (ordered-map
		:prop-header	ofp-queue-prop-header
		:rate	        :uint16
		:pad	        :ubyte
			))



(defcodec  ofp-action-mpls-ttl
 	 (ordered-map
	;;	:type	        :uint16
	;;	:len	        :uint16
		:mpls-ttl	:ubyte
		:pad	        (repeat 3 :ubyte)
                ))



(defcodec  ofp-generic
 	 (ordered-map
	;;	:header   ofp-header
		:payload  (repeated :ubyte :prefix :none )
                ))


(defcodec  ofp-header-only
 	 (ordered-map
		:header	ofp-header
		))


(defcodec  ofp-multipart-reply
 	 (ordered-map
          ;;	:header	ofp-header ;; only for ports
		:mp-req-type	ofp-multipart-type
		:flags	:uint16
		:pad	:uint32
		:ports	(repeated ofp-port :prefix :none )))






(defn return-proper-codec
  [hd]
  (cond
       (and (=  (:type hd)  :OFPT-HELLO)  (> (:length hd) 8))         ofp-hello 
       (=  (:type hd)  :OFPT-HELLO )                                  ofp-generic
       (=  (:type hd)  :OFPT-ERROR)                                   ofp-error-msg
       (=  (:type hd)  :OFPT-ECHO-REQUEST )                           ofp-generic
       (=  (:type hd)  :OFPT-ECHO-REPLY )                             ofp-generic
       (=  (:type hd)  :OFPT-FEATURES-REQUEST)                        ofp-generic
       (=  (:type hd)  :OFPT-FEATURES-REPLY)                          ofp-switch-features
       (=  (:type hd)  :OFPT-SET-CONFIG)                              ofp-switch-config
       (=  (:type hd)  :OFPT-MULTIPART-REQUEST)                       ofp-multipart-request
       (=  (:type hd)  :OFPT-MULTIPART-REPLY)                         ofp-multipart-reply
       (=  (:type hd)  :OFPT-PACKET-IN )                              ofp-packet-in
       (=  (:type hd)  :OFPT-PACKET-OUT)                              ofp-packet-out
       (=  (:type hd)  :OFPT-FLOW-MOD )                               ofp-flow-mod
       (=  (:type hd)  :OFPT-PORT-STATUS )                            ofp-port-status
       :else                                                          ofp-generic
       
       ))




;;(decode openflow (encode openflow {:header {:version OFP-VERSION :type :OFPT-PORT-STATUS :xid 0 :length 80}}))



(defn return-openflow-header
  [cc]
  ofp-header)



(defcodec openflow
  (e/header
    ofp-header
    #(return-proper-codec %)
    #(%  :header)     ))


;; initial flow set message to switch
;; (map str-bytes (encode openflow  {:header {:version OFP-VERSION :type  :OFPT-FLOW-MOD :length 80 :xid 1522153902 } 
;;                 :table-id 0
;;                 :cookie-mask 0
;;                 :out-group 0
;;                 :command :OFPFC-ADD
;;                 :hard-timeout 0
;;                 :pad 0
;;                 :priority 0
;;                 :cookie 0
;;                 :out-port 0
;;                 :flags []
;;                 :buffer-id OFP-NO-BUFFER
;;                 :idle-timeout 0
;;                 :match {
;;                        :header {:type :OFPMT-OXM  :length 4 }
;;                        :oxm-fields []
;;                         :pad [0 0 0 0]
;;                         }
;;                                   :instruction {
;;                                                 :pad 0
;;                               :len 24
;;                               :type :OFPIT-APPLY-ACTIONS
;;                  :actions
;;                               { :header { :type :OFPAT-OUTPUT :length 16 }
;;                                :pad [0 0 0 0 0 0]
;;                                :max-len 0xffff
;;                                :port 0xfffffffd
;;                                }
                              
;;                               }
;;                                   } ))


;; ;;=>
;; ("04 0E 00 50 5A BA 39 AE" "00 00 00 00 00 00 00 00" "00 00 00 00 00 00 00 00" "00" "00" "00 00" "00 00" "00 00" "FF FF FF FF" "00 00 00 00" "00 00 00 00" "00 00" "00 00" "00 01 00 04 00 00 00 00" "00 04" "00 18" "00 00 00 00" "00 00 00 10 FF FF FF FD FF FF 00 00 00 00 00 00")
;;=>
;;{:in-port 2, :default 0, :type :OFPT-PACKET-OUT, :actions-len 16, :actions [{:type :OFPAT-OUTPUT, :length 16, :pad [0 0 0 0 0 0], :max-len 65509, :port 0}], :xid 0, :pad [0 0 0 0 0 0], :length 40, :version 4, :buffer-id 20}

;; (map str-bytes  (encode openflow   { :header {:version OFP-VERSION :type  :OFPT-PACKET-OUT :length 40 :xid 1522153902 } 
;;                                    :buffer-id 261
;;                                    :in-port 4
;;                                    :actions-len 16
;;                                    :pad [0 0 0 0 0 0]
;;                                    :actions [{ :header {:type :OFPAT-OUTPUT :length 16 }
;;                                              :max-len 65509
;;                                              :port 0xfffffffb
;;                                              :pad [0 0 0 0 0 0]
;;                                              }]
;;                                          }))


;; ;;=>
;; ("04 0D 00 28 5A BA 39 AE" "00 00 01 05" "00 00 00 04" "00 10" "00 00 00 00 00 00" "00 00 00 10 FF FF FF FB FF E5 00 00 00 00 00 00")

