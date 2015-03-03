(ns lisa.ip
  (:require [lisa.gloss.extension :as e]
            [clojure.string :as str]
            [gloss.core :refer :all]
          ;;  [gloss.io :refer :all]
            )
  (:use    [lisa.menum]
           [lisa.common]))




(defcodec ether-type
  (enum :uint16
        {
         :ARP           0x0806
         :RARP          0x8035
         :IPV4          0x0800
         :LLDP          0x88CC
         :BSN           0x8942
         :VLAN-UNTAGGED 0xffff
         :IPV6          0x86dd
         }
        ))


(defcodec hw-type
  (enum :uint16
        {
         :ethernet    0x0001
         :frame-relay 0x000f
         :atm         0x0015
         :ipsec       0x001f
         :unknown     5   
         }
        ))

(defcodec proto-type
  (enum :uint16
        {
       :IPV4   0x0800
       :IPX    0x8037
       :802_1Q 0x8100
       :IPV6   0x86dd
         }))

(defcodec ip-protocols
  (enum :ubyte
        {
         :ICMP  0x01
         :IGMP  0x02
         :TCP   0x06
         :UDP   0x11
         }))

(defcodec arp-opcode
  (enum :uint16
        {
         :ARP-REQUEST   1
         :ARP-REPLY     2
         :RARP-REQUEST  3
         :RARP-REPLY    4
         }))


;; basic ethernet frame
(defcodec ethernet-header
  (ordered-map-defaults
   :destination mac-addr  :default  "00:00:00:00:00:01"
   :source mac-addr       :default  "00:00:00:00:00:02"
   :ether-type ether-type :default   :ARP
   ))




(defcodec arp
  (ordered-map-defaults
  ;; :header       ethernet-header
   :hw-type      hw-type          :default     :ethernet
   :proto-type   proto-type       :default     :IPV4
   :hw-size      :ubyte           :default     6
   :proto-size   :ubyte           :default     4
   :opcode       arp-opcode       :default     :ARP-REPLY
   :sender-mac   mac-addr         :default     "00:00:00:00:00:02"
   :sender-ip    ip-addr          :default     "10.0.0.2"
   :target-mac   mac-addr         :default     "00:00:00:00:00:01"
   :target-ip    ip-addr          :default      "10.0.0.1"
   ))




;; (map str-bytes (encode  arp {}))


(defcodec ip-frame-flags
  (multi-enum-val :ubyte
   {
    :NO-FRAGMENT      64
    :SET-FRAGMENT     32
    }))




(defcodec icmp-type
  (enum :ubyte
        {
         :ICMP-ECHOREPLY	0	;;/* Echo Reply			*/
         :ICMP-DEST-UNREACH	3	;;/* Destination Unreachable	*/
         :ICMP-SOURCE-QUENCH	4	;;/* Source Quench		*/
         :ICMP-REDIRECT		5	;;/* Redirect (change route)	*/
         :ICMP-ECHO		8	;;/* Echo Request			*/
         :ICMP-TIME-EXCEEDED	11	;;/* Time Exceeded		*/
         :ICMP-PARAMETERPROB	12	;;/* Parameter Problem		*/
         :ICMP-TIMESTAMP	13	;;/* Timestamp Request		*/
         :ICMP-TIMESTAMPREPLY	14	;;/* Timestamp Reply		*/
         :ICMP-INFO-REQUEST	15	;;/* Information Request		*/
         :ICMP-INFO-REPLY	16	;;/* Information Reply		*/
         :ICMP-ADDRESS		17	;;/* Address Mask Request		*/
         :ICMP-ADDRESSREPLY	18	;;/* Address Mask Reply		*/
         :NR-ICMP-TYPES		18
         }))


(defcodec icmp-code
  (enum :ubyte
        {
         :ICMP-NET-UNREACH	0	;;/* Network Unreachable		*/
         :ICMP-HOST-UNREACH	1	;; /* Host Unreachable		*/
         :ICMP-PROT-UNREACH	2	;;/* Protocol Unreachable		*/
         :ICMP-PORT-UNREACH	3	;;/* Port Unreachable		*/
         :ICMP-FRAG-NEEDED	4	;;/* Fragmentation Needed/DF set	*/
         :ICMP-SR-FAILED	5	;;/* Source Route failed		*/
         :ICMP-NET-UNKNOWN	6
         :ICMP-HOST-UNKNOWN	7
         :ICMP-HOST-ISOLATED	8
         :ICMP-NET-ANO		9
         :ICMP-HOST-ANO		10
         :ICMP-NET-UNR-TOS	11
         :ICMP-HOST-UNR-TOS	12
         :ICMP-PKT-FILTERED	13	;;/* Packet filtered */
         :ICMP-PREC-VIOLATION	14	;;/* Precedence violation */
         :ICMP-PREC-CUTOFF	15	;;/* Precedence cut off */
         :NR-ICMP-UNREACH	15	;;/* instead of hardcoding immediate value */
         }))



(defcodec ipv4-frame
  (ordered-map-defaults 
    :version-size       eth-byte         :default  {:version 4 :size 20}
    :diffserv           :ubyte           :default  0x00
    :length             :uint16          :default  0x84
    :identification     :uint16          :default  0x00ba
    :flags              ip-frame-flags   :default  [:NO-FRAGMENT]
    :offset             :uint16          :default  0x00
    :ttl                :ubyte           :default  64
    :protocol           ip-protocols     :default  :ICMP
    :checksum           :uint16          :default  0x25EB
    :source             ip-addr          :default  "10.0.0.1"
    :destination        ip-addr          :default  "10.0.0.2"
    :message            (repeated :ubyte :prefix :none)  :default [0 0 0 0 0 0 0 0 0 0 0]
    ))



;; (map str-bytes (encode  ipv4-frame  {}) )
;; ;;=>
;; ("45" "00" "00 84" "00 BA" "40" "00 00" "40" "01" "25 EB" "0A 00 00 01" "0A 00 00 02" "00 00 00 00 00 00 00 00 00 00 00")



;; this is done for the demo, we ignore all other packets except ARP
(defcodec ip-frame-demo
  (ordered-map
   :message (repeated :ubyte :prefix :none)
   ))



(defn return-ip-codec
  [hd]
  (cond
       (=  (:ether-type hd)  :ARP)                   arp
       (=  (:ether-type hd)  :IPV4)                  ip-frame-demo
       (=  (:ether-type hd)  :IPV6)                  ip-frame-demo
       :else  arp ))




(defcodec ip
  (header
    ethernet-header
    #(return-ip-codec %)
    #(% :header)
    ))




