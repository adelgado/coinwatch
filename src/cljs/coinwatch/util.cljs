(ns coinwatch.util)

(defn- pad ^String [^Long n]
  (if (< (count (str n)) 2)
    (str "0" n)
    (str n)))

(defn format-date ^String [^String date]
  (prn date)
  (let [date*   (js/Date. date)
        day     (pad (.getDate date*))
        month   (pad (+ 1 (.getMonth date*)))
        year    (.getFullYear date*)
        hours   (pad (.getHours date*))
        minutes (pad (.getMinutes date*))]
    (str day "-" month "-" year " | " hours ":" minutes)))
