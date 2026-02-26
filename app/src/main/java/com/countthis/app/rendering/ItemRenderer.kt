package com.countthis.app.rendering

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.widget.FrameLayout
import android.widget.ImageView
import com.caverock.androidsvg.SVG
import com.countthis.app.R
import com.countthis.app.enums.ItemTheme
import com.countthis.app.enums.PatternMode
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.random.Random

class ItemRenderer(private val context: Context) {
    private data class ItemAsset(
        val drawableRes: Int? = null,
        val svgRawRes: Int? = null
    )

    private val animalDrawables = listOf(
        R.drawable.ic_cat,
        R.drawable.ic_dog,
        R.drawable.ic_bird,
        R.drawable.ic_fish,
        R.drawable.ic_butterfly,
        R.drawable.ic_rabbit,
        R.drawable.ic_turtle,
        R.drawable.ic_star
    )

    private val shapeDrawables = listOf(
        R.drawable.ic_circle,
        R.drawable.ic_square,
        R.drawable.ic_triangle,
        R.drawable.ic_hexagon
    )

    private val funnySvgAssets = listOf(
        ItemAsset(svgRawRes = R.raw.funny_81398),
        ItemAsset(svgRawRes = R.raw.funny_36564),
        ItemAsset(svgRawRes = R.raw.funny_81394),
        ItemAsset(svgRawRes = R.raw.funny_87694),
        ItemAsset(svgRawRes = R.raw.funny_25626),
        ItemAsset(svgRawRes = R.raw.funny_67049),
        ItemAsset(svgRawRes = R.raw.funny_39121),
        ItemAsset(svgRawRes = R.raw.funny_87962)
    )

    private val svgDrawableCache = mutableMapOf<Int, PictureDrawable>()

    fun renderItems(
        container: FrameLayout,
        count: Int,
        theme: ItemTheme,
        pattern: PatternMode
    ) {
        container.removeAllViews()

        val containerWidth = container.width.toFloat()
        val containerHeight = container.height.toFloat()

        if (containerWidth <= 0 || containerHeight <= 0) {
            container.post { renderItems(container, count, theme, pattern) }
            return
        }

        when (pattern) {
            PatternMode.SCATTERED -> renderScattered(container, count, theme, containerWidth, containerHeight)
            PatternMode.GRID -> renderGrid(container, count, theme, containerWidth, containerHeight)
            PatternMode.CLUSTERED_5 -> renderClustered(container, count, theme, containerWidth, containerHeight, 5)
            PatternMode.CLUSTERED_10 -> renderClustered(container, count, theme, containerWidth, containerHeight, 10)
            PatternMode.MIXED_ITEMS -> renderMixed(container, count, theme, containerWidth, containerHeight)
        }
    }

    private fun renderScattered(
        container: FrameLayout,
        count: Int,
        theme: ItemTheme,
        containerWidth: Float,
        containerHeight: Float
    ) {
        val selectedAsset = getRandomAsset(theme)
        val usedPositions = mutableListOf<Pair<Float, Float>>()

        val itemSize = calculateItemSize(count)
        val minDistance = itemSize * 1.15f
        val maxAttempts = if (count > 50) 200 else 100

        for (i in 0 until count) {
            val imageView = createImageView(selectedAsset, itemSize)

            var position: Pair<Float, Float>
            var attempts = 0

            do {
                val x = Random.nextFloat() * (containerWidth - itemSize)
                val y = Random.nextFloat() * (containerHeight - itemSize)
                position = Pair(x, y)
                attempts++

                if (attempts >= maxAttempts) break
            } while (usedPositions.any { isOverlapping(it, position, minDistance) })

            usedPositions.add(position)

            imageView.x = position.first
            imageView.y = position.second

            container.addView(imageView)
        }
    }

