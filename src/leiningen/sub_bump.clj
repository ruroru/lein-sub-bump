(ns leiningen.sub-bump
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn get-parent-artifacts [project]
  "Returns set of artifact names from parent dependencies"
  (set (conj (map first (:dependencies project))
             ".")))

(defn escape-regex [s]
  (str/replace s #"[\[\]\\\.\*\+\?\^\$\{\}\(\)\|]" "\\\\$0"))

(defn update-subproject-file [file-path parent-version parent-artifacts]
  (let [content (slurp file-path)
        updated-content
        (reduce (fn [text artifact]
                  (str/replace text
                               (re-pattern (str "(?<=\\[" (escape-regex (str artifact))
                                                "\\s)\"[^\"]+\""))
                               (str "\"" parent-version "\"")))
                content
                parent-artifacts)]
    (spit file-path updated-content)
    (println "Updated" file-path)))

(defn sub-bump
  [project & args ]
  (let [version (:version project)
        parent-artifacts (get-parent-artifacts project)
        sub-projects (map (fn [sub-project-path]
                            (str sub-project-path "/project.clj"))
                          (:sub project))]
    (doseq [sub-project sub-projects]
      (when (.exists (io/file sub-project))
        (update-subproject-file sub-project version parent-artifacts)))))