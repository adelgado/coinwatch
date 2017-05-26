(ns coinwatch.cfg
  (:require [environ.core :refer [env]]))

(def ^:private env-vars
  [:coindesk-bitcoin-url
   :timeout])

(doseq [v env-vars]
  (if (nil? (v (select-keys env env-vars)))
    (throw (Exception. (format "env variable '%s' is not set" v)))))