    private fun renderGrid(
        container: FrameLayout,
        count: Int,
        theme: ItemTheme,
        containerWidth: Float,
        containerHeight: Float
    ) {
        val selectedAsset = getRandomAsset(theme)

        val columns = ceil(sqrt(count.toDouble())).toInt()
        val rows = ceil(count.toDouble() / columns).toInt()

        val itemSize = minOf(
            calculateItemSize(count),
            (containerWidth / columns) * 0.8f,
            (containerHeight / rows) * 0.8f
        )

        val cellWidth = containerWidth / columns
        val cellHeight = containerHeight / rows

        var itemIndex = 0
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                if (itemIndex >= count) break

                val imageView = createImageView(selectedAsset, itemSize)

                // Center item in cell with slight random offset
                val centerX = col * cellWidth + (cellWidth - itemSize) / 2
                val centerY = row * cellHeight + (cellHeight - itemSize) / 2

                val offsetX = Random.nextFloat() * 10 - 5
                val offsetY = Random.nextFloat() * 10 - 5

                imageView.x = centerX + offsetX
                imageView.y = centerY + offsetY

                container.addView(imageView)
                itemIndex++
            }
        }
    }

    private fun renderClustered(
        container: FrameLayout,
        count: Int,
        theme: ItemTheme,
        containerWidth: Float,
        containerHeight: Float,
        clusterSize: Int
    ) {
        val selectedAsset = getRandomAsset(theme)
        val itemSize = calculateItemSize(count)

        val numClusters = ceil(count.toDouble() / clusterSize).toInt()
        val clusterCenters = mutableListOf<Pair<Float, Float>>()

        // Generate cluster centers
        val minClusterDistance = itemSize * clusterSize * 0.5f
        for (i in 0 until numClusters) {
            var center: Pair<Float, Float>
            var attempts = 0

            do {
                val x = Random.nextFloat() * (containerWidth - itemSize * 3) + itemSize * 1.5f
                val y = Random.nextFloat() * (containerHeight - itemSize * 3) + itemSize * 1.5f
                center = Pair(x, y)
                attempts++

                if (attempts >= 50) break
            } while (clusterCenters.any { isOverlapping(it, center, minClusterDistance) })

            clusterCenters.add(center)
        }

        // Place items around cluster centers
        var itemIndex = 0
        for ((clusterIndex, center) in clusterCenters.withIndex()) {
            val itemsInThisCluster = minOf(clusterSize, count - itemIndex)

            for (i in 0 until itemsInThisCluster) {
                val imageView = createImageView(selectedAsset, itemSize)

                // Place items in a circle around cluster center
                val angle = (i.toFloat() / itemsInThisCluster) * 2 * Math.PI
                val radius = itemSize * 0.8f

                val offsetX = (radius * kotlin.math.cos(angle)).toFloat()
                val offsetY = (radius * kotlin.math.sin(angle)).toFloat()

                imageView.x = center.first + offsetX
                imageView.y = center.second + offsetY

                container.addView(imageView)
                itemIndex++
            }
        }
    }

    private fun renderMixed(
        container: FrameLayout,
        count: Int,
        theme: ItemTheme,
        containerWidth: Float,
        containerHeight: Float
    ) {
        val assets = getThemeAssets(theme)
        val targetAsset = assets.random()
        val targetCount = Random.nextInt(
            maxOf(1, count / 3),
            maxOf(2, (count * 2) / 3)
        )

        val usedPositions = mutableListOf<Pair<Float, Float>>()
        val itemSize = calculateItemSize(count)
        val minDistance = itemSize * 1.15f
        val maxAttempts = if (count > 50) 200 else 100

        var targetPlaced = 0
        for (i in 0 until count) {
            val asset = if (targetPlaced < targetCount) {
                targetPlaced++
                targetAsset
            } else {
                (assets - targetAsset).ifEmpty { assets }.random()
            }

            val imageView = createImageView(asset, itemSize)

            var position: Pair<Float, Float>
            var attempts = 0

            do {
                val x = Random.nextFloat() * (containerWidth - itemSize)
                val y = Random.nextFloat() * (containerHeight - itemSize)
                position = Pair(x, y)
                attempts++

                if (attempts >= maxAttempts) break
            } while (usedPositions.any { isOverlapping(it, position, minDistance) })

            usedPositions.add(position)

            imageView.x = position.first
            imageView.y = position.second

            container.addView(imageView)
        }
    }

    private fun calculateItemSize(count: Int): Float {
        return when {
            count <= 20 -> 120f
            count <= 35 -> 90f
            count <= 50 -> 70f
            count <= 75 -> 55f
            else -> 45f
        }
    }

    private fun createImageView(asset: ItemAsset, size: Float): ImageView {
        val imageView = ImageView(context)
        imageView.layoutParams = FrameLayout.LayoutParams(
            size.toInt(),
            size.toInt()
        )
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER

        val drawableRes = asset.drawableRes
        val svgRawRes = asset.svgRawRes

        if (drawableRes != null) {
            imageView.setImageResource(drawableRes)
        } else if (svgRawRes != null) {
            val svgDrawable = getSvgDrawable(svgRawRes)
            if (svgDrawable != null) {
                imageView.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null)
                imageView.setImageDrawable(svgDrawable)
            } else {
                imageView.setImageResource(R.drawable.ic_star)
            }
        } else {
            imageView.setImageResource(R.drawable.ic_star)
        }

        return imageView
    }

    private fun getRandomAsset(theme: ItemTheme): ItemAsset {
        return getThemeAssets(theme).random()
    }

    private fun getThemeAssets(theme: ItemTheme): List<ItemAsset> {
        return when (theme) {
            ItemTheme.ANIMALS -> animalDrawables.map { ItemAsset(drawableRes = it) }
            ItemTheme.SHAPES -> shapeDrawables.map { ItemAsset(drawableRes = it) }
            ItemTheme.FRUITS, ItemTheme.EMOJI, ItemTheme.NUMBERS -> funnySvgAssets
        }
    }

    private fun getSvgDrawable(rawRes: Int): PictureDrawable? {
        return svgDrawableCache[rawRes] ?: run {
            try {
                context.resources.openRawResource(rawRes).use { input ->
                    val svg = SVG.getFromInputStream(input)
                    val picture = svg.renderToPicture()
                    val drawable = PictureDrawable(picture)
                    svgDrawableCache[rawRes] = drawable
                    drawable
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun isOverlapping(pos1: Pair<Float, Float>, pos2: Pair<Float, Float>, minDist: Float): Boolean {
        val dx = pos1.first - pos2.first
        val dy = pos1.second - pos2.second
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        return distance < minDist
    }
}
