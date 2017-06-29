(module
  (type (;0;) (func (param i32) (result i32)))
  (type (;1;) (func))
  (func (;0;) (type 0) (param i32) (result i32)
    get_local 0
    i32.const 3
    i32.lt_s
    if (result i32)
      i32.const 1
      return
    else
      get_local 0
      i32.const 2
      i32.sub
      call 0
      get_local 0
      i32.const 1
      i32.sub
      call 0
      return
    end)
  (func (;1;) (type 1)
    i32.const 30
    call 0
    drop)
  (memory (;0;) 64 64)
  (start 1))
