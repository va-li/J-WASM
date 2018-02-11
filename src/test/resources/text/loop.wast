(module
  (type (;0;) (func (param i32) (result i32)))
  (type (;1;) (func))
(func (;0;) (type 0) (param i32) (result i32)
    block
      loop
        (set_local 0 (i32.sub (get_local 0) (i32.const 1)))
        (br_if 0 (get_local 0))
      end
    end
    (get_local 0)
)
(func (;1;) (type 1)
	i32.const 20
    call 0
    drop)
(memory (;0;) 64 64)
  (start 1))