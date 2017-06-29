(module
	(memory 64 64)
	(func $add (param i32 i32) (result i32)
		i32.const 65536
		get_local 1
		i32.add)
	(func $main (block))
	(start $main)
)
