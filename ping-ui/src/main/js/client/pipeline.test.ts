import { describe, expect, it } from "vitest"
import { compose, type Handler } from "./pipeline"

describe("compose 洋葱执行器", () => {
  it("执行顺序 = 数组顺序（chain[0] 最外层，下行先于内层）", async () => {
    const order: string[] = []
    const a: Handler = async (_ctx, next) => {
      order.push("a-down")
      const r = await next()
      order.push("a-up")
      return r
    }
    const b: Handler = async (_ctx, next) => {
      order.push("b-down")
      const r = await next()
      order.push("b-up")
      return r
    }
    const terminal: Handler = async (_ctx) => {
      order.push("terminal")
      return "data"
    }

    const result = await compose([a, b, terminal])({})

    expect(order).toEqual(["a-down", "b-down", "terminal", "b-up", "a-up"])
    expect(result).toBe("data")
  })

  it("短路：不调 next 不触发下一环，短路值即链兑现值", async () => {
    let innerCalled = false
    const short: Handler = async () => "cached"
    const inner: Handler = async (_ctx, next) => {
      innerCalled = true
      return next()
    }

    const result = await compose([short, inner])({})

    expect(result).toBe("cached")
    expect(innerCalled).toBe(false)
  })

  it("return 值逐层冒泡：每环 await next() 后可加工数据", async () => {
    const upper: Handler = async (_ctx, next) => {
      const data = (await next()) as { n: number }
      return { n: data.n + 1 }
    }
    const lower: Handler = async (_ctx, next) => {
      const data = (await next()) as { n: number }
      return { n: data.n * 10 }
    }
    const terminal: Handler = async () => ({ n: 1 })

    // 上行逆序加工：1 → ×10=10 → +1=11
    expect(await compose([upper, lower, terminal])({})).toEqual({ n: 11 })
  })

  it("next() 重复调用以失败拒绝", async () => {
    const bad: Handler = async (_ctx, next) => {
      await next()
      return next()
    }
    await expect(compose([bad])({})).rejects.toThrow("next() 被重复调用")
  })

  it("链尽头无人 return 兑现 undefined", async () => {
    const pass: Handler = async (_ctx, next) => next()

    expect(await compose([pass, pass, pass])({})).toBeUndefined()
  })

  it("Handler 异常原样透传不包装（同步与异步抛出均成立）", async () => {
    const boom = new TypeError("real bug")
    const syncThrow: Handler = () => {
      throw boom
    }
    const asyncThrow: Handler = async () => {
      throw boom
    }

    await expect(compose([syncThrow])({})).rejects.toBe(boom)
    await expect(compose([asyncThrow])({})).rejects.toBe(boom)
  })

  it("深链（5 层）洋葱语义正确：下行 0→4，上行 4→0", async () => {
    const trace: string[] = []
    const make = (name: string): Handler => async (_ctx, next) => {
      trace.push(`${name}-down`)
      const r = await next()
      trace.push(`${name}-up`)
      return r
    }
    const terminal: Handler = async () => {
      trace.push("terminal")
      return "ok"
    }

    const result = await compose(
      [make("h0"), make("h1"), make("h2"), make("h3"), terminal],
    )({})

    expect(result).toBe("ok")
    expect(trace).toEqual([
      "h0-down", "h1-down", "h2-down", "h3-down", "terminal",
      "h3-up", "h2-up", "h1-up", "h0-up",
    ])
  })

  it("相同袋子贯穿整链：各环读写同一 Context 对象", async () => {
    const writer: Handler = async (ctx, next) => {
      ctx.operationId = "written"
      return next()
    }
    const reader: Handler = async (ctx) => ctx.operationId

    expect(await compose([writer, reader])({})).toBe("written")
  })

  it("compose 入参校验：非数组或含非函数即抛", () => {
    expect(() => compose(undefined as unknown as Handler[])).toThrow("compose 需要数组")
    expect(() => compose([null as unknown as Handler])).toThrow("必须是函数")
  })
})
