(ns lein-sub-bump.core-test
  (:require [babashka.fs :as babashka-fs]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [leiningen.sub-bump :as sub-bump]
            ))

(defn- create-directory-if-not-exists [dir]
  (if (not (babashka-fs/exists? dir))
    (babashka-fs/create-dir dir)))

(def project {:version "1.9.9"
              :dependencies [['sub-project-two "0.1.0-SNAPSHOT"]]  ; Note the ' before sub-project-two
              :sub     ["./target/sub-project-one"
                        "./target/sub-project-two"]})

(deftest updates-project-clj
  (create-directory-if-not-exists "./target")
  (create-directory-if-not-exists "./target/sub-project-one")
  (create-directory-if-not-exists "./target/sub-project-two")

  (spit "./target/sub-project-one/project.clj" (slurp (io/resource "project1.clj")))
  (spit "./target/sub-project-two/project.clj" (slurp (io/resource "project2.clj")))

  (sub-bump/sub-bump project nil)
  (is (str/includes? (slurp "./target/sub-project-one/project.clj") "1.9.9"))
  (is (str/includes? (slurp "./target/sub-project-two/project.clj") "1.9.9"))

  )
