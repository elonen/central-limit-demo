## Central Limit Theorem interactive demo

[![Build Status](https://travis-ci.com/elonen/central-limit-demo.svg?branch=master)](https://travis-ci.com/elonen/central-limit-demo)

This is source code for the interactive web demonstration and explanation about _central limit theorem_, published at https://elonen.iki.fi/articles/centrallimit/index.en.html

It's written in Rust (with wasm-pack). The actual code is [src/lib.rs](src/lib.rs).

Why not Javascript? Because this was my perfect excuse to learn *Rust + Wasm* workflow. For large repetition values, calculation can also actually get pretty intense, so this is in fact a good fit for Rust.

The original 2001 version was a Java applet, see `old__CentralLimitApplet.java`.

### 🛠️ Building

```
wasm-pack build --target web --release
```
