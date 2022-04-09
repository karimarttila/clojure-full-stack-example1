(ns simpleserver.view.home
  (:require [simpleserver.util :as ss-util]))

(defn home-page []
  (when ss-util/debug? (prn "ENTER home-page"))
  [:div
   [:p "I'm alive!"]])
