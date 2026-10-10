import { useEffect, useState } from "react"
import { Moon, Sun } from "lucide-react"
import { Button } from "@/components/ui/button"

type Theme = "light" | "dark"

function apply(theme: Theme) {
  document.documentElement.classList.toggle("dark", theme === "dark")
  localStorage.setItem("ping.theme", theme)
}

/** 明暗主题切换：作用于 globals.css 的 .dark 变量块 */
export function ThemeToggle(): React.ReactNode {
  const [theme, setTheme] = useState<Theme>(() => {
    const saved = localStorage.getItem("ping.theme")
    return saved === "dark" ? "dark" : "light"
  })

  useEffect(() => {
    apply(theme)
  }, [theme])

  return (
    <Button
      variant="ghost"
      size="icon"
      className="h-8 w-8"
      title={theme === "dark" ? "切换为浅色" : "切换为深色"}
      onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
    >
      {theme === "dark" ? (
        <Sun className="h-4 w-4" />
      ) : (
        <Moon className="h-4 w-4" />
      )}
    </Button>
  )
}
