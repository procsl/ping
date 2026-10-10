import { useState } from "react"
import type { ReactNode } from "react"
import type { ComponentNode } from "@/schema/types"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { cn } from "@/lib/utils"

export interface FieldProps {
  node: ComponentNode
}

export interface FormProps {
  node: ComponentNode
  render: (node: ComponentNode) => ReactNode
}

/** 已映射到具体控件的 widget；不在表中的一律降级占位（规范手册 §5.4） */
const KNOWN_WIDGETS = new Set([
  "input",
  "textarea",
  "number",
  "select",
  "radio",
  "checkbox",
  "switch",
  "date_picker",
  "date_range",
  "upload",
  "rate",
  "slider",
])

const CONTROL_CLASS = cn(
  "flex w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm",
  "shadow-xs transition-colors placeholder:text-muted-foreground",
  "focus-visible:border-ring focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-50",
)

const UNSUPPORTED_WIDGETS = new Set(["rate", "slider", "upload", "date_range"])

function FieldLabel({ node, htmlFor }: { node: ComponentNode; htmlFor: string }) {
  return (
    <Label htmlFor={htmlFor}>
      {node.label ?? node.name}
      {node.required && <span className="ml-0.5 text-destructive">*</span>}
    </Label>
  )
}

/**
 * 表单字段：按 widget 映射到既有原子组件 / 原生控件。
 * 未映射的 widget 降级为占位框并告警，不阻断整表。
 */
export function Field({ node }: FieldProps): ReactNode {
  const name = node.name
  if (!name) {
    console.warn(`[ping-ui] 字段缺少 name，已跳过：${node.id ?? node.type}`)
    return null
  }

  const widget = node.widget ?? "input"
  const disabled = node.disabled === true
  const placeholder = node.placeholder
  const options = node.options ?? []

  if (UNSUPPORTED_WIDGETS.has(widget)) {
    console.warn(`[ping-ui] 字段控件 ${widget} 尚未实现，已降级为占位：${name}`)
  }

  let control: ReactNode
  switch (widget) {
    case "textarea":
      control = (
        <textarea
          id={name}
          name={name}
          disabled={disabled}
          placeholder={placeholder}
          maxLength={node.max_length}
          className={cn(CONTROL_CLASS, "min-h-16 resize-y")}
        />
      )
      break

    case "number":
      control = (
        <Input
          id={name}
          name={name}
          type="number"
          disabled={disabled}
          placeholder={placeholder}
          min={node.min}
          max={node.max}
        />
      )
      break

    case "select":
      if (options.length === 0) {
        console.warn(`[ping-ui] select 字段缺少 options，渲染为空下拉：${name}`)
      }
      control = (
        <select id={name} name={name} disabled={disabled} className={CONTROL_CLASS}>
          <option value="">{placeholder ?? "请选择"}</option>
          {options.map((option) => (
            <option key={String(option.value)} value={String(option.value)}>
              {option.label}
            </option>
          ))}
        </select>
      )
      break

    case "radio":
    case "checkbox": {
      const inputType = widget === "radio" ? "radio" : "checkbox"
      control = (
        <div className="flex flex-wrap items-center gap-3 pt-1">
          {options.map((option) => (
            <label
              key={String(option.value)}
              className="flex items-center gap-1.5 text-sm"
              htmlFor={`${name}-${option.value}`}
            >
              <input
                id={`${name}-${option.value}`}
                type={inputType}
                name={name}
                value={String(option.value)}
                disabled={disabled}
                defaultChecked={String(node.default) === String(option.value)}
              />
              {option.label}
            </label>
          ))}
        </div>
      )
      break
    }

    case "switch":
      control = <SwitchControl name={name} disabled={disabled} defaultOn={node.default === "1"} />
      break

    case "date_picker":
      control = (
        <Input id={name} name={name} type="date" disabled={disabled} defaultValue={String(node.default ?? "")} />
      )
      break

    case "date_range":
      control = (
        <div className="flex items-center gap-2">
          <Input aria-label={`${name} 起始`} type="date" disabled={disabled} className="flex-1" />
          <span className="text-muted-foreground">至</span>
          <Input aria-label={`${name} 结束`} type="date" disabled={disabled} className="flex-1" />
        </div>
      )
      break

    case "upload":
      control = (
        <div className="flex items-center gap-2">
          <Input id={name} name={name} type="file" disabled={disabled} />
        </div>
      )
      break

    case "rate":
      control = (
        <div className="flex items-center gap-2">
          <input
            id={name}
            name={name}
            type="range"
            min={node.min ?? 0}
            max={node.max ?? 5}
            step={1}
            disabled={disabled}
            className="accent-primary"
          />
          <span className="text-xs text-muted-foreground">0 - {node.max ?? 5}</span>
        </div>
      )
      break

    case "slider":
      control = (
        <input
          id={name}
          name={name}
          type="range"
          min={node.min ?? 0}
          max={node.max ?? 100}
          disabled={disabled}
          className="w-full accent-primary"
        />
      )
      break

    default:
      if (KNOWN_WIDGETS.has(widget)) {
        control = (
          <Input
            id={name}
            name={name}
            type="text"
            disabled={disabled}
            placeholder={placeholder}
            maxLength={node.max_length}
          />
        )
      } else {
        console.warn(`[ping-ui] 未实现的字段控件 ${widget}，已降级为占位：${name}`)
        control = (
          <div className="rounded-md border border-dashed px-3 py-2 text-xs text-muted-foreground">
            暂不支持的控件类型：{widget}
          </div>
        )
      }
  }

  return (
    <div
      className={cn(
        "space-y-1.5",
        node.col_span === 2 && "sm:col-span-2",
        node.col_span === 3 && "sm:col-span-3",
      )}
    >
      <FieldLabel node={node} htmlFor={name} />
      {control}
    </div>
  )
}

