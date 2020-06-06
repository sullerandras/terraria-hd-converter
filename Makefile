default: build

RELEASE_VERSION=0.2

clean:
	rm -rf out
	rm -f terraria-hd-converter-$(RELEASE_VERSION).jar

build: clean
	javac --target 8 --source 8 -cp src -d out/production/terraria src/com/blogspot/intrepidis/*.java src/com/github/sullerandras/terraria/*.java
	jar cfe terraria-hd-converter-$(RELEASE_VERSION).jar com.github.sullerandras.terraria.Main -C out/production/terraria .

run: build
	java -jar terraria-hd-converter-$(RELEASE_VERSION).jar
