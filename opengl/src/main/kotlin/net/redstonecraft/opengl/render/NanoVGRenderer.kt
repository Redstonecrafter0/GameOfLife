package net.redstonecraft.opengl.render

import net.redstonecraft.opengl.interfaces.LPointed
import org.lwjgl.BufferUtils
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3.*

class NanoVGRenderer(var width: Int = 1920, var height: Int = 1080, val debug: Boolean = false) : LPointed {

    override val pointer = nvgCreate(NVG_ANTIALIAS or NVG_STENCIL_STROKES.let { if (debug) it or NVG_DEBUG else it})

    private val functions = Functions()

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    fun loadFont(name: String, data: ByteArray) {
        val buffer = BufferUtils.createByteBuffer(data.size)
        buffer.put(data)
        buffer.position(0)
        nvgCreateFontMem(pointer, name, buffer, 1)
    }

    fun render(block: NanoVGRenderer.Functions.() -> Unit) {
        nvgBeginFrame(pointer, width.toFloat(), height.toFloat(), 1F)
        try {
            functions.block()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        nvgEndFrame(pointer)
    }

    inner class Functions {
        private val path = Path()
        private val font = TextFont()

        fun rgb(r: Int, g: Int, b: Int) = nvgRGB(r.toByte(), g.toByte(), b.toByte(), NVGColor.create())
        fun rgb(r: Float, g: Float, b: Float) = rgb((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
        fun rgba(r: Int, g: Int, b: Int, a: Int) = nvgRGBA(r.toByte(), g.toByte(), b.toByte(), a.toByte(), NVGColor.create())
        fun rgba(r: Float, g: Float, b: Float, a: Float) = rgba((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt(), (a * 255).toInt())

        fun hsl(h: Int, s: Int, l: Int) = hsl(h / 255F, s / 255F, l / 255F)
        fun hsl(h: Float, s: Float, l: Float) = nvgHSL(h, s, l, NVGColor.create())
        fun hsla(h: Int, s: Int, l: Int, a: Int) = hsla(h / 255F, s / 255F, l / 255F, a / 255F)
        fun hsla(h: Float, s: Float, l: Float, a: Float) = nvgHSLA(h, s, l, (a * 255).toInt().toByte(), NVGColor.create())

        fun linearGradient(cx1: Float, cy1: Float, cx2: Float, cy2: Float, color1: NVGColor, color2: NVGColor) = nvgLinearGradient(pointer, cx1, cy1, cx2, cy2, color1, color2, NVGPaint.create())
        fun boxGradient(x: Float, y: Float, w: Float, h: Float, r: Float, f: Float, color1: NVGColor, color2: NVGColor) = nvgBoxGradient(pointer, x, y, w, h, r, f, color1, color2, NVGPaint.create())
        fun radialGradient(x: Float, y: Float, ir: Float, or: Float, color1: NVGColor, color2: NVGColor) = nvgRadialGradient(pointer, x, y, ir, or, color1, color2, NVGPaint.create())

        fun fill(color: NVGColor, block: NanoVGRenderer.Functions.Path.() -> Unit) {
            nvgBeginPath(pointer)
            path.block()
            nvgFillColor(pointer, color)
            nvgFill(pointer)
            nvgClosePath(pointer)
        }

        fun fill(paint: NVGPaint, block: NanoVGRenderer.Functions.Path.() -> Unit) {
            nvgBeginPath(pointer)
            path.block()
            nvgFillPaint(pointer, paint)
            nvgFill(pointer)
            nvgClosePath(pointer)
        }

        fun stroke(color: NVGColor, width: Float = 1F, block: NanoVGRenderer.Functions.Path.() -> Unit) {
            nvgBeginPath(pointer)
            nvgStrokeWidth(pointer, width)
            path.block()
            nvgStrokeColor(pointer, color)
            nvgStroke(pointer)
            nvgClosePath(pointer)
        }

        fun stroke(paint: NVGPaint, width: Float = 1F, block: NanoVGRenderer.Functions.Path.() -> Unit) {
            nvgBeginPath(pointer)
            nvgStrokeWidth(pointer, width)
            path.block()
            nvgStrokePaint(pointer, paint)
            nvgStroke(pointer)
            nvgClosePath(pointer)
        }

        inner class Path {
            fun miterLimit(limit: Float) = nvgMiterLimit(pointer, limit)
            fun lineCap(cap: Int) = nvgLineCap(pointer, cap)
            fun lineJoin(join: Int) = nvgLineJoin(pointer, join)
            fun globalAlpha(a: Float) = nvgGlobalAlpha(pointer, a)
            fun globalAlpha(a: Int) = globalAlpha(a / 255F)

            fun scissor(x: Float, y: Float, w: Float, h: Float) = nvgScissor(pointer, x, y, w, h)
            fun interSectScissor(x: Float, y: Float, w: Float, h: Float) = nvgIntersectScissor(pointer, x, y, w, h)
            fun resetScissor() = nvgReset(pointer)

            fun arc(cx: Float, cy: Float, r: Float, a0: Float, a1: Float, direction: Int) = nvgArc(pointer, cx, cy, r, a0, a1, direction)
            fun rect(x: Float, y: Float, w: Float, h: Float) = nvgRect(pointer, x, y, w, h)
            fun roundedRect(x: Float, y: Float, w: Float, h: Float, r: Float) = nvgRoundedRect(pointer, x, y, w, h, r)
            fun roundedRectVarying(x: Float, y: Float, w: Float, h: Float, rtl: Float, rtr: Float, rbr: Float, rbl: Float) = nvgRoundedRectVarying(pointer, x, y, w, h, rtl, rtr, rbr, rbl)
            fun ellipse(x: Float, y: Float, rx: Float, ry: Float) = nvgEllipse(pointer, x, y, rx, ry)
            fun circle(x: Float, y: Float, r: Float) = nvgCircle(pointer, x, y, r)

            fun moveTo(x: Float, y: Float) = nvgMoveTo(pointer, x, y)
            fun lineTo(x: Float, y: Float) = nvgLineTo(pointer, x, y)
            fun cubicBezierTo(cx1: Float, cy1: Float, cx2: Float, cy2: Float, x: Float, y: Float) = nvgBezierTo(pointer, cx1, cy1, cx2, cy2, x, y)
            fun quadBezierTo(cx: Float, cy: Float, x: Float, y: Float) = nvgQuadTo(pointer, cx, cy, x, y)
            fun arcTo(x1: Float, y1: Float, x2: Float, y2: Float, r: Float) = nvgArcTo(pointer, x1, y2, x2, y2, r)
        }

        fun font(font: String, color: NVGColor = rgb(1F, 1F, 1F), size: Float = 12F, align: Int = NVG_ALIGN_LEFT, block: NanoVGRenderer.Functions.TextFont.() -> Unit) {
            nvgBeginPath(pointer)
            nvgFontSize(pointer, size)
            nvgFontFace(pointer, font)
            nvgTextAlign(pointer, align)
            nvgFillColor(pointer, color)
            nvgStrokeColor(pointer, color)
            this.font.block()
            nvgClosePath(pointer)
        }

        fun font(font: String, paint: NVGPaint, size: Float = 12F, align: Int = NVG_ALIGN_LEFT, block: NanoVGRenderer.Functions.TextFont.() -> Unit) {
            nvgBeginPath(pointer)
            nvgFontSize(pointer, size)
            nvgFontFace(pointer, font)
            nvgTextAlign(pointer, align)
            nvgFillPaint(pointer, paint)
            nvgStrokePaint(pointer, paint)
            this.font.block()
            nvgClosePath(pointer)
        }

        inner class TextFont {
            fun fontBlur(blur: Float) = nvgFontBlur(pointer, blur)
            fun textLetterSpacing(spacing: Float) = nvgTextLetterSpacing(pointer, spacing)
            fun lineHeight(lineHeight: Float) = nvgTextLineHeight(pointer, lineHeight)

            fun text(x: Float, y: Float, text: String) = nvgText(pointer, x, y, text)
            fun textBox(x: Float, y: Float, maxWidth: Float, text: String) = nvgTextBox(pointer, x, y, maxWidth, text)

            fun textBounds(text: String, x: Float, y: Float): TextBounds {
                val buffer = FloatArray(4)
                nvgTextBounds(pointer, x, y, text, buffer)
                return TextBounds(buffer[0], buffer[1], buffer[2], buffer[3])
            }
            fun textBoxBounds(text: String, x: Float, y: Float, maxWidth: Float): TextBounds {
                val buffer = FloatArray(4)
                nvgTextBoxBounds(pointer, x, y, maxWidth, text, buffer)
                return TextBounds(buffer[0], buffer[1], buffer[2], buffer[3])
            }
        }
    }

}

data class TextBounds(val minX: Float, val minY: Float, val maxX: Float, val maxY: Float)
