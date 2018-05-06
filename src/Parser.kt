import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.*
import javax.xml.parsers.SAXParserFactory

private val BOM = '\uFEFF'

fun parseXml(params: Parameters) {
    try {
        val file = File(params.source)
        val reader = file.bufferedReader()
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()
        removeBOM(reader)
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

fun removeBOM(reader: BufferedReader) {
    reader.mark(1)
    val possibleBOM = CharArray(1)
    reader.read(possibleBOM)
    if (possibleBOM[0] != BOM) {
        reader.reset()
    }
}

class CompilerOutputSAXHandler(val fileSize: Long, val params: Parameters) : DefaultHandler(){

    private var newDocument : StringBuilder = StringBuilder()
    private val MAX_SIZE_BATCH : Int = 1024 * 1024
    var currentSize : Int = 0
    var currentSizeBatch : Int = 0
    var currentFile : Int = 1
    var currentProcent : Int = 0
    var isCreate : Boolean = true

    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        if (newDocument.toString().isBlank()) {
            newDocument.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
                    .append("<Houses>\n")
            currentSize = newDocument.toString().toByteArray().size
            currentSizeBatch = currentSize
        }

        var newHouses : StringBuilder = StringBuilder()
        if (qName.equals("House")) {
            newHouses.append("\t<House")
            for (i in 0..(attributes.length-1)) {
                newHouses = appendAttribute(attributes.getQName(i), attributes.getValue(i), newHouses)
            }
            newHouses.append("/>\n")
            if (currentSizeBatch >= MAX_SIZE_BATCH) {
                writeFile(params, newDocument.toString(), true)
                currentSizeBatch = 0
                newDocument = StringBuilder()
            }
            if (newHouses.toString().toByteArray().size + currentSize > params.size.getSizeInByte()) {
                newDocument.append("</Houses>")
                writeFile(params, newDocument.toString())
                newDocument = StringBuilder()
                currentSize = 0
                currentSizeBatch = 0
                printProcent()
            } else {
                newDocument.append(newHouses.toString())
                currentSize += newHouses.toString().toByteArray().size
                currentSizeBatch = newDocument.toString().toByteArray().size
                printProcent()
            }
        }
    }

    private fun printProcent() {
        val newProcent = 100L * ((currentFile-1)*params.size.getSizeInByte() + currentSize ) / fileSize
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

    private fun writeFile(params: Parameters, homes: String, isBatch : Boolean = false) {
        try {
            val writer = if (isCreate) {
                isCreate = false
                PrintWriter(params.template.replace("<index>", currentFile.toString()))
            } else {
                PrintWriter(OutputStreamWriter(FileOutputStream(
                        params.template.replace("<index>", currentFile.toString())
                        , true)))
            }
            writer.append(homes)
            writer.close()
            if (!isBatch) {
                currentFile++
                isCreate = true
            }
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
