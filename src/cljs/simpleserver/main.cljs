(ns simpleserver.main
  (:require [re-frame.core :as re-frame]
            [re-frame.db]
            [reagent.dom :as r-dom]
            [day8.re-frame.http-fx] ; Needed to register :http-xhrio to re-frame.
            [reagent-dev-tools.core :as dev-tools]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.controllers :as rfc]
            [simpleserver.util :as ss-util]
            [simpleserver.state :as ss-state]
            [simpleserver.view.home :as ssv-home]))



;; ******************************************************************
;; NOTE: When starting ClojureScript REPL in Cursive, give first command:
; (shadow.cljs.devtools.api/repl :app)
; to connect the REPL to the app running in the browser.
;; ******************************************************************

;;; Events ;;;

(re-frame/reg-event-db
  ::initialize-db
  (fn [_ _]
    {:current-route nil
     :token nil
     :debug true
     :login nil
     :signin nil}))

(re-frame/reg-event-fx
  ::ss-state/navigate
  (fn [_ [_ & route]]
    ;; See `navigate` effect in routes.cljs
    {::navigate! route}))

(re-frame/reg-event-db
  ::ss-state/navigated
  (fn [db [_ new-match]]
    (let [old-match (:current-route db)
          new-path (:path new-match)
          controllers (rfc/apply-controllers (:controllers old-match) new-match)]
      (js/console.log (str "new-path: " new-path))
      (cond-> (assoc db :current-route (assoc new-match :controllers controllers))
              (= "/" new-path) (-> (assoc :signin nil)
                                   (assoc :login nil))))))

(re-frame/reg-event-fx
  ::ss-state/logout
  (fn [cofx [_]]
    {:db (assoc (:db cofx) :jwt nil)
     :fx [[:dispatch [::ss-state/navigate ::ss-state/home]]]}))


;; Effects

;; Triggering navigation from events.
(re-frame/reg-fx
  ::navigate!
  (fn [route]
    (apply rfe/push-state route)))


;; Routes

(defn href
  "Return relative url for given route. Url can be used in HTML links."
  ([k]
   (href k nil nil))
  ([k params]
   (href k params nil))
  ([k params query]
   (rfe/href k params query)))

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
  (when ss-util/debug? (prn "ENTER init-routes!"))
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
  (when ss-util/debug?
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
