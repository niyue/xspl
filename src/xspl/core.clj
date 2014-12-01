(ns xspl.core
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as string])
  (:require [clojure.tools.logging :as log])
  (:require [org.httpkit.client :as http-kit]))


(defn load-searches-list
  "Load saved searches list from a path"
  [path]
  (log/info "Load searches list " path)
  (with-open [rdr (io/reader (io/resource path))]
    (doall (line-seq rdr))))

(defn get-saved-searches-conf
  "Retrieve saved searches conf file via HTTP"
  [url]
  (log/info "Retrieve saved search conf file: " url)
  (let [res (http-kit/get url)]
    @res))

(defn get-searches
  "Parse the saved searches conf file and get all searches defined"
  [conf]
  (log/info "Parse conf file...")
  (let [lines (string/split-lines conf)
        split (fn [s del] (subs s (inc (.indexOf s del))))]
    (map #(string/trim (split % "="))
         (filter #(.startsWith % "search") (map string/trim lines)))))

(defn get-commands
  "Parse the search to get all commands used by this search"
  [search]
  (log/info "Parse commands from search string " search)
  (let [with-leading-pipe? (.startsWith search "|")
        full-search (if with-leading-pipe? search (str "| search " search))
        get-command (fn [pipe] (first (string/split pipe #" ")))]
    (map #(get-command (string/trim %))
         (filter (complement string/blank?) (string/split full-search #"\|")))))

(defn fetch-all-commands
  "Fetch all saved searches from a list, parse them and get all search commands used"
  [path]
  (doall
    (let [confs (load-searches-list path)
          get-all-commands (fn [conf] (mapcat get-commands (get-searches (:body (get-saved-searches-conf conf)))))]
      (mapcat get-all-commands confs))))
