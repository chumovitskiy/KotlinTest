fun main (args: Array<String>) {
    var flagName : String = ""
    var isError : Boolean = false
    var params: Parameters = Parameters()
    for (arg in args) {
        if (isError) {
            break
        }
        if (arg.startsWith("-")) {
            //flag
            when(arg) {
                "--help", "-h" -> params.help=true
                "--source", "-sf" -> flagName="sf"
                "--template", "-t" -> flagName="t"
                "--size", "-s" -> flagName="s"
                else -> isError=true
            }
        } else {
            //value
            if (flagName.isBlank()) {
                println("Incorrect input!")
                isError = true
            } else {
                try {
                    when (flagName) {
                        "sf" -> params.source = arg
                        "t" -> params.template = arg
                        "s" -> {
                            when (arg.substring(arg.length - 1, arg.length)) {
                                "b", "B" -> params.size = Size(arg.substring(0, arg.length - 1).toInt(), SizeType.b)
                                "k", "K" -> params.size = Size(arg.substring(0, arg.length - 1).toInt(), SizeType.K)
                                "m", "M" -> params.size = Size(arg.substring(0, arg.length - 1).toInt(), SizeType.M)
                                "g", "G" -> params.size = Size(arg.substring(0, arg.length - 1).toInt(), SizeType.G)
                                else -> params.size = Size(arg.substring(0, arg.length - 1).toInt(), SizeType.b)
                            }
                        }
                        else -> isError = true
                    }
                } catch (e:  NumberFormatException) {
                    println("Incorrect size")
                    isError = true
                }
            }
        }
    }
    if (isError || params.help == true || args.isEmpty()) {
        printHelp()
        return
    }
    parseXml(params)

}

fun printHelp() {
    println("Arguments:\n" +
            "--help or -h are help text\n" +
            "--source <file name> or -sf <file name> are path to source file. Default HOUSE.xml.\n" +
            "--template <template> or -t <template> are path + template name output file. Default outputHOUSE<index>.xml.\n" +
            "--size <size> or -s <size> are max size output files. Default 10M. Example: 10M - 10 Megabyte, 500K - 500 Kilobyte. 256 - 256 byte.\n" +
            "Example:\n" +
            "1. parseXml -h\n" +
            "2. parseXml --size 100M\n" +
            "3. parseXml --source HOUSE.xml -t out.xml -s 500k")
}
