
build:
	./gradlew build

test:
	./gradlew test

testv:
	./gradlew -Dlog4j2.debug=true test -i

.PHONY: build test testv