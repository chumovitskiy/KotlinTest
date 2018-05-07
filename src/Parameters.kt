data class Parameters(var help: Boolean = false, var source: String = "HOUSE.xml",
                      var template: String = "outputHOUSE<index>.xml", var size: Size = Size())

data class Size (val count: Int = 10, val type: SizeType = SizeType.M) {
    fun getSizeInByte() : Int {
        var result = 0
        when (type) {
            SizeType.b -> result = count
            SizeType.K -> result = count * 1024
            SizeType.M -> result = count * 1024 * 1024
            SizeType.G -> result = count * 1024 * 1024 * 1024
        }

        return result
    }
}

enum class SizeType {
    b, K, M, G
}