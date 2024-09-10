package com.example.demo

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@SpringBootApplication
class DemoApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
	try {
		val outputFile = FileOutputStream("src/main/resources/pdf/generated.pdf")
		outputFile.use {
			PdfRendererBuilder().apply {
				withFile(File("src/main/resources/pdf/test.html"))
				useFastMode()
				toStream(it)
				run()
			}
		}
	} catch (e: IOException) {
		throw Exception("Error creating PDF", e)
	}
}
