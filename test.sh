#!/bin/bash

set -x

tmpfile=$(mktemp /tmp/out.XXXXXX)
jarsigner -verify -verbose "./tests/assets/tiny.s.apk" > "$tmpfile"
if ! grep -q 'jar verified' "$tmpfile"; then
  exit 1
fi
java -jar ./target/unsign*.jar "./tests/assets/tiny.s.apk"
jarsigner -verify -verbose "./tests/assets/tiny.s.apk" > "$tmpfile"
if ! grep -q 'jar is unsigned' "$tmpfile"; then
  exit 1
fi
