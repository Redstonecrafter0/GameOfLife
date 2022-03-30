import jni.*
import kotlinx.cinterop.*
import msdfgl.*

@CName("Java_net_redstonecraft_msdfjni_Native_nGenerate")
fun n_msdfgl_create_context(env: CPointer<JNIEnvVar>, clazz: jclass, fontName: jstring, range: jfloat, scale: jfloat, start: jint, end: jint): jint? {
    val fontName = memScoped {
        env.pointed.pointed!!.GetStringUTFChars!!.invoke(env, fontName, null)!!.toKStringFromUtf8()
    }
    val context = msdfgl_create_context(null)
    val font = msdfgl_load_font(context, fontName, range, scale, null)
    msdfgl_generate_glyphs(font, start, end)
    msdfgl_printf()
    msdfgl_destroy_font(font)
    msdfgl_destroy_context(context)
}
