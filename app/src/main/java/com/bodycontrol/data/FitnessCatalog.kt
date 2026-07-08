package com.bodycontrol.data

/** 健身操中的单个动作，图片为 assets 下的 GIF 动图。 */
data class FitnessMove(
    val title: String,
    /** assets 相对路径，例如 fitness/neck/1.gif。 */
    val asset: String,
) {
    val assetUri: String get() = "file:///android_asset/$asset"
}

/** 一套健身操系列。 */
data class FitnessSeries(
    val id: String,
    val title: String,
    val subtitle: String,
    val moves: List<FitnessMove>,
)

/** 内置健身操系列。动图已按系列归入 assets/fitness/<id>/ 下，序号即动作顺序。 */
object FitnessCatalog {

    private fun moves(dir: String, titles: List<String>): List<FitnessMove> =
        titles.mapIndexed { i, t -> FitnessMove(t, "fitness/$dir/${i + 1}.gif") }

    val series: List<FitnessSeries> = listOf(
        FitnessSeries(
            id = "shoulder",
            title = "肩背舒展",
            subtitle = "毛巾辅助开肩，放松肩颈背部",
            moves = moves("shoulder", listOf("动作一", "动作二", "动作三", "动作四", "动作五")),
        ),
        FitnessSeries(
            id = "neck",
            title = "颈椎五式",
            subtitle = "缓解颈椎僵硬的五个动作",
            moves = moves(
                "neck",
                listOf("合掌扩胸", "举臂拉伸", "叉腰侧展", "十指相扣", "头颈相争"),
            ),
        ),
        FitnessSeries(
            id = "home",
            title = "居家放松",
            subtitle = "床边即可完成的舒缓练习",
            moves = moves("home", listOf("动作一", "动作二", "动作三", "动作四")),
        ),
        FitnessSeries(
            id = "pilates",
            title = "户外普拉提",
            subtitle = "站姿塑形与核心激活",
            moves = moves(
                "pilates",
                listOf("动作一", "动作二", "动作三", "动作四", "动作五", "动作六"),
            ),
        ),
        FitnessSeries(
            id = "fullbody",
            title = "全身拉伸",
            subtitle = "一套完整的全身动态拉伸",
            moves = moves(
                "fullbody",
                listOf(
                    "提踵+肩部拉伸",
                    "宽蹲转体",
                    "后撤步过头伸展",
                    "站姿猫式伸展",
                    "宽蹲+手臂环绕",
                    "环绕世界",
                    "交叉步侧伸展",
                    "动态腘绳肌拉伸",
                    "原地踏步",
                ),
            ),
        ),
    )
}
