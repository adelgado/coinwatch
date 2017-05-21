(ns coinwatch.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :price
 (fn [db]
   (:price (:data db))))

(re-frame/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))
