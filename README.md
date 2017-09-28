# J-WASM

> Project for Abstract Machines course SS2017 at TU Vienna

Interpreter for a subset of [WebAssembly](http://webassembly.org) wirten in Java

## Getting started

### Prerequisites

* Java SDK 8 or higher
* Maven

### Installation

Inside the projects root directory run

```
$ mvn install
```

This will compile the sources and generate an executable jar file.
Now change into the the newly generated sub directory `target` and run

```
$ java -jar j-wasm-0.1-SNAPSHOT.jar
```

This currently should produce an error. 

## Features

### Current functionality

* Interpreting WebAssembly in Binary Encoding from file
* Types: 64 bit integer
* Integer operations
* Control constructs and instructions (no loops yet)
* Local Variables
* Constants
* Functions (without call-indirect)

### Planned functionality

* Control construct: Loop
* Linear Memory

### Optional Functionality

* Interpreting text representation of WebAssembly
* REPL (ReadEvalPrintLoop) Frontend