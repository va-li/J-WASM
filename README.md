# J-WASM

> Project for Abstract Machines course SS2017 at TU Vienna

Interpreter for a subset of [WebAssembly](http://webassembly.org) wirten in Java

## Getting started

### Prerequisites

* Java SDK 8
* Maven

### Installation

Inside the project's root directory run

```
$ mvn install
```

This will compile the sources and generate an executable jar file.
Now change into the the newly generated sub directory `target` and run

```
$ java -jar j-wasm-0.1-SNAPSHOT.jar
```

This will print the usage message.

## Examples

Inside the project's root directory run

```
$ java -jar target/j-wasm-0.1-SNAPSHOT.jar src/test/resources/binary/mem_data.wasm
```
This will execute the WebAssembly binary encoded module `mem_data.wasm`.

Some simple test cases can be found in the `src/test/resources` directory. In there the folder `binary` contains WebAssembly binary encoded files. Inside the `text` directory you can find files encoded in WebAssembly's human readable text format containing the same instructions as their counterparts inside `binary` with the same name.

To write your own WebAssembly modules you can use the [wat2wasm demo](https://cdn.rawgit.com/WebAssembly/wabt/aae5a4b7/demo/wat2wasm/) to write WebAssembly in text format and download the produced binary file and execute it with J-WASM. Just be sure to only use the instructions set covered by this implementation (see "Features" below).

### J-WASM Output

Since WebAssembly does not have a "print()"-like built in functionality by design to produce any observable output we have included the possibility to dump the [linear memory](http://webassembly.org/docs/semantics/#linear-memory) contents to a file inside the current directory after successful execution by passing the `-d` or `--dump-linear-memory` flag to the J-WASM jar when executing a WebAssembly module: 
```
java -jar target/j-wasm-0.1-SNAPSHOT.jar src/test/resources/binary/mem_data.wasm -d
```

The dump file can then be inspected with a hex viewer (when using linux's `hexdump` utility be sure to use it's `-C` option flag to avoid endianess confusion!).

Another way to observe what is happening inside J-WASM is obviously to use a debugger (e.g. Eclipse, IntelliJ).

## Features

### Current functionality

* Interpreting WebAssembly in Binary Encoding from file
* Types: 32 bit integer
* Integer i32 operations
* Control constructs and instructions (without `br_table`)
* Local Variables
* Constants
* Functions (without `call-indirect`)
* Linear Memory (including predefined data segments)
