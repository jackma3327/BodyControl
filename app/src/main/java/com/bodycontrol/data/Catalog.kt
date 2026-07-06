package com.bodycontrol.data

import com.bodycontrol.R

/** 一个可练习条目，可选带背景音乐（res/raw 资源）。 */
data class TrackItem(
    val id: String,
    val title: String,
    val description: String,
    val rawResId: Int? = null,
)

/** 分类：瑜伽 / 气功 / 呼吸法 / 动作库。 */
data class Category(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconKey: String,
    val items: List<TrackItem>,
)

/** 全部内置内容。现有音乐已按分类归入对应条目。 */
object Catalog {
    val categories: List<Category> = listOf(
        Category(
            id = "yoga",
            title = "瑜伽",
            subtitle = "体式流动与冥想唱诵",
            iconKey = "yoga",
            items = listOf(
                TrackItem(
                    id = "yoga_sun_108",
                    title = "拜日式 108 遍（纯练习版）",
                    description = "循环引导，适合完整拜日式长时练习。",
                    rawResId = R.raw.sun_salutation_108,
                ),
                TrackItem(
                    id = "yoga_wahe_guru",
                    title = "Epic Wahe Guru",
                    description = "昆达里尼冥想唱诵，适合冥想与深度放松。",
                    rawResId = R.raw.epic_wahe_guru,
                ),
            ),
        ),
        Category(
            id = "qigong",
            title = "气功",
            subtitle = "站桩与传统功法配乐口令",
            iconKey = "qigong",
            items = listOf(
                TrackItem(
                    id = "qg_zhanzhuang",
                    title = "站桩",
                    description = "静心站立，配乐辅助放松入静。",
                    rawResId = R.raw.zhan_zhuang,
                ),
                TrackItem(
                    id = "qg_baduanjin",
                    title = "八段锦",
                    description = "八段锦完整配乐口令，跟随节奏练习。",
                    rawResId = R.raw.ba_duan_jin,
                ),
                TrackItem(
                    id = "qg_babujingang",
                    title = "八部金刚功",
                    description = "八部金刚功配乐口令，循序习练。",
                    rawResId = R.raw.ba_bu_jin_gang_gong,
                ),
            ),
        ),
        Category(
            id = "breath",
            title = "呼吸法",
            subtitle = "调息与放松呼吸练习",
            iconKey = "breath",
            items = listOf(
                TrackItem(
                    id = "br_belly",
                    title = "腹式呼吸",
                    description = "吸气时腹部隆起，呼气时腹部回落，每次 5–10 分钟。",
                ),
                TrackItem(
                    id = "br_478",
                    title = "4-7-8 呼吸法",
                    description = "吸气 4 秒、屏息 7 秒、呼气 8 秒，助眠减压。",
                ),
                TrackItem(
                    id = "br_box",
                    title = "箱式呼吸",
                    description = "吸—屏—呼—屏，各 4 秒，稳定情绪与专注。",
                ),
            ),
        ),
        Category(
            id = "moves",
            title = "动作库",
            subtitle = "基础拉伸与稳定练习",
            iconKey = "moves",
            items = listOf(
                TrackItem(
                    id = "mv_neck",
                    title = "颈肩放松",
                    description = "缓解久坐带来的颈肩僵硬的基础拉伸。",
                ),
                TrackItem(
                    id = "mv_spine",
                    title = "脊柱灵活",
                    description = "猫牛式与脊柱扭转，激活并放松脊柱。",
                ),
                TrackItem(
                    id = "mv_core",
                    title = "核心稳定",
                    description = "平板支撑与死虫式，强化核心力量。",
                ),
                TrackItem(
                    id = "mv_legs",
                    title = "下肢拉伸",
                    description = "髋部与腿部拉伸，改善柔韧度。",
                ),
            ),
        ),
    )
}
