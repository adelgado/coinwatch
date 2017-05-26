{:shared
 {:env {:coindesk-bitcoin-url "http://api.coindesk.com/v1/bpi/currentprice.json"}}

 :dev
 [:shared
  {:env {:timeout 5000}}]

:prod
[:shared
 {:env {:timeout 3000}}]}
