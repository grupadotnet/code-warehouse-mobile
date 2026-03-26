package pk.knpmi.barcode.common

/**
 * A generic wrapper for handling data states between the API and the UI.
 * It helps track whether data is successfully loaded, failed, or currently fetching.
 */
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}