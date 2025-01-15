import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

fun saveBitmapAsUri(context: Context, bitmap: Bitmap): Uri? {
    return try {
        // Crea una directory privata per le immagini dell'app se non esiste
        val imagesDir = File(context.filesDir, "manga_images").apply {
            if (!exists()) {
                mkdirs()
            }
        }

        // Crea un file con nome univoco
        val imageFile = File(imagesDir, "manga_${UUID.randomUUID()}.jpg")

        // Salva il bitmap nel file
        FileOutputStream(imageFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }

        // Converti il percorso del file in URI
        Uri.fromFile(imageFile)
    } catch (e: IOException) {
        Log.e("SaveImage", "Error saving image", e)
        null
    }
}

// Funzione aggiuntiva per copiare un'immagine dalla galleria
fun copyImageToAppStorage(context: Context, sourceUri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(sourceUri)
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        saveBitmapAsUri(context, bitmap)
    } catch (e: IOException) {
        Log.e("CopyImage", "Error copying image", e)
        null
    }
}