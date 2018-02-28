;; swaps two predefined data entries in linear memory
(module
  	(type (func))
	(func (type 0)
      	i32.const 11
  		i32.const 10
  		i32.load8_u
      	i32.const 10
  		i32.const 11
  		i32.load8_u
      	i32.store8
      	i32.store8
	)
	(memory 1 1)
  	(data 0 (offset i32.const 10) "\a0" "\1b") ;; "\a0" is hex value 0xa0
  	(start 0)
)