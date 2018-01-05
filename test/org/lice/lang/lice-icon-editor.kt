package org.lice.lang


import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun task2() {
	val bigIcon = ImageIO.read(File("res/icons/big_icon.png"))
	(0 until bigIcon.width).forEach { x ->
		(0 until bigIcon.height).forEach { y ->
			val o = bigIcon.getRGB(x, y)
			print((Color(o).rgb + 0xFFFFFF).toString(16))
			dealWithPixel(bigIcon, x, y)
		}
		println()
	}
	ImageIO.write(bigIcon, "PNG", File("big_icon.png"))
}

fun Array<String>.main() {
	task2()
}

private fun dealWithPixel(icon: BufferedImage, x: Int, y: Int) {
	val o = icon.getRGB(x, y) + 0xFFFFFF
	var a = 256L - listOf(o and 0xFF, (o shr 8) and 0xFF, o shr 16).sum() / 3
	a = (16 * Math.sqrt(a.toDouble())).toLong()
	a = (16 * Math.sqrt(a.toDouble())).toLong()
	when {
		a < 0x90 -> a = a / 2
		a < 0xa0 -> a = a * 2 / 3
		a < 0xb0 -> a = a * 3 / 4
	}
	if (o == 0xfffffe) a = 0
	icon.setRGB(x, y, ((a shl 24) or o.toLong()).toInt())
	print("[${"%02x".format(a)}] ")
}
