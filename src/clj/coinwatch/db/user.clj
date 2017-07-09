(ns coinwatch.db.user
  (:require [clojure.java.jdbc :as jdbc]
            [coinwatch.db.db :as db]))

(defn authenticate [db-spec email password]
  {:id 0})

(defn create [db-spec email password]
  (jdbc/insert! db-spec :user {:email email :password password}))

(defn authenticate* [db-spec email password]
  (jdbc/query
    db-spec
    ["select id from \"user\" where email = ? and password = ?"
     email
     password]))

