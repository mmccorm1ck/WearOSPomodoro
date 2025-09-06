package com.example.pomodoro.tile

import android.content.Context
import androidx.wear.protolayout.ColorBuilders.ColorProp
import androidx.wear.protolayout.DimensionBuilders.DegreesProp
import androidx.wear.protolayout.DimensionBuilders.DpProp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.ARC_ANCHOR_START
import androidx.wear.protolayout.LayoutElementBuilders.Arc
import androidx.wear.protolayout.LayoutElementBuilders.ArcLine
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.STROKE_CAP_BUTT
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.layouts.EdgeContentLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService


private const val RESOURCES_VERSION = "0"

val SECTION_GREEN = ColorProp.Builder(-936181966).build()
val SECTION_RED = ColorProp.Builder(-922799566).build()
val SECTION_BLUE = ColorProp.Builder(-936234241).build()

const val WORK_LENGTH = 25
const val BREAK_LENGTH = 5
const val REST_LENGTH = 30
const val CYCLE_NO = 3
const val CURRENT_TIME = 10 // Stand-in for timer

/**
 * Skeleton for a tile with no images.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ) = resources(requestParams)

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ) = tile(requestParams, this)
}

private fun resources(
    requestParams: RequestBuilders.ResourcesRequest
): ResourceBuilders.Resources {
    return ResourceBuilders.Resources.Builder()
        .setVersion(RESOURCES_VERSION)
        .build()
}

private fun tile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): TileBuilders.Tile {
    val singleTileTimeline = TimelineBuilders.Timeline.Builder()
        .addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setLayout(
                    LayoutElementBuilders.Layout.Builder()
                        .setRoot(tileLayout(requestParams, context))
                        .build()
                )
                .build()
        )
        .build()

    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(singleTileTimeline)
        .build()
}

private fun tileLayout(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): LayoutElement {
    val totalTime = REST_LENGTH + (WORK_LENGTH + BREAK_LENGTH) * (CYCLE_NO -1) + WORK_LENGTH
    val workAngle = WORK_LENGTH * 180f / totalTime
    val breakAngle = BREAK_LENGTH * 180f / totalTime
    val restAngle = REST_LENGTH * 180f / totalTime
    val currentAngle = CURRENT_TIME * 180f /totalTime
    val sectionBox = Box.Builder() // Box containing phase segments
    var curAngle = 0f
    for (i in 1..<CYCLE_NO) { // Add work/break sections
        sectionBox.addContent(
            makeSection(curAngle, workAngle, SECTION_RED)
        )
        curAngle += workAngle
        sectionBox.addContent(
            makeSection(curAngle, breakAngle, SECTION_GREEN)
        )
        curAngle += breakAngle
    }
    sectionBox.addContent( // Add final work section
        makeSection(curAngle, workAngle, SECTION_RED)
    )
    curAngle += workAngle
    sectionBox.addContent( // Add rest section
        makeSection(curAngle, restAngle, SECTION_BLUE)
    )
    val handBox = Box.Builder() // Box containing timer hand
        .addContent(
            makeHand(currentAngle)
        )
    return EdgeContentLayout.Builder(requestParams.deviceConfiguration)
        .setResponsiveContentInsetEnabled(true)
        .setEdgeContentBehindAllOtherContent(true)
        .setEdgeContent(
            sectionBox.build()
        )
        .setContent(
            handBox.build()
        )
        .build()
}

fun makeSection(start: Float, length: Float, color: ColorProp) : LayoutElement {
    return Arc.Builder()
        .setAnchorType(ARC_ANCHOR_START)
        .setAnchorAngle(
            DegreesProp.Builder(start).build()
        )
        .addContent(
            ArcLine.Builder()
                .setLength(
                    DegreesProp.Builder(length).build()
                )
                .setColor(color)
                .setThickness(
                    DpProp.Builder(70f).build()
                )
                .setStrokeCap(STROKE_CAP_BUTT)
                .build()
        )
        .build()
}

fun makeHand(angle: Float) : LayoutElement {
    val handBox = Box.Builder()
        .addContent(
            Arc.Builder()
                .addContent(
                    ArcLine.Builder()
                        .setColor(
                            ColorProp.Builder(-1).build()
                        )
                        .setLength(
                            DegreesProp.Builder(8f).build()
                        )
                        .setThickness(
                            DpProp.Builder(90f).build()
                        )
                        .setStrokeCap(STROKE_CAP_BUTT)
                        .build()
                )
                .setAnchorAngle(
                    DegreesProp.Builder(angle).build()
                )
                .build()

        )
    val centreBox = Box.Builder()
        .setHeight(DpProp.Builder(21f).build())
        .setWidth(DpProp.Builder(21f).build())
        .addContent(
            Arc.Builder()
                .addContent(
                    ArcLine.Builder()
                        .setColor(
                            ColorProp.Builder(-1).build()
                        )
                        .setLength(
                            DegreesProp.Builder(360f).build()
                        )
                        .setThickness(
                            DpProp.Builder(10f).build()
                        )
                        .build()
                )
                .build()
    )
    return Box.Builder()
        .addContent(
            handBox.build()
        )
        .addContent(
            centreBox.build()
        )
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData(::resources) {
    tile(it, context)
}