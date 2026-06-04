package com.sofar.core.ai.edge.data.entity.llm

enum class Accelerator(val label: String) {
  CPU(label = "CPU"),
  GPU(label = "GPU"),
  NPU(label = "NPU"),
}