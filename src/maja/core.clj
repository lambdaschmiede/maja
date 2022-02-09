(ns maja.core
  (:import (io.honeycomb.libhoney LibHoney)))

(def ^:dynamic root-trace nil)
(def ^:dynamic traces [])

(defn uuid []
  (java.util.UUID/randomUUID))

(defn now []
  (System/currentTimeMillis))

(defn init-honeycomb
  "Initializes a Honeycomb Client"
  [{:keys [write-key dataset sample-rate]}]
  (if (some? write-key)
    (LibHoney/create (->
                      (LibHoney/options)
                      (.setWriteKey write-key)
                      (.setDataset dataset)
                      (.setSampleRate sample-rate)
                      (.build)))
    nil))

(defn send-event
  "Sends an event outside of any active trace"
  [params timestamp honey]
  (when (some? honey)
    (-> (.createEvent honey)
        (.addFields params)
        (.setTimestamp timestamp)
        (.send))))

(defn send-in-trace
  "Sends a single event inside the active trace, e.g. an error event"
  [params honey]
  (send-event (merge {"trace.trace_id" root-trace
                      "trace.parent_id" (last (pop traces))} params)
              (now)
              honey))

(defn- fqfn
  "Returns a fully qualified name for the function"
  [function]
  (let [m (meta function)]
    (str (:ns m) "/" (:name m))))

(defn- wrap-fn
  "If applied to a function, it gets wrapped with the Honeycomb tracing"
  [function honey]
  (alter-var-root
   function
   (fn [f]
     (fn [& n]
       (binding [root-trace (or root-trace (uuid))
                 traces (conj traces (uuid))]
         (let [start (now)
               result (apply f n)
               duration (- (now) start)
               span-id (last traces)
               parent-span-id (last (pop traces))]

           (send-event (-> {"duration_ms" duration
                            "name" (fqfn function)
                            "trace.span_id" span-id
                            "trace.trace_id" root-trace}
                           (cond-> parent-span-id (assoc "trace.parent_id" parent-span-id)))
                       start
                       honey)
           result))))))

(defn- find-traced-fn
  "Finds all functions in all namespaces which are annotated with the `:traced` metadata"
  []
  (->>
   (all-ns)
   (mapcat ns-publics)
   (vals)
   (filter #(:traced (meta %)))))

(defn wrap-all-trace-methods
  "Wraps all functions in all namespaces with the Honeycomb Tracing function"
  [honey]
  (->> (find-traced-fn)
      (map #(wrap-fn % honey))
      (doall)))
