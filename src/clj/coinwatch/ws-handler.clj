(ns coinwatch.ws-handler
  (:require [ring-jetty.util.ws :as ws]))

(def all-sessions (ref #{}))

(defn- on-connect [session]
  (dosync
   (alter all-sessions conj session)))

(defn- on-close [session code reason]
  (dosync
   (alter all-sessions disj session)))

(defn- on-text [session message]
  (doseq [session* @all-sessions]
    (ws/send! session* (str
                 (.. session* getSession getRemoteAddress getHostName)
                 ":"
                 message))))

(defn- on-bytes [session payload offset len]
  nil)

(defn- on-error [session e]
  (.printStackTrace e)
  (dosync
   (alter all-sessions disj session)))

(def app
  {:on-connect on-connect
   :on-error on-error
   :on-text on-text
   :on-close on-close
   :on-bytes on-bytes})
