(ns coinwatch.db.db
  (:require [coinwatch.cfg :refer [cfg]]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [jdbc.pool.c3p0 :as pool]))

(def db-spec
  (pool/make-datasource-spec
   {:subprotocol "postgresql"
    :subname (str "//" (:db-host cfg)
                  ":" (:db-port cfg)
                  "/" (:db-name cfg))
    :user (:db-user cfg)
    :password (:db-password cfg)}))
