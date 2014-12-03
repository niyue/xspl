(ns xspl.core
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as string])
  (:require [clojure.tools.logging :as log])
  (:require [org.httpkit.client :as http-kit]))

(defn splunk-built-in-commands
  "Return a set containg all splunk built-in commands"
  []
  #{"accum" "addinfo" "addtotals" "amodel" "analyzefields" "anomalies" "anomalousvalue" "append" "appendcols" "appendpipe"
    "arules" "associate" "audit" "bucket" "chart" "cluster" "collect" "contingency" "convert" "correlate"
    "crawl" "da" "dbinspect" "dedup" "delete" "delta" "diff" "erex" "eval" "eventcount"
    "eventstats" "extract" "fields" "filldown" "fillnull" "format" "gauge" "gentimes" "geostats" "head"
    "input" "inputcsv" "inputlookup" "iplocation" "join" "kmeans" "kvform" "loadjob" "localize" "localop"
    "lookup" "makecontinuous" "makemv" "map" "metadata" "multikv" "mvcombine" "mvexpand" "nomv" "outlier"
    "outputcsv" "outputlookup" "outputtext" "overlap" "predict" "rangemap" "rare" "regex" "relevancy" "reltime"
    "rename" "replace" "return" "reverse" "rex" "savedsearch" "search" "searchtxn" "selfjoin" "sendemail"
    "set" "sichart" "sirare" "sistats" "sitimechart" "sitop" "sort" "spath" "stats" "strcat"
    "streamstats" "table" "tail" "timechart" "top" "transaction" "trendline" "typeahead" "typelearner" "typer"
    "uniq" "untable" "where" "x11" "xmlkv" "xyseries"})

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

(defn parse-lines
  "Parse lines read from a multiple line conf file"
  [lines]
  (loop [[fl & rs] lines
         ls []
         l ""]
    (if (nil? fl)
      ls
      (if (.endsWith fl "\\")
        (recur rs ls (str l (subs fl 0 (dec (count fl)))))
        (if (zero? (count l))
          (recur rs (conj ls fl) "")
          (recur rs (conj ls (str l fl)) ""))))))

(defn get-searches
  "Parse the saved searches conf file and get all searches defined"
  [conf]
  (log/info "Parse conf file...")
  (let [all-lines (string/split-lines conf)
        lines (parse-lines all-lines)
        split (fn [s del] (subs s (inc (.indexOf s del))))]
    (map #(string/trim (split % "="))
         (filter #(.startsWith % "search") (map string/trim lines)))))

(defn get-commands
  "Parse the search to get all commands used by this search"
  [search]
  (log/info "Parse commands from search string " search)
  (let [with-leading-pipe? (.startsWith search "|")
        full-search (if with-leading-pipe? search (str "| search " search))
        get-command (fn [pipe] (first (string/split pipe #" ")))
        built-in-commands (splunk-built-in-commands)
        normalize-command (fn [cmd] (cond
                                      (built-in-commands cmd) cmd
                                      (.startsWith cmd "`") "search_macro"
                                      :else "custom_command"))]
    (map #(normalize-command (get-command (string/trim %)))
         (filter (complement string/blank?) (string/split full-search #"\|")))))

(defn fetch-all-searches
  "Fetch all searches from a list, parse them and get all searches used"
  [path]
  (doall
    (let [confs (load-searches-list path)
          get-conf-searches (fn [conf-url] (get-searches (:body (get-saved-searches-conf conf-url))))]
    (mapcat get-conf-searches confs))))

(defn analyze-searches
  "Analyze all searches/commands and their usages"
  [searches]
  (log/info "Analyzing " (count searches) " searches...")
  (let [commands (mapcat get-commands searches)
        commands-map (group-by identity commands)
        commands-usage (zipmap (keys commands-map) (map count (vals commands-map)))
        sorted-usage (sort-by second > (into [] commands-usage))]
    { :searches-count (count searches)
      :command-types-count (count commands-map)
      :total-commands-count (count commands)
      :usage sorted-usage }))



