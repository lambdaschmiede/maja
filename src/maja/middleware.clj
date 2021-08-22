(ns maja.middleware
  (:require [maja.core :refer [send-event now uuid root-trace traces]]))

(defn honey-middleware
  "Returns a middleware which automatically wraps "
  [handler honey]
    (fn [request]
    (with-redefs [root-trace (uuid) ;; The middleware is the entry point and should always generate a new trace
                  traces (conj traces (uuid))]
      (let [start-time (now)
            response (handler request)]

        (send-event {"Timestamp" start-time
                     "duration_ms" (- (now) start-time)
                     "status" (:status response)
                     "method" (name (:request-method request))
                     "path" (:uri request)
                     "name" "honey-middleware"
                     "trace.span_id" (last traces)
                     "trace.trace_id" root-trace}
                    honey)
        response))))
