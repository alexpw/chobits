# chobits

A Clojure library designed to facilitate working with bit fields.

## Why?

Choosing to use a bit field is met with concern about added code complexity and a decrease in readability.

Can this concern be alleviated with the right abstraction?  We'll see.

## Usage

(via clojars): [chobits "0.1.0"]

The tests and code docs will show you the flexibility of method arguments.

Ideally, the only time you work at the bit level is when defining the properties, once.  Then, you work with a field and properties.

### Terminology

Coercion/conversion methods are the bulk of the API.  The terms are important, especially if they differ slightly from what you're used to.

__property map__, aka, flags, options: a hash-map used as an enum to map flags to bits.

__property__: a flag, as a keyword or a sequence of keywords (when used with a nested property map).

__field__: a bit field; a number that represents the on/off state of multiple properties.

__bit__: a single bit, e.g, 2r1.  Treated as an alias to a key in the property map.

__bytes__: a java byte array.  Probably from JDBC/MySQL.

### Init
I've spelled out the methods used in this introduction here, for clarity of their origin.
```
(ns foo.core
  (:require [chobits.core :refer [with-props ->field field-> in-field? ->bit bit->]))
```
### Basic: flat property map
```
(def days-of-week {
  :sun 2r1
  :mon 2r10
  :tue 2r100
  :wed 2r1000
  :thu 2r10000
  :fri 2r100000
  :sat 2r1000000})

(defn is-meeting-allowed-on-day?
    [day]
    (with-props days-of-week 1
        (in-field? (->field :mon :wed :fri) day)))

(is-meeting-allowed-on-day? :sun)
=> false
(is-meeting-allowed-on-day? :mon)
=> true
```
### Organized: supports __uniformly__ nested property maps.
```
(def comment-approval {
    :admin {
      :approved 2r1
      :rejected 2r10
      :waiting  2r100}
    :author {
      :approved 2r1000
      :rejected 2r10000
      :waiting  2r100000}})
```
Instead of separate columns in SQL, like this:
```
CREATE TABLE comments (
  `admin_approval` enum('approved','rejected','waiting') NOT NULL DEFAULT 'waiting',
  `author_approval` enum('approved','rejected','waiting') NOT NULL DEFAULT 'waiting',
    ...
);
```
We'll use a bit field:
```
CREATE TABLE comments (
  `approval` bit(6) NOT NULL,
    ...
);
```
But when you query it, you get a byte array, but don't you worry.
```
=> (first (query "SELECT approval FROM comments LIMIT 1"))
{:approval #<byte>[] [B@174c2fa5]}
```
Chobits makes sense of this for you when using (->field) or (in-field?), but also exposes the fn (bytes->field).
```
(def approval (-> (query "SELECT approval FROM comments LIMIT 1")
                  (get-in [0 :approval])))

=> (with-props comment-approval 2 (->field approval))
[(:admin :rejected)]
```
We can easily deconstruct the bit field into a vector of map paths.  The lower-level (->bit) takes that map path and gives you the corresponding bit.
```
=> (with-props comment-approval 2
    (prn (field-> approval))
    (prn (mapv ->bit (field-> approval)))
    (prn (mapv #(get-in comment-approval %) (field-> approval))))

[(:admin :rejected) (:author :approved)]
[2 8]
[2 8]
nil
```
### SELECT'ing a bit field
If we want to construct queries based on a bit value or field:
```
(with-props comment-approval 2

    ;; Match an entire bit field.  Note the "b".
    (query "SELECT * FROM comments WHERE approval = b?" (->field [:admin :rejected]))

    ;; Match a bit within the field
    (query "SELECT * FROM comments WHERE approval & b?" (->bit [:admin :approved])))
```

### INSERT'ing a bit field
```
(with-props comment-approval 2
    (exec "INSERT INTO comments SET approval = ?"
          (->field [:admin :approved] [:author :waiting])))
```
## License

Release under the MIT license. See LICENSE for the full license.
