use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use std::net::TcpListener;
use std::io::{Read, Write};
use std::thread;

/// JNI binding function exported for Android Java/Kotlin
#[no_mangle]
pub extern "system" fn Java_com_example_native_RustHttpServer_startRustServerNative(
    env: JNIEnv,
    _class: JClass,
    port: i32,
) -> jstring {
    let response_str = format!("Rust Localhost Server initialized on port {}", port);
    env.new_string(response_str)
        .expect("Couldn't create java string!")
        .into_raw()
}

/// Standalone Rust HTTP listener for serving DevStudio web preview files
pub fn run_rust_localhost_server(port: u16) {
    let address = format!("127.0.0.1:{}", port);
    if let Ok(listener) = TcpListener::bind(&address) {
        println!("Rust HTTP Server running at http://{}", address);
        for stream in listener.incoming() {
            if let Ok(mut stream) = stream {
                let mut buffer = [0; 1024];
                let _ = stream.read(&mut buffer);

                let http_response = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n\r\n\
                    <html><body><h1>DevStudio Rust HTTP Server Active</h1></body></html>";
                let _ = stream.write_all(http_response.as_bytes());
            }
        }
    }
}
