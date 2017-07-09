(ns coinwatch.cfg
  (:require [environ.core :refer [env]]
            [clojure.tools.logging :as log]))

(def ^:private env-vars
  [:db-host :db-port :db-name :db-user :db-password])

(doseq [v env-vars]
  (when (nil? (v (select-keys env env-vars)))
    (let [msg (format "env variable '%s' is not set" v)]
      (throw (IllegalArgumentException. msg))
      (log/error msg))))

(def cfg (select-keys env env-vars))
