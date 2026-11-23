package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.ScreenGuardCatalog
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

object PouchImageHelper {
    fun createCameraTempUri(context: Context): Uri {
        val file = File(context.cacheDir, "pouch_cam_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun compressImageUriToDataUrl(
        context: Context,
        uri: Uri,
        maxDimension: Int = 420,
        quality: Int = 72
    ): String? {
        return try {
            val resolver = context.contentResolver

            // 1. Inspect bounds without loading pixels into RAM
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            val srcW = boundsOpts.outWidth
            val srcH = boundsOpts.outHeight
            if (srcW <= 0 || srcH <= 0) return null

            var sampleSize = 1
            while ((srcW / sampleSize) > maxDimension * 2 || (srcH / sampleSize) > maxDimension * 2) {
                sampleSize *= 2
            }

            // 2. Decode downsampled bitmap
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: return null

            // 3. Apply EXIF orientation if present
            val orientation = try {
                resolver.openInputStream(uri)?.use { exifStream ->
                    ExifInterface(exifStream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (_: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            val oriented = applyExifOrientation(decoded, orientation)
            if (oriented !== decoded) decoded.recycle()

            // 4. Exact scale down to maxDimension on longest side
            val longest = max(oriented.width, oriented.height)
            val finalBitmap = if (longest > maxDimension) {
                val ratio = maxDimension.toFloat() / longest.toFloat()
                val targetW = (oriented.width * ratio).roundToInt().coerceAtLeast(1)
                val targetH = (oriented.height * ratio).roundToInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(oriented, targetW, targetH, true)
                if (scaled !== oriented) oriented.recycle()
                scaled
            } else {
                oriented
            }

            // 5. Compress JPEG and keep Firestore doc footprint compact (~15-60 KB)
            val baos = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            var bytes = baos.toByteArray()
            if (bytes.size > 90_000) {
                baos.reset()
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 52, baos)
                bytes = baos.toByteArray()
            }
            finalBitmap.recycle()

            val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:image/jpeg;base64,$b64"
        } catch (_: Exception) {
            null
        }
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (_: Exception) {
            bitmap
        }
    }

    fun decodeDataUriBitmap(dataUrl: String): ImageBitmap? {
        if (!dataUrl.startsWith("data:image", ignoreCase = true)) return null
        val commaIdx = dataUrl.indexOf(',')
        if (commaIdx < 0 || commaIdx >= dataUrl.length - 1) return null
        return try {
            val base64Part = dataUrl.substring(commaIdx + 1)
            val bytes = Base64.decode(base64Part, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
fun PouchThumbImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val cleanUrl = imageUrl.trim()
    val isDataUri = cleanUrl.startsWith("data:image", ignoreCase = true)
    val decodedBitmap = remember(cleanUrl) {
        if (isDataUri) PouchImageHelper.decodeDataUriBitmap(cleanUrl) else null
    }

    when {
        isDataUri && decodedBitmap != null -> {
            Image(
                bitmap = decodedBitmap,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        isDataUri && decodedBitmap == null -> {
            Box(
                modifier = modifier.background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = contentDescription,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        else -> {
            AsyncImage(
                model = cleanUrl.ifBlank { ScreenGuardCatalog.POUCH_IMAGE_URL },
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
    }
}
