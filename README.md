## Central Limit Theorem interactive demo

This is source code for the interactive demonstration and explanation about _central limit theorem_, published at https://elonen.iki.fi/articles/centrallimit/index.en.html

It's written in Rust / wasm-pack + Javascript.

Why not JS only? Because this was my perfect excuse to learn *Rust* to *Wasm* workflow. For large repetitions, calculation can also actually get pretty heavy, so this is in fact a good fit for Rust.

### 🛠️ Building

```
wasm-pack build --target web --release
```
