//! Test suite for the Web and headless browsers.

#![cfg(target_arch = "wasm32")]

extern crate wasm_bindgen_test;
use wasm_bindgen_test::*;
use central_limit_demo::simulate_and_redraw;

//use web_sys::{Document, HtmlCanvasElement, CanvasRenderingContext2d};

wasm_bindgen_test_configure!(run_in_browser);

#[wasm_bindgen_test]
fn try_render() {

    let window = web_sys::window().expect("no global `window` exists");
    let document = window.document().expect("should have a document on window");
    let body = document.body().expect("document should have a body");

    let el = document.create_element("div").unwrap();
    el.set_inner_html(r###"
      <div style="border: 1px solid black; width: 480;">
	      <canvas id="canvas" height="320" width="480"></canvas>
	      <input id="simu_sum_of"value="10"/>
	      <input id="simu_func"  value="sin(x/10)+pi"/>
	      <input id="simu_min" value="0"/>
	      <input id="simu_max" value="10"/>
	      <input id="simu_repeat_times" value="1000"/>
      </div>
    "###);
	body.append_child(&el).unwrap();

	simulate_and_redraw().unwrap();
}
