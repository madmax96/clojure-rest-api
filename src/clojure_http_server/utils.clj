(ns clojure-http-server.utils
  (:require [clojure.string :as str]
            [ring.util.codec :refer [base64-decode]])
  (:import (java.io File)))

;Validation

(defn- error-messages-for
  "Return a seq of error messages"
  [to-validate message-validator-pairs]
  (map first (filter #(not ((second %) to-validate))
                     (partition 2 message-validator-pairs))))

(defn validate
  "Returns a map with a vector of errors for each key"
  [to-validate validations]
  (reduce (fn [errors validation]
            (let [[fieldname validation-check-groups] validation
                  value (get to-validate fieldname)
                  error-messages (error-messages-for value validation-check-groups)]
              (if (empty? error-messages)
                errors
                (assoc errors fieldname error-messages))))
          {}
          validations))

;validation example from 'Clojure for the Brave and True' book.
(defmacro if-valid
  "Handle validation more concisely"
  [to-validate validations errors-name & then-else]
  `(let [~errors-name (validate ~to-validate ~validations)]
     (if (empty? ~errors-name)
       ~@then-else)))

(defn handle-base64Image-upload
  [image username]
  (let [[info image-data] (str/split image #",")
        [image-info encoding] (str/split info #";")
        [image-type format] (str/split image-info #"/")]
    (if (or
         (not= image-type "data:image")
         (not= encoding "base64"))
      false
      (let [decoded (base64-decode image-data)
            filename (str username "-" (crypto.random/url-part 8) "." format)]
        (clojure.java.io/copy
         decoded
         (File. (str "resources/image-uploads/" filename)))
        filename))))