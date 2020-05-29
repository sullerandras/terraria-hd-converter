default: build

clean:
	rm -rf out
	rm -f terraria.jar

build: clean
	javac --target 8 --source 8 -cp src -d out/production/terraria src/com/blogspot/intrepidis/*.java src/com/github/sullerandras/terraria/*.java
	jar cfe terraria.jar com.github.sullerandras.terraria.Main -C out/production/terraria .

run: build
	java -jar terraria.jar
