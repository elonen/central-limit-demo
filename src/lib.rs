use wasm_bindgen::prelude::*;
use wasm_bindgen::JsCast;
use std::f64;
use web_sys::{Document, HtmlCanvasElement, CanvasRenderingContext2d};
extern crate meval;


// Import from Javascript
#[wasm_bindgen]
extern "C" {
    fn alert(s: &str);

    #[wasm_bindgen(js_namespace = console)]
    fn log(s: &str);

    #[wasm_bindgen(js_namespace = Math)]
    pub fn random() -> f64;
}

// Startpoint
#[wasm_bindgen(start)]
pub fn run() -> Result<(), JsValue> {
    // Set panic handler
    #[cfg(feature = "console_error_panic_hook")]
    console_error_panic_hook::set_once();
    Ok(())
}

// Export to Javascript
#[wasm_bindgen]
pub fn simulate_and_redraw() -> Result<(), JsValue> {
    simulate().or_else(|e| { log(&format!("APP ERROR: {}", &e)); alert(&e); Ok(()) })
}



fn get_html_objs() -> (Document, HtmlCanvasElement, CanvasRenderingContext2d)
{
    let document = web_sys::window().unwrap().document().unwrap();
    let canvas = document.get_element_by_id("canvas").unwrap();
    let canvas: web_sys::HtmlCanvasElement = canvas.dyn_into::<web_sys::HtmlCanvasElement>().map_err(|_| ()).unwrap();
    let context = canvas.get_context("2d").unwrap().unwrap().dyn_into::<web_sys::CanvasRenderingContext2d>().unwrap();
    (document, canvas, context)
}

/// Read HTML Input element with given ID as string
fn get_input_str(elem_id: &'static str) -> Result<String, String>
{
    let (doc, _, _) = get_html_objs();
    let elem = doc.get_element_by_id(elem_id)
        .ok_or(format!("HTML element '{}' not found.", elem_id))?;
    let val_str = elem.dyn_into::<web_sys::HtmlInputElement>()
        .or(Err(format!("HTML element '{}' failed to cast as Input.", elem_id)))?.value();
    Ok(val_str)
}

/// Read numeric HTML Input element with given ID, and enforce min & max values
fn get_limited_input_num(elem_id: &'static str, min: i32, max: i32 ) -> Result<i32, String>
{
    let val_str = get_input_str(elem_id)?;
    let val_num = val_str.trim().parse::<i32>()
        .or(Err(format!("HTML element '{}' value '{}' is not an integer.", elem_id, val_str)))?;
    if val_num < min || val_num > max { 
        return Err(format!("Value {} (of '{}') is out of bounds ({}-{}).", val_num, elem_id, min, max)); };
    Ok(val_num)
}

pub fn simulate() -> Result<(), String>
{
        // Get parms
        let min = get_limited_input_num("simu_min", -9999, 9999)?;
        let max = get_limited_input_num("simu_max", -9999, 9999)?;
        let sum_of = get_limited_input_num("simu_sum_of", 1, 1000)?;
        let repeat = get_limited_input_num("simu_repeat_times", 1, 10000000)?;
        let func_str = get_input_str("simu_func")?;

        if min >= max { return Err(format!("Min must be < Max."))};
        let (total_min, total_max) = (min*sum_of, max*sum_of);

        log(&format!("Simulating... min = {}, max = {}, total_min = {}, total_max = {}, sum_of = {}, func = {}",
            min, max, total_min, total_max, sum_of, func_str));
        
        let range = (max - min + 1) as usize;
        let total_range = (total_max - total_min + 1) as usize;


        // Calculate frequency table
        let freq: Vec<_> = 
        {
            // Calculate propabilities from the function
            let mut freq = vec![0 as i32; total_range];
            let mut f_total = 0.0;
            let mut prob = vec![0.0; range];

            // Parse math expression
            let expr: meval::Expr = func_str.parse().map_err(|e| format!("EVALUATION ERROR: {}", e))?;
            let mut mctx = meval::Context::new();
            mctx.func("rnd", |_| random());
            let f = expr.bind_with_context(mctx, "x").map_err(|e| format!("VARIABLE ERROR: {}", e))?;

            for i in 0..range {
                let x = (i as i32) + min;
                prob[i] = f(x as f64);
                f_total += prob[i];
            }

            // Normalize P to 0..1
            let prob: Vec<_> = prob.iter().map(|x| x/f_total).collect();

            // Simulate the experiment
            let mut max_freq = 0;
            for _i in 0..repeat {
                let mut sum = 0;
                for _j in 0..sum_of {
                    let mut d = random();
                    for k in 0..range {
                        if d > prob[k] && k < range-1 
                            { d -= prob[k]; }
                        else
                            { sum += k; break; };
                    }
                }
                freq[sum] += 1;
                max_freq = i32::max(max_freq, freq[sum]);
            }

            // Normalize freq to 0..1
            freq.iter().map(|x| (*x as f64) / (max_freq as f64)).collect()
        };
        log("done");

        // Draw the results
        {
            // Prepare canvas
            let (_, cnv, ctx) = get_html_objs();
            let (w, h) = (cnv.width() as f64, cnv.height() as f64);
            ctx.clear_rect(0.0, 0.0, cnv.width().into(), cnv.height().into());
            ctx.set_stroke_style(&JsValue::from("blue"));

            const MARGIN_X: f64 = 8.0;
            const MARGIN_Y: f64 = 30.0;

            // Draw horizontal scale
            {
                let y = h-MARGIN_X;
                ctx.stroke_text(&format!("{}", total_min), 2.0, y).unwrap();
                
                let max_str = format!("{}", total_max);
                let max_str_w = ctx.measure_text(&max_str).unwrap().width();
                ctx.stroke_text(&max_str, (w-max_str_w)-2.0, y).unwrap();

                let avg_str = format!("{}", (total_min + total_max) as f64 / 2.0);
                let avg_str_w = ctx.measure_text(&avg_str).unwrap().width();
                ctx.stroke_text(&avg_str, (w-avg_str_w)/2.0, y).unwrap();
            }

            // Draw the result histogram
            ctx.set_line_width(1.0);
            ctx.begin_path();
            for x in 0..(w as usize - 1) {
                let v = (total_range * x) as f64 / (w as f64 - 1.0);
                let v = freq[v as usize] * (h-MARGIN_Y*2.0) + 1.0;
                ctx.move_to( x as f64, h-MARGIN_Y );
                ctx.line_to( x as f64, h-MARGIN_Y-v );
            }
            ctx.stroke();
        }

    Ok(())
}

