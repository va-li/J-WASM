(module
  (type (;0;) (func (param i32) (result i32)))
  (type (;1;) (func))
  (func (;0;) (type 0) (param i32) (result i32)
    get_local 0
    i32.const 1000000
    i32.lt_s
    if (result i32)
      get_local 0
      call 0
      i32.const 1
      i32.add
      return
    else
    end)
  (func (;1;) (type 1)
    i32.const 1
    call 0
    drop)
  (memory (;0;) 64 64)
  (start 1))