function SwitchControl({
  name,
  disabled,
  defaultOn,
}: {
  name: string
  disabled: boolean
  defaultOn: boolean
}) {
  const [on, setOn] = useState(defaultOn)
  return (
    <Button
      id={name}
      name={name}
      type="button"
      size="sm"
      variant={on ? "default" : "outline"}
      disabled={disabled}
      aria-pressed={on}
      onClick={() => setOn((value) => !value)}
    >
      {on ? "已启用" : "已停用"}
    </Button>
  )
}

/** 表单动作：submit / reset / cancel */
export function FormAction({ node }: FieldProps): ReactNode {
  const kind = node.kind ?? "submit"
  const label =
    node.name ?? (kind === "cancel" ? "取消" : kind === "reset" ? "重置" : "提交")
  const variant =
    kind === "cancel" || kind === "reset"
      ? "outline"
      : node.danger
        ? "destructive"
        : "default"

  return (
    <Button
      type={kind === "submit" ? "submit" : "button"}
      variant={variant}
      size="sm"
      title={node.loading ?? undefined}
    >
      {label}
    </Button>
  )
}

/** 表单容器：字段按 cols 分栏，动作固定在末行 */
export function Form({ node, render }: FormProps): ReactNode {
  const children = node.containers ?? []
  const fields = children.filter((child) => child.type === "field")
  const actions = children.filter((child) => child.type === "form_action")
  const others = children.filter(
    (child) => child.type !== "field" && child.type !== "form_action",
  )
  const cols = node.cols ?? 1

  return (
    <div className="rounded-md border p-4">
      <div className="mb-3 flex items-baseline justify-between">
        <h3 className="text-sm font-semibold">{node.name ?? node.title ?? "表单"}</h3>
        {node.description && (
          <span className="text-xs text-muted-foreground">{node.description}</span>
        )}
      </div>

      <div
        className={cn(
          "grid gap-4",
          cols === 2 && "sm:grid-cols-2",
          cols === 3 && "sm:grid-cols-3",
        )}
      >
        {fields.map((child) => (
          <div key={child.id ?? child.name}>{render(child)}</div>
        ))}
        {others.map((child) => (
          <div key={child.id ?? child.name}>{render(child)}</div>
        ))}
      </div>

      {actions.length > 0 && (
        <div className="mt-4 flex justify-end gap-2 border-t pt-3">
          {actions.map((child) => (
            <div key={child.id ?? child.name}>{render(child)}</div>
          ))}
        </div>
      )}
    </div>
  )
}
