package cn.zhzgo.study.utils

import java.time.LocalTime
import kotlin.random.Random

object GreetingUtils {
    private val lateNightGreetings = listOf(
        "夜深了",
        "还没睡呀",
        "该休息了",
        "披星戴月",
        "熬夜中"
    )

    private val earlyMorningGreetings = listOf(
        "早安",
        "清晨好",
        "起真早",
        "晨光熹微",
        "新的一天"
    )

    private val morningGreetings = listOf(
        "上午好",
        "元气满满",
        "奋斗中",
        "加油呀",
        "学习中"
    )

    private val noonGreetings = listOf(
        "中午好",
        "该吃饭了",
        "午间好",
        "歇一会",
        "吃饱没"
    )

    private val afternoonGreetings = listOf(
        "下午好",
        "坚持住",
        "不负时光",
        "喝杯茶",
        "努力中"
    )

    private val eveningGreetings = listOf(
        "傍晚好",
        "夕阳美",
        "快下课了",
        "天快黑了",
        "休息下"
    )

    private val nightGreetings = listOf(
        "晚上好",
        "辛苦了",
        "该复盘了",
        "月色好",
        "静下心"
    )

    fun getGreeting(): String {
        val hour = LocalTime.now().hour
        val greetings = when (hour) {
            in 0..4 -> lateNightGreetings
            in 5..8 -> earlyMorningGreetings
            in 9..11 -> morningGreetings
            in 12..13 -> noonGreetings
            in 14..17 -> afternoonGreetings
            in 18..19 -> eveningGreetings
            else -> nightGreetings
        }
        return greetings[Random.nextInt(greetings.size)]
    }
}
