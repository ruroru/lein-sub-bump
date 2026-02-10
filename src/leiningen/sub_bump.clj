(ns leiningen.sub-bump
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.io PushbackReader)))

(defn read-project-name
  "Reads a project.clj file and returns the full artifact name as declared
   in defproject, e.g. \"org.clojars.jj/boa-async-query\"."
  [file-path]
  (with-open [rdr (PushbackReader. (io/reader file-path))]
    (let [form (read rdr)]
      ;; form is (defproject <name> <version> ...)
      (str (nth form 1)))))

(defn collect-sub-project-names
  "Step 1: walks every sub-project directory and returns a set of the full
   artifact names found in their project.clj files."
  [base-dir subs]
  (reduce (fn [names sub]
            (let [path (str base-dir "/" sub "/project.clj")]
              (if (.exists (io/file path))
                (conj names (read-project-name path))
                names)))
          #{}
          subs))

(defn escape-regex [s]
  (str/replace s #"[\[\]\\\.\*\+\?\^\$\{\}\(\)\|]" "\\\\$0"))

(defn bump-versions-in-file
  "Step 2: rewrites file-path, replacing the version string of any dependency
   whose full name (including group) is in artifact-names."
  [file-path new-version artifact-names]
  (let [content (slurp file-path)
        updated (reduce
                  (fn [text artifact]
                    (str/replace
                      text
                      (re-pattern (str "\\[" (escape-regex artifact) "(\\s+)\"[^\"]+\""))
                      (fn [[_match ws]]
                        (str "[" artifact ws "\"" new-version "\""))))
                  content
                  artifact-names)]
    (spit file-path updated)
    (println "Updated" file-path)))

(defn sub-bump
  [project & _args]
  (let [version  (:version project)
        base-dir (or (:root project) "target")
        subs     (:sub project)]

    ;; Step 1: collect the full artifact names from the sub-project files
    (let [artifact-names (collect-sub-project-names base-dir subs)]
      (println "Bumping to" version)
      (println "Sub-project artifacts:" artifact-names)

      ;; Step 2: update every sub-project file
      (doseq [sub subs
              :let [path (str base-dir "/" sub "/project.clj")]
              :when (.exists (io/file path))]
        (bump-versions-in-file path version artifact-names)))))