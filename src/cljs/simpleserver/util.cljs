(ns simpleserver.util)

(def debug? ^boolean goog.DEBUG)

(defn header []
  [:div
   [:p "HEADER!"]])
