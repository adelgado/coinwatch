(ns coinwatch.service.user
  (:require [coinwatch.cfg :refer [cfg]]
            [clostache.parser :as mustache]
            [coinwatch.db
             [db :as db]
             [user :as user-db]]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

(defn authenticate [email password]
  [:ok (user-db/authenticate db/db-spec email password)])
