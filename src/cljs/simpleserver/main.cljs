(ns simpleserver.main
  (:require [re-frame.core :as re-frame]
            [re-frame.db]
            [reagent.dom :as r-dom]
            [day8.re-frame.http-fx] ; Needed to register :http-xhrio to re-frame.
            [reagent-dev-tools.core :as dev-tools]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [simpleserver.util :as ss-util]
            [simpleserver.state :as ss-state]
            [simpleserver.view.home :as ssv-home]))


;; Routes

(def routes
  ["/"
   [""
    {:name ::ss-state/home
     :view ssv-home/home-page
     :link-text "Home"
     :controllers
     [{:start (fn [& params] (js/console.log (str "Entering home page, params: " params)))
       :stop (fn [& params] (js/console.log (str "Leaving home page, params: " params)))}]}]])

(defn on-navigate [new-match]
  (when ss-util/debug? (prn "ENTER on-navigate, new-match" new-match))
  (when new-match
    (re-frame/dispatch [::ss-state/navigated new-match])))

(def router
  (rf/router
    routes
    {:data {:coercion rss/coercion}}))

(defn init-routes! []
  (ss-util/debug? (prn "ENTER init-routes!"))
  (rfe/start!
    router
    on-navigate
    {:use-fragment true}))

(defn router-component [_]
  (when ss-util/debug? (prn "ENTER router-component"))
  (let [current-route @(re-frame/subscribe [::ss-state/current-route])
        path-params (:path-params current-route)
        _ (when ss-util/debug? (prn "router-component, path-params" path-params))]
    [:div
     [ss-util/header]
     ; NOTE: when you supply the current-route to the simpleserver.view it can parse path-params there (from path).
     (when current-route
       [(-> current-route :data :view) current-route])]))


;; Setup

(defn dev-setup []
  (when (ss-util/debug?)
    (enable-console-print!)
    (prn "dev mode")))

(defn ^:dev/after-load start []
  (js/console.log "ENTER start")
  (re-frame/clear-subscription-cache!)
  (init-routes!)
  (r-dom/render [router-component {:router router}
                 (if (:open? @dev-tools/dev-state)
                   {:style {:padding-bottom (str (:height @dev-tools/dev-state) "px")}})]
                (.getElementById js/document "app")))


(defn ^:export init []
  (js/console.log "ENTER init")
  (re-frame/dispatch-sync [::initialize-db])
  (dev-tools/start! {:state-atom re-frame.db/app-db})
  (dev-setup)
  (start))

(comment
  ;(reagent.dom/render [])
  ;(require '[hashp.core :include-macros true])
  ;(let [a #p (range 5)] a)

  )
