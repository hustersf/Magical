package com.sofar.core.ai.edge.data.entity.llm

sealed interface LlmStreamToken {
  /**
   * 1. 状态一：普通文本片段
   * 当模型每生成一个新词（Token），底层回调触发，我们就会向协程流（Flow）发送这个状态。
   * text: 当前吐出的局部字符碎片。
   * thinkingText: 针对有“深度思考”能力的模型（如 Gemma 2/3），用来接收其思维链的内容（对应官方的 partialThinkingResult）。
   */
  data class PartialText(val text: String, val thinkingText: String?) : LlmStreamToken

  /**
   * 2. 状态二：推理出错
   * 对应官方回调的 onError(throwable)。一旦端侧因为内存挤爆、芯片报错或计算中断，
   * 我们通过这个状态把具体的异常（Throwable）向上抛给业务层。
   */
  data class Error(val throwable: Throwable) : LlmStreamToken

  /**
   * 3. 状态三：推理结束
   * 对应官方回调的 onDone()。代表大模型已经把所有的回答都吐完了。
   * 业务层（Repository）一收到这个通知，就知道该把拼装好的完整句式一次性存入 Room 数据库了。
   */
  object EndOfStream : LlmStreamToken
}
