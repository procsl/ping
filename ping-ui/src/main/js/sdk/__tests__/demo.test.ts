import { describe, expect, it } from "vitest"
import { createPingUi } from "../index"
import { demoTree, registerDemoHandlers } from "../demo/demo-tree"

describe("demo 三条路径", () => {
  it("正常命中、未注册降级、抛错隔离均产生预期输出", () => {
    const sdk = createPingUi({ namespace: "demo" })
    sdk.init()
    const output = registerDemoHandlers(sdk)
    sdk.run(demoTree)

    // 正常命中：card / row / text 均被渲染
    expect(output.rendered).toContain("card:demo-root")
    expect(output.rendered).toContain("row:row-1")
    expect(output.rendered).toContain("text:text-1")
    expect(output.rendered).toContain("text:text-2")

    // 未注册降级：unknown_widget 走 error_handler，子节点保留
    expect(output.errors.some((e) => e.startsWith("unknown_widget:"))).toBe(true)
    expect(output.rendered).toContain("text:text-3")

    // 抛错隔离：broken 抛错被捕获，同级 text-4 照常执行
    expect(output.rendered).toContain("broken:broken-1")
    expect(output.errors.some((e) => e.startsWith("broken:"))).toBe(true)
    expect(output.rendered).toContain("text:text-4")
  })
})
