package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.ScreenGuardCatalog
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseNavy
import com.example.ui.theme.iCaseRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

object PouchImageHelper {
    private const val MAX_FIRESTORE_VIDEO_BYTES = 350 * 1024
    private const val MAX_LOCAL_VIDEO_BYTES = 10 * 1024 * 1024

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

    /**
     * Reads a short picked video clip. Clips <= 350 KB are base64-encoded so they sync cleanly
     * via Firestore/Export; larger clips up to 10 MB are saved inside app filesDir for local playback.
     */
    fun saveShortVideoUriToUrl(context: Context, uri: Uri): Pair<String?, String?> {
        return try {
            val resolver = context.contentResolver
            val mime = resolver.getType(uri)?.takeIf { it.startsWith("video/") } ?: "video/mp4"
            val bytes = resolver.openInputStream(uri)?.use { input ->
                val out = ByteArrayOutputStream()
                val buf = ByteArray(16 * 1024)
                var total = 0
                while (true) {
                    val read = input.read(buf)
                    if (read <= 0) break
                    total += read
                    if (total > MAX_LOCAL_VIDEO_BYTES) {
                        return null to "Video is too large (>10 MB). Trim under 10 MB or paste a YouTube / MP4 link."
                    }
                    out.write(buf, 0, read)
                }
                out.toByteArray()
            } ?: return null to "Could not read selected video file."

            if (bytes.isEmpty()) {
                return null to "Selected video file is empty."
            }

            if (bytes.size <= MAX_FIRESTORE_VIDEO_BYTES) {
                val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                return "data:$mime;base64,$b64" to null
            }

            val dir = File(context.filesDir, "media_videos").apply { mkdirs() }
            val ext = when {
                mime.contains("webm") -> "webm"
                mime.contains("3gpp") -> "3gp"
                else -> "mp4"
            }
            val dest = File(dir, "vid_${System.currentTimeMillis()}.$ext")
            dest.writeBytes(bytes)
            Uri.fromFile(dest).toString() to "Saved on this device (>350 KB). For Website sync, use a YouTube or MP4 URL."
        } catch (_: Exception) {
            null to "Unable to import selected video."
        }
    }

    fun decodeVideoDataUriToTempFile(context: Context, dataUrl: String): File? {
        if (!dataUrl.startsWith("data:video", ignoreCase = true)) return null
        val commaIdx = dataUrl.indexOf(',')
        if (commaIdx < 0 || commaIdx >= dataUrl.length - 1) return null
        return try {
            val base64Part = dataUrl.substring(commaIdx + 1)
            val bytes = Base64.decode(base64Part, Base64.DEFAULT)
            val outFile = File(context.cacheDir, "preview_${dataUrl.hashCode()}.mp4")
            if (!outFile.exists() || outFile.length() != bytes.size.toLong()) {
                outFile.writeBytes(bytes)
            }
            outFile
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

@Composable
fun MediaVideoPlayerDialog(
    title: String,
    videoUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cleanUrl = videoUrl.trim()
    val isYouTube = ScreenGuardCatalog.isYouTubeUrl(cleanUrl)
    val isDataVideo = cleanUrl.startsWith("data:video", ignoreCase = true)

    var playbackUri by remember(cleanUrl) {
        mutableStateOf<Uri?>(if (!isYouTube && !isDataVideo && cleanUrl.isNotBlank()) Uri.parse(cleanUrl) else null)
    }
    var isPreparing by remember(cleanUrl) { mutableStateOf(isDataVideo) }
    var playbackError by remember(cleanUrl) { mutableStateOf(false) }

    LaunchedEffect(cleanUrl) {
        if (isDataVideo) {
            isPreparing = true
            val tempFile = withContext(Dispatchers.IO) {
                PouchImageHelper.decodeVideoDataUriToTempFile(context, cleanUrl)
            }
            playbackUri = tempFile?.let { Uri.fromFile(it) }
            playbackError = playbackUri == null
            isPreparing = false
        }
    }

    fun openExternally() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl))
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title.ifBlank { "Product Video" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = iCaseNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close Video", tint = Color(0xFF64748B))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("media_video_player_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isYouTube) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(iCaseRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play YouTube Video",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "YouTube Video Ready",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = cleanUrl,
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Button(
                            onClick = { openExternally() },
                            colors = ButtonDefaults.buttonColors(containerColor = iCaseRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 46.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Watch on YouTube / Browser", fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (isPreparing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else if (playbackUri != null && !playbackError) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    val controller = MediaController(ctx)
                                    controller.setAnchorView(this)
                                    setMediaController(controller)
                                    setVideoURI(playbackUri)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = false
                                        start()
                                    }
                                    setOnErrorListener { _, _, _ ->
                                        playbackError = true
                                        true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = iCaseBlue, modifier = Modifier.size(28.dp))
                        Text(
                            text = "Could not play directly inside preview.",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = iCaseNavy
                        )
                        if (cleanUrl.startsWith("http", ignoreCase = true)) {
                            OutlinedButton(onClick = { openExternally() }) {
                                Text("Open Video Link", fontWeight = FontWeight.Bold, color = iCaseBlue)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 44.dp)) {
                Text("Close", fontWeight = FontWeight.Bold, color = iCaseNavy)
            }
        }
    )
}

