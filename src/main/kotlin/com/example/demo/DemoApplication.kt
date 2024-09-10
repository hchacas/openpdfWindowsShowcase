package com.example.demo

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.pdfboxout.visualtester.PdfVisualTester
import com.openhtmltopdf.pdfboxout.visualtester.PdfVisualTester.PdfCompareResult
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import javax.imageio.ImageIO

@SpringBootApplication
class DemoApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
	try {
		val compare = true
		if (compare) {
			val outputFile = ByteArrayOutputStream()
			outputFile.use {
				PdfRendererBuilder().apply {
					withFile(File("src/main/resources/pdf/test.html"))
					useFastMode()
					toStream(it)
					run()
				}
			}
			compareFile(
				readPdfAsByteArray("src/main/resources/pdf/base.pdf"),
				outputFile.toByteArray()
			)
		} else {
			val outputFile = FileOutputStream("src/main/resources/pdf/base.pdf")
			outputFile.use {
				PdfRendererBuilder().apply {
					withFile(File("src/main/resources/pdf/test.html"))
					useFastMode()
					toStream(it)
					run()
				}
			}
		}



	} catch (e: IOException) {
		throw Exception("Error creating PDF", e)
	}
}

fun readPdfAsByteArray(filePath: String): ByteArray {
	val file = File(filePath)

	// Check if the file exists and is a file
	if (!file.exists() || !file.isFile) {
		throw IllegalArgumentException("File does not exist or is not a valid file: $filePath")
	}

	// Read the PDF file as a byte array
	return file.readBytes()
}


fun compareFile(
	expectedPdfBytes: ByteArray,
	actualPdfBytes: ByteArray
): Boolean {
	val outputPath = "build/compare-pdf/"
	val resource = "pdfSample"
	Files.createDirectories(Paths.get(outputPath))

	val problems = PdfVisualTester.comparePdfDocuments(expectedPdfBytes, actualPdfBytes, resource, false)

	if (problems.isNotEmpty()) {
		System.err.println("Found problems with test case ($resource):")
		System.err.println(
			problems
				.stream()
				.map { p: PdfCompareResult -> p.logMessage }
				.collect(Collectors.joining("\n    ", "[\n    ", "\n]"))
		)

		System.err.println("For test case ($resource) writing failure artefacts to '$outputPath'")
		val outPdf = File(outputPath, "$resource---actual.pdf")
		Files.write(outPdf.toPath(), actualPdfBytes)
	}

	for (result in problems) {
		if (result.testImages != null) {
			var output = File(outputPath, resource + "---" + result.pageNumber + "---diff.png")
			ImageIO.write(result.testImages.createDiff(), "png", output)

			output = File(outputPath, resource + "---" + result.pageNumber + "---actual.png")
			ImageIO.write(result.testImages.actual, "png", output)

			output = File(outputPath, resource + "---" + result.pageNumber + "---expected.png")
			ImageIO.write(result.testImages.expected, "png", output)
		}
	}

	return problems.isEmpty()
}
