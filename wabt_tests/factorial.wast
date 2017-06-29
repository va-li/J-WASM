(module
  (type (;0;) (func (param i32) (result i32)))
  (type (;1;) (func (param i32))
  (func (;0;) (type 0) (param i32) (result i32)
    get_local 0
    i32.const 0
    i32.eq
    if (result i32)  ;; label = @1
      i32.const 1
      return
    else
      get_local 0
      get_local 0
      i32.const 1
      i32.sub
      call 0
      i32.mul
      return
    end)
  (func (;1;) (type 1) (param i32)
    get_local 0
    call 0
    drop)
  (memory (;0;) 64 64)
  (start 1))
