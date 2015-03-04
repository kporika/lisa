# lisa

A Clojure library designed to encode/decode openflow messages from binary to clojure hashmaps.

Please see the docs @ https://github.com/kporika/lisa/docs/uberdoc.html


## Usage

lein repl 

(use 'gloss.io 'lisa.common  'lisa.ofcodec)

(map str-bytes (encode ofp-port {}))
("00 00 00 00" "00 00 00 00" "00 01 12 23 45 56" "00 00" "6C 6F 6F 70 62 61 63 6B 00 00 00 00 00 00 00 00" "00 00 00 01" "00 00 00 01" "00 00 10 40" "00 00 10 40" "00 00 10 40" "00 00 10 40" "00 00 27 10" "00 00 27 10")

(decode ofp-port (encode ofp-port {}))
{:pad2 0, :port-no 0, :advertised [:OFPPF-FIBER :OFPPF-10GB-FD], :curr-speed 10000, :supported [:OFPPF-FIBER :OFPPF-10GB-FD], :config [:OFPPC-PORT-DOWN], :peer [:OFPPF-FIBER :OFPPF-10GB-FD], :name "loopback", :max-speed 10000, :state [:OFPPS-LINK-DOWN], :hw-addr "0:1:12:23:45:56", :pad 0, :curr [:OFPPF-FIBER :OFPPF-10GB-FD]}



## License

Copyright © 2014 

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
