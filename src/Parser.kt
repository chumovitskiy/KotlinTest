import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.*
import javax.xml.parsers.SAXParserFactory

fun parseXml(params: Parameters) {
    try {
        val file = File(params.source)
        val reader: BufferedReader = file.bufferedReader()
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()
        parser.parse(InputSource(reader), CompilerOutputSAXHandler(file.length(), params))
    } catch (e : SAXException) {
        println("SAXException: $e")
    } catch (e : IOException) {
        println("IOException: $e")
    } catch (e : IllegalArgumentException) {
        println("IllegalArgumentException: $e")
    } catch (e : NullPointerException) {
        println("NullPointerException: $e")
    }

}

class CompilerOutputSAXHandler(val fileSize: Long, val params: Parameters) : DefaultHandler(){

    var newDocument : StringBuilder = StringBuilder()
    var currentSize : Int = 0
    var currentFile : Int = 1
    var currentProcent : Int = 0

    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        if (newDocument.toString().isBlank()) {
            newDocument.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
                    .append("<Houses>\n")
            currentSize = newDocument.toString().toByteArray().size
        }

        var newHoues : StringBuilder = StringBuilder()
        if (qName.equals("House")) {
            newHoues.append("\t<House")
            for (i in 0..(attributes.length-1)) {
                newHoues = appendAttribute(attributes.getQName(i), attributes.getValue(i), newHoues)
            }
            newHoues.append("/>\n")
            if (newHoues.toString().toByteArray().size + currentSize > params.size.getSizeInByte()) {
                newDocument.append("</Houses>")
                writeFile(params, newDocument.toString())
                newDocument = StringBuilder()
                currentSize = 0
                printProcent()
            } else {
                newDocument.append(newHoues.toString())
                currentSize = newDocument.toString().toByteArray().size
                printProcent()
            }
        }
    }

    private fun printProcent() {
        val newProcent = 100 * ((currentFile-1)*params.size.getSizeInByte() + currentSize ) / fileSize
        if (newProcent.toInt() != currentProcent) {
            currentProcent = newProcent.toInt()
            println ("Complte: $currentProcent%")
        }

    }

    @Throws(SAXException::class)
    override fun endDocument() {
        newDocument.append("</Houses>")
        writeFile(params, newDocument.toString())
    }

    private fun writeFile(params: Parameters, homes: String) {
        try {
            val writer = PrintWriter(params.template.replace("<index>", currentFile.toString()))
            writer.append(newDocument.toString())
            writer.close()
            currentFile++
        } catch (e: FileNotFoundException) {
            println("FileNotFoundException: $e" )
        } catch (e: SecurityException) {
            println("SecurityException: $e" )
        }
    }

    private fun appendAttribute(name: String, value: String, str: StringBuilder) : StringBuilder{
        if (!value.isBlank()) {
            str.append(" ").append(name).append("=\"").append(value).append("\"")
        }

        return str
    }
}
